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
#include "mc_serial_generic.h"
#include "mc_serial.h"
#include "od_outputTask.h"


/* Constant definition used to turn on/off the pre-emptive scheduler. */
static const short sUsingPreemption = configUSE_PREEMPTION;


#define SERIAL_COMM_TASK_PRIORITY			( tskIDLE_PRIORITY + 3 )
/*---------------------------------------------------------------------------*/

void tickTask(void *pvParameters)
{
    DEBUGUARTPRINT("\r\n*** tickTask entered! ***");

    extern xQueueHandle protocolQueue;

    for (;;) {
/*
      vTaskList (buffer);
      DEBUGPRINT ("%s", buffer);
      vTaskGetRunTimeStats (buffer);
      DEBUGPRINT ("%s", buffer);
*/
	if (pdPASS != sendMsg(MSG_TICK, protocolQueue, NULL)) {
	    DEBUGPRINT("FATAL ERROR: protocol queue is full!\n", 'a');
	}
	vTaskDelay(10 / portTICK_RATE_MS);	// 10ms tick time

    }
}

/*---------------------------------------------------------------------------*/

int main(void)
{
    /* set up the controller */
    mc_init_sys_boot();

    /* Activate the busses */
    initBusses();
    /* Activate the protocols */
    initProtocols();
    /* start the serial side */
    serial_init();

    /*activate the output task */
    initOutput();

    DEBUGPRINT("*** Starting FreeRTOS ***\n", 'a');

    // Version String
    DEBUGPRINT("OOBD Build: %s\n", SVNREV);

    //! \todo move the task generation into the mc_sys_generic.c file
    /* starting with UDS protocol of the list by default */
    if (pdPASS == xTaskCreate(odparr[1], (const signed portCHAR *) "prot",
			      configMINIMAL_STACK_SIZE, (void *) NULL,
			      TASK_PRIO_LOW, &xTaskProtHandle))
	DEBUGPRINT("*** 'prot' Task created ***\n", 'a');
    else
	DEBUGPRINT("*** 'prot' Task NOT created ***\n", 'a');

    if (pdPASS == xTaskCreate(tickTask, (const signed portCHAR *) "Tick",
			      configMINIMAL_STACK_SIZE, (void *) NULL,
			      TASK_PRIO_LOW, (xTaskHandle *) NULL))
	DEBUGPRINT("*** 'Tick' Task created ***\n", 'a');
    else
	DEBUGPRINT("*** 'Tick' Task NOT created ***\n", 'a');

    mc_init_sys_tasks();


    /* Set the scheduler running.  This function will not return unless a task calls vTaskEndScheduler(). */
    vTaskStartScheduler();

    DEBUGPRINT("Something got wrong, RTOS terminated !!!\n", 'a');
    mc_init_sys_shutdown();
    return 1;
}

/*---------------------------------------------------------------------------*/

void vApplicationIdleHook(void)
{
    mc_sys_idlehook();
}

/*---------------------------------------------------------------------------*/
