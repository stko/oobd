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

/**
 * MC specific system routines
 */

/* OOBD headers. */
#include "od_config.h"
#include "od_base.h"
#include "mc_sys_generic.h"

//! All Errormessages of the OS Function Block
char *oobd_Error_Text_OS[] = {
    "",				// indox 0 is no error
    "can't generate protocol task",
    "Unknown command",
    "Command not supported",
    "Output Pin not supported"
};


//startupProtocol and startupBus need to be static to "be there" when another task get started with their address as parameter
portBASE_TYPE startupProtocol;
portBASE_TYPE startupBus;


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
	if (sysIoCtrl(args->args[ARG_VALUE_1], 0,
		      args->args[ARG_VALUE_2], 0, 0) == pdTRUE) {
	    createCommandResultMsg
		(FBID_SYS_GENERIC, ERR_CODE_NO_ERR, 0, NULL);
	    return pdTRUE;
	} else {
	    createCommandResultMsg
		(FBID_SYS_GENERIC,
		 ERR_CODE_OS_UNKNOWN_OUTPUT_PIN,
		 args->args[ARG_VALUE_1],
		 ERR_CODE_OS_UNKNOWN_OUTPUT_PIN_TEXT);
	    return pdFALSE;
	}
	break;
/*    case PARAM_PROTOCOL:
	//! \todo this kind of task switching is not design intent
	//! \todo no use of protocol table, its hardcoded instead
	if (VALUE_PARAM_PROTOCOL_CAN_RAW == args->args[ARG_VALUE_1]) {	
	    printser_string("Protocol CAN RAW activated!");
	    vTaskDelete(xTaskProtHandle);
	    vTaskDelay(100 / portTICK_RATE_MS);
	   
	    if (pdPASS == xTaskCreate(odparr[0], (const signed portCHAR *)
				      "prot", configMINIMAL_STACK_SIZE,
				      (void *) NULL, TASK_PRIO_LOW,
				      &xTaskProtHandle))
		DEBUGPRINT("\r\n*** 'prot' Task created ***", 'a');
	    else
		DEBUGPRINT("\r\n*** 'prot' Task NOT created ***", 'a');
	}
	if (VALUE_PARAM_PROTOCOL_CAN_UDS == args->args[ARG_VALUE_1]) {	
	    printser_string("Protocol CAN UDS activated!");
	    vTaskDelete(xTaskProtHandle);
	    vTaskDelay(100 / portTICK_RATE_MS);
	   
	    if (pdPASS == xTaskCreate(odparr[1], (const signed portCHAR *)
				      "prot", configMINIMAL_STACK_SIZE,
				      (void *) NULL, TASK_PRIO_LOW,
				      &xTaskProtHandle)) {
		DEBUGPRINT("\r\n*** 'prot' Task created ***", 'a');
		createCommandResultMsg(FBID_SYS_GENERIC, ERR_CODE_NO_ERR,
				       0, NULL);
	    } else {
		createCommandResultMsg
		    (FBID_SYS_GENERIC,
		     ERR_CODE_OS_NO_PROTOCOL_TASK,
		     0, ERR_CODE_OS_NO_PROTOCOL_TASK_TEXT);
		DEBUGPRINT("\r\n*** 'prot' Task NOT created ***", 'a');
	    }
	}
	return pdTRUE;
	break;

*/


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
    startupProtocol = mc_sys_get_startupProtocol();
    startupBus = mc_sys_get_startupBus();
    DEBUGPRINT("Inital protocol: %d\n", startupProtocol);
    DEBUGPRINT("Inital bus: %d\n", startupBus);
    //! \todo move the task generation into the mc_sys_generic.c file
    /* starting with UDS protocol of the list by default */
    if (pdPASS ==
	xTaskCreate(odparr[startupProtocol],
		    (const signed portCHAR *) "prot",
		    configMINIMAL_STACK_SIZE, (void *) &startupBus,
		    TASK_PRIO_LOW, &xTaskProtHandle))
	DEBUGPRINT("*** 'prot' Task created ***\n", 'a');
    else
	DEBUGPRINT("*** 'prot' Task NOT created ***\n", 'a');

    mc_init_sys_tasks_specific();
}

void mc_init_sys_shutdown()
{
    DEBUGPRINT("shutdown systems\n", 'a');
    mc_init_sys_shutdown_specific();
}
