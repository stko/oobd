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
 * generic part of the CAN routines
 */

/* OOBD headers. */
/*-----------------------------------------------------------*/
#include "od_config.h"
#include "od_protocols.h"
#include "odb_can.h"

print_cbf printdata_CAN = NULL;
struct CanConfig *canConfig;
/*-----------------------------------------------------------*/

void
print_telegram(portBASE_TYPE msgType, void *data, printChar_cbf printchar)
{
    static data_packet *dp;
    dp = data;
    printser_string("# 0x");
    printser_int(dp->recv, 16);
    printser_string("  0x");
    printser_int(dp->err, 16);
    printser_string("  ");
    printser_int(dp->len, 10);
    printser_string("  ");
    int i;
    for (i = 0; i < 8; i++) {
	printser_uint8ToHex(dp->data[i]);
	printser_string("  ");
    }
    printLF();
}

/*-----------------------------------------------------------*/
void bus_param_canPrint(param_data * args)
{
    DEBUGPRINT("can Parameter receiced %ld-%ld\n", args->args[ARG_RECV],
	       args->args[ARG_CMD]);
    if (args->args[ARG_CMD] == PARAM_INFO) {
	switch (args->args[ARG_CMD]) {
	case VALUE_PARAM_INFO_BUS:	/* p 0 2 */
	    printser_string("CAN Bus");
	    printLF();
	    printEOT();
	    break;
	case VALUE_PARAM_INFO_BUS_MODE:	/* p 0 4 */
	    switch (canConfig->mode) {
	    case VALUE_BUS_SILENT_MODE:
		printser_string("0 - CAN Transceiver in 'Silent Mode'");
		printLF();
		printEOT();
		break;
	    case VALUE_BUS_LOOP_BACK_MODE:
		printser_string("1 - CAN Transceiver in 'Loop Back Mode'");
		printLF();
		printEOT();
		break;
	    case VALUE_BUS_LOOP_BACK_WITH_SILENT_MODE:
		printser_string
		    ("2 - CAN Transceiver in 'Loop Back combined with Silent Mode'");
		printLF();
		printEOT();
		break;
	    case VALUE_BUS_NORMAL_MODE:
		printser_string("3 - CAN Transceiver in 'Normal Mode'");
		printLF();
		printEOT();
		break;
	    }
	    break;
	case VALUE_PARAM_INFO_BUS_CONFIG:	/* p 0 5 */
	    switch (canConfig->busConfig) {
	    case VALUE_BUS_CONFIG_11bit_125kbit:
		printser_string("1 = ISO 15765-4, CAN 11bit ID/125kBaud");
		printLF();
		printEOT();
		break;
	    case VALUE_BUS_CONFIG_11bit_250kbit:
		printser_string("2 = ISO 15765-4, CAN 11bit ID/250kBaud");
		printLF();
		printEOT();
		break;
	    case VALUE_BUS_CONFIG_11bit_500kbit:
		printser_string("3 = ISO 15765-4, CAN 11bit ID/500kBaud");
		printLF();
		printEOT();
		break;
	    case VALUE_BUS_CONFIG_11bit_1000kbit:
		printser_string("4 - ISO 15765-4, CAN 11bit ID/1000kBaud");
		printLF();
		printEOT();
		break;
	    case VALUE_BUS_CONFIG_29bit_125kbit:
		printser_string("5 - ISO 15765-4, CAN 29bit ID/125kBaud");
		printLF();
		printEOT();
		break;
	    case VALUE_BUS_CONFIG_29bit_250kbit:
		printser_string("6 - ISO 15765-4, CAN 29bit ID/250kBaud");
		printLF();
		printEOT();
		break;
	    case VALUE_BUS_CONFIG_29bit_500kbit:
		printser_string("7 - ISO 15765-4, CAN 29bit ID/500kBaud");
		printLF();
		printEOT();
		break;
	    case VALUE_BUS_CONFIG_29bit_1000kbit:
		printser_string("8 - ISO 15765-4, CAN 29bit ID/1000kBaud");
		printLF();
		printEOT();
		break;
	    }
	default:
	    createCommandResultMsg(FBID_BUS_GENERIC,
				   ERR_CODE_OS_UNKNOWN_COMMAND, 0,
				   ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);

	    break;
	}
    } else {
	createCommandResultMsg(FBID_BUS_GENERIC,
			       ERR_CODE_OS_UNKNOWN_COMMAND, 0,
			       ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);

    }
}


/*-----------------------------------------------------------*/

void odb_can_printParam(portBASE_TYPE msgType, param_data * args,
			printChar_cbf printchar)
{
    bus_param_canPrint(args);
}

/*-----------------------------------------------------------*/

void odb_can_setup()
{

    printdata_CAN = print_telegram;

    extern bus_init actBus_init;
    extern bus_send actBus_send;
    extern bus_flush actBus_flush;
    extern bus_param actBus_param;
    extern bus_paramPrint actBus_paramPrint;
    extern bus_close actBus_close;
    /* assign the actual can bus functions to the generic function pointers */
    actBus_init = bus_init_can;
    actBus_send = bus_send_can;
    actBus_flush = bus_flush_can;
    actBus_param = bus_param_can;
    actBus_paramPrint = bus_param_canPrint;
    actBus_close = bus_close_can;
}

/*-----------------------------------------------------------*/

void odb_can_init()
{
    odbarr[ODB_CAN] = odb_can_setup;

}

/*-----------------------------------------------------------*/
