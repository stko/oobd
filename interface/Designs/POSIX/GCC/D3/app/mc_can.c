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
int iSocketReceive = 0;
QueueHandle_t xCANReceiveQueue = NULL;
struct sockaddr_can xSendAddress;
int iSocketSend = 0, iReturn = 0, iSendTaskList = pdTRUE;

//callback function for received data
recv_cbf reportReceivedData = NULL;

void CAN1_Configuration(uint8_t CAN_BusConfig, uint8_t CAN_ModeConfig);

//some define copied from STM32 to have simular program layout
#define CAN_Mode_Normal             ((uint8_t)0x00)	//!< normal mode
#define CAN_Mode_LoopBack           ((uint8_t)0x01)	//!< loopback mode
#define CAN_Mode_Silent             ((uint8_t)0x02)	//!< silent mode
#define CAN_Mode_Silent_LoopBack    ((uint8_t)0x03)	//!< loopback combined with silent mode



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
    stCANBusOffErr = 0;
    stCANBusWarningErr = 0;
    stCANBusPassiveErr = 0;
    ctrCANTec = 0;
    ctrCANRec = 0;

    canConfig = pvPortMalloc(sizeof(struct CanConfig));
    if (canConfig == NULL) {
	DEBUGPRINT("Fatal error: Not enough heap to allocate CanConfig!\n",
		   'a');
	return pdFAIL;
    }
    canConfig->bus = VALUE_BUS_MODE_SILENT;	/* default */
    canConfig->busConfig = VALUE_BUS_CONFIG_11bit_500kbit;	/* default */

    // Set-up the Receive Queue and open the socket ready to receive. 
    xCANReceiveQueue = xQueueCreate(20, sizeof(struct can_frame));
    iSocketReceive =
	iSocketOpenCAN(vCANReceiveAndDeliverCallbackOOBD, xCANReceiveQueue,
		       &xReceiveAddress);

    // Remember to open a whole in your Firewall to be able to receive!!!


// CAN use the same socket for send and receive
//iSocketSend = iSocketOpenCAN(NULL, NULL, NULL);
    iSocketSend = iSocketReceive;
    int mystate;
    DEBUGPRINT("get CAN State for %s returns %ld\n", canChannel,
	       can_get_state(canChannel, &mystate));
    DEBUGPRINT("get CAN State mystate %ld\n", mystate);
/*
    struct can_ctrlmode cm;
    memset(&cm, 0, sizeof(cm));
    cm.mask = CAN_CTRLMODE_LOOPBACK | CAN_CTRLMODE_LISTENONLY;
    cm.flags = CAN_CTRLMODE_LOOPBACK;
    DEBUGPRINT("can_set_ctrlmode: %ld\n",
	       can_set_ctrlmode(canChannel, &cm));
    DEBUGPRINT("can_set_bitrate: %ld\n",
	       can_set_bitrate(canChannel, 500000));
*/
    if (iSocketSend != 0) {
	return pdPASS;
    } else {

	vSocketClose(iSocketSend);
	DEBUGPRINT("CAN Task: Unable to open a socket.\n", 'a');
	return pdFAIL;
    }
}





/*-----------------------------------------------------------*/

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
    if (canConfig->busConfig == VALUE_BUS_CONFIG_29bit_125kbit
	|| canConfig->busConfig == VALUE_BUS_CONFIG_29bit_250kbit
	|| canConfig->busConfig == VALUE_BUS_CONFIG_29bit_500kbit
	|| canConfig->busConfig == VALUE_BUS_CONFIG_29bit_1000kbit) {
	frame.can_id = 0x8000 & data->recv;	/* Bit 31=1 for Extended CAN identifier 29 bit */
    } else {

	frame.can_id = data->recv;	/* Standard CAN identifier 11bit */
    }


    frame.can_id = data->recv;
    frame.can_dlc = data->len;
    frame.data[0] = data->data[0];
    frame.data[1] = data->data[1];
    frame.data[2] = data->data[2];
    frame.data[3] = data->data[3];
    frame.data[4] = data->data[4];
    frame.data[5] = data->data[5];
    frame.data[6] = data->data[6];
    frame.data[7] = data->data[7];
    iReturn = iSocketCANSendTo(iSocketSend, &frame, &xSendAddress);
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

/*----------------------------------------------------------------------------*/

uint16_t CAN_GetFilterReg16(uint8_t FilterID, uint8_t FilterReg,
			    uint8_t FilterPos)
{

/*    if (FilterPos == 0) {	// IDLow 
    // Get the LowID of the 32bit Filter register Fx.FR1
    if (FilterReg == 1)		// FR1
	return (uint16_t) (CAN1->
			   sFilterRegister[FilterID].FR1 & 0x0000FFFF) >>
	    5;
    else if (FilterReg == 2)	// FR2
	return (uint16_t) (CAN1->
			   sFilterRegister[FilterID].FR2 & 0x0000FFFF) >>
	    5;
    else
	return NULL;
}

else
if (FilterPos == 1) {		// ID High
if (FilterReg == 1)		// FR1
    return (uint16_t) (CAN1->
		       sFilterRegister[FilterID].FR1 >> 16 & 0x0000FFFF) >>
	5;
else if (FilterReg == 2)	// FR2
    return (uint16_t) (CAN1->
		       sFilterRegister[FilterID].FR2 >> 16 & 0x0000FFFF) >>
	5;
else
    return NULL;
} else
*/
    return 0;
}

/*----------------------------------------------------------------------------*/

uint32_t CAN_GetFilterReg32(uint8_t FilterID, uint8_t FilterReg)
{
/*
	if (FilterReg == 1) // FR1
		return (uint32_t)CAN1->sFilterRegister[FilterID].FR1>>3;
	else if (FilterReg == 2) // FR2
		return (uint32_t)CAN1->sFilterRegister[FilterID].FR2>>3;
	else
		return NULL;
*/
    return 0;
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
	    CAN1_Configuration(args->args[ARG_VALUE_1], CAN_Mode_Silent);	/* reinitialization of CAN interface */
	canConfig->busConfig = args->args[ARG_VALUE_1];
	createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0, NULL);
	break;

    case PARAM_BUS_MODE:
	rxCount = 0;
	txCount = 0;
	errCount = 0;
	switch (args->args[ARG_VALUE_1]) {
	case VALUE_BUS_MODE_SILENT:
	    CAN1_Configuration((uint8_t) canConfig->busConfig, CAN_Mode_Silent);	/* set CAN interface to silent mode */
	    canConfig->bus = args->args[ARG_VALUE_1];	/* set config.bus to current value of Paramter */
	    // send event information to the ILM task
	    CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_OFF);
	    createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0,
				   NULL);
	    break;
	case VALUE_BUS_MODE_LOOP_BACK:
	    CAN1_Configuration((uint8_t) canConfig->busConfig, CAN_Mode_LoopBack);	/* set CAN interface to loop back mode */
	    canConfig->bus = args->args[ARG_VALUE_1];	/* set config.bus to current value of Paramter */
	    // send event information to the ILM task
	    CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_ON);
	    createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0,
				   NULL);
	    break;
	case VALUE_BUS_MODE_LOOP_BACK_WITH_SILENT:
	    CAN1_Configuration((uint8_t) canConfig->busConfig, CAN_Mode_Silent_LoopBack);	/* set CAN interface to loop back combined with silent mode */
	    canConfig->bus = args->args[ARG_VALUE_1];	/* set config.bus to current value of Paramter */
	    // send event information to the ILM task
	    CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_ON);
	    createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0,
				   NULL);
	    break;
	case VALUE_BUS_MODE_NORMAL:
	    CAN1_Configuration((uint8_t) canConfig->busConfig, CAN_Mode_Normal);	/* set CAN interface to normal mode */
	    canConfig->bus = args->args[ARG_VALUE_1];	/* set config.bus to current value of Paramter */
	    // send event information to the ILM task
	    CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_ON);
	    createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0,
				   NULL);
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
	switch (args->args[ARG_VALUE_1]) {
	case 0:
	case 1:
	    CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_OFF);
	    CreateEventMsg(MSG_EVENT_BUS_CHANNEL,
			   args->args[ARG_VALUE_1] == 1 ? 1 : 2);
	    //! \todo anything to do to switch between the different sockets
	    createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0,
				   NULL);
	    break;
	default:
	    createCommandResultMsg(FBID_BUS_SPEC,
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED,
				   args->args[ARG_VALUE_1],
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
	    break;
	}
	break;
    case PARAM_BUS_Can11FilterID:	/* 11bit CAN filter ID reconfig */
	/* check CAN-ID */
	if (args->args[ARG_VALUE_2] < 0x7FF) {
	    /* check if Filter Number is odd */
	    if ((args->args[ARG_VALUE_1] >= 1)
		&& (args->args[ARG_VALUE_1] <= 10)) {

	    } else {
		createCommandResultMsg(FBID_BUS_SPEC,
				       ERR_CODE_OS_COMMAND_NOT_SUPPORTED,
				       args->args[ARG_VALUE_1],
				       ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
		break;
	    }

	    if (args->args[ARG_VALUE_1] & 1) {
	    } else {
	    }

	    //CAN_FilterInit(&CAN_FilterInitStructure);
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
	//CAN_FilterInit(&CAN_FilterInitStructure);
	createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0, NULL);
	break;

    case PARAM_BUS_Can11MaskID:	/* 11bit CAN filter mask ID reconfig */
	/* check CAN-ID */
	if (args->args[ARG_VALUE_2] <= 0x7FF) {
	    /* check filter mask number */
	    if ((args->args[ARG_VALUE_1] >= 1)
		&& (args->args[ARG_VALUE_1] <= 10)) {
	    } else {
		createCommandResultMsg(FBID_BUS_SPEC,
				       ERR_CODE_OS_COMMAND_NOT_SUPPORTED,
				       args->args[ARG_VALUE_1],
				       ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
		break;
	    }

	    /* CAN filter mask ID reconfig */
	    if (args->args[ARG_VALUE_1] & 1) {
		createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0,
				       NULL);
	    } else {
//      CAN_FilterInit(&CAN_FilterInitStructure);
		createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0,
				       NULL);
	    }
	} else {
	    createCommandResultMsg(FBID_BUS_SPEC,
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED,
				   args->args[ARG_VALUE_2],
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
	}
	break;

    case PARAM_BUS_Can29MaskID:	/* 29bit CAN filter mask ID reconfig */
	//    CAN_FilterInit(&CAN_FilterInitStructure);
	createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0, NULL);
	break;

    case PARAM_BUS_CanFilterReset:	/* 11bit CAN filter mask ID reconfig */
	for (i = 0; i < 14; i++) {
//      CAN_FilterInit(&CAN_FilterInitStructure);
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
    vSocketClose(iSocketSend);
    vSocketClose(iSocketReceive);
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
    return errCount;
}

/*----------------------------------------------------------------------------*/

UBaseType_t bus_tx_error_can()
{
    return 0;
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
    return 0;
}

BaseType_t bus_passive_error_can()
{
    /* check for Error passive flag */
    return 0;
}

BaseType_t bus_warning_error_can()
{
    /* check for Error Warning flag */
    return 0;
}

BaseType_t bus_tec_can()
{
    /* read Transmit Error Counter of CAN hardware */
    return 0;
}

BaseType_t bus_rec_can()
{
    /* read Receive Error Counter of CAN hardware */
    return 0;
}

void CAN1_Configuration(uint8_t CAN_BusConfig, uint8_t CAN_ModeConfig)
{

    if (CAN_BusConfig == VALUE_BUS_CONFIG_11bit_125kbit || CAN_BusConfig
	== VALUE_BUS_CONFIG_29bit_125kbit) {


    }

    else if (CAN_BusConfig == VALUE_BUS_CONFIG_11bit_250kbit
	     || CAN_BusConfig == VALUE_BUS_CONFIG_29bit_250kbit) {


    }

    else if (CAN_BusConfig == VALUE_BUS_CONFIG_11bit_500kbit
	     || CAN_BusConfig == VALUE_BUS_CONFIG_29bit_500kbit) {


    }

    else if (CAN_BusConfig == VALUE_BUS_CONFIG_11bit_1000kbit
	     || CAN_BusConfig == VALUE_BUS_CONFIG_29bit_1000kbit) {


    }

    else {

	/* default CAN bus speed is set to 500kbaud */

    }

    //   CAN_Init(CAN1, &CAN_InitStructure);

}
