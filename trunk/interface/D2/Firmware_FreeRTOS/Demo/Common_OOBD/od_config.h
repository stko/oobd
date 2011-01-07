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
#include <netinet/in.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <sys/stat.h>
#include <mqueue.h>
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

#include "AsyncIO/AsyncIO.h"
#include "AsyncIO/AsyncIOSocket.h"
#include "AsyncIO/PosixMessageQueueIPC.h"
#include "AsyncIO/AsyncIOSerial.h"


// debugging macros so we can pin down message origin at a glance
#define WHERESTR  "[file %s, line %d]: "
#define WHEREARG  __FILE__, __LINE__
#define DEBUGPRINT2(...)       fprintf(stderr, __VA_ARGS__)
#define DEBUGPRINT3(...)       printf(__VA_ARGS__)
#define DEBUGPRINT(_fmt, ...)  DEBUGPRINT2(WHERESTR _fmt, WHEREARG, __VA_ARGS__)
//...




/* Priority definitions for the tasks . */
#define TASK_PRIO_LOW		( tskIDLE_PRIORITY + 1 )
#define TASK_PRIO_MID		( tskIDLE_PRIORITY + 2 )
#define TASK_PRIO_HIGH		( tskIDLE_PRIORITY + 3 )

#define mainDEBUG_LOG_BUFFER_SIZE	( ( unsigned short ) 20480 )
/* Remove some of the CPU intensive tasks. */
#define mainCPU_INTENSIVE_TASKS		0

/* define Queue sizes */
#define QUEUE_SIZE_PROTOCOL ( 5 )
#define QUEUE_SIZE_OUTPUT   ( 5 )
#define QUEUE_SIZE_INPUT    ( 5 )


/* define message types */
#define MSG_NONE		( 0 )
#define MSG_INIT		( 1 )
#define MSG_SERIAL_IN		( 2 )
#define MSG_BUS_RECV		( 3 )
#define MSG_SERIAL_DATA		( 4 )
#define MSG_SERIAL_PARAM	( 5 )


/*-------- Global Vars --------------*/







//! callback function for print a single char

//! signature of the printChar function that will be called 
//! to output a single char
typedef void (*printChar_cbf) (char a);



#endif /* INC_OD_CONFIG_H */
