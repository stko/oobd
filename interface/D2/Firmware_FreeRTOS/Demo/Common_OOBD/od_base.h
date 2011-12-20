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

void initProtocols ();
void initBusses ();


//! Serial Input Error constants and texts
#define ERR_CODE_SERIAL_NO_ERR 0
#define ERR_CODE_SERIAL_SYNTAX_ERR 1
#define ERR_CODE_SERIAL_SYNTAX_ERR_TEXT "Syntax Error"


/* data packet structure
* used to describe a generic data packet.
* define before include busses and protocol headers, as definition is needed there
*/
typedef struct data_packet
{
  portBASE_TYPE recv;		//!< id of the receiver
  portBASE_TYPE len;		//!< data length
  portBASE_TYPE err;		//!< only when receiving data: contains ODB_ERR- Codes
  unsigned char *data;		//!< pointer to the data bytes
};


// first a forward declaration to keep the compiler happy ;-)

typedef struct error_data error_data;

/* error packet structure
* used to send error message to output task.
*/
typedef struct error_data
{
  portBASE_TYPE class;		//!< Major Error class
  portBASE_TYPE subClass;	//!< deeper error definition
  portBASE_TYPE detail;		//!< detailed error definition
   char *text;		//!< textual error description
};


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
typedef struct param_data
{
  portBASE_TYPE key;		//!< the parameter key
  portBASE_TYPE value; 		//!< the parameter value
};


/** callback function for parameter outputs (& handling)
*signature of the function that will be called
* when the output tasks wants to output something about parameters
*/

typedef void (*printParam_cbf) (param_data * param);





// first a forward declaration to keep the compiler happy ;-)

typedef struct data_packet data_packet;

/* generic messageTypes to put "everything" in a queue */
typedef struct MsgData
{
  portBASE_TYPE len;
  void *addr;
  void *print;			//!< callback function to output the data or error
};

typedef struct MsgData MsgData;

typedef struct OdMsg
{
  portBASE_TYPE msgType;
  MsgData *msgPtr;
};

typedef struct OdMsg OdMsg;

#include "od_protocols.h"


MsgData *createDataMsg (data_packet * data);
MsgData *createMsg (void *data, size_t size);
void disposeMsg (MsgData * p);
void disposeDataMsg (MsgData * p);
portBASE_TYPE sendMsg (portBASE_TYPE msgType, xQueueHandle recv,
		       MsgData * msg);
portBASE_TYPE sendMsgFromISR (portBASE_TYPE msgType, xQueueHandle recv,
			      MsgData * msg);
portBASE_TYPE waitMsg (xQueueHandle recv, MsgData ** msg,
		       portBASE_TYPE timeout);

// Print functions

void printser_string (char const *str);

void printser_int (int value, int base);

void printser_uint32ToHex (uint32_t value);

void printser_uint16ToHex (uint16_t value);

void printser_uint8ToHex (uint8_t value);

void printLF();
#endif /* INC_OD_BASE_H */
