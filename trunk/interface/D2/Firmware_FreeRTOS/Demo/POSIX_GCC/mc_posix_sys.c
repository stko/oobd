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


	OOBD is using FreeRTOS (www.FreeRTOS.org)

*/

/**
 * MC specific system routines
 */

/* OOBD headers. */
#include "od_config.h"
#include "od_base.h"
#include "mc_sys_generic.h"
#include "mc_sys.h"

void mc_init_sys_boot()
{
    DEBUGPRINT("boot system\n", 'a');
}



void
printParam_sys(portBASE_TYPE msgType, void *data, printChar_cbf printchar)
{
    static param_data *pd;
    pd = data;
    portBASE_TYPE cmdKey = pd->key, cmdValue = pd->value;	/* the both possible params */
    switch (cmdKey) {
    case PARAM_INFO:
	eval_param_sys(cmdKey, cmdValue);
	switch (cmdValue) {

	case VALUE_PARAM_INFO_VERSION:	/* p 0 0 */
	    printser_string("OOBD ");
	    printser_string(OOBDDESIGN);
	    printser_string(" ");
	    printser_string(SVNREV);
	    printser_string(" ");
	    printser_string(BUILDDATE);
	    break;
	case VALUE_PARAM_INFO_SERIALNUMBER:	/* p 0 1 */
	    printser_string("000");
	    break;
	}
    case PARAM_PROTOCOL:
	// \todo this kind of task switching is not design intent
	// \todo no use of protocol table, its hardcoded instead
	if (VALUE_PARAM_PROTOCOL_CAN_RAW == cmdValue) {	/* p 4 1 */
	    printser_string("Protocol CAN RAW activated!");
	    vTaskDelete(xTaskProtHandle);
	    vTaskDelay(100 / portTICK_RATE_MS);
	    /* */
	    if (pdPASS == xTaskCreate(odparr[0], (const signed portCHAR *)
				      "prot",
				      configMINIMAL_STACK_SIZE,
				      (void *) NULL,
				      TASK_PRIO_LOW, &xTaskProtHandle))
		DEBUGUARTPRINT("\r\n*** 'prot' Task created ***");
	    else
		DEBUGUARTPRINT("\r\n*** 'prot' Task NOT created ***");
	}
	if (VALUE_PARAM_PROTOCOL_CAN_UDS == cmdValue) {	/* p 4 2 */
	    printser_string("Protocol CAN UDS activated!");
	    vTaskDelete(xTaskProtHandle);
	    vTaskDelay(100 / portTICK_RATE_MS);
	    /* */
	    if (pdPASS == xTaskCreate(odparr[1], (const signed portCHAR *)
				      "prot",
				      configMINIMAL_STACK_SIZE,
				      (void *) NULL,
				      TASK_PRIO_LOW, &xTaskProtHandle)) {
		DEBUGUARTPRINT("\r\n*** 'prot' Task created ***");
		createCommandResultMsg
		    (ERR_CODE_SOURCE_SERIALIN, ERR_CODE_NO_ERR, 0, NULL);
	    } else {
		createCommandResultMsg
		    (ERR_CODE_SOURCE_SERIALIN,
		     ERR_CODE_OS_NO_PROTOCOL_TASK,
		     0, ERR_CODE_OS_NO_PROTOCOL_TASK_TEXT);
		DEBUGUARTPRINT("\r\n*** 'prot' Task NOT created ***");
	    }
	}
	break;


    default:
	//sendParam(cmdKey, cmdValue);
	break;
    }

    printLF();
    printEOT();
}

portBASE_TYPE eval_param_sys(portBASE_TYPE param, portBASE_TYPE value)
{
    CreateParamOutputMsg(param, value, printParam_sys);
}

void mc_init_sys_tasks()
{
    DEBUGPRINT("init system tasks\n", 'a');
}

void mc_init_sys_shutdown()
{
    DEBUGPRINT("shutdown systems\n", 'a');
}

void mc_sys_idlehook()
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
