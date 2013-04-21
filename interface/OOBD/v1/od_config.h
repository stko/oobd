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
#include "semphr.h"
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
#define QUEUE_SIZE_PROTOCOL ( 25  )
#define QUEUE_SIZE_OUTPUT   ( 5 )
#define QUEUE_SIZE_INPUT    ( 5 )
#define QUEUE_SIZE_ILM    ( 5 )


//! \todo wir brauchen noch eine äquivalente Deklaration aller Busse wie hier für alle Protokolle

  /*! \defgroup protocol_table Available Protocols: Overview
     To make sure each application will get the same protocol now and in the future when requesting one, the available protocols are numbered here globaly

     x is as :
     *  @{
   */

  // number of all OOBD globally known Protocols 
#define SYS_NR_OF_PROTOCOLS 				(4)
#define VALUE_PARAM_PROTOCOL_CAN_RAW			(0)
#define VALUE_PARAM_PROTOCOL_CAN_UDS			(1)
#define VALUE_PARAM_PROTOCOL_KLINE_FASTINIT		(2)
#define VALUE_PARAM_PROTOCOL_CAN_RTD			(3)
//#define VALUE_PARAM_PROTOCOL_xxx              (11) //please add new protocols here - this declaration becomes then globally valid for all OOBD firmware implementations

  /*! @} */



//! Number of allowed arguments per command
#define MAX_NUM_OF_ARGS (5)


#define LEDFLASHTIME (100)	//!< LED long flash period time in ticks
#define LEDFLASHTIMESHORT (10)	//!< LED short flash period time in ticks
#define LED_SERIAL_TIMEOUT (1000)	//!< LED timeout for the serial input

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
     listen
//!< listen level
} globalConfig;

#endif				/* INC_OD_CONFIG_H */
