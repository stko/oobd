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


	OOBD is using FreeTROS (www.FreeRTOS.org)

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
#include "mc_i2c_routines.h"
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
  // char buffer[1024];
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
#ifdef OOBD_PLATFORM_STM32
void
blinkLedTask (void *pvParameters)
{
  portTickType xLastExecutionTime;

  DEBUGUARTPRINT ("\r\n*** prvBlinkLedTask entered! ***");

  /* Initialise the xLastExecutionTime variable on task entry. */
  xLastExecutionTime = xTaskGetTickCount ();

  for (;;)
    {
      /* Simple toggle the LED periodically.  This just provides some timing
         verification. */
      vTaskDelayUntil (&xLastExecutionTime,
		       (portTickType) 1000 / portTICK_RATE_MS);
      /* Output high -> LED off for 1000ms = 1sec */
      GPIO_SetBits (GPIOB, GPIO_Pin_4);	/* LED2 - green */
      GPIO_SetBits (GPIOB, GPIO_Pin_5);	/* LED1 - red */

      vTaskDelayUntil (&xLastExecutionTime,
		       (portTickType) 1000 / portTICK_RATE_MS);
      /* Output low -> LED on for 1000ms = 1sec */
      GPIO_ResetBits (GPIOB, GPIO_Pin_4);	/* LED2 - green */
      GPIO_ResetBits (GPIOB, GPIO_Pin_5);	/* LED1 - red */
    }
}
#endif
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
/* 	I2C_LowLevel_Init(I2C1); */
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
    uart1_puts("\r\nOOBD Build: ");
    uart1_puts(SVNREV);
  #endif

/*
#ifdef OOBD_PLATFORM_STM32
    if (Success == I2C_Master_BufferRead(I2C1,Buffer_Rx1,1,Polling, 0x28))
      {
        DEBUGUARTPRINT("*** I2C read successfully! ***");
        DEBUGUARTPRINT(Buffer_Rx1);
      }
    else
      DEBUGUARTPRINT("*** I2C read error! ***");
#endif
*/

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
      xTaskCreate (blinkLedTask, (const signed portCHAR *) "blinkLed",
		   configMINIMAL_STACK_SIZE, (void *) NULL, TASK_PRIO_LOW,
		   (xTaskHandle *) NULL))
    DEBUGUARTPRINT ("\r\n*** 'blinkLed' Task created ***");
  else
    DEBUGUARTPRINT ("\r\n*** 'blinkLed' Task NOT created ***");
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
