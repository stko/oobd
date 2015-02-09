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




QueueHandle_t protocolQueue;


//! All Errormessages of the OS Function Block
char *oobd_Error_Text_OS[] = {
    "",				// indox 0 is no error
    "can't generate protocol task",
    "Unknown command",
    "Command not supported",
    "Output Pin not supported"
};


//startupProtocol and startupBus need to be static to "be there" when another task get started with their address as parameter
UBaseType_t startupProtocol;
UBaseType_t startupBus;
SemaphoreHandle_t protocollBinarySemaphore;

void mc_init_sys_boot()
{
    DEBUGPRINT("boot system\n", 'a');
    mc_init_sys_boot_specific();
    // generate binary semaphore for protocol switching
    vSemaphoreCreateBinary(protocollBinarySemaphore);
}




void
printParam_sys(UBaseType_t msgType, void *data, printChar_cbf printchar)
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

/* evaluation of p 1 x x x commands */
UBaseType_t eval_param_sys(param_data * args)
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

    case PARAM_PROTOCOL:
	startupProtocol = args->args[ARG_VALUE_1];
	startupBus = args->args[ARG_VALUE_2];

	if (startupProtocol < SYS_NR_OF_PROTOCOLS
	    && startupBus < SYS_NR_OF_BUSSES
	    && odparr[startupProtocol] != NULL
	    && odbarr[startupBus] != NULL) {
	    sendMsg(MSG_PROTOCOL_STOP, protocolQueue, NULL);
	    if (xSemaphoreTake(protocollBinarySemaphore, 2000 / portTICK_PERIOD_MS) == pdPASS) {	// wait 2s for the protocol task to finish
		//job done, free semaphore again for the new protocol task
		xSemaphoreGive(protocollBinarySemaphore);
		// and start new protocol..
		if (mc_start_protocol(startupProtocol, startupBus) == 0) {
		    createCommandResultMsg
			(FBID_SYS_GENERIC, ERR_CODE_NO_ERR, 0, NULL);
		    return pdTRUE;
		} else {
		    createCommandResultMsg
			(FBID_SYS_GENERIC,
			 ERR_CODE_OS_NO_PROTOCOL_TASK,
			 0, ERR_CODE_OS_NO_PROTOCOL_TASK_TEXT);
		    return pdFALSE;
		}

	    }
	    return pdFALSE;
	} else {
	    createCommandResultMsg
		(FBID_SYS_GENERIC,
		 ERR_CODE_OS_COMMAND_NOT_SUPPORTED,
		 0, ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
	    return pdFALSE;
	}
	break;


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
    /* starting with UDS protocol of the list by default */
    mc_start_protocol(startupProtocol, startupBus);
    mc_init_sys_tasks_specific();
}

void mc_init_sys_shutdown()
{
    DEBUGPRINT("shutdown systems\n", 'a');
    mc_init_sys_shutdown_specific();
}


UBaseType_t mc_start_protocol(UBaseType_t protocol, UBaseType_t bus)
{
    if (protocol < SYS_NR_OF_PROTOCOLS && bus < SYS_NR_OF_BUSSES
	&& odparr[protocol] != NULL && odbarr[bus] != NULL) {
	startupProtocol = protocol;
	startupBus = bus;
	if (pdPASS ==
	    xTaskCreate(odparr[startupProtocol],
			(const signed portCHAR *) "prot",
			configMINIMAL_STACK_SIZE, (void *) &startupBus,
			TASK_PRIO_LOW, &xTaskProtHandle)) {
	    DEBUGPRINT("*** 'prot' Task created ***\n", 'a');
	    return 0;
	} else {
	    DEBUGPRINT("*** 'prot' Task NOT created ***\n", 'a');
	    return -2;
	}
    } else {
	return -1;
    }
}
