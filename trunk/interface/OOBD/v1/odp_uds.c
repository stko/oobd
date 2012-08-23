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
 * implementation of the UDS protocol
 */

#include "odp_uds.h"


/* some defines only need internally */
#define SM_UDS_STANDBY 			( 0 )
#define SM_UDS_INIT    			( 1 )
#define SM_UDS_WAIT_FOR_FC 		( 2 )
#define SM_UDS_WAIT_FOR_CF 		( 3 )
#define SM_UDS_WAIT_FOR_ANSWER 		( 4 )
#define SM_UDS_WAIT_FOR_BUFFERDUMP	( 5 )
#define SM_UDS_SEND_CF			( 6 )

#define UDSSIZE ( 4095 )
//>>>> oobdtemple protocol buffer >>>>
/*!

  \brief generate a protocol receive & transmit buffer

*/
//<<<< oobdtemple protocol buffer <<<<

typedef struct UDSBUFFER {
    portBASE_TYPE len;
    unsigned char data[UDSSIZE];
} UDSBuffer;

extern char *oobd_Error_Text_OS[];


/*!
\brief shorten a 11-bit ID to save some Tester-Present -Memory

In general for each Module ID (0x700- 0x7FF) a flag is needed in an array to store the actual tester present status.

but as half of the address range is reserved for the tester answer IDs, we can shrink the IDs by half to save memory on the flag array

*/

portBASE_TYPE odp_uds_reduceID(portBASE_TYPE id)
{
    return ((id & 0xF0) >> 1) + ((id & 0xFF) & 0x07);	// remove bit 3 (=8) out of the id

}


//>>>> oobdtemple protocol protocol2bus  >>>>
/*!
\brief move data from protocol buffer into the bus send buffer
*/
//<<<< oobdtemple protocol protocol2bus <<<<

void
odp_uds_data2CAN(unsigned char *dataPtr, unsigned char *canPtr,
		 portBASE_TYPE len, portBASE_TYPE start)
{
    portBASE_TYPE i;

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

void
udp_uds_CAN2data(UDSBuffer * udsPtr, unsigned char *canPtr,
		 portBASE_TYPE startFrom, portBASE_TYPE len)
{
    int i;
    for (i = 0; i < len; i++) {
	udsPtr->data[startFrom + i] = *canPtr++;
    }

}

/*!
\brief generates tester presents

As the software design does not allow global vars for the dynamic loadable protocols, we have to use some pointer to
important variables instead to allow subroutines, otherways these subroutines won't see the "global" variables

\todo global TP On/off by using Module - ID 0
*/

void
odp_uds_generateTesterPresents(unsigned char *tpArray,
			       unsigned char *canBuffer,
			       bus_send actBus_send,
			       portBASE_TYPE actTPFreq)
{
    data_packet dp;
    portBASE_TYPE i;
    int actAddr;
    // first we fill the telegram with the tester present data
    dp.len = 8;			/* Tester present message must be 8 bytes */
    dp.data = canBuffer;
    canBuffer[0] = 2;
    canBuffer[1] = 0x3E;	// Service Tester Present

    // fill with padding zeros
    for (i = 2; i < 8; i++) {
	canBuffer[i] = 0;
    }
    for (i = 0; i < 256; i++) {
	if ((i & 8) == 0) {	// if it is not just a tester address
	    actAddr = odp_uds_reduceID(i);
	    if (tpArray[actAddr] > 0) {	/* marked for receive TPs */
		tpArray[actAddr]--;
		if (tpArray[actAddr] == 0) {
		    dp.recv = i + 0x700;
		    actBus_send(&dp);
		    tpArray[actAddr] = actTPFreq;
		}
	    }
	}
    }
}



//>>>> oobdtemple protocol bus2protocol  >>>>
/*!
\brief prints the received protocol buffer as Hexdump to the serial port

This function is called through the output task, when the protocol sends a MSG_DUMP_BUFFER message to request a buffer dump
*/
//<<<< oobdtemple protocol bus2protocol <<<<

void
odp_uds_printdata_Buffer(portBASE_TYPE msgType, void *data,
			 printChar_cbf printchar)
{
    extern xQueueHandle inputQueue;

    UDSBuffer **doublePtr;
    UDSBuffer *myUDSBuffer;
    doublePtr = data;
    myUDSBuffer = *doublePtr;
    int i;
    for (i = 0; i < myUDSBuffer->len; i++) {
	printser_uint8ToHex(myUDSBuffer->data[i]);
	if ((i % 8) == 0 && i > 0 && i < myUDSBuffer->len - 1) {
	    printLF();
	}
    }
    if (!((i % 8) == 0 && i > 0 && i < myUDSBuffer->len - 1)) {
	printLF();
    }

    printEOT();
    /* clear the buffer */
    myUDSBuffer->len = 0;
    /* release the input queue */
    if (pdPASS != sendMsg(MSG_SERIAL_RELEASE, inputQueue, NULL)) {
	DEBUGPRINT("FATAL ERROR: input queue is full!\n", "a");
    }

}


void odp_uds_printParam(portBASE_TYPE msgType, void *data,
			printChar_cbf printchar)
{
    param_data *args = data;
    extern bus_paramPrint actBus_paramPrint;
    if (args->args[ARG_CMD] == PARAM_INFO
	&& args->args[ARG_VALUE_1] == VALUE_PARAM_INFO_VERSION) {
	printser_string("1 - UDS (ISO14229-1)");
	printLF();
	printEOT();
    } else {
	createCommandResultMsg(FBID_PROTOCOL_GENERIC,
			       FBID_PROTOCOL_GENERIC, 0,
			       ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
    }
}



/*-----------------------------------------------------------*/
/** callback function, called from CAN Rx ISR
 *  transfers received telegrams into Msgqueue
 */

void odp_uds_recvdata(data_packet * p)
{
    DEBUGPRINT("packet receivedl!\n", 'a');
    MsgData *msg;
    extern xQueueHandle protocolQueue;
    if (NULL != (msg = createDataMsg(p))) {
	if (pdPASS != sendMsg(MSG_BUS_RECV, protocolQueue, msg)) {
	    DEBUGPRINT("FATAL ERROR: protocol queue is full!\n", 'a');
	} else {
	    DEBUGUARTPRINT
		("\r\n*** odp_uds_recvdata: sendMsg - protocolQueue ***");
	}
    } else {
	DEBUGPRINT("FATAL ERROR: Out of Heap space!l\n", 'a');
    }
}

//! \bug Sachlich falsch: Das Bus- Listening muß im Bus erfolgen, nicht im Protocoll, sonst sieht man evt. Busprobleme nicht
void odp_uds_dumpFrame(data_packet * p, print_cbf print_data)
{
    MsgData *msg;
    extern xQueueHandle outputQueue;
    if (NULL != (msg = createDataMsg(p))) {
	msg->print = print_data;
	if (pdPASS != sendMsg(MSG_BUS_RECV, outputQueue, msg)) {
	    DEBUGPRINT("FATAL ERROR: output queue is full!\n", 'a');
	}
    } else {
	DEBUGPRINT("FATAL ERROR: Out of Heap space!l\n", 'a');
    }
}



/*!

structure of UDS- CAN telegram taken fom http://www.canbushack.com/blog/index.php/2010/03/19/iso-15765-2-can-transport-layer-yes-it-can-be-fun

*/


void obp_uds(void *pvParameters)
{
    int keeprunning = 1;
    data_packet *dp;
    data_packet actDataPacket;
    portBASE_TYPE busToUse = *(portBASE_TYPE *) pvParameters;
/* function pointers to the bus interface */
    extern bus_init actBus_init;
    extern bus_send actBus_send;
    extern bus_flush actBus_flush;
    extern bus_param actBus_param;
    extern bus_close actBus_close;
    extern xQueueHandle protocolQueue;
    extern xQueueHandle outputQueue;
    extern xQueueHandle inputQueue;
    extern print_cbf printdata_CAN;

    MsgData *msg;
    MsgData *ownMsg;
    param_data *args;
    portBASE_TYPE sequenceCounter;
    portBASE_TYPE remainingBytes;
    portBASE_TYPE actBufferPos;
    portBASE_TYPE actFrameLen;
    portBASE_TYPE blockSize_BS;
    portBASE_TYPE separationTime_ST;

    portBASE_TYPE stateMachine_state = 0;
    extern xSemaphoreHandle protocollBinarySemaphore;
    int i;
    unsigned char telegram[8];
    /* Memory eater Nr. 1: The UDS message buffer */
    UDSBuffer *udsBuffer;
    /* Memory eater Nr. 2: The Tester Present Marker Flags */
    unsigned char tp_Flags[128];
    portBASE_TYPE msgType;
    struct UdsConfig udsConfig;

    /* Init default parameters */
    udsConfig.recvID = 0x7DF;
    udsConfig.sendID = 0x00;	// 0 disables special sendID
    udsConfig.timeout = 6;
    udsConfig.listen = 0;
    udsConfig.timeoutPending = 150;
    udsConfig.blockSize = 0;
    udsConfig.separationTime = 0;
    udsConfig.tpFreq = 250;

    portBASE_TYPE timeout = 0;
    blockSize_BS = 0;
    separationTime_ST = 0;
    //catch the "Protocoll is running" Semaphore
    xSemaphoreTake(protocollBinarySemaphore, portMAX_DELAY);

    DEBUGPRINT("Start Bus nr %d\n", busToUse);
    /* activate the bus... */
    odbarr[busToUse] ();
    actBus_init();
    /* tell the Rx-ISR about the function to use for received data */
    busControl(ODB_CMD_RECV, odp_uds_recvdata);
    udsBuffer = pvPortMalloc(sizeof(struct UDSBUFFER));
    udsBuffer->len = 0;
    if (udsBuffer == NULL) {
	keeprunning = 0;
	DEBUGPRINT("Fatal error: Not enough heap to allocate UDSBuffer!\n",
		   'a');
    }
    /* reset the Tester Present Array */
    for (i = 0; i < 128; i++) {
	tp_Flags[i] = 0;
    }
    for (; keeprunning;) {

	if (MSG_NONE != (msgType = waitMsg(protocolQueue, &msg, portMAX_DELAY)))	// portMAX_DELAY
	    /* handle message */
	{
	    switch (msgType) {
	    case MSG_BUS_RECV:
		dp = msg->addr;
		if (udsConfig.listen > 0) {
		    odp_uds_dumpFrame(dp, printdata_CAN);
		}
		if (((udsConfig.sendID == 0 ? dp->recv == (udsConfig.recvID | 8) : dp->recv == udsConfig.sendID)) || udsConfig.recvID == 0x7DF) {	/* Tester Address correct / we sendes a broadcast (udsConfig.recvID==0x7DF)? */
		    if (dp->data[0] == 0x03 && dp->data[1] == 0x7f && dp->data[3] == 0x78)	//Response pending
		    {
			timeout = udsConfig.timeoutPending;
		    } else {
			if (stateMachine_state == SM_UDS_WAIT_FOR_FC) {
			    if ((dp->data[0] & 0xF0) == 0x30) {	/* FlowControl */
				DEBUGPRINT("FlowControl received", 'a');
				//! \todo how to correctly support "wait" if LowNibble of PCI is 1?
				if (udsConfig.blockSize == 0) {
				    blockSize_BS = dp->data[1];	/* take the block size out of the FC block */
				} else {
				    blockSize_BS = udsConfig.blockSize;	/* use the config value instead the one from FC */
				}
				if (blockSize_BS > 0) {	/* add 1, if set, which is needed, as  the countdown routine counts down only to 1, not to 0 */
				    blockSize_BS++;
				}
				if (udsConfig.separationTime == 0) {
				    separationTime_ST = dp->data[2];	/* take the separation time out of the FC block */
				} else {
				    separationTime_ST = udsConfig.separationTime;	/* use the config value instead the one from FC */
				}
				if (separationTime_ST > 0) {	/* add 1, if set, which is needed, as  the countdown routine counts down only to 1, not to 0 */
				    separationTime_ST++;
				}
				stateMachine_state = SM_UDS_SEND_CF;
			    } else {	/* wrong answer */
				stateMachine_state = SM_UDS_STANDBY;
				udsBuffer->len = 0;
				createCommandResultMsg
				    (FBID_PROTOCOL_GENERIC,
				     ERR_CODE_UDS_MISSING_FLOW_CONTROL,
				     (dp->data[0] & 0xF0),
				     ERR_CODE_UDS_MISSING_FLOW_CONTROL_TEXT);
			    }

			}
			if (stateMachine_state == SM_UDS_SEND_CF) {
			    /* Caution: This "if state" needs to be straight after
			       the Flow Control handling above, so that when the state 
			       SM_UDS_SEND_CF is reached, the state machine starts straight to send
			     */

			    //! \todo delayed, block wise sending of Consecutive frame still needs to be implemented
			    while (remainingBytes > 0) {
				DEBUGPRINT("Remaining bytes: %ld\n",
					   remainingBytes);
				actFrameLen =
				    remainingBytes >
				    7 ? 7 : remainingBytes;
				odp_uds_data2CAN(&udsBuffer->data
						 [actBufferPos], &telegram,
						 actFrameLen, 1);
				sequenceCounter =
				    sequenceCounter <
				    14 ? sequenceCounter + 1 : 0;
				actBufferPos += actFrameLen;
				remainingBytes -= actFrameLen;
				actDataPacket.data[0] = 0x20 + sequenceCounter;	// prepare CF
				if (udsConfig.listen > 0) {
				    odp_uds_dumpFrame(&actDataPacket,
						      printdata_CAN);
				}
				actBus_send(&actDataPacket);
			    }
			    stateMachine_state = SM_UDS_WAIT_FOR_ANSWER;
			    timeout = udsConfig.timeout;
			}
			if (stateMachine_state == SM_UDS_WAIT_FOR_CF) {
			    if ((dp->data[0] & 0xF0) == 0x20) {	/* consecutive Frame */
				DEBUGPRINT
				    ("Consecutive Frame seq. %ld\n",
				     sequenceCounter);
				sequenceCounter =
				    sequenceCounter >
				    14 ? 0 : sequenceCounter + 1;
				if ((dp->data[0] & 0x0F) ==
				    sequenceCounter) {
				    DEBUGPRINT("Sequence ok seq. %ld\n",
					       sequenceCounter);
				    actFrameLen =
					remainingBytes >
					7 ? 7 : remainingBytes;
				    udp_uds_CAN2data(udsBuffer,
						     &(dp->data[1]),
						     actBufferPos,
						     actFrameLen);
				    actBufferPos += actFrameLen;
				    remainingBytes -= actFrameLen;
				    timeout = udsConfig.timeout;
				    DEBUGPRINT
					("actualBufferPos %ld remaining Bytes %ld\n",
					 actBufferPos, remainingBytes);
				    if (remainingBytes == 0) {	/* finished */
					stateMachine_state =
					    SM_UDS_STANDBY;
					timeout = 0;
					/* to dump the  buffer, we send the address of the udsbuffer to the print routine */
					ownMsg =
					    createMsg(&udsBuffer,
						      sizeof(udsBuffer));
					/* add correct print routine; */
					ownMsg->print =
					    odp_uds_printdata_Buffer;
					// send event information to the ILM task
					CreateEventMsg
					    (MSG_EVENT_PROTOCOL_RECEIVED,
					     0);
					/* forward data to the output task */
					if (pdPASS !=
					    sendMsg(MSG_DUMP_BUFFER,
						    outputQueue, ownMsg)) {
					    DEBUGPRINT
						("FATAL ERROR: output queue is full!\n",
						 'a');
					}
				    }
				} else {	/* sequence error! */
				    stateMachine_state = SM_UDS_STANDBY;
				    createCommandResultMsg
					(FBID_PROTOCOL_GENERIC,
					 ERR_CODE_UDS_WRONG_SEQUENCE_COUNT,
					 (dp->data[0] & 0x0F),
					 ERR_CODE_UDS_WRONG_SEQUENCE_COUNT_TEXT);
				    DEBUGPRINT
					("Sequence Error! Received %d , expected %ld\n",
					 dp->data[0] & 0x0F,
					 sequenceCounter);
				    timeout = 0;
				    if (pdPASS !=
					sendMsg(MSG_SERIAL_RELEASE,
						inputQueue, NULL)) {
					DEBUGPRINT
					    ("FATAL ERROR: input queue is full!\n",
					     'a');

				    }
				}

			    } else {
				stateMachine_state = SM_UDS_STANDBY;
				createCommandResultMsg
				    (FBID_PROTOCOL_GENERIC,
				     ERR_CODE_UDS_MISSING_FIRST_FRAME,
				     (dp->data[0] & 0xF0),
				     ERR_CODE_UDS_MISSING_FIRST_FRAME_TEXT);
				DEBUGPRINT
				    ("Wrong Frame Error! Received %d , expected 0x2x\n",
				     dp->data[0]);
				timeout = 0;
				if (pdPASS !=
				    sendMsg(MSG_SERIAL_RELEASE,
					    inputQueue, NULL)) {
				    DEBUGPRINT
					("FATAL ERROR: input queue is full!\n",
					 'a');

				}
			    }
			}
			if (stateMachine_state == SM_UDS_WAIT_FOR_ANSWER) {
			    if ((dp->data[0] & 0xF0) == 0x10) {	/* FirstFrame */
				sequenceCounter = 0;	//first Frame counts as sequence 0 already
				remainingBytes =
				    (dp->data[0] & 0xF) * 256 +
				    dp->data[1];
				actBufferPos = 6;
				DEBUGPRINT("First Frame with %ld Bytes\n",
					   remainingBytes);
				udsBuffer->len = remainingBytes;	/* set the buffer size alredy inhope, that all goes well ;-) */
				remainingBytes -= 6;	/* the first 6 bytes are already in the FF */
				udp_uds_CAN2data(udsBuffer, &(dp->data[2]),
						 0, 6);
				actDataPacket.recv = udsConfig.recvID;
				actDataPacket.data = &telegram;
				actDataPacket.len = 8;
				for (i = 0; i < 8; i++) {	/* just fill the telegram with 0 */
				    telegram[i] = 0;
				}
				telegram[0] = 0x30;	/* 0x30 = 3=>FlowControl, 0=>CTS = ContinoueToSend */
				stateMachine_state = SM_UDS_WAIT_FOR_CF;
				timeout = udsConfig.timeout;
				if (udsConfig.listen > 0) {
				    odp_uds_dumpFrame(&actDataPacket,
						      printdata_CAN);
				}
				actBus_send(&actDataPacket);
			    } else {
				if ((dp->data[0] & 0xF0) == 0x00) {	/*Single Frame */
				    DEBUGPRINT
					("Single Frame with %d Bytes\n",
					 dp->data[0]);
				    udsBuffer->len = dp->data[0];
				    udp_uds_CAN2data(udsBuffer,
						     &(dp->data[1]), 0,
						     dp->data[0]);
				    stateMachine_state = SM_UDS_STANDBY;
				    timeout = 0;
				    /* to dump the  buffer, we send the address of the udsbuffer to the print routine */
				    ownMsg =
					createMsg(&udsBuffer,
						  sizeof(udsBuffer));
				    /* add correct print routine; */
				    ownMsg->print =
					odp_uds_printdata_Buffer;
				    // send event information to the ILM task
				    CreateEventMsg
					(MSG_EVENT_PROTOCOL_RECEIVED, 0);
				    /* forward data to the output task */
				    if (pdPASS !=
					sendMsg(MSG_DUMP_BUFFER,
						outputQueue, ownMsg)) {
					DEBUGPRINT
					    ("FATAL ERROR: output queue is full!\n",
					     'a');

				    }
				}
			    }
			}
		    }
		}
		break;
	    case MSG_SERIAL_DATA:
		if (stateMachine_state == SM_UDS_STANDBY) {	/* only if just nothing to do */
		    dp = (data_packet *) msg->addr;
		    if (((udsBuffer->len) + dp->len) <= UDSSIZE) {
			/* copy the data into the uds- buffer */
			for (i = 0; i < dp->len; i++) {
			    udsBuffer->data[udsBuffer->len++] =
				dp->data[i];
			}
		    } else {
			createCommandResultMsg(FBID_PROTOCOL_GENERIC,
					       ERR_CODE_UDS_DATA_TOO_LONG_ERR,
					       (udsBuffer->len) + dp->len,
					       ERR_CODE_UDS_DATA_TOO_LONG_ERR_TEXT);
		    }
		}
		break;
	    case MSG_SERIAL_PARAM:
		args = (portBASE_TYPE *) msg->addr;
		DEBUGPRINT("protocol parameter received %ld %ld %ld\n",
			   args->args[ARG_RECV], args->args[ARG_CMD],
			   args->args[ARG_VALUE_1]);

		switch (args->args[ARG_RECV]) {
		case FBID_PROTOCOL_GENERIC:
		    DEBUGPRINT
			("generic protocol parameter received %ld %ld\n",
			 args->args[ARG_CMD], args->args[ARG_VALUE_1]);
		    switch (args->args[ARG_CMD]) {
		    case PARAM_INFO:
			CreateParamOutputMsg(args, odp_uds_printParam);
			break;
			// and here we proceed all command parameters
		    case PARAM_LISTEN:
			udsConfig.listen = args->args[ARG_VALUE_1];
			createCommandResultMsg(FBID_PROTOCOL_GENERIC,
					       ERR_CODE_NO_ERR, 0, NULL);
			break;
		    default:
			createCommandResultMsg(FBID_PROTOCOL_GENERIC,
					       ERR_CODE_OS_UNKNOWN_COMMAND,
					       0,
					       ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
			break;
		    }
		    break;
		case FBID_PROTOCOL_SPEC:
		    DEBUGPRINT("uds protocol parameter received %ld %ld\n",
			       args->args[ARG_CMD],
			       args->args[ARG_VALUE_1]);
		    switch (args->args[ARG_CMD]) {
			// first we commend out all parameters  which are not used to generate the right "unknown parameter" message in the default - area
			/*
			   case PARAM_ECHO:
			   break;
			   case PARAM_TIMEOUT_PENDING:
			   break;
			   case PARAM_BLOCKSIZE:
			   break;
			   case PARAM_FRAME_DELAY:
			   break;
			 */
		    case PARAM_TIMEOUT:
			udsConfig.timeout = args->args[ARG_VALUE_1] + 1;
			createCommandResultMsg(FBID_PROTOCOL_SPEC,
					       ERR_CODE_NO_ERR, 0, NULL);
			break;
		    case PARAM_RECVID:
			udsConfig.recvID = args->args[ARG_VALUE_1];
			createCommandResultMsg(FBID_PROTOCOL_SPEC,
					       ERR_CODE_NO_ERR, 0, NULL);
			break;
		    case PARAM_SENDID:
			udsConfig.sendID = args->args[ARG_VALUE_1];
			createCommandResultMsg(FBID_PROTOCOL_SPEC,
					       ERR_CODE_NO_ERR, 0, NULL);
			break;
		    case PARAM_TP_ON:
			tp_Flags[odp_uds_reduceID(args->args[ARG_VALUE_1])]
			    = udsConfig.tpFreq;
			createCommandResultMsg(FBID_PROTOCOL_SPEC,
					       ERR_CODE_NO_ERR, 0, NULL);
			break;
		    case PARAM_TP_OFF:
			tp_Flags[odp_uds_reduceID(args->args[ARG_VALUE_1])]
			    = 0;
			createCommandResultMsg(FBID_PROTOCOL_SPEC,
					       ERR_CODE_NO_ERR, 0, NULL);
			break;
		    case PARAM_TP_FREQ:
			udsConfig.tpFreq = args->args[ARG_VALUE_1];
			createCommandResultMsg(FBID_PROTOCOL_SPEC,
					       ERR_CODE_NO_ERR, 0, NULL);
			break;
			createCommandResultMsg(FBID_PROTOCOL_SPEC,
					       ERR_CODE_NO_ERR, 0, NULL);
		    default:
			createCommandResultMsg(FBID_PROTOCOL_SPEC,
					       ERR_CODE_OS_UNKNOWN_COMMAND,
					       0,
					       ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
			break;
		    }
		    break;
		case FBID_BUS_GENERIC:
		case FBID_BUS_SPEC:
		    actBus_param(args);	/* forward the received params to the underlying bus. */
		    break;
		default:
		    createCommandResultMsg(FBID_PROTOCOL_SPEC,
					   ERR_CODE_OS_UNKNOWN_COMMAND, 0,
					   ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
		    break;
		}
	    case MSG_INIT:
		DEBUGPRINT("Reset Protocol\n", 'a');
		udsBuffer->len = 0;
		break;
	    case MSG_PROTOCOL_STOP:
		DEBUGPRINT("Stop Protocol\n", 'a');
		keeprunning = 0;
		break;
	    case MSG_SEND_BUFFER:
		/* let's Dance: Starting the transfer protocol */
		if (udsBuffer->len > 0) {
		    actDataPacket.recv = udsConfig.recvID;
		    actDataPacket.data = &telegram;
		    actDataPacket.len = 8;
		    if (udsBuffer->len < 8) {	/* its just single frame */
			odp_uds_data2CAN(&udsBuffer->data[0], &telegram,
					 udsBuffer->len, 1);
			actDataPacket.data[0] = udsBuffer->len;
			udsBuffer->len = 0;	/* prepare buffer to receive */
			actBufferPos = 0;
			if (udsConfig.listen > 0) {
			    odp_uds_dumpFrame(&actDataPacket,
					      printdata_CAN);
			}
			actBus_send(&actDataPacket);
			stateMachine_state = SM_UDS_WAIT_FOR_ANSWER;
			timeout = udsConfig.timeout;
		    } else {	/* we have to send multiframes */
			odp_uds_data2CAN(&udsBuffer->data[0], &telegram, 6,
					 2);
			actDataPacket.data[0] = 0x10 + (udsBuffer->len / 256);	/* prepare FF */
			actDataPacket.data[1] = udsBuffer->len % 256;
			sequenceCounter = 0;
			remainingBytes = udsBuffer->len - 6;
			actBufferPos = 0;
			udsBuffer->len = 0;	/* prepare buffer to receive */
			if (udsConfig.listen > 0) {
			    odp_uds_dumpFrame(&actDataPacket,
					      printdata_CAN);
			}
			actBus_send(&actDataPacket);
			stateMachine_state = SM_UDS_WAIT_FOR_FC;
			timeout = udsConfig.timeout;
		    }

		} else {	/* no data to send? */
		    createCommandResultMsg
			(FBID_PROTOCOL_GENERIC, ERR_CODE_NO_ERR, 0, NULL);
		    DEBUGPRINT("Send input task release msg\n", 'a');

		    /* just release the input again */
		    if (pdPASS !=
			sendMsg(MSG_SERIAL_RELEASE, inputQueue, NULL)) {
			DEBUGPRINT("FATAL ERROR: input queue is full!\n",
				   'a');
		    }
		}
		break;
	    case MSG_TICK:
		if (timeout > 0) {	/* we just waiting for an answer */
		    if (timeout == 1) {	/* time's gone... */
			udsBuffer->len = 0;
			DEBUGPRINT("Timeout!\n", 'a');
			createCommandResultMsg(FBID_PROTOCOL_GENERIC,
					       ERR_CODE_UDS_TIMEOUT, 0,
					       ERR_CODE_UDS_TIMEOUT_TEXT);
			stateMachine_state = SM_UDS_STANDBY;
			if (pdPASS !=
			    sendMsg(MSG_SERIAL_RELEASE, inputQueue, NULL))
			{
			    DEBUGPRINT
				("FATAL ERROR: input queue is full!\n",
				 'a');

			}
		    }
		    timeout--;
		}
		/* Start generating tester present messages */
		odp_uds_generateTesterPresents(&tp_Flags, &telegram,
					       actBus_send,
					       udsConfig.tpFreq);
		break;
	    }
	    disposeMsg(msg);
	}
	/* vTaskDelay (5000 / portTICK_RATE_MS); */

    }

    /* Do all cleanup here to finish task */
    actBus_close();
    free(udsBuffer);
    xSemaphoreGive(protocollBinarySemaphore);

    vTaskDelete(NULL);
}

void obd_uds_init()
{
    odparr[VALUE_PARAM_PROTOCOL_CAN_UDS] = obp_uds;
}