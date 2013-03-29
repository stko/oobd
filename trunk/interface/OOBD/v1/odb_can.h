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
void bus_close_can();

void odb_can_init();

portBASE_TYPE bus_rx_error_can();
portBASE_TYPE bus_tx_error_can();
void bus_clear_rx_error_can();
void bus_clear_tx_error_can();

portBASE_TYPE bus_rx_count_can();
portBASE_TYPE bus_tx_count_can();
void bus_clear_rx_count_can();
void bus_clear_tx_count_can();
uint16_t CAN_GetFilterReg16(uint8_t FilterID, uint8_t FilterReg,
			    uint8_t FilterPos);
uint32_t CAN_GetFilterReg32(uint8_t FilterID, uint8_t FilterReg);

// Add obd_uds_init() to the list of protocols to be initialized

#define LIST_OF_BUSSES_TO_INITIALIZE #LIST_OF_BUSSES_TO_INITIALIZE odb_can_init();



portBASE_TYPE bus_param_can_spec(param_data * args);
portBASE_TYPE bus_param_can_generic(param_data * args);
void bus_param_can_generic_Print(portBASE_TYPE msgType, void *data,
				 printChar_cbf printchar);
void bus_param_can_spec_Print(portBASE_TYPE msgType, void *data,
			      printChar_cbf printchar);




  /*! \defgroup prot_can_generic_parm Commands: Generic CAN Commands
     The generic CAN commands are as follows, where the command is as P 9 value ...

     x is as :
     *  @{
   */


/* define parameter types */
/*! \brief switch listen mode on or off

Default: off

  \param value 0 = off !=0 = on
  \param channel bus channel

*/
#define PARAM_LISTEN    	    ( 1 )
/*! \brief set bus mode

Default: VALUE_BUS_SILENT_MODE

  \param value see VALUE_BUS defs
  \param channel bus channel
*/
#define PARAM_BUS_MODE 		    ( 2 )
/*! \brief set bus bit and speed

Default: 11bit 500kb

  \param value see VALUE_BUS_CONFIG_ defs
  \param channel bus channel
*/
#define PARAM_BUS_CONFIG 	    ( 3 )
/*! \brief set the actual output channel


  \param channel bus channel 0-x

*/
#define PARAM_BUS_OUTPUT_ACTIVATE 		    ( 4 )
/*! \brief set the actual CAN Filter IDs - 11bit


  \param CAN Filter ID - 11bit

*/
#define	PARAM_BUS_Can11FilterID				( 10 )
/*! \brief set the actual CAN Filter IDs - 29bit


  \param CAN Filter ID - 11bit

*/
#define	PARAM_BUS_Can29FilterID				( 12 )
/*! \brief set the actual CAN Mask IDs - 11bit


  \param CAN Mask ID - 11bit

*/
#define	PARAM_BUS_Can11MaskID				( 11 )
/*! \brief set the actual CAN Mask IDs - 29bit


  \param CAN Mask ID - 29 bit

*/
#define	PARAM_BUS_Can29MaskID				( 13 )
/*! \brief set the actual CAN Filter reset


  \param CAN filter reset

*/
#define	PARAM_BUS_CanFilterReset				( 14 )

/* define values of CAN specific parameter */
#define VALUE_BUS_MODE_SILENT					( 0 )	//!< set CAN bus into silent mode
#define VALUE_BUS_MODE_LOOP_BACK				( 1 )	//!< set CAN bus into loopback mode
#define VALUE_BUS_MODE_LOOP_BACK_WITH_SILENT	( 2 )	//!< set CAN bus into loopback/silent mode
#define VALUE_BUS_MODE_NORMAL					( 3 )	//!< set CAN bus into normal mode
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
