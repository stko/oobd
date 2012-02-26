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
 * generic part of the CAN routines
 */


#ifndef INC_ODB_CAN_H
#define INC_ODB_CAN_H
#include "od_base.h"

portBASE_TYPE bus_init_can ();
portBASE_TYPE bus_send_can (data_packet * data);
void bus_flush_can ();
portBASE_TYPE bus_param_can (portBASE_TYPE param, portBASE_TYPE value);
void bus_close_can ();

void odb_can_init ();


/* define values of CAN specific parameter */
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

/* store all parameter in one single struct to maybe later store such param sets in EEPROM */
struct CanConfig
{
  portBASE_TYPE recvID, //!< Module ID
    bus,                //!< id of actual used bus
    mode,                //!< id of actual used Tranceiver mode
    busConfig          //!< nr of actual used bus configuration
} ;


#endif /* INC_ODB_CAN_H */
