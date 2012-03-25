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

//! All Errormessages of the OS Function Block
char *oobd_Error_Text_OS[4]={
		"", // indox 0 is no error
		"can't generate protocol task",
		"Unknown command",
		"Command not supported"
};



void mc_init_sys_boot()
{
    DEBUGPRINT("boot system\n", 'a');
    mc_init_sys_boot_specific();
}




void
printParam_sys(portBASE_TYPE msgType, void *data, printChar_cbf printchar)
{
    param_data *args;
    args = data;
    DEBUGPRINT("sys parameter received: %ld / %ld\n", args->args[ARG_RECV],
	       args->args[ARG_CMD]);
    switch (args->args[ARG_CMD]) {

    default:
//      printParam_sys_specific(args, printchar);
	break;
    }
}

portBASE_TYPE eval_param_sys(param_data * args)
{
    int i;
    switch (args->args[ARG_CMD]) {
 	case PARAM_SET_OUTPUT:
		sysIoCtrl(args->args[ARG_VALUE_1], 0,
				args->args[ARG_VALUE_2], 0,
				0);
	    return pdTRUE;
		break;
	case PARAM_PROTOCOL:
	    //! \todo this kind of task switching is not design intent
	    //! \todo no use of protocol table, its hardcoded instead
	    if (VALUE_PARAM_PROTOCOL_CAN_RAW == args->args[ARG_VALUE_1]) {	/* p 4 1 */
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
	    if (VALUE_PARAM_PROTOCOL_CAN_UDS == args->args[ARG_VALUE_1]) {	/* p 4 2 */
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
		    evalResult(FBID_SYS_GENERIC, ERR_CODE_NO_ERR, 0, NULL);
		} else {
		    evalResult
			(FBID_SYS_GENERIC,
			 ERR_CODE_OS_NO_PROTOCOL_TASK,
			 0, ERR_CODE_OS_NO_PROTOCOL_TASK_TEXT);
		    DEBUGPRINT("\r\n*** 'prot' Task NOT created ***", 'a');
		}
	    }
	    return pdTRUE;
	    break;

	//! \todo remove dirty IO implementation
//-----------------------------------------------------------
// QUICK AND DIRTY IMPLEMENTATION of IO control
//-----------------------------------------------------------
    case 98:
	for (i = 0; i < 6; i++) {
	    sysIoCtrl(i, 0,
		      (args->args[ARG_VALUE_1] & (1 << i)) == 0 ? 0 : 1, 0,
		      0);
	}
	evalResult(FBID_SYS_GENERIC, ERR_CODE_NO_ERR, 0, NULL);
	return pdTRUE;
	break;
//-----------------------------------------------------------
//-----------------------------------------------------------



	default:
	    createCommandResultMsg
		(FBID_SYS_GENERIC,
		 ERR_CODE_OS_UNKNOWN_COMMAND,
		 0, ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
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
