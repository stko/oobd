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
 * OOBD Firmware config 
 */

#ifndef INC_OD_CONFIG_H
#define INC_OD_CONFIG_H


/* System headers. */
#include <stdio.h>
#include <time.h>
#include <sys/time.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <errno.h>
#include <unistd.h>

#include "FreeRTOS.h"
#include "task.h"
#include "queue.h"




/* Priority definitions for the tasks . */
#define TASK_PRIO_LOW		( tskIDLE_PRIORITY + 1 )
#define TASK_PRIO_MID		( tskIDLE_PRIORITY + 2 )
#define TASK_PRIO_HIGH  ( tskIDLE_PRIORITY + 3 )

#define mainDEBUG_LOG_BUFFER_SIZE	( ( unsigned short ) 20480 )
/* Remove some of the CPU intensive tasks. */
#define mainCPU_INTENSIVE_TASKS		0

/* define Queue sizes */
#define QUEUE_SIZE_PROTOCOL ( 5 )
#define QUEUE_SIZE_OUTPUT   ( 5 )
#define QUEUE_SIZE_INPUT    ( 5 )
#define QUEUE_SIZE_LED		( 3 )


/* define message types */
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

/* define parameter types */
#define PARAM_INFO    		    ( 0 )
#define PARAM_ECHO    		    ( 1 )
#define PARAM_LINEFEED    	    ( 2 )
#define PARAM_LISTEN    	    ( 3 )
#define PARAM_PROTOCOL 		    ( 4 )
#define PARAM_BUS_MODE 		    ( 5 )
#define PARAM_BUS_CONFIG 	    ( 6 )
#define PARAM_TIMEOUT 		    ( 7 )
#define PARAM_TIMEOUT_PENDING       ( 8 )
#define PARAM_BLOCKSIZE 	    ( 9 )
#define PARAM_FRAME_DELAY 	    ( 10 )
#define PARAM_RECVID  		    ( 11 )
#define PARAM_TP_ON                 ( 12 )
#define PARAM_TP_OFF                ( 13 )
#define PARAM_TP_FREQ               ( 14 )
#define ODB_CMD_RECV		    ( 15 )	//!< only for internal use: sets the callback routine when receiving a bus packet
#define PARAM_SENDID  		    ( 16 )
#define PARAM_BUS 		    ( 17 )
#define PARAM_RESET           ( 99 )

/* define values of parameter */
#define VALUE_PARAM_INFO_VERSION 				( 0 )
#define VALUE_PARAM_INFO_SERIALNUMBER   		( 1 )
#define VALUE_PARAM_INFO_BUS   					( 2 )
#define VALUE_PARAM_INFO_PROTOCOL       		( 3 )
#define VALUE_PARAM_INFO_BUS_MODE		( 4 )
#define VALUE_PARAM_INFO_BUS_CONFIG   			( 5 )

#define VALUE_PARAM_PROTOCOL_CAN_RAW			(1)
#define VALUE_PARAM_PROTOCOL_CAN_UDS			(2)
#define VALUE_PARAM_PROTOCOL_KLINE_FASTINIT		(10)

#define VALUE_LF_CRLF (0)
#define VALUE_LF_LF (1)
#define VALUE_LF_CR (2)

//! Number of allowed arguments per command
#define MAX_NUM_OF_ARGS (5)

/*-------- Global Vars --------------*/

//! callback function for print a single char

//! signature of the printChar function that will be called 
//! to output a single char
typedef void (*printChar_cbf) (char a);

/* store all parameter in one single struct to maybe later store such param sets in EEPROM */
struct GlobalConfig {
    portBASE_TYPE recvID,	//!< Module ID
     sendID,			//!< sender ID, used when the expected answer ID <> recvID || 8
     timeout,			//!< timeout in systemticks
     listen			//!< listen level
} globalConfig;

#endif				/* INC_OD_CONFIG_H */
