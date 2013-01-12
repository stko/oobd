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

/* UDP Packet size to send/receive. */
#define mainUDP_SEND_ADDRESS		"127.0.0.1"
#define UDP_PORT_SEND			( 9998 )
#define UDP_PORT_RECEIVE		( 9999 )

/* global vars */
struct sockaddr_in xReceiveAddress;
int iSocketReceive = 0;
xQueueHandle xUDPReceiveQueue = NULL;
struct sockaddr_in xSendAddress;
int iSocketSend = 0, iReturn = 0, iSendTaskList = pdTRUE;

//callback function for received data
recv_cbf reportReceicedData = NULL;
/* Send/Receive UDP packets. */
void prvUDPTask(void *pvParameters);

xTaskHandle xprvUDPTaskHandle;
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
// Initialise Receives sockets. 
    xReceiveAddress.sin_family = AF_INET;
    xReceiveAddress.sin_addr.s_addr = INADDR_ANY;
    xReceiveAddress.sin_port = htons(UDP_PORT_RECEIVE);

    // Set-up the Receive Queue and open the socket ready to receive. 
    xUDPReceiveQueue = xQueueCreate(2, sizeof(xUDPPacket));
    iSocketReceive =
	iSocketOpenUDP(vUDPReceiveAndDeliverCallback, xUDPReceiveQueue,
		       &xReceiveAddress);

    // Remember to open a whole in your Firewall to be able to receive!!!


    iSocketSend = iSocketOpenUDP(NULL, NULL, NULL);

    if (iSocketSend != 0) {
	xSendAddress.sin_family = AF_INET;
	/* Set the UDP main address to reflect your local subnet. */
	iReturn =
	    !inet_aton(mainUDP_SEND_ADDRESS,
		       (struct in_addr *) &(xSendAddress.sin_addr.s_addr));
	xSendAddress.sin_port = htons(UDP_PORT_SEND);
	/* Create a Task which waits to receive messages and sends its own when it times out. */
	xTaskCreate(prvUDPTask, "UDPRxTx", configMINIMAL_STACK_SIZE, NULL,
		    TASK_PRIO_MID, &xprvUDPTaskHandle);

	/* Remember to open a whole in your Firewall to be able to receive!!! */

	return pdPASS;
    } else {

	vSocketClose(iSocketSend);
	DEBUGPRINT("UDP Task: Unable to open a socket.\n", 'a');
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
    static xUDPPacket xPacket;
    xPacket.ucPacket[0] = data->recv & 0xFF;	//just use the LByte
    xPacket.ucPacket[1] = data->len;
    xPacket.ucPacket[2] = 0;	// err not used here
    xPacket.ucPacket[3] = data->data[0];
    xPacket.ucPacket[4] = data->data[1];
    xPacket.ucPacket[5] = data->data[2];
    xPacket.ucPacket[6] = data->data[3];
    xPacket.ucPacket[7] = data->data[4];
    xPacket.ucPacket[8] = data->data[5];
    xPacket.ucPacket[9] = data->data[6];
    xPacket.ucPacket[10] = data->data[7];
    iReturn = iSocketUDPSendTo(iSocketSend, &xPacket, &xSendAddress);
    if (sizeof(xUDPPacket) != iReturn) {
	DEBUGPRINT("UDP Failed to send whole packet: %d.\n", errno);
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
    vSocketClose(iSocketSend);
    vSocketClose(iSocketReceive);
    vTaskDelete(xprvUDPTaskHandle);
    free(canConfig);
}



/*-----------------------------------------------------------*/

void prvUDPTask(void *pvParameters)
{
    static xUDPPacket xPacket;
    static data_packet dp;
    //struct sockaddr_in xSendAddress;
    // int iSocketSend, iReturn = 0, iSendTaskList = pdTRUE;
    //xQueueHandle xUDPReceiveQueue = (xQueueHandle) pvParameters;
    /* Open a socket for sending. */
    for (;;) {
	if (pdPASS ==
	    xQueueReceive(xUDPReceiveQueue, &xPacket,
			  2500 / portTICK_RATE_MS)) {
	    rxCount++;
	    if (rxCount > 100000) {
		rxCount /= 2;
		txCount /= 2;
		errCount /= 2;

	    }
	    /* Data received. Process it. */
	    dp.recv = xPacket.ucPacket[0] + 0x700;	// add the HByte again
	    dp.len = xPacket.ucPacket[1];
	    dp.err = xPacket.ucPacket[2];	// use received value for error simulations
	    if (dp.err) {
		errCount++;
		if (errCount > 100000) {
		    rxCount /= 2;
		    txCount /= 2;
		    errCount /= 2;
		}
	    }
	    dp.data = &xPacket.ucPacket[3];	// data starts here
	    //xPacket.ucNull = 0; /* Ensure the string is terminated. */
	    //DEBUGPRINT ("--%s", xPacket.ucPacket);
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
