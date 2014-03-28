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
 * implementation of the CAN RAW protocol
 */


#ifndef INC_ODP_CANRAW_H
#define INC_ODP_CANRAW_H

void obd_canraw_init();
inline int sendMoreFrames();

// Add obd_canraw_init() to the list of protocols to be initialized
#define LIST_OF_PROTOCOLS_TO_INITIALIZE #LIST_OF_PROTOCOLS_TO_INITIALIZE obd_canraw_init();



//! UDS protocol Error constants and texts
#define ERR_CODE_CANRAW_DATA_TOO_LONG_ERR 1
#define ERR_CODE_CANRAW_DATA_TOO_LONG_ERR_TEXT "<- Data block longer 4095 Bytes"

/*! \defgroup param_protocol_canraw Commands: Commands of the CAN Raw Protocol
     The generic CAN commands are as follows, where the command is as P 7 value ...

     x is as :


*  @{
   */


/* define parameter types */

#define PARAM_CANRAW_FRAME_DELAY 	    ( 1 )
#define PARAM_CANRAW_SENDID  		    ( 2 )
/*! 



@} */

/* store all parameter in one single struct to maybe later store such param sets in EEPROM */
struct CanRawConfig {
    UBaseType_t recvID,	//!< receiver ID
     separationTime;		//!< delay between two frames
};

#endif				/* INC_ODP_CANRAW_H */
