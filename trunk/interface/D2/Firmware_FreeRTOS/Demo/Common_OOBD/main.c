/*
	
	This file is part of the OOBD.org distribution.

	OOBD.org is free software; you can redistribute it and/or modify it
	under the terms of the GNU General Public License (version 2) as published
	by the Free Software Foundation and modified by the FreeRTOS exception.

	OOBD.org is distributed in the hope that it will be useful, but WITHOUT
	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
	FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
	more details.

	You should have received a copy of the GNU General Public License along
	with FreeRTOS.org; if not, write to the Free Software Foundation, Inc., 59
	Temple Place, Suite 330, Boston, MA  02111-1307  USA.


	1 tab == 2 spaces!

	Please ensure to read the configuration and relevant port sections of the
	online documentation.


	OOBD is using FreeRTOS (www.FreeRTOS.org)

*/

/**
 * Creates all the application tasks, then starts the
 * scheduler.
 */

/* OOBD headers. */
#include "od_config.h"
#include "od_protocols.h"
#include "od_base.h"
#include "od_serial.h"
#include "od_outputTask.h"
#ifdef OOBD_PLATFORM_STM32
#include "stm32f10x.h"		/* ST Library v3.4..0 specific header files */
#include "SystemConfig.h"	/* STM32 hardware specific header file */
#include "mc_misc.h"
#endif

/* Constant definition used to turn on/off the pre-emptive scheduler. */
static const short sUsingPreemption = configUSE_PREEMPTION;

#define SERIAL_COMM_TASK_PRIORITY			( tskIDLE_PRIORITY + 3 )
/*---------------------------------------------------------------------------*/

void
tickTask (void *pvParameters)
{
  DEBUGUARTPRINT ("\r\n*** tickTask entered! ***");

  extern xQueueHandle protocolQueue;

  for (;;)
    {
/*
      vTaskList (buffer);
      DEBUGPRINT ("%s", buffer);
      vTaskGetRunTimeStats (buffer);
      DEBUGPRINT ("%s", buffer);
*/
      if (pdPASS != sendMsg (MSG_TICK, protocolQueue, NULL))
	{
	  DEBUGPRINT ("FATAL ERROR: protocol queue is full!\n", 'a');
	}
      vTaskDelay (10 / portTICK_RATE_MS);	// 10ms tick time

    }
}
/*---------------------------------------------------------------------------*/

int
main (void)
{
  /*!< At this stage the microcontroller clock setting is already configured,
     this is done through SystemInit() function which is called from startup
     file (startup_stm32f10x_xx.s) before to branch to application main.
     To reconfigure the default setting of SystemInit() function, refer to
     system_stm32f10x.c file
   */
#ifdef OOBD_PLATFORM_STM32
  /* Buffer of data to be received by I2C1 */
  /*  uint8_t Buffer_Rx1[255]; */

  /* SystemInit(); *//* not needed as SystemInit() is called from startup */
  /* Initialize DXM1 hardware, i.e. GPIO, CAN, USART1 */
  System_Configuration ();

  /* Initialize the I2C EEPROM driver ----------------------------------------*/
  sEE_Init();

#endif

  /* Activate the busses */
  initBusses ();
  /* Activate the protocols */
  initProtocols ();
  /* start the serial side */
  serial_init ();

  /*activate the output task */
  initOutput ();

  DEBUGUARTPRINT ("\r\n*** Starting FreeRTOS ***");

  // Version String
  #ifdef OOBD_PLATFORM_POSIX
    DEBUGPRINT ("OOBD Build: %s\n", SVNREV);
  #else
    printser_string("OOBD Build: ");
    printser_string(SVNREV);
  #endif


#ifdef OOBD_PLATFORM_EEPROMpart
    uint8_t Tx1_Buffer[] = "OOBD-Cup EEPROM Test";
	#define sEE_WRITE_ADDRESS1        0x00
	#define sEE_READ_ADDRESS1         0x00
	#define countof(a) (sizeof(a) / sizeof(*(a)))
    #define BUFFER_SIZE1             (countof(Tx1_Buffer)-1)
    volatile uint16_t NumDataRead = 0;
    uint8_t Rx1_Buffer[BUFFER_SIZE1];

    printser_string("\r\nShow Tx1_buffer:");
    printser_string(Tx1_Buffer);

    /* First write in the memory followed by a read of the written data --------*/
    /* Write on I2C EEPROM from sEE_WRITE_ADDRESS1 */
    sEE_WriteBuffer(Tx1_Buffer, sEE_WRITE_ADDRESS1, BUFFER_SIZE1);

    /* Set the Number of data to be read */
    NumDataRead = BUFFER_SIZE1;

    /* Read from I2C EEPROM from sEE_READ_ADDRESS1 */
    sEE_ReadBuffer(Rx1_Buffer, sEE_READ_ADDRESS1, (uint16_t *)(&NumDataRead));

    printser_string("\r\nShow Rx1_buffer:");
    printser_string(Rx1_Buffer);
#endif


  // starting with the first protocol in the list
  if (pdPASS == xTaskCreate (odparr[0], (const signed portCHAR *) "prot",
			     configMINIMAL_STACK_SIZE, (void *) NULL,
			     TASK_PRIO_LOW, (xTaskHandle *) NULL))
    DEBUGUARTPRINT ("\r\n*** 'prot' Task created ***");
  else
    DEBUGUARTPRINT ("\r\n*** 'prot' Task NOT created ***");

  if (pdPASS == xTaskCreate (tickTask, (const signed portCHAR *) "Tick",
			     configMINIMAL_STACK_SIZE, (void *) NULL,
			     TASK_PRIO_LOW, (xTaskHandle *) NULL))
    DEBUGUARTPRINT ("\r\n*** 'Tick' Task created ***");
  else
    DEBUGUARTPRINT ("\r\n*** 'Tick' Task NOT created ***");

#ifdef OOBD_PLATFORM_STM32
  if (pdPASS ==
      xTaskCreate (Led1Task, (const signed portCHAR *) "LED1",
		   configMINIMAL_STACK_SIZE, (void *) NULL, TASK_PRIO_LOW,
		   (xTaskHandle *) NULL))
    DEBUGUARTPRINT ("\r\n*** 'LED1' Task created ***");
  else
    DEBUGUARTPRINT ("\r\n*** 'LED1' Task NOT created ***");

  if (pdPASS ==
      xTaskCreate (Led2Task, (const signed portCHAR *) "LED2",
		   configMINIMAL_STACK_SIZE, (void *) NULL, TASK_PRIO_LOW,
		   (xTaskHandle *) NULL))
    DEBUGUARTPRINT ("\r\n*** 'LED2' Task created ***");
  else
    DEBUGUARTPRINT ("\r\n*** 'LED2' Task NOT created ***");
#endif

#ifdef OOBD_PLATFORM_STM32
  /* initialize Interrupt Vector table and activate interrupts */
  NVIC_Configuration ();
#endif

  /* Set the scheduler running.  This function will not return unless a task calls vTaskEndScheduler(). */
  vTaskStartScheduler ();

  DEBUGUARTPRINT ("\r\nSomething got wrong, RTOS terminated !!!");

#ifdef OOBD_PLATFORM_STM32
  SCB->AIRCR = 0x05FA0604;	/* soft reset */
#endif

  return 1;
}
/*---------------------------------------------------------------------------*/

void
vApplicationIdleHook (void)
{
  /* The co-routines are executed in the idle task using the idle task hook. */
//  vCoRoutineSchedule();        /* Comment this out if not using Co-routines. */

#ifdef __GCC_POSIX__
//  struct timespec xTimeToSleep, xTimeSlept;
  /* Makes the process more agreeable when using the Posix simulator. */
//  xTimeToSleep.tv_sec = 1;
//  xTimeToSleep.tv_nsec = 0;
//  nanosleep (&xTimeToSleep, &xTimeSlept);
#endif
}

/*---------------------------------------------------------------------------*/
