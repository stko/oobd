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
 * Definition of structures and constants for data exchange between protocol and bus
 */

#ifndef INC_OD_PROTOCOLS_H
#define INC_OD_PROTOCOLS_H



/* include all busses and protocols here */
#include "od_base.h"
#include "odb_can.h"
#include "odp_uds.h"

/* List of all bus types known by OOBD */

#define ODB_BUS_NOBUS 	 	0
#define ODB_BUS_CAN11B500 	1
#define ODB_BUS_CAN11B125 	2

/* busControl commands for the busControl(command, parameter) call */


/* Init 
 * param: one of the known ODB_BUS_xx types from above 
 */
#define ODB_CMD_INIT 		0
/* Send 
 * param: pointer to data_packet structure
 */
#define ODB_CMD_SEND 		1
/* Flush 
 * param: none (null) 
 */
#define ODB_CMD_FLUSH 		2
/* Close 
 * param: nome (null)
 */
#define ODB_CMD_CLOSE 		3
/* Receive 
 * param: function ptr to a recv_cbf as typedef below
 */
#define ODB_CMD_RECV		4


/* bus and protocol error codes */

#define ODB_ERR_NONE		0
#define ODB_ERR_ERRFRAME 	1
#define ODP_ERR_NOMODULE	2



//! callback function for RX

//! signature of the protocol function that will be called 
//! if data has been received or an bus error occured
typedef void (*recv_cbf) (data_packet * p);


//! callback function for print data

//! signature of the print function that will be called 
//! if data has been received or an bus error occured
typedef void (*print_cbf) (portBASE_TYPE msgType, void *data,
			   printChar_cbf printchar);


//! function pointer prototypes to enable bus driver switching

typedef portBASE_TYPE (*bus_init) ();
typedef portBASE_TYPE (*bus_send) (data_packet * data);
typedef void (*bus_flush) ();
typedef portBASE_TYPE (*bus_param) (portBASE_TYPE cmd, void *param);
typedef void (*bus_close) ();


//enumeration for identifiers for all available protocols
enum prots
{
  ODP_UDS,
  // ODP_SIZE defines the number of available protocols
  ODP_SIZE
};

// define a array to store all protocols
void (*odparr[ODP_SIZE]) (void *pvParameters);

//enumeration for identifiers for all available busses
enum busses
{
  ODB_CAN,
  // ODB_SIZE defines the number of available busses
  ODB_SIZE
};

// define a array to store all busses
void (*odbarr[ODB_SIZE]) ();


// function prototype for the bus interface

portBASE_TYPE busControl (portBASE_TYPE cmd, void *param);

#endif /* INC_OD_PROTOCOLS_H */
