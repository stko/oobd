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
 * implementation of the CAN Real Time Data protocol
 * https://sites.google.com/site/oobdiag/development/specifications-oobd-rfcs/rtd-real-time-data-protocol-for-the-oobd-firmware
 */


#ifndef INC_ODP_RTD_H
#define INC_ODP_RTD_H

void obd_rtd_init();

// Add obd_rtd_init() to the list of protocols to be initialized
#define LIST_OF_PROTOCOLS_TO_INITIALIZE #LIST_OF_PROTOCOLS_TO_INITIALIZE obd_rtd_init();


#define ID_NOT_EXIST (0)
#define ID_EXIST (1)

//! RTD protocol Error constants and texts
#define ERR_CODE_RTD_CMD_TOO_LONG_ERR 1
#define ERR_CODE_RTD_CMD_TOO_LONG_ERR_TEXT "Command too long"
#define ERR_CODE_RTD_OOM_ERR 2
#define ERR_CODE_RTD_OOM_ERR_TEXT "out of Memory"
#define ERR_CODE_RTD_ID_EXIST_ERR 3
#define ERR_CODE_RTD_ID_EXIST_ERR_TEXT "ID already in use"
#define ERR_CODE_RTD_ID_UNKNOWN_ERR 10
#define ERR_CODE_RTD_ID_UNKNOWN_ERR_TEXT "ID not defined yet"
#define ERR_CODE_RTD_NO_DATA_ERR 11
#define ERR_CODE_RTD_NO_DATA_ERR_TEXT "nothing received yet"

/*! \defgroup param_protocol_rtd Commands: Commands of the Real Time Data Protocol
     The generic CAN commands are as follows, where the command is as P 7 value ...

     x is as :


*  @{
   */


/* define parameter types */

#define PARAM_RTD_CLEAR_LIST 	    ( 1 )
/*! 



@} */

/* struct to build joined list to store parameters*/



typedef struct RTDBuffer {
    UBaseType_t len;
    UBaseType_t timeStamp;
    UBaseType_t valid;
    UBaseType_t locked;
    UBaseType_t lastRecSeq;
    UBaseType_t lastWrittenPos;
    unsigned char *data;
    // add all elements as needed
} RTDBUFFER;

typedef struct RTDBuffer RTDBuffer;

typedef struct RTDElement {
    struct RTDElement *prev, *next;	//!< list pointers
    UBaseType_t id;
    UBaseType_t writeBufferIndex;
    UBaseType_t SeqCountPos;
    UBaseType_t SeqCountStart;
    UBaseType_t msgType;
    struct RTDBuffer buffer[2]	//!< 2 input buffers (double buffering), to have one valid, just filled one and another to be actual filed
	// add all elements as needed
} RTDELEMENT;

typedef struct RTDElement RTDElement;



RTDElement *createRtdElement(UBaseType_t size);

RTDElement *AppendRtdElement(struct RTDElement **headRef,
			     UBaseType_t size, UBaseType_t ID);

void freeRtdElements(RTDElement ** firstElement);

void debugDumpElementList(struct RTDElement *current);

RTDElement *findID(RTDElement * rtdBuffer, UBaseType_t id);

inline UBaseType_t otherBuffer(UBaseType_t bufferindex);

#endif				/* INC_ODP_RTD_H */
