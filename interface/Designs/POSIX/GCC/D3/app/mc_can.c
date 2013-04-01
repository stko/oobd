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


/* global vars */
struct sockaddr_can xReceiveAddress;
int iSocketReceive = 0;
xQueueHandle xCANReceiveQueue = NULL;
struct sockaddr_can xSendAddress;
int iSocketSend = 0, iReturn = 0, iSendTaskList = pdTRUE;

//callback function for received data
recv_cbf reportReceicedData = NULL;
/* Send/Receive CAN packets. */
void prvCANTask(void *pvParameters);

xTaskHandle xprvCANTaskHandle;
portBASE_TYPE rxCount;
portBASE_TYPE txCount;
portBASE_TYPE errCount;
extern char *oobd_Error_Text_OS[];
extern struct CanConfig *canConfig;

/*-----------------------------------------------------------*/

portBASE_TYPE bus_init_can()
{
    rxCount = 0;
    txCount = 0;
    errCount = 0;
    canConfig = pvPortMalloc(sizeof(struct CanConfig));
    if (canConfig == NULL) {
	DEBUGPRINT("Fatal error: Not enough heap to allocate CanConfig!\n",
		   'a');
	return pdFAIL;
    }
    canConfig->bus = VALUE_BUS_MODE_SILENT;	/* default */
    canConfig->busConfig = VALUE_BUS_CONFIG_11bit_500kbit;	/* default */

    // Set-up the Receive Queue and open the socket ready to receive. 
    xCANReceiveQueue = xQueueCreate(2, sizeof(struct can_frame));
    iSocketReceive =
	iSocketOpenCAN(vCANReceiveAndDeliverCallback, xCANReceiveQueue,
		       &xReceiveAddress);

    // Remember to open a whole in your Firewall to be able to receive!!!


// CAN use the same socket for send and receive
//iSocketSend = iSocketOpenCAN(NULL, NULL, NULL);
    iSocketSend = iSocketReceive;

    if (iSocketSend != 0) {
	/* Create a Task which waits to receive messages and sends its own when it times out. */
	xTaskCreate(prvCANTask, "CANRxTx", configMINIMAL_STACK_SIZE, NULL,
		    TASK_PRIO_MID, &xprvCANTaskHandle);

	/* Remember to open a whole in your Firewall to be able to receive!!! */

	return pdPASS;
    } else {

	vSocketClose(iSocketSend);
	DEBUGPRINT("CAN Task: Unable to open a socket.\n", 'a');
	return pdFAIL;
    }

}





/*-----------------------------------------------------------*/

portBASE_TYPE bus_send_can(data_packet * data)
{
    int i;
    DEBUGPRINT("CAN- Send Buffer with len %ld\n", data->len);
    /*++++++++++++++++++++
       for (i = 0; i < data->len; i++) {
       printser_uint8ToHex(data->data[i]);
       printser_string(" ");
       }
       printser_string("\r");
     */
    static struct can_frame frame;
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


/*-----------------------------------------------------------*/
void bus_flush_can()
{
    DEBUGPRINT("Flush CAN\n", 'a');
}


/*-----------------------------------------------------------*/

void bus_param_can_spec_Print(portBASE_TYPE msgType, void *data,
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
/*
	if (FilterPos == 0) // IDLow 
	{
		if (FilterReg == 1) // FR1
			return (uint16_t)(CAN1->sFilterRegister[FilterID].FR1 & 0x0000FFFF)>>5;
		else if (FilterReg == 2) // FR2
			return (uint16_t)(CAN1->sFilterRegister[FilterID].FR2 & 0x0000FFFF)>>5;
		else
			return NULL;
	}
	else if (FilterPos == 1)// ID High
	{
		if (FilterReg == 1) // FR1
			return (uint16_t)(CAN1->sFilterRegister[FilterID].FR1>>16 & 0x0000FFFF)>>5;
		else if (FilterReg == 2) // FR2
			return (uint16_t)(CAN1->sFilterRegister[FilterID].FR2>>16 & 0x0000FFFF)>>5;
		else
			return NULL;
	}
	else
		return NULL;
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
portBASE_TYPE bus_param_can_spec(param_data * args)
{


    switch (args->args[ARG_CMD]) {
    case PARAM_BUS_CONFIG:
	rxCount = 0;
	txCount = 0;
	errCount = 0;
	if (args->args[ARG_VALUE_1] != 0)
	    canConfig->busConfig = args->args[ARG_VALUE_1];
	createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0, NULL);
	break;

    case PARAM_BUS_MODE:
	rxCount = 0;
	txCount = 0;
	errCount = 0;
	switch (args->args[ARG_VALUE_1]) {
	case VALUE_BUS_MODE_SILENT:
	    // send event information to the ILM task
	    CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_OFF);
	    createCommandResultMsg(FBID_BUS_SPEC,
				   ERR_CODE_NO_ERR, 0, NULL);
	    break;
	case VALUE_BUS_MODE_LOOP_BACK:
	    // send event information to the ILM task
	    CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_ON);
	    createCommandResultMsg(FBID_BUS_SPEC,
				   ERR_CODE_NO_ERR, 0, NULL);
	    break;
	case VALUE_BUS_MODE_LOOP_BACK_WITH_SILENT:
	    // send event information to the ILM task
	    CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_ON);
	    createCommandResultMsg(FBID_BUS_SPEC,
				   ERR_CODE_NO_ERR, 0, NULL);
	    break;
	case VALUE_BUS_MODE_NORMAL:
	    // send event information to the ILM task
	    CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_ON);
	    createCommandResultMsg(FBID_BUS_SPEC,
				   ERR_CODE_NO_ERR, 0, NULL);
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
	    sysIoCtrl(5000, 0, args->args[ARG_VALUE_1], 0,	//5000 is just a dummy value, as a channel switch is not part of the generic part at all
		      0);
	    //! \bug this delay causes the protocol task to sleep for this time, but dring that his message queue runs full
	    vTaskDelay(250 / portTICK_RATE_MS);	// wait to give the mechanic relay time to switch
	    createCommandResultMsg(FBID_BUS_SPEC,
				   ERR_CODE_NO_ERR, 0, NULL);
	    break;
	default:
	    createCommandResultMsg(FBID_BUS_SPEC,
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED,
				   args->args[ARG_VALUE_1],
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
	    break;
	}
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
    reportReceicedData = NULL;
    vSocketClose(iSocketSend);
    vSocketClose(iSocketReceive);
    vTaskDelete(xprvCANTaskHandle);
    vPortFree(canConfig);
}



/*-----------------------------------------------------------*/

void prvCANTask(void *pvParameters)
{
    static struct can_frame frame;
    static data_packet dp;
    //struct sockaddr_in xSendAddress;
    // int iSocketSend, iReturn = 0, iSendTaskList = pdTRUE;
    //xQueueHandle xCANReceiveQueue = (xQueueHandle) pvParameters;
    /* Open a socket for sending. */
    for (;;) {
	if (pdPASS ==
	    xQueueReceive(xCANReceiveQueue, &frame,
			  2500 / portTICK_RATE_MS)) {
	    rxCount++;
	    if (rxCount > 100000) {
		rxCount /= 2;
		txCount /= 2;
		errCount /= 2;

	    }
	    /* Data received. Process it. */
	    dp.recv = frame.can_id;	// add the HByte again
	    dp.len = frame.can_dlc;
	    dp.err = 0;		// dammed, real can bus does not allow error simulation anymore :-(
	    if (dp.err) {
		errCount++;
		if (errCount > 100000) {
		    rxCount /= 2;
		    txCount /= 2;
		    errCount /= 2;
		}
	    }
	    dp.data = &frame.data[0];	// data starts here
	    if (reportReceicedData)
		reportReceicedData(&dp);
	}
    }


    /* Unable to open the socket. Bail out. */
    vTaskDelete(NULL);
}



portBASE_TYPE busControl(portBASE_TYPE cmd, void *param)
{
    switch (cmd) {
    case ODB_CMD_RECV:
	reportReceicedData = param;
	return pdPASS;
	break;
    default:
	return pdFAIL;
	break;
    }
}


portBASE_TYPE bus_rx_error_can()
{
    return errCount;
}

portBASE_TYPE bus_tx_error_can()
{
    return 0;
}


void bus_clear_rx_error_can()
{
    errCount = 0;
}


void bus_clear_tx_error_can()
{
}



portBASE_TYPE bus_rx_count_can()
{
    return rxCount;
}


portBASE_TYPE bus_tx_count_can()
{
    return txCount;
}


void bus_clear_rx_count_can()
{
    rxCount = 0;
}


void bus_clear_tx_count_can()
{
    txCount = 0;
}

portBASE_TYPE bus_busoff_error_can()
{
    /* check for Bus-off flag */
    return 0;
}

portBASE_TYPE bus_passive_error_can()
{
    /* check for Error passive flag */
    return 0;
}

portBASE_TYPE bus_warning_error_can()
{
    /* check for Error Warning flag */
    return 0;
}

portBASE_TYPE bus_tec_can()
{
    /* read Transmit Error Counter of CAN hardware */
    return 0;
}

portBASE_TYPE bus_rec_can()
{
    /* read Receive Error Counter of CAN hardware */
    return 0;
}
