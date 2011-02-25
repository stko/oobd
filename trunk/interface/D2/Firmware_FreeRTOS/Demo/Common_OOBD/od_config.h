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


	OOBD is using FreeTROS (www.FreeRTOS.org)

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
//#include "croutine.h"

//#include "partest.h"
/* Demo file headers. */
// #include "crflash.h"
// #include "print.h"
// #include "fileIO.h"

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


/* define message types */
#define MSG_NONE		( 0 )
#define MSG_INIT		( 1 )	//!< generic msg to initialize (whatever)
#define MSG_SERIAL_IN		( 2 )	//!< a char coming from the serial input
#define MSG_BUS_RECV		( 3 )	//!< received some data on the bus
#define MSG_SERIAL_DATA		( 4 )	//!< received some data to be send from the serial input
#define MSG_SERIAL_PARAM	( 5 )	//!< received a paramter set from the cmd line
#define MSG_INPUT_FEEDBACK	( 6 )	//!< feedback to the cmd line
#define MSG_TICK		( 7 )	//!< system clock tick
#define MSG_SERIAL_RELEASE	( 8 )	//!< tells the serial input to listen for cmds again
#define MSG_SEND_BUFFER		( 9 )	//!< tells the  protocol to send the filled input buffer
#define MSG_DUMP_BUFFER		( 10 )	//!< buffer filled, request to dump

/* define parameter types */
#define PARAM_INFO    		    ( 0 )
#define PARAM_ECHO    		    ( 1 )
#define PARAM_LISTEN    	    ( 2 )
#define PARAM_PROTOCOL 		    ( 3 )
#define PARAM_BUS 		        ( 4 )
#define PARAM_BUS_CONFIG 	    ( 5 )
#define PARAM_TIMEOUT 		    ( 6 )
#define PARAM_TIMEOUT_PENDING ( 7 )
#define PARAM_BLOCKSIZE 	    ( 8 )
#define PARAM_FRAME_DELAY 	  ( 9 )
#define PARAM_RECVID  		    ( 10 )
#define PARAM_TP_ON           ( 11 )
#define PARAM_TP_OFF          ( 12 )
#define PARAM_TP_FREQ         ( 13 )
#define ODB_CMD_RECV		( 14 )	//!< only for internal use: sets the callback routine when receiving a bus packet
#define PARAM_BOOTLOADER      ( 91 )

/*-------- Global Vars --------------*/



//! callback function for print a single char

//! signature of the printChar function that will be called 
//! to output a single char
typedef void (*printChar_cbf) (char a);



#endif /* INC_OD_CONFIG_H */
