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
#ifdef OOBD_PLATFORM_POSIX
#include <netinet/in.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <sys/stat.h>
#include <mqueue.h>
#endif
#include <errno.h>
#include <unistd.h>

#include "FreeRTOS.h"
#include "task.h"
#include "queue.h"

#ifdef OOBD_PLATFORM_POSIX
#include "AsyncIO/AsyncIO.h"
#include "AsyncIO/AsyncIOSocket.h"
#include "AsyncIO/PosixMessageQueueIPC.h"
#include "AsyncIO/AsyncIOSerial.h"
#endif

// debugging macros so we can pin down message origin at a glance
#ifdef OOBD_PLATFORM_POSIX	// switch debug messages on or off
#define WHERESTR  "[file %s, line %d]: "
#define WHEREARG  __FILE__, __LINE__
#define DEBUGPRINT2(...)       fprintf(stderr, __VA_ARGS__)
#define DEBUGPRINT3(...)       printf(__VA_ARGS__)
#define DEBUGPRINT(_fmt, ...)  DEBUGPRINT2(WHERESTR _fmt, WHEREARG, __VA_ARGS__)
#define DEBUGUARTPRINT(...)
#elif DEBUG_SERIAL_STM32
#define DEBUGPRINT(_fmt, ...) DEBUGUARTPRINT(__VA_ARGS__)
#define DEBUGUARTPRINT(...)  uart1_puts(__VA_ARGS__)
#else
#define DEBUGPRINT(_fmt, ...)
#define DEBUGUARTPRINT(...)
#endif

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
#define MSG_SERIAL_DATA		  ( 4 )	//!< received some data to be send from the serial input
#define MSG_SERIAL_PARAM	  ( 5 )	//!< received a paramter set from the cmd line
#define MSG_INPUT_FEEDBACK	( 6 )	//!< feedback to the cmd line
#define MSG_TICK		        ( 7 )	//!< system clock tick
#define MSG_SERIAL_RELEASE  ( 8 )	//!< tells the serial input to listen for cmds again
#define MSG_SEND_BUFFER		  ( 9 )	//!< tells the  protocol to send the filled input buffer
#define MSG_DUMP_BUFFER		  ( 10 )	//!< buffer filled, request to dump

/* define parameter types */
#define PARAM_INFO    		    ( 0 )
#define PARAM_ECHO    		    ( 1 )
#define PARAM_LINEFEED    	    ( 2 )
#define PARAM_LISTEN    	    ( 3 )
#define PARAM_PROTOCOL 		    ( 4 )
#define PARAM_BUS 		    ( 5 )
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
#define PARAM_RESET           ( 99 )

/* define values of parameter */
#define VALUE_BUS_SILENT_MODE					( 0 )
#define VALUE_BUS_LOOP_BACK_MODE				( 1 )
#define VALUE_BUS_LOOP_BACK_WITH_SILENT_MODE	( 2 )
#define VALUE_BUS_NORMAL_MODE					( 3 )
#define VALUE_BUS_CONFIG_11bit_125kbit  		( 1 )
#define VALUE_BUS_CONFIG_11bit_250kbit  		( 2 )
#define VALUE_BUS_CONFIG_11bit_500kbit  		( 3 )
#define VALUE_BUS_CONFIG_11bit_1000kbit 		( 4 )
#define VALUE_BUS_CONFIG_29bit_125kbit  		( 5 )
#define VALUE_BUS_CONFIG_29bit_250kbit  		( 6 )
#define VALUE_BUS_CONFIG_29bit_500kbit  		( 7 )
#define VALUE_BUS_CONFIG_29bit_1000kbit 		( 8 )
#define VALUE_PARAM_INFO_VERSION 				( 0 )
#define VALUE_PARAM_INFO_SERIALNUMBER   		( 1 )
#define VALUE_PARAM_INFO_PROTOCOL       		( 3 )
#define VALUE_PARAM_INFO_BUS   					( 4 )
#define VALUE_PARAM_INFO_BUS_CONFIG   			( 5 )
#define VALUE_PARAM_INFO_ADC_POWER      		( 6 )
#define VALUE_PARAM_INFO_CPU_INFO 				( 10 )
#define VALUE_PARAM_INFO_MEM_LOC  				( 11 )
#define VALUE_PARAM_INFO_ROM_TABLE_LOC  		( 12 )
#define VALUE_PARAM_INFO_FREE_HEAP_SIZE			( 13 )
#define VALUE_PARAM_INFO_CRC32					( 14 )

#define VALUE_LF_CRLF (0)
#define VALUE_LF_LF (1)
#define VALUE_LF_CR (2)

/*-------- Global Vars --------------*/
unsigned char BTM222_RespBuffer[25];
unsigned char BTM222_BtAddress[18];
unsigned char BTM222_UartSpeed;

unsigned int  BufCnt, BTM222_UART_Rx_Flag;

//! callback function for print a single char

//! signature of the printChar function that will be called 
//! to output a single char
typedef void (*printChar_cbf) (char a);

/* store all parameter in one single struct to maybe later store such param sets in EEPROM */
struct UdsConfig
{
  portBASE_TYPE recvID, //!< Module ID
    sendID, 		  //!< sender ID, used when the expected answer ID <> recvID || 8
    timeout,            //!< timeout in systemticks
    listen,             //!< listen level
    bus,                //!< id of actual used bus
    busConfig,          //!< nr of actual used bus configuration
    timeoutPending,     //!< timeout for response pending delays in system ticks
    blockSize,          //!< max. number of frames to send, overwrites the values received from Module, if > 0.
    separationTime,     //!< delay between two frames,overwrites the values received from Module, if > 0
    tpFreq              //!< time between two tester presents in systemticks
} config;



#endif /* INC_OD_CONFIG_H */
