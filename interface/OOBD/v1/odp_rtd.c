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
 * implementation of the Real Time Data protocol
 */
#include "odp_rtd.h"
/* some defines only need internally */
#define CMDBUFFERSIZE ( 10 )
extern char *oobd_Error_Text_OS[];
RTDElement *FirstRTDElement;
//>>>> oobdtemple protocol protocol2bus  >>>>
/*!
\brief move data from protocol buffer into the bus send buffer
*/
//<<<< oobdtemple protocol protocol2bus <<<<
// The Real Time Data (RTD) protocol does not send anything, so there's no function here in place
// which would create any CAN output
//>>>> oobdtemple protocol bus2protocol  >>>>
/*!
\brief move data from bus receive buffer into the protocol receive buffer
*/
//<<<< oobdtemple protocol bus2protocol <<<<
// not needed for RTD, because here the whole sorting of received data is already done in the ISR
//>>>> oobdtemple protocol printdata_Buffer  >>>>
/*!
\brief prints the received protocol buffer as Hexdump to the serial port

This function is called through the output task, when the protocol sends a MSG_DUMP_BUFFER message to request a buffer dump
*/
//<<<< oobdtemple protocol printdata_Buffer <<<<
void
odp_rtd_printdata_Buffer(UBaseType_t msgType, void *data,
			 printChar_cbf printchar)
{
    extern QueueHandle_t inputQueue;
    RTDElement **doublePtr;
    RTDElement *myRTDElement;
    RTDBuffer *myRTDBuffer;
    myRTDBuffer = NULL;
    DEBUGPRINT("start the input buffer dump\n", "a");
    if (data == NULL) {		// the requested CAN- ID does not exist, so we generate a faked general responce error
	printser_string("7F000001");
	printLF();
    } else {
//      doublePtr = data;
//      myRTDElement = *doublePtr;
	myRTDElement = data;
	UBaseType_t bufferIndex;
	bufferIndex = otherBuffer(myRTDElement->writeBufferIndex);
	DEBUGPRINT("Element address %lX, Buffer %d \n", myRTDElement,
		   bufferIndex);
	if (myRTDElement->buffer[bufferIndex].valid == pdFALSE) {	// the requested CAN- ID is not valid, so we generate a faked general responce error
	    DEBUGPRINT("buffer invalid\n", "a");
	    printser_string("7F000002");
	    printLF();
	} else {
	    DEBUGPRINT("dump buffer:\n", "a");
	    printser_string("62");
	    printser_uint32ToHex(myRTDElement->buffer[bufferIndex].
				 timeStamp * portTICK_PERIOD_MS);
	    printser_uint8ToHex(myRTDElement->buffer[bufferIndex].flags);	// print the flag Byte
	    myRTDElement->buffer[bufferIndex].flags &= 0xFE;	//reset bit 0 (New data)
	    int i;
	    for (i = 0; i < myRTDElement->buffer[bufferIndex].len; i++) {
		printser_uint8ToHex(myRTDElement->buffer[bufferIndex].
				    data[i]);
		DEBUGPRINT(" buffer[%d]:%02X\n", i,
			   myRTDElement->buffer[bufferIndex].data[i]);
		if ((i % 8) == 0 && i > 0
		    && i < myRTDElement->buffer[bufferIndex].len - 1) {
		    printLF();
		}
	    }
	    if (!
		((i % 8) == 0 && i > 0
		 && i < myRTDElement->buffer[bufferIndex].len - 1)) {
		printLF();
	    }
	}
	/* unlock buffer */
	myRTDElement->buffer[bufferIndex].locked = pdFALSE;
    }
    printEOT();
    /* release the input queue */
    if (pdPASS != sendMsg(MSG_SERIAL_RELEASE, inputQueue, NULL)) {
	DEBUGPRINT("FATAL ERROR: input queue is full!\n", "a");
    }

}

//>>>> oobdtemple protocol printParam  >>>>
/*!
\brief prints the requested parameter of the actual protocol

This function is called through the output task, when the protocol sends a MSG_HANDLE_PARAM message to request a parameter output
*/
//<<<< oobdtemple protocol printParam <<<<


void odp_rtd_printParam(UBaseType_t msgType, void *data,
			printChar_cbf printchar)
{
    param_data *args = data;
    extern bus_paramPrint actBus_paramPrint;
    if (args->args[ARG_CMD] == PARAM_INFO
	&& args->args[ARG_VALUE_1] == VALUE_PARAM_INFO_VERSION) {
	printser_string("3 - Real Time Data");
	printLF();
	printEOT();
    } else {
	createCommandResultMsg(FBID_PROTOCOL_SPEC,
			       FBID_PROTOCOL_SPEC, 0,
			       ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
    }
}


//>>>> oobdtemple protocol recvdata  >>>>

/*!
\brief callback function, called from BUS Rx ISR

transfers received telegrams into Msgqueue
 */
//<<<< oobdtemple protocol recvdata <<<<

void odp_rtd_recvdata(data_packet * p, UBaseType_t callFromISR)
{
    extern RTDElement *FirstRTDElement;
    RTDElement *actElement = FirstRTDElement;
    /* FK:
     * this is one of the exciting routines of  RTD:-)
     * whenever a can frame comes in, this routine needs to distingluish, if the frame is needed,
     * so it has to run through the RTDElement list to see if that frame belongs to any requested messages.
     * if yes, than it has to be placed on the right place into the appropiate message buffer
     * while doing that, it might have to switch between double buffers, check for consistancy and if the actual
     * buffer is actual just locked, because the output task is just requested to output that buffer.
     * 
     * So quite a lot to do here, and all needs to happen as a interrupt service routine before the next can frame comes already in..
     */
    actElement = findID(FirstRTDElement, p->recv);
    //searching for the element with the same ID as the received element
    // found the right ID?
    if (actElement == NULL) {
	return;
    }
    DEBUGPRINT("Found ID : %4X len: %ld \n", p->recv, p->len);
    UBaseType_t writeBufferIndex = actElement->writeBufferIndex;
    DEBUGPRINT("Element len: %ld \n",
	       actElement->buffer[writeBufferIndex].len);
    UBaseType_t len = actElement->buffer[writeBufferIndex].len;
    UBaseType_t i;
    UBaseType_t newLen;
    unsigned char seq;

    switch (actElement->msgType) {
    case 0:			//single frames
	if (len == p->len) {	// check if received length equals expected length
	    for (i = 0; len > 0; i++) {
		actElement->buffer[writeBufferIndex].data[i] = p->data[i];
		DEBUGPRINT
		    ("single frame: write %02X to index %ld in Buffer %d  with remaining length %ld \n",
		     p->data[i], i, writeBufferIndex, len);
		len--;
	    }
	    actElement->buffer[writeBufferIndex].valid = pdTRUE;
	    actElement->buffer[writeBufferIndex].flags |= 0x01;	//set bit 0 (New data)
	    if (callFromISR) {
		actElement->buffer[writeBufferIndex].timeStamp =
		    xTaskGetTickCountFromISR();
	    } else {
		actElement->buffer[writeBufferIndex].timeStamp =
		    xTaskGetTickCount();
	    }
	    if (!actElement->buffer[otherBuffer(writeBufferIndex)].locked) {
		actElement->writeBufferIndex =
		    otherBuffer(actElement->writeBufferIndex);
	    }
	}
	break;
    case 1:			//custom msg format
	seq = p->data[actElement->SeqCountPos];
	if ((seq == actElement->SeqCountStart) || (seq == actElement->buffer[writeBufferIndex].lastRecSeq + 1)) {	// is that the right next sequence (seq=0 starts from scratch)
	    if (seq == actElement->SeqCountStart) {
		actElement->buffer[writeBufferIndex].valid = pdFALSE;
		actElement->buffer[writeBufferIndex].lastWrittenPos = 0;
	    }
	    for (i = 1;
		 i < p->len
		 && actElement->buffer[writeBufferIndex].lastWrittenPos <
		 len; i++) {
		DEBUGPRINT
		    ("custom multi frame: sequence %02X: write %02X to index %ld\n",
		     seq, p->data[i],
		     actElement->buffer[writeBufferIndex].lastWrittenPos);
		actElement->buffer[writeBufferIndex].
		    data[actElement->buffer
			 [writeBufferIndex].lastWrittenPos++]
		    = p->data[i];
	    }
	    actElement->buffer[writeBufferIndex].lastRecSeq = seq;	//save actual received seq
	    if (actElement->buffer[writeBufferIndex].lastWrittenPos >= len) {	//that was the last frame
		actElement->buffer[writeBufferIndex].valid = pdTRUE;
		actElement->buffer[writeBufferIndex].flags |= 0x01;	//set bit 0 (New data)
		if (callFromISR) {
		    actElement->buffer[writeBufferIndex].timeStamp =
			xTaskGetTickCountFromISR();
		} else {
		    actElement->buffer[writeBufferIndex].timeStamp =
			xTaskGetTickCount();
		}
		actElement->buffer[writeBufferIndex].lastRecSeq = 0;	//reset the seqcount for the next time
		if (!actElement->buffer[writeBufferIndex].locked) {
		    actElement->writeBufferIndex =
			otherBuffer(actElement->writeBufferIndex);
		    DEBUGPRINT("switching buffer to : %ld \n",
			       actElement->writeBufferIndex);
		}
	    }
	} else {		// wrong sequence? then reset the seqCount and start from scratch
	    actElement->buffer[writeBufferIndex].lastRecSeq = 0;
	    DEBUGPRINT("seq counter wrong: %d \n", seq);
	}
	break;
    case 2:			//ISO-TP format
	seq = p->data[0];
	DEBUGPRINT("seq %02X , masked seq %02X , las seq %02X\n", seq,
		   seq & 0xF0,
		   actElement->buffer[writeBufferIndex].lastRecSeq);
	if (((seq & 0xF0) == 0x10) ||	//First Frame
	    (((seq & 0xF0) == 0x20) &&
	     ((seq == actElement->buffer[writeBufferIndex].lastRecSeq + 1)
	      || ((actElement->buffer[writeBufferIndex].lastRecSeq ==
		   0x2F) && (seq == 0x20))
	     )
	    )
	    ) {			// is that the right next sequence (seq=0 starts from scratch)
	    if ((seq & 0xF0) == 0x10) {
		actElement->buffer[writeBufferIndex].valid = pdFALSE;
		actElement->buffer[writeBufferIndex].lastWrittenPos = 0;
		// let's see if we have to adjust the Fuufer size....
		newLen = (seq & 0x0F) * 256 + p->data[0];
		if (actElement->buffer[writeBufferIndex].len != newLen) {
		    DEBUGPRINT("old len: %02X new len %20X\n",
			       actElement->buffer[writeBufferIndex].len,
			       newLen);
		    if (actElement->buffer[writeBufferIndex].data != NULL) {
			vPortFree(actElement->
				  buffer[writeBufferIndex].data);
		    }
		    actElement->buffer[writeBufferIndex].data =
			pvPortMalloc(newLen);
		    if (actElement->buffer[writeBufferIndex].data != NULL) {
			actElement->buffer[writeBufferIndex].len = newLen;
			len = newLen;
		    }
		}
	    }
	    if (actElement->buffer[writeBufferIndex].data != NULL) {
		for (i = ((seq & 0xF0) == 0x10) ? 2 : 1;
		     i < p->len
		     && actElement->
		     buffer[writeBufferIndex].lastWrittenPos < len; i++) {
		    DEBUGPRINT
			("ISO-TP frame: sequence %02X: write %02X to index %ld\n",
			 seq, p->data[i],
			 actElement->
			 buffer[writeBufferIndex].lastWrittenPos);
		    actElement->buffer[writeBufferIndex].
			data[actElement->buffer
			     [writeBufferIndex].lastWrittenPos++]
			= p->data[i];
		}
		if ((seq & 0xF0) == 0x10) {
		    actElement->buffer[writeBufferIndex].lastRecSeq = 0x20;
		} else {
		    actElement->buffer[writeBufferIndex].lastRecSeq = seq;	//save actual received seq
		}
		if (actElement->buffer[writeBufferIndex].lastWrittenPos >= len) {	//that was the last frame
		    actElement->buffer[writeBufferIndex].valid = pdTRUE;
		    actElement->buffer[writeBufferIndex].flags |= 0x01;	//set bit 0 (New data)
		    if (callFromISR) {
			actElement->buffer[writeBufferIndex].timeStamp =
			    xTaskGetTickCountFromISR();
		    } else {
			actElement->buffer[writeBufferIndex].timeStamp =
			    xTaskGetTickCount();
		    }
		    actElement->buffer[writeBufferIndex].lastRecSeq = 0;	//reset the seqcount for the next time
		    if (!actElement->buffer[writeBufferIndex].locked) {
			actElement->writeBufferIndex =
			    otherBuffer(actElement->writeBufferIndex);
			DEBUGPRINT("switching buffer to : %ld \n",
				   actElement->writeBufferIndex);
		    }
		}
	    } else {
		actElement->buffer[writeBufferIndex].len = 0;
	    }

	} else {		// wrong sequence? then reset the seqCount and start from scratch
	    actElement->buffer[writeBufferIndex].lastRecSeq = 0;
	    DEBUGPRINT("seq counter wrong: %d \n", seq);
	}
	break;
    default:
	break;
    }
}

//>>>> oobdtemple protocol dumpFrame  >>>>

/*!
\brief requests the output of received bus frame
\bug Sachlich falsch: Das Bus- Listening muß im Bus erfolgen, nicht im Protocoll, sonst sieht man evt. Busprobleme nicht
 */
//<<<< oobdtemple protocol dumpFrame <<<<

//as the RTD task itself does not receive data from the can bus directly, there's also no output routine for such data here
// to receive or to dump everything would be too time consuming

//>>>> oobdtemple protocol mainloop  >>>>

/*!
\brief Main protocol loop


 */
//<<<< oobdtemple protocol mainloop <<<<


void odp_rtd(void *pvParameters)
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
    UBaseType_t showBusTransfer = 0;
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
    int length_data_telegram = 0;
    extern RTDElement *FirstRTDElement;
    FirstRTDElement = NULL;
    extern print_cbf printdata_CAN;
    UBaseType_t stateMachine_state = 0;
    UBaseType_t actBufferPos = 0;
    /* Init default parameters */
    /* tell the Rx-ISR about the function to use for received data */
    busControl(ODB_CMD_RECV, odp_rtd_recvdata);
    protocolBuffer = createODPBuffer(CMDBUFFERSIZE);
    if (protocolBuffer == NULL) {
	keeprunning = 0;
    } else {
	protocolBuffer->len = 0;
    }
/* the RTD protocol does not have a config yet--
    extern protocolConfigPtr actProtConfigPtr;
    struct RTDConfig *protocolConfig;
    protocolConfig = pvPortMalloc(sizeof(struct RTDConfig));
    if (protocolConfig == NULL) {
	keeprunning = 0;
    } else {
	actProtConfigPtr = protocolConfig;
	protocolConfig->recvID = 0x7DF;
	protocolConfig->separationTime = 0;
    }
*/
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
		if (showBusTransfer > 0) {
		    // no incoming dumps for RTD
		    //odp_rtd_dumpFrame(dp, printdata_CAN);
		}
//>>>> oobdtemple protocol MSG_SERIAL_DATA  >>>>    
		break;
	    case MSG_SERIAL_DATA:
//<<<< oobdtemple protocol MSG_SERIAL_DATA <<<<
		dp = (data_packet *) msg->addr;
		// data block received from serial input which need to be handled now
		if (((protocolBuffer->len) + dp->len) <= CMDBUFFERSIZE) {
		    /* copy the data into the uds- buffer */
		    for (i = 0; i < dp->len; i++) {
			protocolBuffer->data[protocolBuffer->len++] =
			    dp->data[i];
		    }
		} else {
		    createCommandResultMsg(FBID_PROTOCOL_SPEC,
					   ERR_CODE_RTD_CMD_TOO_LONG_ERR,
					   ((protocolBuffer->len) +
					    dp->len),
					   ERR_CODE_RTD_CMD_TOO_LONG_ERR_TEXT);
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
			CreateParamOutputMsg(args, odp_rtd_printParam);
//>>>> oobdtemple protocol MSG_SERIAL_PARAM_2 >>>>    
			break;
			// and here we proceed all command parameters
		    case PARAM_LISTEN:
			showBusTransfer = args->args[ARG_VALUE_1];
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
//<<<< oobdtemple protocol MSG_SERIAL_PARAM_2 <<<<
		case FBID_PROTOCOL_SPEC:
		    //DEBUGPRINT ("can raw protocol parameter received %ld %ld\n", args->args[ARG_CMD], args->args[ARG_VALUE_1]);
		    switch (args->args[ARG_CMD]) {
		    case PARAM_RTD_CLEAR_LIST:

			freeRtdElements(&FirstRTDElement);
			/* 
			 * FK:
			 * clear the RTD-Element List here, means
			 * free all allocated memory pieces ant the RTD-Elements itself
			 */
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
					   ERR_CODE_OS_UNKNOWN_COMMAND, 0,
					   ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
		    break;
		}
//<<<< oobdtemple protocol MSG_OTHERS <<<<
//>>>> oobdtemple protocol MSG_INIT >>>>    
	    case MSG_INIT:
		if (protocolBuffer != NULL) {
		    protocolBuffer->len = 0;
		    UBaseType_t i;
		    for (i = 0; i < CMDBUFFERSIZE; i++)
			protocolBuffer->data[i] = 0;
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
		DEBUGPRINT("\nprotocolBufferlength: %ld\n",
			   protocolBuffer->len);
		if (protocolBuffer->len > 0) {
		    switch (protocolBuffer->data[0]) {
		    case 0x22:	// Return data from bus
			if (protocolBuffer->len == 5) {
			    DEBUGPRINT("\nprotocolBuffer = 0x22 !\n", 'a');
			    UBaseType_t ID =
				(protocolBuffer->data[1] << 24) +
				(protocolBuffer->data[2] << 16) +
				(protocolBuffer->data[3] << 8) +
				protocolBuffer->data[4];
			    RTDElement *myRTDElement;
			    myRTDElement = findID(FirstRTDElement, ID);
			    DEBUGPRINT("Element address %lX\n",
				       myRTDElement);
			    if (myRTDElement != NULL) {
				/* lock buffer */
				myRTDElement->buffer[otherBuffer
						     (myRTDElement->
						      writeBufferIndex)].
				    locked = pdTRUE;
			    }
			    ownMsg = createMsg(myRTDElement, 0);
			    /* add correct print routine; */
			    ownMsg->print = odp_rtd_printdata_Buffer;
			    // send event information to the ILM task
			    CreateEventMsg(MSG_EVENT_PROTOCOL_RECEIVED, 0);
			    /* forward data to the output task */
			    if (pdPASS !=
				sendMsg(MSG_DUMP_BUFFER, outputQueue,
					ownMsg)) {
				DEBUGPRINT
				    ("FATAL ERROR: output queue is full!\n",
				     'a');
			    }
			} else {
			    createCommandResultMsg(FBID_PROTOCOL_SPEC,
						   ERR_CODE_OS_UNKNOWN_COMMAND,
						   0,
						   ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
			}

			break;
		    case 0x27:	// request for new RTD-Element
			if (protocolBuffer->len < 11) {
			    UBaseType_t ID =
				(protocolBuffer->data[1] << 24) +
				(protocolBuffer->data[2] << 16) +
				(protocolBuffer->data[3] << 8) +
				protocolBuffer->data[4];

			    DEBUGPRINT
				("\nprotocolBuffer = 0x27 !\n length_data_telegram = %d \n",
				 length_data_telegram);
			    if (!findID(FirstRTDElement, ID))	// If ID not in Buffer Create the ID
			    {

				length_data_telegram =
				    protocolBuffer->len >
				    7 ? (protocolBuffer->data[6] << 8) +
				    protocolBuffer->data[7] : 8;
				RTDElement *newElement;
				newElement =
				    AppendRtdElement(&FirstRTDElement,
						     length_data_telegram,
						     ID);
				if (newElement == NULL) {
				    createCommandResultMsg
					(FBID_PROTOCOL_SPEC,
					 ERR_CODE_RTD_OOM_ERR, 0,
					 ERR_CODE_RTD_OOM_ERR_TEXT);
				} else {
				    newElement->msgType =
					protocolBuffer->len >
					5 ? protocolBuffer->data[5] : 0;
				    newElement->SeqCountPos =
					protocolBuffer->len >
					8 ? protocolBuffer->data[8] : 0;
				    newElement->SeqCountStart =
					protocolBuffer->len >
					9 ? protocolBuffer->data[9] : 0;
				    createCommandResultMsg
					(FBID_PROTOCOL_SPEC,
					 ERR_CODE_NO_ERR, 0, NULL);
				}
			    } else {
				createCommandResultMsg(FBID_PROTOCOL_SPEC,
						       ERR_CODE_RTD_ID_EXIST_ERR,
						       0,
						       ERR_CODE_RTD_ID_EXIST_ERR_TEXT);
			    }
			} else
			    createCommandResultMsg(FBID_PROTOCOL_SPEC,
						   ERR_CODE_OS_COMMAND_NOT_SUPPORTED,
						   0,
						   ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
			break;
		    default:
			DEBUGPRINT
			    ("protocolBuffer <> 0x22 or 0x27  Buffer[0] = 0x%x\n",
			     protocolBuffer->data[0]);
			break;
		    }

		    debugDumpElementList(FirstRTDElement);
		    // reset the protocolBuffer to receive the next parameter set
		    protocolBuffer->len = 0;
		    /* just release the input again */
		    if (pdPASS !=
			sendMsg(MSG_SERIAL_RELEASE, inputQueue, NULL)) {
			DEBUGPRINT("FATAL ERROR: input queue is full!\n",
				   'a');
		    }
//>>>> oobdtemple protocol MSG_SEND_BUFFER_2 >>>>    
		    UBaseType_t i;
		    for (i = 0; i < CMDBUFFERSIZE; i++)
			protocolBuffer->data[i] = 0;
		} else {	/* no data to send? */
		    createCommandResultMsg(FBID_PROTOCOL_SPEC,
					   ERR_CODE_NO_ERR, 0, NULL);
		    /* just release the input again */
		    if (pdPASS !=
			sendMsg(MSG_SERIAL_RELEASE, inputQueue, NULL)) {
			DEBUGPRINT("FATAL ERROR: input queue is full!\n",
				   'a');
		    }
		}
		break;
//<<<< oobdtemple protocol MSG_SEND_BUFFER_2 <<<<
//>>>> oobdtemple protocol MSG_TICK >>>>    
	    case MSG_TICK:
//<<<< oobdtemple protocol MSG_TICK <<<<
		/* as in the Real time protocol there's nothing to be done after a time tick, we can commend it out
		 */
		/*
		   if (timeout > 0) {   
		   timeout--;
		   }
		 */
//>>>> oobdtemple protocol final >>>>    
		break;
	    }
	    disposeMsg(msg);
	}
	/* vTaskDelay (5000 / portTICK_PERIOD_MS); */

    }

    /* Do all cleanup here to finish task */
    freeRtdElements(&FirstRTDElement);
    actBus_close();
//    vPortFree(protocolConfig);
    freeODPBuffer(protocolBuffer);
    xSemaphoreGive(protocollBinarySemaphore);
    vTaskDelete(NULL);
}

//<<<< oobdtemple protocol final <<<<



void odp_rtd_init()
{
    odparr[VALUE_PARAM_PROTOCOL_CAN_RTD] = odp_rtd;
}



RTDElement *AppendRtdElement(RTDElement ** headRef, UBaseType_t size,
			     UBaseType_t id)
{
    // first we create the new element itself
    DEBUGPRINT("AppendRtdElement  -- start\n", 'a');
    // first we need the pointer "current" to the first element of the list
    // that pointer will be NULL, if the list is actual empty
    RTDElement *current = *headRef;
    RTDElement *newNode;
    // start to allocate the memory
    newNode = pvPortMalloc(sizeof(struct RTDElement));
    if (newNode == NULL) {
	DEBUGPRINT
	    ("Fatal error: Not enough heap to allocate newNode in AppendRtdElement!\n",
	     'a');
	return (RTDElement *) NULL;
    }
    newNode->writeBufferIndex = 0;
    newNode->buffer[0].valid = pdFALSE;
    newNode->buffer[0].locked = pdFALSE;
    newNode->buffer[0].len = size;
    newNode->buffer[0].data = NULL;
    if (size != 0) {
	newNode->buffer[0].data = pvPortMalloc(size);
	if (newNode->buffer[0].data == NULL) {
	    DEBUGPRINT
		("Fatal error: Not enough heap to allocate Buffer 0  in AppendRtdElement!\n",
		 'a');
	    // free the Bufferelement itself
	    vPortFree(newNode);
	    return (RTDElement *) NULL;
	}
    }
    newNode->buffer[1].valid = pdFALSE;
    newNode->buffer[1].locked = pdFALSE;
    newNode->buffer[1].len = size;
    newNode->buffer[1].data = NULL;
    if (size != 0) {
	newNode->buffer[1].data = pvPortMalloc(size);
	if (newNode->buffer[1].data == NULL) {
	    DEBUGPRINT
		("Fatal error: Not enough heap to allocate Buffer 1  in AppendRtdElement!\n",
		 'a');
	    // free the Buffer 0 first
	    vPortFree(newNode->buffer[0].data);
	    // free the Bufferelement itself
	    vPortFree(newNode);
	    return (RTDElement *) NULL;
	}
    }
    newNode->next = NULL;
    newNode->id = id;
    newNode->msgType = -1;
    // in case the list is empty, so this is just the first element
    if (current == NULL) {
	*headRef = newNode;
	DEBUGPRINT("AppendRtdElement  -- added as first element\n", 'a');
    } else {			// there's already some elements in the list, so let's sort the new element into the right place
	// special case: The new element would be the first one
	if (current->id > id) {
	    newNode->next = current;
	    *headRef = newNode;
	    DEBUGPRINT
		("AppendRtdElement  -- inserted before the first element\n",
		 'a');
	} else {
	    // run through the list, as long there's another element left and the next id is lower as the actual id
	    while (current->next != NULL && current->next->id < id) {
		current = current->next;
	    }
	    // insert the actual element into the list
	    newNode->next = current->next;
	    current->next = newNode;
	    DEBUGPRINT
		("AppendRtdElement  -- inserted somewhere in the list\n",
		 'a');
	}
    }

    return newNode;
}


void freeRtdElements(RTDElement ** firstElement)
{
    DEBUGPRINT("Clear element buffer \n", 'a');
    RTDElement *rtdBuffer = *firstElement;
    *firstElement = NULL;	//set the firstelement to Null
    if (rtdBuffer != NULL) {
	RTDElement *next;
	do {
	    if (rtdBuffer != NULL) {
		next = rtdBuffer->next;
		if (rtdBuffer->buffer[0].data != NULL) {
		    vPortFree(rtdBuffer->buffer[0].data);
		}
		if (rtdBuffer->buffer[1].data != NULL) {
		    vPortFree(rtdBuffer->buffer[1].data);
		}
		DEBUGPRINT("Clear element buffer %lX\n", rtdBuffer->id);
		vPortFree(rtdBuffer);
		rtdBuffer = next;
	    }
	} while (next != NULL);
    }
}

void debugDumpElementList(struct RTDElement *current)
{
    int count = 0;
    DEBUGPRINT("Dump the actual element list:\n", 'a');
    while (current != NULL) {
	count++;
	DEBUGPRINT
	    ("Element  %4lX: Size of  %ld Bytes\n", current->id,
	     current->buffer[current->writeBufferIndex].len);
	current = current->next;
    }
    DEBUGPRINT("Total number of elements %d:\n", count);
}

RTDElement *findID(RTDElement * rtdBuffer, UBaseType_t id)
{


    while (rtdBuffer != NULL && rtdBuffer->next != NULL
	   && rtdBuffer->id < id) {
	rtdBuffer = rtdBuffer->next;
    }
    // found the right ID?
    if (rtdBuffer != NULL && rtdBuffer->id == id) {
	return rtdBuffer;
    }
    return NULL;
}

/**! a little help function to make the code easier to read
  \return the other buffernumber: if buffer is 1, it return 0, and if buffer is 0, it returns 1
*/


extern inline UBaseType_t otherBuffer(UBaseType_t bufferindex)
{
    return (bufferindex & 1) ? 0 : 1;
}
