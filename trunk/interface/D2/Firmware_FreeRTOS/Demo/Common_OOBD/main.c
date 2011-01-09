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



/* Constant definition used to turn on/off the pre-emptive scheduler. */
static const short sUsingPreemption = configUSE_PREEMPTION;



/*-----------------------------------------------------------*/


void
tickTask (void *pvParameters)
{
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
      vTaskDelay (10 / portTICK_RATE_MS);	// 10ms tich time

    }
}



/*-----------------------------------------------------------*/

int
main (void)
{
  /* Initialise the FreeRTOS hardware and utilities. */
  //vParTestInitialise ();
  //vPrintInitialise ();
  // Version String
  DEBUGPRINT ("OOBD Build: %s\n", SVNREV);
  /* Activate the busses */
  initBusses ();
  /* Activate the protocols */
  initProtocols ();
  /* start the serial side */
  serial_init ();
  /*activate the output task */
  initOutput ();
  // starting with the first protocol in the list
  xTaskCreate (odparr[0], "prot", configMINIMAL_STACK_SIZE, NULL,
	       TASK_PRIO_HIGH, NULL);
  xTaskCreate (tickTask, "Tick", configMINIMAL_STACK_SIZE, NULL,
	       TASK_PRIO_LOW, NULL);

  /* Set the scheduler running.  This function will not return unless a task calls vTaskEndScheduler(). */
  vTaskStartScheduler ();

  return 1;
}


/*-----------------------------------------------------------*/



void
vApplicationIdleHook (void)
{
  /* The co-routines are executed in the idle task using the idle task hook. */
  // vCoRoutineSchedule();        /* Comment this out if not using Co-routines. */

#ifdef __GCC_POSIX__
  struct timespec xTimeToSleep, xTimeSlept;
  /* Makes the process more agreeable when using the Posix simulator. */
  xTimeToSleep.tv_sec = 1;
  xTimeToSleep.tv_nsec = 0;
  nanosleep (&xTimeToSleep, &xTimeSlept);
#endif
}

/*-----------------------------------------------------------*/
