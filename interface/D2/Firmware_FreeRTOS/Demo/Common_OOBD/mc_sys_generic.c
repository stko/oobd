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


void mc_init_sys_boot()
{
    DEBUGPRINT("boot system\n", 'a');
    mc_init_sys_boot_specific();
}




void
printParam_sys(portBASE_TYPE msgType, void *data, printChar_cbf printchar)
{
    static param_data *pd;
    pd = data;
    portBASE_TYPE cmdKey = pd->key, cmdValue = pd->value;	/* the both possible params */
    DEBUGPRINT("sys parameter received: %ld / %ld\n", cmdKey, cmdValue);
    switch (cmdKey) {

    default:
	printParam_sys_specific(pd, printchar);
	break;
    }
}

portBASE_TYPE eval_param_sys(portBASE_TYPE cmdKey, portBASE_TYPE cmdValue)
{
    int i;
    switch (cmdKey) {
    case PARAM_INFO:
	switch (cmdValue) {
	case VALUE_PARAM_INFO_VERSION:
	case VALUE_PARAM_INFO_SERIALNUMBER:
	    CreateParamOutputMsg(cmdKey, cmdValue, printParam_sys);
	    return pdTRUE;
	    break;
	case PARAM_PROTOCOL:
	    //! \todo this kind of task switching is not design intent
	    //! \todo no use of protocol table, its hardcoded instead
	    if (VALUE_PARAM_PROTOCOL_CAN_RAW == cmdValue) {	/* p 4 1 */
		printser_string("Protocol CAN RAW activated!");
		vTaskDelete(xTaskProtHandle);
		vTaskDelay(100 / portTICK_RATE_MS);
		/* */
		if (pdPASS ==
		    xTaskCreate(odparr[0], (const signed portCHAR *)
				"prot", configMINIMAL_STACK_SIZE,
				(void *) NULL, TASK_PRIO_LOW,
				&xTaskProtHandle))
		    DEBUGPRINT("\r\n*** 'prot' Task created ***", 'a');
		else
		    DEBUGPRINT("\r\n*** 'prot' Task NOT created ***", 'a');
	    }
	    if (VALUE_PARAM_PROTOCOL_CAN_UDS == cmdValue) {	/* p 4 2 */
		printser_string("Protocol CAN UDS activated!");
		vTaskDelete(xTaskProtHandle);
		vTaskDelay(100 / portTICK_RATE_MS);
		/* */
		if (pdPASS ==
		    xTaskCreate(odparr[1], (const signed portCHAR *)
				"prot", configMINIMAL_STACK_SIZE,
				(void *) NULL, TASK_PRIO_LOW,
				&xTaskProtHandle)) {
		    DEBUGPRINT("\r\n*** 'prot' Task created ***", 'a');
		    evalResult(ERR_CODE_SOURCE_OS, ERR_CODE_NO_ERR, 0,
			       NULL);
		} else {
		    evalResult
			(ERR_CODE_SOURCE_OS,
			 ERR_CODE_OS_NO_PROTOCOL_TASK,
			 0, ERR_CODE_OS_NO_PROTOCOL_TASK_TEXT);
		    DEBUGPRINT("\r\n*** 'prot' Task NOT created ***", 'a');
		}
	    }
	    break;

	default:
	    return pdFALSE;
	}
	break;
	//! \todo remove dirty IO implementation
//-----------------------------------------------------------
// QUICK AND DIRTY IMPLEMENTATION of IO control
//-----------------------------------------------------------
    case 98:
	for (i = 0; i < 6; i++) {
	    sysIoCtrl(i, 0, (cmdValue & (1 << i)) == 0 ? 0 : 1, 0, 0);
	}
	break;
//-----------------------------------------------------------
//-----------------------------------------------------------


    default:
	return pdFALSE;
    }
}

void mc_init_sys_tasks()
{
    DEBUGPRINT("init system tasks\n", 'a');
    mc_init_sys_tasks_specific();
}

void mc_init_sys_shutdown()
{
    DEBUGPRINT("shutdown systems\n", 'a');
    mc_init_sys_shutdown_specific();
}
