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

extern char *oobd_Error_Text_OS[];


void mc_init_sys_boot_specific()
{
    DEBUGPRINT("boot the MC specific system\n", 'a');
}


void mc_init_sys_tasks_specific()
{
    DEBUGPRINT("init the MC specific system tasks\n", 'a');
}

void mc_init_sys_shutdown_specific()
{
    DEBUGPRINT("shutdown the MC specific systems\n", 'a');
}


UBaseType_t mc_sys_get_startupProtocol()
{
    return VALUE_PARAM_PROTOCOL_CAN_UDS;
}

UBaseType_t mc_sys_get_startupBus()
{

    return ODB_CAN;
}



void printParam_sys_specific(UBaseType_t msgType, void *data,
			     printChar_cbf printchar)
{
    param_data *args;
    args = data;
    DEBUGPRINT("sys specific parameter received: %ld / %ld\n",
	       args->args[ARG_RECV], args->args[ARG_CMD]);
    switch (args->args[ARG_CMD]) {
    case PARAM_INFO:
	switch (args->args[ARG_VALUE_1]) {

	case VALUE_PARAM_INFO_VERSION:	/* p 0 0 */
	    printser_string("OOBD ");
	    printser_string(OOBDDESIGN);
	    printser_string(" ");
	    printser_string(SVNREV);
	    printser_string(" ");
	    printser_string("POSIX");
	    printser_string(" ");
	    printser_string(BUILDDATE);
	    printLF();
	    printEOT();
	    break;
	case VALUE_PARAM_INFO_SERIALNUMBER:	/* p 0 1 */
	    printser_string("000");
	    printLF();
	    printEOT();
	    break;
	default:
	    evalResult
		(FBID_SYS_SPEC, ERR_CODE_OS_UNKNOWN_COMMAND, 0,
		 ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
	}
	break;
    default:
	break;
    }
}

UBaseType_t eval_param_sys_specific(param_data * args)
{
    switch (args->args[ARG_CMD]) {
    case PARAM_INFO:
	switch (args->args[ARG_VALUE_1]) {
	case VALUE_PARAM_INFO_VERSION:
	case VALUE_PARAM_INFO_SERIALNUMBER:
	    CreateParamOutputMsg(args, printParam_sys_specific);
	    return pdTRUE;
	    break;
	default:
	    createCommandResultMsg
		(FBID_SYS_SPEC,
		 ERR_CODE_OS_UNKNOWN_COMMAND,
		 0, ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
	    return pdFALSE;
	}
	break;
    default:
	createCommandResultMsg
	    (FBID_SYS_SPEC,
	     ERR_CODE_OS_UNKNOWN_COMMAND,
	     0, ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
	return pdFALSE;
	break;
    }
}


UBaseType_t sysIoCtrl(UBaseType_t pinID, UBaseType_t lowerValue,
		      UBaseType_t upperValue, UBaseType_t duration,
		      UBaseType_t waveType)
{
//    DEBUGPRINT("Pin: %ld to value %ld\n", pinID, upperValue);
    switch (pinID) {
    case IO_LED_WHITE:
//      DEBUGPRINT("IO_LED_WHITE set to %ld\n", upperValue);
	return pdTRUE;
	break;
    case IO_LED_GREEN:
//      DEBUGPRINT("IO_LED_GREEN set to %ld\n", upperValue);
	return pdTRUE;
	break;
    case IO_LED_RED:
//      DEBUGPRINT("IO_LED_RED set to %ld\n", upperValue);
	return pdTRUE;
	break;
    case IO_BUZZER:
	DEBUGPRINT("IP_BUZZER set to %ld\n", upperValue);
	return pdTRUE;
	break;
    default:
	DEBUGPRINT("unknown output pin\n", upperValue);
	return pdFALSE;
	break;
    }
}



UBaseType_t sysSound(UBaseType_t frequency, UBaseType_t volume)
{
    DEBUGPRINT("Play frequency of %d Hz for %d ticks duration\n",
	       frequency, volume);
    return pdTRUE;
}


void mc_sys_idlehook()
{
    /* The co-routines are executed in the idle task using the idle task hook. */
//  vCoRoutineSchedule();        /* Comment this out if not using Co-routines. */

#ifdef __GCC_POSIX__
    struct timespec xTimeToSleep, xTimeSlept;
    /* Makes the process more agreeable when using the Posix simulator. */
    xTimeToSleep.tv_sec = 0;
    xTimeToSleep.tv_nsec = 5000;
    nanosleep(&xTimeToSleep, &xTimeSlept);
    //DEBUGPRINT("Idle..\n",'a');
#endif
}
