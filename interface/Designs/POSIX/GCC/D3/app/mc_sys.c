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
#include "mc_can.h";

// posix specific argument handling
#include <getopt.h>

/* Flag set by ‘--verbose’. */
static int verbose_flag;
char *serialPort;
char *tcpPort;
char *canChannel[MAXCANCHANNEL];


extern char *oobd_Error_Text_OS[];


void mc_init_sys_boot_specific()
{
    DEBUGPRINT("boot the MC specific system\n", 'a');
// setting defaults first, to then let them override by otopns, in case they are given
    serialPort = "/tmp/OOBD";
    tcpPort = "1234";
    memset(&canChannel[0], 0, sizeof(canChannel));
    canChannel[0] = "oobdcan0";

    int c;
    int channelCount = 0;

    static struct option long_options[] = {
	/* These options set a flag. */
	{"verbose", no_argument, &verbose_flag, 1},
	{"brief", no_argument, &verbose_flag, 0},
	/* These options don’t set a flag.
	   We distinguish them by their indices. */
	{"dummy1", no_argument, 0, 'a'},
	{"dummy2", no_argument, 0, 'b'},
	{"serial-port", required_argument, 0, 's'},
	{"tcp-port", required_argument, 0, 't'},
	{"can-channel", required_argument, 0, 'c'},
	{0, 0, 0, 0}
    };
    /* getopt_long stores the option index here. */
    int option_index = 0;
    while (1) {

	c = getopt_long(argc, argv, "abs:t:c:",
			long_options, &option_index);

	/* Detect the end of the options. */
	if (c == -1)
	    break;

	switch (c) {
	case 0:
	    /* If this option set a flag, do nothing else now. */
	    if (long_options[option_index].flag != 0)
		break;
	    printf("option %s", long_options[option_index].name);
	    if (optarg)
		printf(" with arg %s", optarg);
	    printf("\n");
	    break;

	case 'a':
	    puts("option -a\n");
	    break;

	case 'b':
	    puts("option -b\n");
	    break;

	case 's':
	    printf("option -s: Set serial port to `%s'\n", optarg);
	    serialPort = optarg;
	    break;

	case 't':
	    printf("option -t: Set Telnet Port to `%s'\n", optarg);
	    tcpPort = optarg;
	    break;

	case 'c':
	    if (channelCount < MAXCANCHANNEL) {
		printf("option -c: Set Can Channel to `%s'\n", optarg);
		canChannel[channelCount++] = optarg;
	    }
	    break;

	case '?':
	    /* getopt_long already printed an error message. */
	    break;

	default:
	    abort();
	}
    }

    /* Instead of reporting ‘--verbose’
       and ‘--brief’ as they are encountered,
       we report the final status resulting from them. */
    if (verbose_flag)
	puts("verbose flag is set");

    /* Print any remaining command line arguments (not options). */
    if (optind < argc) {
	printf("non-option ARGV-elements: ");
	while (optind < argc)
	    printf("%s ", argv[optind++]);
	putchar('\n');
    }

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
	case VALUE_PARAM_INFO_VOLTAGE:	/* p 0 2 */
	    printser_string("12000");
	    printLF();
	    printEOT();
	    break;
	case VALUE_PARAM_INFO_DEVICE:	/* p 0 3 */
	    printser_string("OOBD-Posix 4711");
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
	case VALUE_PARAM_INFO_VOLTAGE:
	case VALUE_PARAM_INFO_DEVICE:
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
    xTimeToSleep.tv_sec = 1;
    xTimeToSleep.tv_nsec = 0;
    nanosleep(&xTimeToSleep, &xTimeSlept);
    //DEBUGPRINT("Idle..\n",'a');
#endif
}
