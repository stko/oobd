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
 * 
 */


#ifndef INC_OD_BASE_H
#define INC_OD_BASE_H

#include <stdint.h>
#include "od_config.h"
#include "mc_sys.h"

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
typedef portBASE_TYPE(*param_cbf) (void *param);




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



/*! \defgroup functionblocks Commands: Addressing the different Function Blocks

As descripted on \ref index , the functionality is splitted  into  up to 10 functional blocks. To adress command to this different blocks,
the first value x of a parameter command (p x ...) adresses the functional block 


   *  @{
   */


#define FBID_SYS_GENERIC 0	//!<The generic part of the system
#define FBID_SYS_SPEC 1		//!<The mc specific part of the system
#define FBID_SERIALIN_GENERIC 2	//!< The generic part of the serial_in process
#define FBID_SERIALIN_SPEC 3	//!< The implementation specific part of the serial_in process
#define FBID_SERIALOUT_GENERIC 4	//!< The generic part of the serial_out process
#define FBID_SERIALOUT_SPEC 5	//!< The implementation specific part of the serial_out process
#define FBID_PROTOCOL_GENERIC 6	//!<The generic part of the actual protocol
#define FBID_PROTOCOL_SPEC 7	//!<The implementation specific part of the actual protocol -but up to now a protocol is planned as always generic, but not implementation speicif
#define FBID_BUS_GENERIC 8	//!<The generic part of the actual bus
#define FBID_BUS_SPEC 9		//!<The implementation specific part of the actual bus

/*! 

@} */



  /*! \addtogroup system_generic_parm 
   *  @{
   */

#define PARAM_INFO    		    ( 0 )	//!< Displays system infos

/*! 

@} */



/* define internal commands */
#define ODB_CMD_RECV		    ( 15 )	//!< only for internal use: sets the callback routine when receiving a bus packet



/* define values of parameter */
#define VALUE_PARAM_INFO_VERSION 				( 0 )
#define VALUE_PARAM_INFO_SERIALNUMBER   		( 1 )
#define VALUE_PARAM_INFO_BUS   					( 2 )
#define VALUE_PARAM_INFO_PROTOCOL       		( 3 )
#define VALUE_PARAM_INFO_BUS_MODE		( 4 )
#define VALUE_PARAM_INFO_BUS_CONFIG   			( 5 )


/* Global message types */
#define MSG_NONE		        ( 0 )
#define MSG_INIT		        ( 1 )	//!< generic msg to initialize (whatever)
#define MSG_SERIAL_IN		    ( 2 )	//!< a char coming from the serial input
#define MSG_BUS_RECV		    ( 3 )	//!< received some data on the bus
#define MSG_SERIAL_DATA		 	( 4 )	//!< received some data to be send from the serial input
#define MSG_SERIAL_PARAM	 	( 5 )	//!< received a paramter set from the cmd line
#define MSG_INPUT_FEEDBACK		( 6 )	//!< feedback to the cmd line
#define MSG_TICK		        ( 7 )	//!< system clock tick
#define MSG_SERIAL_RELEASE  	( 8 )	//!< tells the serial input to listen for cmds again
#define MSG_SEND_BUFFER		  	( 9 )	//!< tells the  protocol to send the filled input buffer
#define MSG_DUMP_BUFFER		  	( 10 )	//!< buffer filled, request to dump
#define MSG_HANDLE_PARAM		( 11 )	//!< handle parameter command outputs
#define MSG_EVENT_PROTOCOL_RECEIVED	( 12 )	//!< message to ilm handler: protocol telegram received
#define MSG_EVENT_BUS_MODE		( 13 )	//!< message to ilm handler: bus mode has changed
#define MSG_EVENT_BUS_CHANNEL		( 14 )	//!< message to ilm handler: bus channel has changed. value= selected channel
#define MSG_EVENT_CMDLINE		( 15 )	//!< message to ilm handler: serial input line received

//! Global Event values
#define MSG_EVENT_BUS_MODE_OFF		( 0 )	//!< message value of MSG_EVENT_BUS_MODE: Bus is offline
#define MSG_EVENT_BUS_MODE_ON		( 1 )	//!< message value of MSG_EVENT_BUS_MODE: Bus is online

//! Error constants
#define ERR_CODE_NO_ERR 0	//!<generic value for No Error

//! \todo the text constants needs to be replaced against a function, otherways we fill the program code with repeating error texts
//! Error OS constants
#define ERR_CODE_OS_NO_PROTOCOL_TASK 1	//!<basic OS Error
//#define ERR_CODE_OS_NO_PROTOCOL_TASK_TEXT "can't generate protocol task"      //!<basic OS Error
#define ERR_CODE_OS_NO_PROTOCOL_TASK_TEXT oobd_Error_Text_OS[ERR_CODE_OS_NO_PROTOCOL_TASK]	//!<basic OS Error
#define ERR_CODE_OS_UNKNOWN_COMMAND 2	//!< OS couldn't resolve command
//#define ERR_CODE_OS_UNKNOWN_COMMAND_TEXT "Unknown command"    //!<unknown command
#define ERR_CODE_OS_UNKNOWN_COMMAND_TEXT oobd_Error_Text_OS[ERR_CODE_OS_UNKNOWN_COMMAND]	//!<unknown command
#define ERR_CODE_OS_COMMAND_NOT_SUPPORTED 3	//!< OS couldn't resolve command
//#define ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT "Command not supported"        //!<unknown command
#define ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT oobd_Error_Text_OS[ERR_CODE_OS_COMMAND_NOT_SUPPORTED]	//!<unknown command


//! help constants to address the parameter array 
#define ARG_RECV (0)		//!<receiver of the parameter
#define ARG_CMD (1)		//!<index of command
#define ARG_VALUE_1 (2)		//!<index of 1. value
#define ARG_VALUE_2 (3)		//!<index of 2. value


  /*! \addtogroup serial_generic_parm 

   *  @{
   */


#define VALUE_LF_CRLF (0)	//!< set Linefeed to CRLF
#define VALUE_LF_LF (1)		//!< set Linefeed to LF
#define VALUE_LF_CR (2)		//!< set Linefeed to CR
  /*! @} */



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
    portBASE_TYPE argv;		//!< Nr of Args
    portBASE_TYPE args[MAX_NUM_OF_ARGS];	//!< Array of Args
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


void initILM();

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
void CreateParamOutputMsg(param_data * args, print_cbf printRoutine);

// Print functions

void printser_string(char const *str);

void printser_int(int value, int base);

void printser_uint32ToHex(uint32_t value);

void printser_uint16ToHex(uint16_t value);

void printser_uint8ToHex(uint8_t value);

void printLF();

void evalResult(portBASE_TYPE source, portBASE_TYPE errType,
		portBASE_TYPE detail, char *text);

void printEOT();

#endif				/* INC_OD_BASE_H */
