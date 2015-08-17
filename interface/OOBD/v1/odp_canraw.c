//>>>> oobdtemple protocol header>>>>
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


/* OOBD headers. */
#include "od_config.h"
#include "od_base.h"
#include "od_protocols.h"

//<<<< oobdtemple protocol header <<<<
/**
 * implementation of the CAN raw protocol
 */

#include "odp_canraw.h"


/* some defines only need internally */

/* some defines only need internally */
#define SM_CANRAW_STANDBY 			( 0 )
#define SM_CANRAW_SEND			( 1 )

#define CANRAWBUFFERSIZE ( 4095 )


extern char *oobd_Error_Text_OS[];
//define OOBD protocol specific xTick
static volatile uint16_t xTickNew = 0;
xTickOld = 0, xTickCurrent = 0;



//>>>> oobdtemple protocol protocol2bus  >>>>
/*!
\brief move data from protocol buffer into the bus send buffer
*/
//<<<< oobdtemple protocol protocol2bus <<<<

void
odp_canraw_data2CAN(unsigned char *dataPtr, unsigned char *canPtr,
		    UBaseType_t len, UBaseType_t start)
{
    UBaseType_t i;

    // fill unused bytes first
    for (i = start; i < 8; i++) {
	canPtr[i] = 0;
    }
    canPtr = &canPtr[start];
    for (; len > 0; len--) {
	*canPtr++ = *dataPtr++;
    }

}

//>>>> oobdtemple protocol bus2protocol  >>>>
/*!
\brief move data from bus receive buffer into the protocol receive buffer
*/
//<<<< oobdtemple protocol bus2protocol <<<<

//not needed here, as the raw can does not handle any dedicated answers

//>>>> oobdtemple protocol printdata_Buffer  >>>>
/*!
\brief prints the received protocol buffer as Hexdump to the serial port

This function is called through the output task, when the protocol sends a MSG_DUMP_BUFFER message to request a buffer dump
*/
//<<<< oobdtemple protocol printdata_Buffer <<<<

//not needed here, as the raw can does not handle any dedicated answers


//>>>> oobdtemple protocol printParam  >>>>
/*!
\brief prints the requested parameter of the actual protocol

This function is called through the output task, when the protocol sends a MSG_HANDLE_PARAM message to request a parameter output
*/
//<<<< oobdtemple protocol printParam <<<<


void odp_canraw_printParam(UBaseType_t msgType, void *data,
			   printChar_cbf printchar)
{
    param_data *args = data;
    extern bus_paramPrint actBus_paramPrint;
    if (args->args[ARG_CMD] == PARAM_INFO
	&& args->args[ARG_VALUE_1] == VALUE_PARAM_INFO_VERSION) {
	printser_string("0 - Raw CAN");
	printLF();
	printEOT();
    } else {
	createCommandResultMsg(FBID_PROTOCOL_GENERIC,
			       FBID_PROTOCOL_GENERIC, 0,
			       ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
    }
}


//>>>> oobdtemple protocol recvdata  >>>>

/*!
\brief callback function, called from BUS Rx ISR

transfers received telegrams into Msgqueue
 */
//<<<< oobdtemple protocol recvdata <<<<

void odp_canraw_recvdata(data_packet * p, UBaseType_t callFromISR)
{
    extern print_cbf printdata_CAN;
    extern printChar_cbf printChar;
    extern protocolConfigPtr actProtConfigPtr;
    struct CanRawConfig *protocolConfig;
    short ByteCnt;

    if (callFromISR)
	xTickNew = (uint16_t) xTaskGetTickCountFromISR();
    else
	xTickNew = (uint16_t) xTaskGetTickCount();

    if (xTickNew < xTickOld)	// check for xTick overflow
	xTickOld = 0;

    if (xTickCurrent >= 59999)	// limit timestamp to 0-59999 tick (ms)
	xTickCurrent = 0;

    xTickCurrent = xTickCurrent + (xTickNew - xTickOld);
    xTickOld = xTickNew;	// set latest value to xTickOld for next duration
    p->timestamp = xTickCurrent;

    protocolConfig = actProtConfigPtr;
    if (protocolConfig != NULL) {
	if (protocolConfig->showBusTransfer == 1) {	//normal output
	    MsgData *msg;
	    extern QueueHandle_t protocolQueue;
	    if (NULL != (msg = createDataMsg(p))) {
		UBaseType_t res = 0;
		if (callFromISR) {
		    res = sendMsgFromISR(MSG_BUS_RECV, protocolQueue, msg);
		} else {
		    res = sendMsg(MSG_BUS_RECV, protocolQueue, msg);
		}
		if (res != pdPASS) {
		    disposeMsg(msg);
		    DEBUGPRINT("FATAL ERROR: protocol queue is full!\n",
			       'a');
		}
	    } else {
		DEBUGPRINT("FATAL ERROR: Out of Heap space!l\n", 'a');
	    }
	}
	if (protocolConfig->showBusTransfer == 2) {	//normal output, but straight from the ISR
	    printdata_CAN(MSG_BUS_RECV, p, printChar);
	}
	if (protocolConfig->showBusTransfer == 3) {
	    // Lawicel format: Estimated out of http://lxr.free-electrons.com/source/drivers/net/can/slcan.c line 110 cc.
	    if (p->ide == 0x01) {	// Bit 32 set, so it's an extended CAN ID
		printser_string("T");
		printser_uint32ToHex(p->recv & 0x1FFFFFFF);
	    } else {
		printser_string("t");
		printser_int((p->recv & 0x700) >> 8, 10);
		printser_uint8ToHex(p->recv & 0x00FF);
	    }
	    printser_int(p->len, 10);
	    ByteCnt = 0;
	    while (ByteCnt != p->len) {
		printser_uint8ToHex(p->data[ByteCnt]);
		ByteCnt++;
	    }
	    if (p->err == 0x01)
		printser_string("FFFF");	// if error occurs set timestamp to 0xFFFF
	    else
		printser_uint16ToHex(p->timestamp * portTICK_PERIOD_MS & 0xFFFF);	//reduce down to 16 bit = 65536 ms = ~ 1 min
	    printLF();
	}
	if (protocolConfig->showBusTransfer == 4) {
	    printser_uint8ToRaw(255);	//startbyte
	    printser_uint8ToRaw((p->len & 0xF) |	// bit 0-3: DLC
				((p->err & 3) << 4) |	//bit 4-5 : Error flag
				(((p->ide == 0x01) ? 1 : 0) << 5)	//bit 6: Extended CAN ID
		);		//Status flag
	    printser_uint16ToRawCoded(p->timestamp * portTICK_PERIOD_MS & 0xFFFF);	//reduce down to 16 bit = 65536 ms = ~ 1 min
	    if ((p->ide == 0x01)) {	// Bit 32 set, so it's an exended CAN ID
		printser_uint32ToRawCoded(p->recv & 0x1FFFFFFF);
	    } else {
		printser_uint16ToRawCoded(p->recv & 0x1FFFFFFF);
	    }
	    int i;

	    for (i = 0; i < p->len; i++) {
		printser_uint8ToRawCoded(p->data[i]);
	    }
	}
    }
}

//>>>> oobdtemple protocol dumpFrame  >>>>

/*!
\brief requests the output of received bus frame
\bug Sachlich falsch: Das Bus- Listening muß im Bus erfolgen, nicht im Protocoll, sonst sieht man evt. Busprobleme nicht
 */
//<<<< oobdtemple protocol dumpFrame <<<<

void odp_canraw_dumpFrame(data_packet * p, print_cbf print_data)
{
    MsgData *msg;
    extern QueueHandle_t outputQueue;
    if (NULL != (msg = createDataMsg(p))) {
	msg->print = print_data;
	if (pdPASS != sendMsg(MSG_BUS_RECV, outputQueue, msg)) {
	    disposeMsg(msg);
	    DEBUGPRINT("FATAL ERROR: output queue is full!\n", 'a');
	}
    } else {
	DEBUGPRINT("FATAL ERROR: Out of Heap space!l\n", 'a');
    }
}


//>>>> oobdtemple protocol mainloop  >>>>

/*!
\brief Main protocol loop


 */
//<<<< oobdtemple protocol mainloop <<<<


void obp_canraw(void *pvParameters)
{
//>>>> oobdtemple protocol initmain  >>>>
    int keeprunning = 1;
    data_packet *dp;
    data_packet actDataPacket;
    UBaseType_t busToUse = *(UBaseType_t *) pvParameters;
/* function pointers to the bus interface */
    extern bus_init actBus_init;
    extern bus_send actBus_send;
    extern bus_flush actBus_flush;
    extern bus_param actBus_param;
    extern bus_close actBus_close;
    extern QueueHandle_t protocolQueue;
    extern QueueHandle_t outputQueue;
    extern QueueHandle_t inputQueue;
    MsgData *msg;
    MsgData *ownMsg;
    param_data *args;
    extern SemaphoreHandle_t protocollBinarySemaphore;
    UBaseType_t msgType;
    UBaseType_t timeout = 0;
    int i;
    //catch the "Protocoll is running" Semaphore
    xSemaphoreTake(protocollBinarySemaphore, portMAX_DELAY);
    /* activate the bus... */
    odbarr[busToUse] ();
    actBus_init();
    ODPBuffer *protocolBuffer;
    protocolBuffer = NULL;
    // start with the protocol specific initalisation
//<<<< oobdtemple protocol initmain <<<<
    extern print_cbf printdata_CAN;
    UBaseType_t stateMachine_state = 0;
    UBaseType_t actBufferPos = 0;
    /* tell the Rx-ISR about the function to use for received data */
    busControl(ODB_CMD_RECV, odp_canraw_recvdata);
    protocolBuffer = createODPBuffer(CANRAWBUFFERSIZE);
    if (protocolBuffer == NULL) {
	keeprunning = 0;
    } else {
	protocolBuffer->len = 0;
    }
    extern protocolConfigPtr actProtConfigPtr;
    struct CanRawConfig *protocolConfig;
    protocolConfig = pvPortMalloc(sizeof(struct CanRawConfig));
    if (protocolConfig == NULL) {
	keeprunning = 0;
    } else {
	actProtConfigPtr = protocolConfig;
	protocolConfig->recvID = 0x7DF;
	protocolConfig->separationTime = 0;
	protocolConfig->showBusTransfer = 0;
    }
//>>>> oobdtemple protocol mainloop_start  >>>>    
    for (; keeprunning;) {

	if (MSG_NONE != (msgType = waitMsg(protocolQueue, &msg, portMAX_DELAY)))	// portMAX_DELAY
	    /* handle message */
	{
	    switch (msgType) {
//<<<< oobdtemple protocol mainloop_start <<<<
//>>>> oobdtemple protocol MSG_BUS_RECV  >>>>    
	    case MSG_BUS_RECV:
		dp = msg->addr;
//<<<< oobdtemple protocol MSG_BUS_RECV <<<<
		if (protocolConfig->showBusTransfer > 0) {
		    odp_canraw_dumpFrame(dp, printdata_CAN);
		}
		// no more action, Raw CAN does not manage any answers from the bus
//>>>> oobdtemple protocol MSG_SERIAL_DATA  >>>>    
		break;
	    case MSG_SERIAL_DATA:
//<<<< oobdtemple protocol MSG_SERIAL_DATA <<<<
		if (stateMachine_state == SM_CANRAW_STANDBY) {	/* only if just nothing to do */
		    dp = (data_packet *) msg->addr;
		    // data block received from serial input which need to be handled now
		    if (((protocolBuffer->len) + dp->len) <=
			CANRAWBUFFERSIZE) {
			/* copy the data into the uds- buffer */
			for (i = 0; i < dp->len; i++) {
			    protocolBuffer->data[protocolBuffer->len++] =
				dp->data[i];
			}
		    } else {
			createCommandResultMsg
			    (FBID_PROTOCOL_GENERIC,
			     ERR_CODE_CANRAW_DATA_TOO_LONG_ERR,
			     (protocolBuffer->len) + dp->len,
			     ERR_CODE_CANRAW_DATA_TOO_LONG_ERR_TEXT);
		    }
		}
//>>>> oobdtemple protocol MSG_SERIAL_PARAM_1 >>>>    
		break;
	    case MSG_SERIAL_PARAM:
		args = (UBaseType_t *) msg->addr;
		/*
		 * DEBUGPRINT("protocol parameter received %ld %ld %ld\n",
		 args->args[ARG_RECV], args->args[ARG_CMD],
		 args->args[ARG_VALUE_1]);
		 */
		switch (args->args[ARG_RECV]) {
		case FBID_PROTOCOL_GENERIC:
		    /*
		     *    DEBUGPRINT
		     ("generic protocol parameter received %ld %ld\n",
		     args->args[ARG_CMD], args->args[ARG_VALUE_1]);
		     */
		    switch (args->args[ARG_CMD]) {
		    case PARAM_INFO:
//<<<< oobdtemple protocol MSG_SERIAL_PARAM_1 <<<<
			CreateParamOutputMsg(args, odp_canraw_printParam);
//>>>> oobdtemple protocol MSG_SERIAL_PARAM_2 >>>>    
			break;
			// and here we proceed all command parameters
		    case PARAM_LISTEN:
			xTickCurrent = 0;	// set current Timestamp to "0" if Listen mode ist activated
			protocolConfig->showBusTransfer =
			    args->args[ARG_VALUE_1];
			createCommandResultMsg(FBID_PROTOCOL_GENERIC,
					       ERR_CODE_NO_ERR, 0, NULL);
			break;
		    default:
			createCommandResultMsg
			    (FBID_PROTOCOL_GENERIC,
			     ERR_CODE_OS_UNKNOWN_COMMAND, 0,
			     ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
			break;
		    }
		    break;
//<<<< oobdtemple protocol MSG_SERIAL_PARAM_2 <<<<
		case FBID_PROTOCOL_SPEC:
		    //DEBUGPRINT ("can raw protocol parameter received %ld %ld\n", args->args[ARG_CMD], args->args[ARG_VALUE_1]);
		    switch (args->args[ARG_CMD]) {
			// first we commend out all parameters  which are not used to generate the right "unknown parameter" message in the default - area
			/*
			   case PARAM_ECHO:
			   break;
			   case PARAM_TIMEOUT_PENDING:
			   break;
			   case PARAM_BLOCKSIZE:
			   break;
			 */
		    case PARAM_CANRAW_FRAME_DELAY:
			protocolConfig->separationTime =
			    args->args[ARG_VALUE_1] + 1;
			createCommandResultMsg(FBID_PROTOCOL_SPEC,
					       ERR_CODE_NO_ERR, 0, NULL);
			break;
		    case PARAM_CANRAW_SENDID:
			protocolConfig->recvID = args->args[ARG_VALUE_1];
			createCommandResultMsg(FBID_PROTOCOL_SPEC,
					       ERR_CODE_NO_ERR, 0, NULL);
			break;
		    default:
			createCommandResultMsg(FBID_PROTOCOL_SPEC,
					       ERR_CODE_OS_UNKNOWN_COMMAND,
					       0,
					       ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
			break;
		    }
		    break;
//>>>> oobdtemple protocol MSG_OTHERS >>>>    
		case FBID_BUS_GENERIC:
		case FBID_BUS_SPEC:
		    actBus_param(args);	/* forward the received params to the underlying bus. */
		    break;
		default:
		    createCommandResultMsg(FBID_PROTOCOL_SPEC,
					   ERR_CODE_OS_UNKNOWN_COMMAND,
					   0,
					   ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
		    break;
		}
//<<<< oobdtemple protocol MSG_OTHERS <<<<
//>>>> oobdtemple protocol MSG_INIT >>>>    
	    case MSG_INIT:
		if (protocolBuffer != NULL) {
		    protocolBuffer->len = 0;
		}
//<<<< oobdtemple protocol MSG_INIT <<<<
//>>>> oobdtemple protocol MSG_PROTOCOL_STOP >>>>    
		break;
	    case MSG_PROTOCOL_STOP:
		keeprunning = 0;
		break;
//<<<< oobdtemple protocol MSG_PROTOCOL_STOP <<<<
//>>>> oobdtemple protocol MSG_SEND_BUFFER >>>>    
	    case MSG_SEND_BUFFER:
		/* let's Dance: Starting the transfer protocol */
//<<<< oobdtemple protocol MSG_SEND_BUFFER <<<<
		if (protocolBuffer->len > 0) {
		    actBufferPos = 0;
		    for (; sendMoreFrames(protocolBuffer, &actBufferPos, &protocolConfig->showBusTransfer, &stateMachine_state, &timeout, printdata_CAN, actBus_send););	// fire all in one shot.
//>>>> oobdtemple protocol MSG_SEND_BUFFER_2 >>>>    
		} else {	/* no data to send? */
		    createCommandResultMsg
			(FBID_PROTOCOL_GENERIC, ERR_CODE_NO_ERR, 0, NULL);
		    /* just release the input again */
		    if (pdPASS !=
			sendMsg(MSG_SERIAL_RELEASE, inputQueue, NULL)) {
			printser_string("Input queue is full!");
			DEBUGPRINT
			    ("FATAL ERROR: input queue is full!\n", 'a');
		    }
		}
		break;
//<<<< oobdtemple protocol MSG_SEND_BUFFER_2 <<<<
//>>>> oobdtemple protocol MSG_TICK >>>>    
	    case MSG_TICK:
//<<<< oobdtemple protocol MSG_TICK <<<<
		if (timeout > 0) {	/* we just waiting for the next frame to send */
		    if (timeout == 1) {	/* time's gone... */
			for (; sendMoreFrames(protocolBuffer, &actBufferPos, &protocolConfig->showBusTransfer, &stateMachine_state, &timeout, printdata_CAN, actBus_send););	// fire all in one shot.
			if (timeout < 2) {	//
			    protocolBuffer->len = 0;
			    createCommandResultMsg
				(FBID_PROTOCOL_GENERIC,
				 ERR_CODE_NO_ERR, 0, NULL);
			    stateMachine_state = SM_CANRAW_STANDBY;
			    if (pdPASS !=
				sendMsg(MSG_SERIAL_RELEASE, inputQueue,
					NULL)) {
				printser_string("INPQUE_FULL");
				DEBUGPRINT
				    ("FATAL ERROR: input queue is full!\n",
				     'a');
			    }
			}
		    }
		    timeout--;
		}
//>>>> oobdtemple protocol final >>>>    
		break;
	    }
	    disposeMsg(msg);
	}
	/* vTaskDelay (5000 / portTICK_PERIOD_MS); */

    }

    /* Do all cleanup here to finish task */
    actBus_close();
    vPortFree(protocolConfig);
    freeODPBuffer(protocolBuffer);
    xSemaphoreGive(protocollBinarySemaphore);
    vTaskDelete(NULL);
}

//<<<< oobdtemple protocol final <<<<

int sendMoreFrames(ODPBuffer *
		   protocolBuffer,
		   UBaseType_t *
		   actBufferPos_ptr,
		   UBaseType_t *
		   showBusTransfer_ptr,
		   UBaseType_t *
		   stateMachine_state_ptr,
		   UBaseType_t *
		   timeout_ptr,
		   print_cbf printdata_CAN, bus_send actBus_send)
{
    unsigned char telegram[8];
    data_packet actDataPacket;
#define MINIMALBYTESTOSEND 3
    if (protocolBuffer->len > 0
	&& *actBufferPos_ptr + MINIMALBYTESTOSEND < protocolBuffer->len) {
	actDataPacket.recv =
	    protocolBuffer->data[*actBufferPos_ptr] * 256 * 256 * 256;
	(*actBufferPos_ptr)++;
	actDataPacket.recv +=
	    protocolBuffer->data[*actBufferPos_ptr] * 256 * 256;
	(*actBufferPos_ptr)++;
	actDataPacket.recv +=
	    protocolBuffer->data[*actBufferPos_ptr] * 256;
	(*actBufferPos_ptr)++;
	actDataPacket.recv += protocolBuffer->data[*actBufferPos_ptr];
	(*actBufferPos_ptr)++;
	actDataPacket.data = &telegram;
	actDataPacket.len =
	    protocolBuffer->data[*actBufferPos_ptr] >
	    8 ? 8 : protocolBuffer->data[*actBufferPos_ptr];
	(*actBufferPos_ptr)++;
	odp_canraw_data2CAN(&protocolBuffer->data
			    [*actBufferPos_ptr],
			    &telegram, actDataPacket.len, 0);
	(*actBufferPos_ptr) += actDataPacket.len;
	if (*showBusTransfer_ptr > 0) {
	    odp_canraw_dumpFrame(&actDataPacket, printdata_CAN);
	}
	actBus_send(&actDataPacket);
	*stateMachine_state_ptr = SM_CANRAW_SEND;
	if ((*actBufferPos_ptr) + MINIMALBYTESTOSEND < protocolBuffer->len) {	//some more to send?
	    *timeout_ptr = protocolBuffer->data[*actBufferPos_ptr];
	    (*actBufferPos_ptr)++;
	    if (*timeout_ptr == 0) {	//send immediadly
		return 1;
	    } else {
		return 0;	// break the send loop and wait
	    }
	} else {
	    *timeout_ptr = 1;	//this will let the MSG_TICK event to do all the cleanup
	    return 0;
	}
    }
    return 0;
}

void obd_canraw_init()
{
    odparr[VALUE_PARAM_PROTOCOL_CAN_RAW] = obp_canraw;
}
