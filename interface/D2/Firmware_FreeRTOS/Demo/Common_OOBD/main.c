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


	1 tab == 4 spaces!

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
#include "stm32f10x.h"
#include "SystemConfig.h"
#endif

/* Constant definition used to turn on/off the pre-emptive scheduler. */
static const short sUsingPreemption = configUSE_PREEMPTION;
/*
xQueueHandle protocolQueue;
xQueueHandle internalSerialRxQueue;
xQueueHandle protocolQueue;
xQueueHandle inputQueue;
*/
#define SERIAL_COMM_TASK_PRIORITY			( tskIDLE_PRIORITY + 3 )
/*-----------------------------------------------------------*/


void
tickTask (void *pvParameters)
{
#ifdef DEBUG_SERIAL
  uart1_puts("\r\n*** tickTask entered! ***");
#endif
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
#ifdef DEBUG_SERIAL
  uart1_puts("\r\n*** FATAL ERROR: protocol queue is full! ***");
#endif
	}
      vTaskDelay (10 / portTICK_RATE_MS);	// 10ms tich time

    }
}

/*-----------------------------------------------------------*/

int
main (void)
{
	/*!< At this stage the microcontroller clock setting is already configured,
		       this is done through SystemInit() function which is called from startup
		       file (startup_stm32f10x_xx.s) before to branch to application main.
		       To reconfigure the default setting of SystemInit() function, refer to
		       system_stm32f10x.c file
	 */

	/* Initialise the FreeRTOS hardware and utilities. */
	/* Initialize DXM1 hardware, i.e. GPIO, CAN, USART1 */
#ifdef OOBD_PLATFORM_STM32

	SystemInit();
	System_Configuration();

	#ifdef DEBUG_SERIAL
		uart1_puts("\r\n*** Starting RTOS ***");
	#endif
#endif
  // Version String
//  DEBUGPRINT ("OOBD Build: %s\n", SVNREV);

  /* Activate the busses */
  initBusses ();
  /* Activate the protocols */
  initProtocols ();
  /* start the serial side */
  serial_init ();
  /*activate the output task */
  initOutput ();


/*
  // starting with the first protocol in the list
  if (pdPASS == xTaskCreate (odparr[0], (const signed portCHAR *) "prot", configMINIMAL_STACK_SIZE, (void *) NULL,
	       TASK_PRIO_LOW, (xTaskHandle *) NULL))
	  uart1_puts("\r\n*** 'prot' Task created ***");
  else
	  uart1_puts("\r\n*** 'prot' Task NOT created ***");
  if (pdPASS == xTaskCreate (tickTask, (const signed portCHAR *) "Tick", configMINIMAL_STACK_SIZE, (void *) NULL,
	       TASK_PRIO_LOW, (xTaskHandle *) NULL))
	  uart1_puts("\r\n*** 'Tick' Task created ***");
  else
	  uart1_puts("\r\n*** 'Tick' Task NOT created ***");
*/


  /* Set the scheduler running.  This function will not return unless a task calls vTaskEndScheduler(). */
  vTaskStartScheduler ();
#ifdef DEBUG_SERIAL
  uart1_puts("\r\nSomething got wrong, RTOS terminated !!!");
#endif
  return 1;
}
/*-----------------------------------------------------------*/


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

/*-----------------------------------------------------------*/
