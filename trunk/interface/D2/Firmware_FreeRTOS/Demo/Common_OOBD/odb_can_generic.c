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

void odb_can_printParam(portBASE_TYPE msgType, void *data,
			printChar_cbf printchar)
{
    static param_data *pd;
    pd = data;
    portBASE_TYPE cmdKey = pd->key, cmdValue = pd->value;	/* the both possible params */
    switch (cmdKey) {
    case VALUE_PARAM_INFO_BUS_MODE:	/* p 0 4 */
	switch (canConfig.mode) {
	case VALUE_BUS_SILENT_MODE:
	    printser_string("0 - CAN Transceiver in 'Silent Mode'");
	    break;
	case VALUE_BUS_LOOP_BACK_MODE:
	    printser_string("1 - CAN Transceiver in 'Loop Back Mode'");
	    break;
	case VALUE_BUS_LOOP_BACK_WITH_SILENT_MODE:
	    printser_string
		("2 - CAN Transceiver in 'Loop Back combined with Silent Mode'");
	    break;
	case VALUE_BUS_NORMAL_MODE:
	    printser_string("3 - CAN Transceiver in 'Normal Mode'");
	    break;
	}
	break;
    case VALUE_PARAM_INFO_BUS_CONFIG:	/* p 0 5 */
	switch (canConfig.busConfig) {
	case VALUE_BUS_CONFIG_11bit_125kbit:
	    printser_string("1 = ISO 15765-4, CAN 11bit ID/125kBaud");
	    break;
	case VALUE_BUS_CONFIG_11bit_250kbit:
	    printser_string("2 = ISO 15765-4, CAN 11bit ID/250kBaud");
	    break;
	case VALUE_BUS_CONFIG_11bit_500kbit:
	    printser_string("3 = ISO 15765-4, CAN 11bit ID/500kBaud");
	    break;
	case VALUE_BUS_CONFIG_11bit_1000kbit:
	    printser_string("4 - ISO 15765-4, CAN 11bit ID/1000kBaud");
	    break;
	case VALUE_BUS_CONFIG_29bit_125kbit:
	    printser_string("5 - ISO 15765-4, CAN 29bit ID/125kBaud");
	    break;
	case VALUE_BUS_CONFIG_29bit_250kbit:
	    printser_string("6 - ISO 15765-4, CAN 29bit ID/250kBaud");
	    break;
	case VALUE_BUS_CONFIG_29bit_500kbit:
	    printser_string("7 - ISO 15765-4, CAN 29bit ID/500kBaud");
	    break;
	case VALUE_BUS_CONFIG_29bit_1000kbit:
	    printser_string("8 - ISO 15765-4, CAN 29bit ID/1000kBaud");
	    break;
	}
#ifdef OOBD_PLATFORM_STM32
    case VALUE_PARAM_INFO_ADC_POWER:	/* p 0 6 */
	printser_int((readADC1(8) * (3.15 / 4096)) * 10000, 10);	/* result in mV */
	printser_string(" mV");
	break;
    case VALUE_PARAM_INFO_CPU_INFO:	/* p 0 10 */
	sendCPUInfo();		/* send CPU Info */
	break;
    case VALUE_PARAM_INFO_MEM_LOC:	/* p 0 11 */
	sendMemLoc(0x8002400);	/* send Mem Location */
	break;
    case VALUE_PARAM_INFO_ROM_TABLE_LOC:	/* p 0 12 */
	sendRomTable();		/* send ROM Table */
	break;
    case VALUE_PARAM_INFO_FREE_HEAP_SIZE:	/* p 0 13 */
	printser_string("Total Heap (in byte): ");
	printser_int(configTOTAL_HEAP_SIZE, 10);
	printser_string("Free Heap (in byte): ");
	printser_int(xPortGetFreeHeapSize(), 10);	/* send FreeRTOS free heap size */
	break;
    case VALUE_PARAM_INFO_CRC32:	/* p 0 14 */
	if (CheckCrc32() == 0) {
	    printser_string("CRC-32 application check passed!");
	} else {
	    printser_string("CRC-32 application check failed");
	}
    case VALUE_PARAM_INFO_BTM222_DEVICENAME:	/* p 0 20 */
	printser_string(BTM222_DeviceName);
	break;
    case VALUE_PARAM_INFO_BTM222_UART_SPEED:	/* p 0 21 */
	switch (BTM222_UartSpeed) {
	case '0':
	    printser_string("4800 bit/s");
	    break;

	case '1':
	    printser_string("9600 bit/s");
	    break;

	case '2':
	    printser_string("19200 bit/s");
	    break;

	case '3':
	    printser_string("38400 bit/s");
	    break;

	case '4':
	    printser_string("57600 bit/s");
	    break;

	case '5':
	    printser_string("115200 bit/s");
	    break;

	case '6':
	    printser_string("230400 bit/s");
	    break;

	case '7':
	    printser_string("460800 bit/s");
	    break;

	default:
	    printser_string("not detected");
	    break;
	}
	break;

#endif


#ifdef OOBD_PLATFORM_STM32
	// \todo to be moved into mc specific file
    case PARAM_RESET:
	if (1 == cmdValue) {
	    DEBUGUARTPRINT("\r\n*** Softreset performed !!!");
	    SCB->AIRCR = 0x05FA0604;	/* soft reset */
	}
	if (2 == cmdValue) {
	    DEBUGUARTPRINT("\r\n*** Hardreset performed !!!");
	    SCB->AIRCR = 0x05FA0004;	/* hard reset */
	}
	break;
#endif
    }
}

/*-----------------------------------------------------------*/

void odb_can_setup()
{

    printdata_CAN = print_telegram;

    extern bus_init actBus_init;
    extern bus_send actBus_send;
    extern bus_flush actBus_flush;
    extern bus_param actBus_param;
    extern bus_close actBus_close;
    /* assign the actual can bus functions to the generic function pointers */
    actBus_init = bus_init_can;
    actBus_send = bus_send_can;
    actBus_flush = bus_flush_can;
    actBus_param = bus_param_can;
    actBus_close = bus_close_can;
}

/*-----------------------------------------------------------*/

void odb_can_init()
{
    odbarr[ODB_CAN] = odb_can_setup;
}

/*-----------------------------------------------------------*/
