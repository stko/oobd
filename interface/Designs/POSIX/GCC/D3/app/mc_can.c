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
 * MC specific can routines
 */

/* OOBD headers. */
#include "od_base.h"
#include "od_protocols.h"
#include "odb_can.h"
#include "mc_can.h"
#include "mc_sys_generic.h"
#include "mc_sys.h"
#include <libsocketcan.h>

extern char *oobd_Error_Text_OS[];
extern struct CanConfig *canConfig;

/* global vars */
struct sockaddr_can xReceiveAddress;
QueueHandle_t xCANReceiveQueue = NULL;
struct sockaddr_can xSendAddress;
int iSocketCan = 0, iReturn = 0, iCanBusIndex = 0, extended = 0;
struct can_filter rfilter[MAXCANFILTER];

//callback function for received data
recv_cbf reportReceivedData = NULL;

BaseType_t rxCount;
BaseType_t txCount;
BaseType_t errCount;
BaseType_t stCANBusOffErr;
BaseType_t stCANBusWarningErr;
BaseType_t stCANBusPassiveErr;
BaseType_t ctrCANTec;
BaseType_t ctrCANRec;

UBaseType_t bus_init_can()
{
    rxCount = 0;
    txCount = 0;
    errCount = 0;
/*
    stCANBusOffErr = 0;
    stCANBusWarningErr = 0;
    stCANBusPassiveErr = 0;
    ctrCANTec = 0;
    ctrCANRec = 0;
*/
    canConfig = pvPortMalloc(sizeof(struct CanConfig));
    if (canConfig == NULL) {
	DEBUGPRINT("Fatal error: Not enough heap to allocate CanConfig!\n",
		   'a');
	return pdFAIL;
    }
    canConfig->bus = VALUE_BUS_MODE_SILENT;	/* default */
    canConfig->busConfig = VALUE_BUS_CONFIG_11bit_500kbit;	/* default */
    canConfig->mode = VALUE_BUS_MODE_SILENT;	/* default */
    // Set-up the Receive Queue and open the socket ready to receive. 
    xCANReceiveQueue = xQueueCreate(20, sizeof(struct can_frame));
    // close all can filters
    memset(&rfilter, -1, sizeof(rfilter));

    bus_change_state_can(pdFALSE);
    return pdPASS;
}

/*-----------------------------------------------------------*/

UBaseType_t bus_change_state_can(UBaseType_t onlyClose)
{

    if (iSocketCan) {
	vSocketClose(iSocketCan);
	iSocketCan = 0;
    }
    can_do_stop(canChannel[iCanBusIndex]);
    if (onlyClose) {		// if onlyClose is set, then close only device and exit
	return pdPASS;
    }


    int mystate = CAN_STATE_STOPPED;
    DEBUGPRINT("get CAN State for %s returns %ld\n",
	       canChannel[iCanBusIndex],
	       can_get_state(canChannel[iCanBusIndex], &mystate));
    DEBUGPRINT("get CAN State mystate %ld\n", mystate);
    struct can_ctrlmode cm;
    memset(&cm, 0, sizeof(cm));
    switch (canConfig->busConfig) {
    case VALUE_BUS_CONFIG_29bit_125kbit:
    case VALUE_BUS_CONFIG_29bit_250kbit:
    case VALUE_BUS_CONFIG_29bit_500kbit:
    case VALUE_BUS_CONFIG_29bit_1000kbit:
	extended = 1;
	break;
    default:
	extended = 0;
    }
    switch (canConfig->busConfig) {
    case VALUE_BUS_CONFIG_11bit_125kbit:
    case VALUE_BUS_CONFIG_29bit_125kbit:
	DEBUGPRINT("Try to set bitrate to 125000\n", mystate);
	can_set_bitrate(canChannel[iCanBusIndex], 125000);
	break;
    case VALUE_BUS_CONFIG_11bit_250kbit:
    case VALUE_BUS_CONFIG_29bit_250kbit:
	DEBUGPRINT("Try to set bitrate to 250000\n", mystate);
	can_set_bitrate(canChannel[iCanBusIndex], 250000);
	break;
    case VALUE_BUS_CONFIG_11bit_500kbit:
    case VALUE_BUS_CONFIG_29bit_500kbit:
	DEBUGPRINT("Try to set bitrate to 500000\n", mystate);
	can_set_bitrate(canChannel[iCanBusIndex], 500000);
	break;
    case VALUE_BUS_CONFIG_11bit_1000kbit:
    case VALUE_BUS_CONFIG_29bit_1000kbit:
	DEBUGPRINT("Try to set bitrate to 1000000\n", mystate);
	can_set_bitrate(canChannel[iCanBusIndex], 1000000);
	break;
    }

/* for reference (defined in libcansocket
	#define CAN_CTRLMODE_LOOPBACK           0x01    // Loopback mode
	#define CAN_CTRLMODE_LISTENONLY         0x02    // Listen-only mode
	#define CAN_CTRLMODE_3_SAMPLES          0x04    // Triple sampling mode
	#define CAN_CTRLMODE_ONE_SHOT           0x08    // One-Shot mode
	#define CAN_CTRLMODE_BERR_REPORTING     0x10    // Bus-error reporting
	#define CAN_CTRLMODE_FD                 0x20    // CAN FD mode
	#define CAN_CTRLMODE_PRESUME_ACK        0x40    // Ignore missing CAN ACKs
*/

    switch (canConfig->mode) {

	/*
	   As just read in https://www.raspberrypi.org/forums/viewtopic.php?f=44&t=7027&start=425 
	   from by Zeta » Tue Feb 04, 2014 9:44 pm , the optimized  mcp2515 does not allow Loopback
	   and throws
	   RTNETLINK answers: Device or resource busy
	   which is also my error. So I took this original code and remove the Loopback
	   out of it -  But then tons of error frames appeared, so I changed it
	   back and left this comment in...
	 */




    case VALUE_BUS_MODE_SILENT:
	cm.mask = CAN_CTRLMODE_LOOPBACK | CAN_CTRLMODE_LISTENONLY;
	cm.flags = CAN_CTRLMODE_LISTENONLY;

	break;
    case VALUE_BUS_MODE_LOOP_BACK:
	cm.mask = CAN_CTRLMODE_LOOPBACK | CAN_CTRLMODE_LISTENONLY;
	cm.flags = CAN_CTRLMODE_LOOPBACK;
	break;
    case VALUE_BUS_MODE_LOOP_BACK_WITH_SILENT:
	cm.mask = CAN_CTRLMODE_LOOPBACK | CAN_CTRLMODE_LISTENONLY;
	cm.flags = CAN_CTRLMODE_LOOPBACK | CAN_CTRLMODE_LISTENONLY;
	break;
    case VALUE_BUS_MODE_NORMAL:
	cm.mask = CAN_CTRLMODE_LOOPBACK | CAN_CTRLMODE_LISTENONLY;
	cm.flags = 0;
	break;
    }
    DEBUGPRINT("Try to set ctrl mode bitrate to %ld %ld\n", cm.mask,
	       cm.flags);
    can_set_ctrlmode(canChannel[iCanBusIndex], &cm);
    //can_do_restart(canChannel[iCanBusIndex]);
    
    // \TODO can_get_state returns 0(=CAN_STATE_ERROR_ACTIVE) on gs_usb devices, even if the device is down, so force the start here :-|
    //if ( mystate == CAN_STATE_STOPPED ) { 
    if ( 1 ) { 
	can_do_start(canChannel[iCanBusIndex]);
    }
    can_set_restart_ms(canChannel[iCanBusIndex], 100);

    iSocketCan =
	iSocketOpenCAN(canChannel[iCanBusIndex],
		       vCANReceiveAndDeliverCallbackOOBD, xCANReceiveQueue,
		       &xReceiveAddress);
    DEBUGPRINT("Still running on %s ?!?\n", canChannel[iCanBusIndex]);

// CAN use the same socket for send and receive
    if (iSocketCan == 0) {
	DEBUGPRINT("CAN Task: Unable to open a socket.\n", 'a');
	return pdFAIL;
    } else {
	setsockopt(iSocketCan, SOL_CAN_RAW, CAN_RAW_FILTER, &rfilter,
		   sizeof(rfilter));
    }
    return pdPASS;

}




/*-----------------------------------------------------------*/

uint16_t CAN_GetFilterReg16(uint8_t FilterID, uint8_t FilterReg,
			    uint8_t FilterPos)
{
    if (FilterID >= 1 && FilterID <= MAXCANFILTER) {
	return (uint16_t) rfilter[FilterID - 1].can_mask;
    } else {
	return 0;
    }
}

/*----------------------------------------------------------------------------*/

uint32_t CAN_GetFilterReg32(uint8_t FilterID, uint8_t FilterReg)
{
    if (FilterID >= 1 && FilterID <= MAXCANFILTER) {
	return (uint32_t) rfilter[FilterID - 1].can_mask;
    } else {
	return 0;
    }
}

/*----------------------------------------------------------------------------*/

UBaseType_t bus_send_can(data_packet * data)
{
    DEBUGPRINT("CAN- Send Buffer with len %ld\n", data->len);
    /*
       int i;
       for (i = 0; i < data->len; i++) {
       printser_uint8ToHex(data->data[i]);
       printser_string(" ");
       }
       printser_string("\r");
     */
    static struct can_frame frame;
    frame.can_id = data->recv;

    if (extended) {
	frame.can_id &= CAN_EFF_MASK;
	frame.can_id |= CAN_EFF_FLAG;
    } else {
	frame.can_id &= CAN_SFF_MASK;
    }

    frame.can_dlc = data->len;
    frame.data[0] = data->data[0];
    frame.data[1] = data->data[1];
    frame.data[2] = data->data[2];
    frame.data[3] = data->data[3];
    frame.data[4] = data->data[4];
    frame.data[5] = data->data[5];
    frame.data[6] = data->data[6];
    frame.data[7] = data->data[7];
    iReturn = iSocketCANSendTo(iSocketCan, &frame, &xSendAddress);
    if (sizeof(frame) != iReturn) {
	DEBUGPRINT("CAN Failed to send whole packet: %d.\n", errno);
    }
    txCount++;
    if (txCount > 100000) {
	rxCount /= 2;
	txCount /= 2;
	errCount /= 2;
    }

    return pdPASS;
}

/*----------------------------------------------------------------------------*/

void bus_flush_can()
{
    DEBUGPRINT("Flush CAN\n", 'a');
}

/*----------------------------------------------------------------------------*/

void bus_param_can_spec_Print(UBaseType_t msgType, void *data,
			      printChar_cbf printchar)
{
    param_data *args;
    args = data;
    DEBUGPRINT
	("Bus Parameter received via Outputtask param %ld value %ld\n",
	 args->args[ARG_RECV], args->args[ARG_CMD]);

}


/*-----------------------------------------------------------*/
UBaseType_t bus_param_can_spec(param_data * args)
{
//CAN_FilterInitTypeDef CAN_FilterInitStructure;
    uint8_t i;

    switch (args->args[ARG_CMD]) {
    case PARAM_BUS_CONFIG:
	rxCount = 0;
	txCount = 0;
	errCount = 0;
	if (args->args[ARG_VALUE_1] != 0)
	    canConfig->busConfig = args->args[ARG_VALUE_1];	//store requested bus bitrate and if 11 or 29bit
	createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0, NULL);
	break;

    case PARAM_BUS_MODE:
	rxCount = 0;
	txCount = 0;
	errCount = 0;
	switch (args->args[ARG_VALUE_1]) {
	case VALUE_BUS_MODE_SILENT:
	    canConfig->mode = args->args[ARG_VALUE_1];	/* set config.mode to current value of Paramter */
	    // send event information to the ILM task
	    CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_OFF);
	    createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0,
				   NULL);
	    bus_change_state_can(pdFALSE);
	    break;
	case VALUE_BUS_MODE_LOOP_BACK:
	case VALUE_BUS_MODE_LOOP_BACK_WITH_SILENT:
	case VALUE_BUS_MODE_NORMAL:
	    canConfig->mode = args->args[ARG_VALUE_1];	/* set config.mode to current value of Paramter */
	    // send event information to the ILM task
	    CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_ON);
	    createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0,
				   NULL);
	    bus_change_state_can(pdFALSE);
	    break;

	default:
	    createCommandResultMsg(FBID_BUS_SPEC,
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED,
				   args->args[ARG_VALUE_1],
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
	    break;
	}
	break;

    case PARAM_BUS_OUTPUT_ACTIVATE:
	rxCount = 0;
	txCount = 0;
	errCount = 0;
	//! if can channel parameter given with that index, change to
	if (canChannel[args->args[ARG_VALUE_1]]) {
	    canConfig->bus = args->args[ARG_VALUE_1];	//store requested bus id
	    canConfig->mode = VALUE_BUS_MODE_SILENT;
	    bus_change_state_can(pdFALSE);
	    iCanBusIndex = args->args[ARG_VALUE_1];
	    CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_OFF);
	    CreateEventMsg(MSG_EVENT_BUS_CHANNEL, args->args[ARG_VALUE_1]);
	    createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0,
				   NULL);
	} else {
	    createCommandResultMsg(FBID_BUS_SPEC,
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED,
				   args->args[ARG_VALUE_1],
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
	}
	break;
    case PARAM_BUS_Can11FilterID:	/* 11bit CAN filter ID reconfig */
	/* check CAN-ID */
	if (args->args[ARG_VALUE_2] <= 0x7FF) {
	    if ((args->args[ARG_VALUE_1] >= 1)
		&& (args->args[ARG_VALUE_1] <= MAXCANFILTER)) {
		rfilter[args->args[ARG_VALUE_1] - 1].can_id =
		    args->args[ARG_VALUE_2];
		if (iSocketCan) {
		    setsockopt(iSocketCan, SOL_CAN_RAW, CAN_RAW_FILTER,
			       &rfilter, sizeof(rfilter));
		}

	    } else {
		createCommandResultMsg(FBID_BUS_SPEC,
				       ERR_CODE_OS_COMMAND_NOT_SUPPORTED,
				       args->args[ARG_VALUE_1],
				       ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
		break;
	    }
	    createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0,
				   NULL);
	} else {
	    createCommandResultMsg(FBID_BUS_SPEC,
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED,
				   args->args[ARG_VALUE_2],
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
	}
	break;

    case PARAM_BUS_Can29FilterID:	/* 29bit CAN filter ID reconfig */
	if ((args->args[ARG_VALUE_1] >= 1)
	    && (args->args[ARG_VALUE_1] <= MAXCANFILTER)) {
	    rfilter[args->args[ARG_VALUE_1] - 1].can_id =
		args->args[ARG_VALUE_2];
	    if (iSocketCan) {
		setsockopt(iSocketCan, SOL_CAN_RAW, CAN_RAW_FILTER,
			   &rfilter, sizeof(rfilter));
	    }

	    createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0,
				   NULL);
	} else {
	    createCommandResultMsg(FBID_BUS_SPEC,
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED,
				   args->args[ARG_VALUE_1],
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
	}
	break;

    case PARAM_BUS_Can11MaskID:	/* 11bit CAN filter mask ID reconfig */
	/* check CAN-ID */
	if (args->args[ARG_VALUE_2] <= 0x7FF) {
	    if ((args->args[ARG_VALUE_1] >= 1)
		&& (args->args[ARG_VALUE_1] <= MAXCANFILTER)) {
		rfilter[args->args[ARG_VALUE_1] - 1].can_mask =
		    args->args[ARG_VALUE_2];
		if (iSocketCan) {
		    setsockopt(iSocketCan, SOL_CAN_RAW, CAN_RAW_FILTER,
			       &rfilter, sizeof(rfilter));
		}

	    } else {
		createCommandResultMsg(FBID_BUS_SPEC,
				       ERR_CODE_OS_COMMAND_NOT_SUPPORTED,
				       args->args[ARG_VALUE_1],
				       ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
		break;
	    }
	    createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0,
				   NULL);
	} else {
	    createCommandResultMsg(FBID_BUS_SPEC,
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED,
				   args->args[ARG_VALUE_2],
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
	}
	break;

    case PARAM_BUS_Can29MaskID:	/* 29bit CAN filter mask ID reconfig */
	/* check CAN-ID */
	if ((args->args[ARG_VALUE_1] >= 1)
	    && (args->args[ARG_VALUE_1] <= MAXCANFILTER)) {
	    rfilter[args->args[ARG_VALUE_1] - 1].can_mask =
		args->args[ARG_VALUE_2];
	    if (iSocketCan) {
		setsockopt(iSocketCan, SOL_CAN_RAW, CAN_RAW_FILTER,
			   &rfilter, sizeof(rfilter));
	    }

	} else {
	    createCommandResultMsg(FBID_BUS_SPEC,
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED,
				   args->args[ARG_VALUE_1],
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
	    break;
	}
	createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0, NULL);
	break;

    case PARAM_BUS_CanFilterReset:	/*  CAN filter mask ID reconfig */
	memset(&rfilter, -1, sizeof(rfilter));
	if (iSocketCan) {
	    setsockopt(iSocketCan, SOL_CAN_RAW, CAN_RAW_FILTER, &rfilter,
		       sizeof(rfilter));
	}
	createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0, NULL);
	break;

    default:
	createCommandResultMsg(FBID_BUS_SPEC,
			       ERR_CODE_OS_UNKNOWN_COMMAND, 0,
			       ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
	break;
    }

    return pdPASS;
}


/*-----------------------------------------------------------*/
void bus_close_can()
{
    extern struct CanConfig *canConfig;
    reportReceivedData = NULL;
    vSocketClose(iSocketCan);
    iSocketCan = 0;
    vPortFree(canConfig);
}



/*-----------------------------------------------------------*/



void vCANReceiveAndDeliverCallbackOOBD(int iSocket, void *pvContext)
{
    static struct can_frame frame;
    //struct sockaddr_can xReceiveAddress;
    static data_packet dp;
    if (sizeof(struct can_frame) ==
	iSocketCANReceiveISR(iSocket, &frame, &xReceiveAddress)) {
	rxCount++;
	if (rxCount > 100000) {
	    rxCount /= 2;
	    txCount /= 2;
	    errCount /= 2;

	}
	/* Data received. Process it. */
	dp.recv = frame.can_id;	// add the HByte again
	dp.len = frame.can_dlc;
	dp.err = frame.can_id && 80000000 != 0;
	if (dp.err) {
	    errCount++;
	    if (errCount > 100000) {
		rxCount /= 2;
		txCount /= 2;
		errCount /= 2;
	    }
	}
	dp.data = &frame.data[0];	// data starts here
	if (reportReceivedData)
	    reportReceivedData(&dp, pdTRUE);
    }
}




UBaseType_t busControl(UBaseType_t cmd, void *param)
{
    switch (cmd) {
    case ODB_CMD_RECV:
	reportReceivedData = param;
	return pdPASS;
	break;
    default:
	return pdFAIL;
	break;
    }
}

/*----------------------------------------------------------------------------*/

UBaseType_t bus_rx_error_can()
{
    struct can_berr_counter errCount;
    can_get_berr_counter(canChannel[iCanBusIndex], &errCount);
    return errCount.rxerr;
}

/*----------------------------------------------------------------------------*/

UBaseType_t bus_tx_error_can()
{
    struct can_berr_counter errCount;
    can_get_berr_counter(canChannel[iCanBusIndex], &errCount);
    return errCount.txerr;
}

/*----------------------------------------------------------------------------*/

void bus_clear_rx_error_can()
{
    errCount = 0;
}

/*----------------------------------------------------------------------------*/

void bus_clear_tx_error_can()
{
}

/*----------------------------------------------------------------------------*/

UBaseType_t bus_rx_count_can()
{
    return rxCount;
}

/*----------------------------------------------------------------------------*/

UBaseType_t bus_tx_count_can()
{
    return txCount;
}

/*----------------------------------------------------------------------------*/

void bus_clear_rx_count_can()
{
    rxCount = 0;
}

/*----------------------------------------------------------------------------*/

void bus_clear_tx_count_can()
{
    txCount = 0;
}

BaseType_t bus_busoff_error_can()
{
    /* check for Bus-off flag */
    struct can_device_stats devstats;
    can_get_device_stats(canChannel[iCanBusIndex], &devstats);
    return devstats.bus_off;
}

BaseType_t bus_passive_error_can()
{
    /* check for Error passive flag */
    struct can_device_stats devstats;
    can_get_device_stats(canChannel[iCanBusIndex], &devstats);
    return devstats.error_passive;
}

BaseType_t bus_warning_error_can()
{
    /* check for Error Warning flag */
    struct can_device_stats devstats;
    can_get_device_stats(canChannel[iCanBusIndex], &devstats);
    return devstats.error_warning;
}

BaseType_t bus_tec_can()
{
    /* read Transmit Error Counter of CAN hardware */
    struct can_berr_counter errCount;
    can_get_berr_counter(canChannel[iCanBusIndex], &errCount);
    return errCount.txerr;
}

BaseType_t bus_rec_can()
{
    /* read Receive Error Counter of CAN hardware */
    struct can_berr_counter errCount;
    can_get_berr_counter(canChannel[iCanBusIndex], &errCount);
    return errCount.rxerr;
}
