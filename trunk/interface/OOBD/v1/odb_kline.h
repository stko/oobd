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
 * generic part of the K-LINE routines
 */


#ifndef INC_ODB_KLINE_H
#define INC_ODB_KLINE_H
#include "od_base.h"

UBaseType_t bus_init_kline();
UBaseType_t bus_send_kline(data_packet * data);
void bus_flush_kline();
void bus_close_kline();

void odb_kline_init();


UBaseType_t bus_rx_error_kline();
UBaseType_t bus_tx_error_kline();
void bus_clear_rx_error_kline();
void bus_clear_tx_error_kline();

UBaseType_t bus_rx_count_kline();
UBaseType_t bus_tx_count_kline();
void bus_clear_rx_count_kline();
void bus_clear_tx_count_kline();


// Add obd_uds_init() to the list of protocols to be initialized

#define LIST_OF_BUSSES_TO_INITIALIZE #LIST_OF_BUSSES_TO_INITIALIZE odb_can_init();

UBaseType_t bus_param_kline_spec(param_data * args);
UBaseType_t bus_param_kline_generic(param_data * args);
void bus_param_kline_generic_Print(UBaseType_t msgType, void *data,
				   printChar_cbf printchar);
void bus_param_kline_spec_Print(UBaseType_t msgType, void *data,
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

/*! \brief set the K-Line active

  \param value 0 = Transmit !=0 = Receive


*/
#define PARAM_BUS_KLINE_MODE 		    ( 3 )

/*! \brief set the K-Line active

  \param value 0 = off !=0 = on

*/
#define PARAM_BUS_LLINE_ACTIVATE 		    ( 4 )


/* define values of CAN specific parameter */
/*
#define VALUE_BUS_MODE_SILENT					( 0 )	//!< set CAN bus into silent mode
#define VALUE_BUS_MODE_LOOP_BACK				( 1 )	//!< set CAN bus into loopback mode
#define VALUE_BUS_MODE_LOOP_BACK_WITH_SILENT	( 2 )	//!< set CAN bus into loopback/silent mode
#define VALUE_BUS_MODE_NORMAL					( 3 )	//!< set CAN bus into normal mode
*/

  /*! @} */


/* store all parameter in one single struct to maybe later store such param sets in EEPROM */
/*
struct KlineConfig {
     UBaseType_t recvID,	//!< Module ID
     bus,			//!< id of actual used bus
     mode,			//!< id of actual used Tranceiver mode
     busConfig			//!< nr of actual used bus configuration
};
*/

#endif				/* INC_ODB_KLINE_H */
