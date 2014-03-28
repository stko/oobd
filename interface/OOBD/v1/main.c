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


	OOBD C source files requirement:
	Unix EOL file format 
	UTF-8
	formated with "indent -kr"
	  
	Please ensure to read the configuration and relevant port sections of the
	online documentation.


	OOBD is using FreeRTOS (www.FreeRTOS.org)

*/

/*!
 * \mainpage Welcome to the OOBD Firmware Documentation
 * 
 * \section Introduction
 *
 * This documentation explains the firmware functionality and the general concept of how the
 * different components work together.
 *
 *
 *
 * \section The Concept
 *
 * The OOBD firmware, as running with the popular RealTime-OS FreeRTOS, should be a framework for as most as possible hardware platforms and communication protocols and bus systems.
 
 To archive this, the source code is strictly seperated into
   \li generic, controller indepented areas
   \li controller specific sources
   
   and also into 
   
   \li system functions
   \li protocol function
   \li bus functions (=hardware layer)
   
   
 *
 * \image html Firmware_Structure.svg
 
 
 By this it's possible to add new protocols or busses just by adding the sources to the pack, or port the whole application onto another controller by adding another controller dirextory and
 re-write the hardware specific source without the need to touch the rest
 

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

    extern QueueHandle_t protocolQueue;
    extern QueueHandle_t ilmQueue;

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
	if (pdPASS != sendMsg(MSG_TICK, ilmQueue, NULL)) {
	    DEBUGPRINT("FATAL ERROR: ilm queue is full!\n", 'a');
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

    /*activate the ILM task */
    initILM();

    DEBUGPRINT("*** Starting FreeRTOS ***\n", 'a');

    // Version String
    DEBUGPRINT("OOBD Build: %s\n", SVNREV);
    mc_init_sys_tasks();

    if (pdPASS == xTaskCreate(tickTask, (const signed portCHAR *) "Tick",
			      configMINIMAL_STACK_SIZE, (void *) NULL,
			      TASK_PRIO_LOW, (TaskHandle_t *) NULL))
	DEBUGPRINT("*** 'Tick' Task created ***\n", 'a');
    else
	DEBUGPRINT("*** 'Tick' Task NOT created ***\n", 'a');



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
