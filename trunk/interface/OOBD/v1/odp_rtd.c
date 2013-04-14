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

#define CMDBUFFERSIZE ( 8 )


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
odp_rtd_printdata_Buffer(portBASE_TYPE msgType, void *data,
			 printChar_cbf printchar)
{
    extern xQueueHandle inputQueue;
/*
 * FK: 
 * the source below is just a sample from another protocol
 * this piece needs to be rewritten to print out the content of an RTDElement,
 * where in the end the RTDElement needs to be un-locked again, so that the buffer can be 
 * re-filled again by new incoming data
 */
    ODPBuffer **doublePtr;
    ODPBuffer *myUDSBuffer;

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
    /* unlock buffer */
    myUDSBuffer->len = 0;
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


void odp_rtd_printParam(portBASE_TYPE msgType, void *data,
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

void odp_rtd_recvdata(data_packet * p)
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
    DEBUGPRINT("ID: %4X len: %d \n", p->recv, p->len);
	//searching for the element with the same ID as the received element
	while (actElement != NULL && actElement->next != NULL && actElement->id < p->recv){
		actElement=actElement->next;
	}
	// found the right ID?
	if(actElement != NULL && actElement->id == p->recv){
		 DEBUGPRINT("Found ID : %4X len: %d \n", p->recv, p->len);
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

MsgData *msg;
MsgData *ownMsg;
param_data *args;

extern xSemaphoreHandle protocollBinarySemaphore;
portBASE_TYPE msgType;
portBASE_TYPE timeout = 0;
portBASE_TYPE showBusTransfer = 0;
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
portBASE_TYPE stateMachine_state = 0;
portBASE_TYPE actBufferPos = 0;

    /* Init default parameters */

    /* tell the Rx-ISR about the function to use for received data */
busControl(ODB_CMD_RECV, odp_rtd_recvdata);
protocolBuffer = createODPBuffer(CMDBUFFERSIZE);
if (protocolBuffer == NULL) {
    keeprunning = 0;
}
protocolBuffer->len = 0;
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
		createCommandResultMsg(FBID_PROTOCOL_GENERIC,
				       ERR_CODE_RTD_CMD_TOO_LONG_ERR,
				       ((protocolBuffer->len) +
					dp->len),
				       ERR_CODE_RTD_CMD_TOO_LONG_ERR_TEXT);
	    }
//>>>> oobdtemple protocol MSG_SERIAL_PARAM_1 >>>>    
	    break;
	case MSG_SERIAL_PARAM:
	    args = (portBASE_TYPE *) msg->addr;
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
//<<<< oobdtemple protocol MSG_SERIAL_PARAM_2 <<<<
	    case FBID_PROTOCOL_SPEC:
		//DEBUGPRINT ("can raw protocol parameter received %ld %ld\n", args->args[ARG_CMD], args->args[ARG_VALUE_1]);
		switch (args->args[ARG_CMD]) {
		case PARAM_RTD_CLEAR_LIST:

		    freeRtdElement(FirstRTDElement);
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

// protocolBuffer->data[1] .. protocolBuffer->data[4] = id
// protocolBuffer->data[5] .. protocolBuffer->data[6] = total length of data telegram  if == 0 (Null) then data length = 8
// protocolBuffer->data[7]  Position of Sequence counter


		DEBUGPRINT
		    ("\n\nprotocolBuffer->data 0x%02x  0x%02x 0x%02x 0x%02x 0x%02x \n 0x%02x  0x%02x\n\n",
		     protocolBuffer->data[0], protocolBuffer->data[1],
		     protocolBuffer->data[2], protocolBuffer->data[3],
		     protocolBuffer->data[4], protocolBuffer->data[5],
		     protocolBuffer->data[6]);

		switch (protocolBuffer->data[0]) {
		case 0x22:	// Return data from bus
		    DEBUGPRINT("\nprotocolBuffer = 0x22 !\n", 'a');
		    createCommandResultMsg(FBID_PROTOCOL_GENERIC,
					   ERR_CODE_NO_ERR, 0, NULL);

		    break;
		case 0x27:	// request for new RTD-Element
		    if (protocolBuffer->data[5] > 0
			|| protocolBuffer->data[6] > 0)
			length_data_telegram =
			    (protocolBuffer->data[5] << 8) +
			    protocolBuffer->data[6];
		    else
			length_data_telegram = 8;

		    DEBUGPRINT
			("\nprotocolBuffer = 0x27 !\n length_data_telegram = %u \n",
			 length_data_telegram);

		    portBASE_TYPE ID =
			(protocolBuffer->data[1] << 24) +
			(protocolBuffer->data[2] << 16) +
			(protocolBuffer->data[3] << 8) +
			protocolBuffer->data[4];
		    if (!test_ID_Exist(FirstRTDElement, ID))	// If ID not in Buffer Create the ID
		    {

			AppendRtdElement(&FirstRTDElement,
					     length_data_telegram, ID);
			createCommandResultMsg(FBID_PROTOCOL_GENERIC,
					       ERR_CODE_NO_ERR, 0, NULL);


		    } else
			createCommandResultMsg(FBID_PROTOCOL_SPEC,
					       ERR_CODE_RTD_ID_EXIST_ERR,
					       0,
					       ERR_CODE_RTD_ID_EXIST_ERR_TEXT);
		    break;
		default:
		    DEBUGPRINT
			("\nprotocolBuffer <> 0x22 or 0x27  Buffer[0] = 0x%x ",
			 protocolBuffer->data[0]);
		    break;

		}

		debugDumpElementList(FirstRTDElement);


		/* FK:
		 * check if the protocolBuffer contains valid data.
		 * if yes, then:
		 * if first Byte is 27:  create a new RTD-Element, allocate neccessary memory and add it to 
		 * the list 
		 * if first Byte is 22:
		 * search in the RTDElement list for a matching element. Create output message, if found, otherways
		 * prepare error messages
		 */

		// reset the protocolBuffer to receive the next parameter set
		protocolBuffer->len = 0;

		/* just release the input again */
		if (pdPASS !=
		    sendMsg(MSG_SERIAL_RELEASE, inputQueue, NULL)) {
		    DEBUGPRINT("FATAL ERROR: input queue is full!\n", 'a');
		}
//>>>> oobdtemple protocol MSG_SEND_BUFFER_2 >>>>    

	    } else {		/* no data to send? */
		createCommandResultMsg
		    (FBID_PROTOCOL_GENERIC, ERR_CODE_NO_ERR, 0, NULL);

		/* just release the input again */
		if (pdPASS !=
		    sendMsg(MSG_SERIAL_RELEASE, inputQueue, NULL)) {
		    DEBUGPRINT("FATAL ERROR: input queue is full!\n", 'a');
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
    /* vTaskDelay (5000 / portTICK_RATE_MS); */

}

    /* Do all cleanup here to finish task */
actBus_close();
freeODPBuffer(protocolBuffer);
xSemaphoreGive(protocollBinarySemaphore);
vTaskDelete(NULL);
}
//<<<< oobdtemple protocol final <<<<



void odp_rtd_init()
{
    odparr[VALUE_PARAM_PROTOCOL_CAN_RTD] = odp_rtd;
}



RTDElement *AppendRtdElement(RTDElement ** headRef, portBASE_TYPE size,
			     portBASE_TYPE id)
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
    newNode->buffer[0].valid = pdFALSE;
    newNode->buffer[0].data = pvPortMalloc(size);
    if (newNode->buffer[0].data == NULL) {
	DEBUGPRINT
	    ("Fatal error: Not enough heap to allocate Buffer 0  in AppendRtdElement!\n",
	     'a');
	// free the Bufferelement itself
	vPortFree(newNode);
	return (RTDElement *) NULL;
    }
    newNode->buffer[1].valid = pdFALSE;
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
    newNode->next = NULL;
    newNode->id = id;
    newNode->len = size;

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


void freeRtdElement(RTDElement * rtdBuffer)
{

    if (rtdBuffer != NULL) {

	if (rtdBuffer->buffer[0].data != NULL) {
	    vPortFree(rtdBuffer->buffer[0].data);
	}
	if (rtdBuffer->buffer[1].data != NULL) {
	    vPortFree(rtdBuffer->buffer[1].data);
	}
	vPortFree(rtdBuffer);
    }
}

void debugDumpElementList(struct RTDElement *current)
{
    int count = 0;
    DEBUGPRINT("Dump the actual element list:\n", 'a');

    while (current != NULL) {
	count++;
	DEBUGPRINT
	    ("Element  %4X: Size of  %ld Bytes\n", current->id,
	     current->len);

	current = current->next;
    }

    DEBUGPRINT("Total number of elements %d:\n", count);

}

portBASE_TYPE test_ID_Exist(RTDElement * rtdBuffer, portBASE_TYPE id)
{

    while (rtdBuffer != NULL) {
	if (rtdBuffer->id == id)
	    return pdTRUE;
	rtdBuffer = rtdBuffer->next;
    }
    return pdFALSE;


}

/**! a little help function to make the code easier to read
  \return the other buffernumber: if buffer is 1, it return 0, and if buffer is 0, it returns 1
*/


inline portBASE_TYPE otherBuffer(portBASE_TYPE bufferindex)
{
    return (bufferindex & 1) ? 0 : 1;
}
