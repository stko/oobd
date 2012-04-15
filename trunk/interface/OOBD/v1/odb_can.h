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
 * generic part of the CAN routines
 */


#ifndef INC_ODB_CAN_H
#define INC_ODB_CAN_H
#include "od_base.h"

portBASE_TYPE bus_init_can();
portBASE_TYPE bus_send_can(data_packet * data);
void bus_flush_can();
portBASE_TYPE bus_param_can(param_data * args);
void bus_close_can();

void odb_can_init();

  /*! \defgroup prot_can_generic_parm Commands: Generic CAN Commands
     The generic CAN commands are as follows, where the command is as P 8 value ...

     x is as :
     *  @{
   */


/* define parameter types */
/*! \brief switch listen mode on or off

Default: off

  \param value 0 = off !=0 = on
*/
#define PARAM_LISTEN    	    ( 1 )
/*! \brief set bus mode

Default: VALUE_BUS_SILENT_MODE

  \param value see VALUE_BUS defs
*/
#define PARAM_BUS_MODE 		    ( 2 )
/*! \brief set bus bit and speed

Default: 11bit 500kb

  \param value see VALUE_BUS_CONFIG_ defs
*/
#define PARAM_BUS_CONFIG 	    ( 3 )
/*! \brief set ??

\todo was war das nochmal gleich?



  \param value see VALUE - defs
*/
#define PARAM_BUS 		    ( 3 )

/* define values of CAN specific parameter */
#define VALUE_BUS_SILENT_MODE					( 0 )	//!< set CAN bus into silent mode
#define VALUE_BUS_LOOP_BACK_MODE				( 1 )	//!< set CAN bus into loopback mode
#define VALUE_BUS_LOOP_BACK_WITH_SILENT_MODE	( 2 )	//!< set CAN bus into loopback/silent mode
#define VALUE_BUS_NORMAL_MODE					( 3 )	//!< set CAN bus into normal mode
#define VALUE_BUS_CONFIG_11bit_125kbit  		( 1 )	//!< set CAN bus speed to 11bit with 125kbit
#define VALUE_BUS_CONFIG_11bit_250kbit  		( 2 )	//!< set CAN bus speed to 11bit with 250kbit
#define VALUE_BUS_CONFIG_11bit_500kbit  		( 3 )	//!< set CAN bus speed to 11bit with 500kbit
#define VALUE_BUS_CONFIG_11bit_1000kbit 		( 4 )	//!< set CAN bus speed to 11bit with 1000kbit
#define VALUE_BUS_CONFIG_29bit_125kbit  		( 5 )	//!< set CAN bus speed to 29bit with 125kbit
#define VALUE_BUS_CONFIG_29bit_250kbit  		( 6 )	//!< set CAN bus speed to 29bit with 250kbit
#define VALUE_BUS_CONFIG_29bit_500kbit  		( 7 )	//!< set CAN bus speed to 29bit with 500kbit
#define VALUE_BUS_CONFIG_29bit_1000kbit 		( 8 )	//!< set CAN bus speed to 29bit with 1000kbit


  /*! @} */


/* store all parameter in one single struct to maybe later store such param sets in EEPROM */
struct CanConfig {
    portBASE_TYPE recvID,	//!< Module ID
     bus,			//!< id of actual used bus
     mode,			//!< id of actual used Tranceiver mode
     busConfig			//!< nr of actual used bus configuration
};


#endif				/* INC_ODB_CAN_H */
