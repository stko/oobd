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
 * all common routines
 */

/* OOBD headers. */
#include "od_config.h"
#include "od_base.h"
#include "od_protocols.h"
/*
#ifdef OOBD_PLATFORM_STM32
#include "stm32f10x.h"
#endif
*/
bus_init actBus_init = NULL;
bus_send actBus_send = NULL;
bus_flush actBus_flush = NULL;
bus_param actBus_param = NULL;
bus_param actBus_paramPrint = NULL;
bus_close actBus_close = NULL;

portBASE_TYPE lfType = VALUE_LF_CR;




xQueueHandle outputQueue = NULL;
xQueueHandle protocolQueue = NULL;
xQueueHandle ilmQueue = NULL;

char outputBuffer[100];


void initProtocols()
{


    // Clean up the protocol array first before initialize it
    int i;
    for (i = 0; i < SYS_NR_OF_PROTOCOLS; i++)
	odparr[i] = NULL;

    //! \todo here all protocols and busses needs to be initialized, but not hardcoded the uds- protocol
    //LIST_OF_PROTOCOLS_TO_INITIALIZE




    obd_uds_init();
}

void initBusses()
{
    // Clean up the bus array first before initialize it
    int i;
    for (i = 0; i < SYS_NR_OF_BUSSES; i++)
	odbarr[i] = NULL;
    //LIST_OF_BUSSES_TO_INITIALIZE
    odb_can_init();
}


MsgData *createMsg(void *data, size_t size)
{
    // is there any msgdata at all?
    if (data != NULL && size > 0) {
	MsgData *dataDescr;
	// get mem for the MsgData itself + the payload
	dataDescr = pvPortMalloc(sizeof(struct MsgData) + size);
	if (dataDescr != NULL) {
	    // store the payload size
	    dataDescr->len = size;
	    // store the payload address
	    dataDescr->addr = (void *) dataDescr + sizeof(struct MsgData);
	    // copy payload into the fresh mem
	    memcpy(dataDescr->addr, data, size);
	    return dataDescr;
	} else {
	    return NULL;
	}
    } else {
	return NULL;
    }
}


MsgData *createPacketMsg(data_packet * data, size_t size)
{
    // is there any msgdata at all?
    if (data != NULL && size > 0) {
	MsgData *dataDescr;
	// get mem for the MsgData itself + the payload
	dataDescr = pvPortMalloc(sizeof(struct MsgData) + size);
	if (dataDescr != NULL) {
	    // store the payload size
	    dataDescr->len = size;
	    // store the payload address
	    dataDescr->addr = (void *) dataDescr + sizeof(struct MsgData);
	    // copy payload into the fresh mem
	    memcpy(dataDescr->addr, data, size);
	    // now the data lies in another memory area, so we'vr to correct the data->data pointer to this new addresses also
	    data = dataDescr->addr;
	    data->data = (void *) data + sizeof(struct data_packet);
	    return dataDescr;
	} else {
	    return NULL;
	}
    } else {
	return NULL;
    }
}


//! @todo data mem is here copied twice instead just once, that give place for improvements
MsgData *createDataMsg(data_packet * data)
{
    // is there any msgdata at all?
    if (data != NULL) {
	data_packet *newDataDescr;
	MsgData *newMsg;
	// get mem for the new data_packet itself + the payload

	newDataDescr =
	    pvPortMalloc(sizeof(struct data_packet) + data->len);
	if (newDataDescr != NULL) {
	    // copy the values of the source data_packet into the new one
	    memcpy(newDataDescr, data, sizeof(struct data_packet));
	    // store the payload address
	    newDataDescr->data =
		(void *) newDataDescr + sizeof(struct data_packet);
	    // copy payload into the fresh mem
	    memcpy(newDataDescr->data, data->data, data->len);
	    newMsg =
		createPacketMsg(newDataDescr,
				sizeof(struct data_packet) + data->len);
	    vPortFree(newDataDescr);
	    return newMsg;
	} else {
	    return NULL;
	}
    } else {
	return NULL;
    }
}


void disposeMsg(MsgData * p)
{
    if (p != NULL) {
	vPortFree(p);
    }
}


void evalResult(portBASE_TYPE source, portBASE_TYPE errType,
		portBASE_TYPE detail, char *text)
{
    if (errType) {
	printser_string(":Error: ");
	printser_int(source, 10);
	printser_string(" ");
	printser_int(errType, 10);
	printser_string(" ");
	printser_int(detail, 10);
	if (text) {
	    printser_string(" ");
	    printser_string(text);
	}
    } else {
	printser_string(".");
    }
    printLF();
    printser_string(">");

}

void printEOT()
{
    evalResult(0, 0, 0, NULL);
}



void printCommandResult(portBASE_TYPE msgType, void *data,
			printChar_cbf printchar)
{
    error_data eData;
    eData = *(error_data *) data;
    evalResult(eData.source, eData.errType, eData.detail, eData.text);
}

void createCommandResultMsg(portBASE_TYPE eSource, portBASE_TYPE eType,
			    portBASE_TYPE eDetail, char *text)
{
    MsgData *msg;
    error_data eData;
    eData.source = eSource;
    eData.errType = eType;
    eData.detail = eDetail;
    eData.text = text;
    msg = createMsg(&eData, sizeof(eData));
    msg->print = printCommandResult;
    if (pdPASS != sendMsg(MSG_INPUT_FEEDBACK, outputQueue, msg)) {
	DEBUGPRINT("FATAL ERROR: Output queue full!!\n", 'a');
    }
}



void createCommandResultMsgFromISR(portBASE_TYPE eSource,
				   portBASE_TYPE eType,
				   portBASE_TYPE eDetail, char *text)
{
    MsgData *msg;
    error_data eData;
    eData.source = eSource;
    eData.errType = eType;
    eData.detail = eDetail;
    eData.text = text;
    msg = createMsg(&eData, sizeof(eData));
    msg->print = printCommandResult;
    if (pdPASS != sendMsgFromISR(MSG_INPUT_FEEDBACK, outputQueue, msg)) {
	DEBUGPRINT("FATAL ERROR: Output queue full!!\n", 'a');
    }
}

void CreateParamOutputMsg(param_data * args, print_cbf printRoutine)
{
    MsgData *msg;
    extern xQueueHandle protocolQueue;
    if (NULL != (msg = createMsg(args, sizeof(param_data)))) {
	msg->print = printRoutine;
	if (pdPASS != sendMsg(MSG_HANDLE_PARAM, outputQueue, msg)) {
	    DEBUGPRINT("FATAL ERROR: protocol queue is full!\n", 'a');

	}
    } else {
	DEBUGPRINT("FATAL ERROR: Out of Heap space!l\n", 'a');
    }

}


void CreateEventMsg(portBASE_TYPE event, portBASE_TYPE value)
{
    MsgData *msg;
    extern xQueueHandle protocolQueue;
    if (NULL != (msg = createMsg(&value, sizeof(portBASE_TYPE)))) {
	if (pdPASS != sendMsg(event, ilmQueue, msg)) {
	    DEBUGPRINT("FATAL ERROR: ilm queue is full!\n", 'a');

	}
    } else {
	DEBUGPRINT("FATAL ERROR: Out of Heap space!l\n", 'a');
    }

}



/*
void
disposeDataMsg (MsgData * p)
{
  data_packet * dp;
  dp=p->addr;
  vPortFree (dp->data);
  vPortFree (p);
}
*/

portBASE_TYPE
sendMsg(portBASE_TYPE msgType, xQueueHandle recv, MsgData * msg)
{
    OdMsg odMsg;
    odMsg.msgType = msgType;
    odMsg.msgPtr = msg;
    return xQueueSend(recv, &odMsg, 0);
}

portBASE_TYPE
sendMsgFromISR(portBASE_TYPE msgType, xQueueHandle recv, MsgData * msg)
{
    OdMsg odMsg;
    odMsg.msgType = msgType;
    odMsg.msgPtr = msg;
    return xQueueSendFromISR(recv, &odMsg, 0);
}

portBASE_TYPE
waitMsg(xQueueHandle recv, MsgData ** msgdata, portBASE_TYPE timeout)
{
    OdMsg odMsg;
    portBASE_TYPE recvStatus;
    if (pdPASS == (recvStatus = xQueueReceive(recv, &odMsg, timeout))) {
	*msgdata = odMsg.msgPtr;
	return odMsg.msgType;
    } else {
	return MSG_NONE;
    }

}

//! \todo extend the todays printser_string in that way, that the standard \\n LF in the input string is replaced by the configured type pf LFCR. Also don't use printLF() in normal source anymore..
void printLF()
{
    switch (lfType) {
    case VALUE_LF_LF:
	printser_string("\n");
	break;
    case VALUE_LF_CR:
	printser_string("\r");
	break;
    case VALUE_LF_CRLF:
    default:
	printser_string("\r\n");
    }
}

void strreverse(char *begin, char *end)
{

    char aux;

    while (end > begin) {
	aux = *end;
	*end-- = *begin;
	*begin++ = aux;
    }
}

void uint8ToHex(char *buf, uint8_t value)
{
    static const char num[] = "0123456789abcdef";

    // write upper nibble
    buf[0] = num[value >> 4];
    // write lower nibble
    buf[1] = num[value & 0x0F];
    buf[2] = 0;
}

void uint16ToHex(char *buf, uint16_t value)
{
    uint8ToHex(buf, (uint8_t) (value >> 8));
    buf += 2;
    uint8ToHex(buf, (uint8_t) value);
    buf += 2;
    *buf = 0;
}

void uint32ToHex(char *buf, uint32_t value)
{
    uint16ToHex(buf, (uint16_t) (value >> 16));
    buf += 4;
    uint16ToHex(buf, (uint16_t) value);
    buf += 4;
    *buf = 0;
}

void itoa(int value, char *str, int base)
{

    static const char num[] = "0123456789abcdefghijklmnopqrstuvwxyz";
    char *wstr = str;
    int sign;

    div_t res;

    // Validate base
    if (base < 2 || base > 35) {
	*wstr = '\0';
	return;
    }
    // Take care of sign
    if ((sign = value) < 0)
	value = -value;

    // Conversion. Number is reversed.
    do {
	res = div(value, base);
	*wstr++ = num[res.rem];
	value = res.quot;
    }
    while (value != 0);

    if (sign < 0)
	*wstr++ = '-';

    *wstr = '\0';

    // Reverse string
    strreverse(str, wstr - 1);
}

void printser_string(char const *str)
{
    extern printChar_cbf printChar;
    if (str) {
	/* transmit characters until 0 character */
	while (*str) {
	    /* write character to buffer and increment pointer */
	    printChar(*str++);
	}
    }

}

void printser_int(int value, int base)
{
    itoa(value, (char *) &outputBuffer, base);
    printser_string((char *) &outputBuffer);
}

void printser_uint32ToHex(uint32_t value)
{
    uint32ToHex((char *) &outputBuffer, value);
    printser_string((char *) &outputBuffer);
}

void printser_uint16ToHex(uint16_t value)
{
    uint16ToHex((char *) &outputBuffer, value);
    printser_string((char *) &outputBuffer);
}

void printser_uint8ToHex(uint8_t value)
{
    uint8ToHex((char *) &outputBuffer, value);
    printser_string((char *) &outputBuffer);
}
