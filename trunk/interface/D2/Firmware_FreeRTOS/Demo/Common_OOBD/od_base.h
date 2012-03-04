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
 * .
 */


#ifndef INC_OD_BASE_H
#define INC_OD_BASE_H

#include <stdint.h>
#include "od_config.h"

void initProtocols();
void initBusses();

//! callback function for print data

//! signature of the print function that will be called 
//! if data has been received or an bus error occured
typedef void (*print_cbf) (portBASE_TYPE msgType, void *data,
			   printChar_cbf printchar);


//! callback function for handle parameter commands

//! signature of the  function that will be called in protocol or bus handler
//! when a parameter command is given
typedef portBASE_TYPE(*param_cbf) (portBASE_TYPE param,
				   portBASE_TYPE value);




/* data packet structure
* used to describe a generic data packet.
* define before include busses and protocol headers, as definition is needed there
*/
typedef struct data_packet {
    portBASE_TYPE recv;		//!< id of the receiver
    portBASE_TYPE len;		//!< data length
    portBASE_TYPE err;		//!< only when receiving data: contains ODB_ERR- Codes
    unsigned char *data;	//!< pointer to the data bytes
} DATA_PACKET;


// first a forward declaration to keep the compiler happy ;-)

typedef struct error_data error_data;

/** error packet structure
* used to send error message to output task.
*/
typedef struct error_data {
    portBASE_TYPE source;	//!< Source task or handler, which throws the error
    portBASE_TYPE errType;	//!< classification of error type
    portBASE_TYPE detail;	//!< detailed error definition
    char *text;			//!< textual error description
} ERROR_DATA;


//! Error Source task constants
#define ERR_CODE_NO_ERR 0	//!<generic falue for No Error
#define ERR_CODE_SOURCE_OS 1	//!<basic OS Error
#define ERR_CODE_SOURCE_SERIALIN 2	//!<Serial Input Task Error
#define ERR_CODE_SOURCE_SERIALOUT 3	//!<Serial Output Task Error
#define ERR_CODE_SOURCE_PROTOCOL 4	//!<Protocol Task Error
#define ERR_CODE_SOURCE_BUS 5	//!<Bus Handler Error

//! Error OS constants
#define ERR_CODE_OS_NO_PROTOCOL_TASK 1	//!<basic OS Error
#define ERR_CODE_OS_NO_PROTOCOL_TASK_TEXT "can't generate protocol task"	//!<basic OS Error
#define ERR_CODE_OS_UNKNOWN_COMMAND 2	//!< OS couldn't resolve command
#define ERR_CODE_OS_UNKNOWN_COMMAND_TEXT "Unknown command"	//!<unknown command


/** callback function for error handling
*signature of the function that will be called
* when the output tasks wants to report an received error
*/

typedef void (*printError_cbf) (error_data * err);




// first a forward declaration to keep the compiler happy ;-)

typedef struct param_data param_data;

/* parameter packet structure
* used to let the output task make outputs about incoming parameters (or to handle them then)
*/
typedef struct param_data {
    portBASE_TYPE key;		//!< the parameter key
    portBASE_TYPE value;	//!< the parameter value
} PARAM_DATA;




// first a forward declaration to keep the compiler happy ;-)

typedef struct data_packet data_packet;

/* generic messageTypes to put "everything" in a queue */
typedef struct MsgData {
    portBASE_TYPE len;
    void *addr;
    void *print;		//!< callback function to output the data or error
} MSGDATA;

typedef struct MsgData MsgData;

typedef struct OdMsg {
    portBASE_TYPE msgType;
    MsgData *msgPtr;
} ODMSG;

typedef struct OdMsg OdMsg;

#include "od_protocols.h"


MsgData *createDataMsg(data_packet * data);
MsgData *createMsg(void *data, size_t size);
void disposeMsg(MsgData * p);
void disposeDataMsg(MsgData * p);
portBASE_TYPE sendMsg(portBASE_TYPE msgType, xQueueHandle recv,
		      MsgData * msg);
portBASE_TYPE sendMsgFromISR(portBASE_TYPE msgType, xQueueHandle recv,
			     MsgData * msg);
portBASE_TYPE waitMsg(xQueueHandle recv, MsgData ** msg,
		      portBASE_TYPE timeout);


void createCommandResultMsg(portBASE_TYPE eSource, portBASE_TYPE eType,
			    portBASE_TYPE eDetail, char *text);
void createCommandResultMsgFromISR(portBASE_TYPE eSource,
				   portBASE_TYPE eType,
				   portBASE_TYPE eDetail, char *text);
void CreateParamOutputMsg(portBASE_TYPE key, portBASE_TYPE value,
			  print_cbf printRoutine);

// Print functions

void printser_string(char const *str);

void printser_int(int value, int base);

void printser_uint32ToHex(uint32_t value);

void printser_uint16ToHex(uint16_t value);

void printser_uint8ToHex(uint8_t value);

void printLF();

void printEOT();

#endif				/* INC_OD_BASE_H */
