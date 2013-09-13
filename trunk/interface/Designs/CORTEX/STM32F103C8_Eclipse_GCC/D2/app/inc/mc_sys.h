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
 * MC generic System header
 */


#ifndef INC_MC_SYS_H
#define INC_MC_SYS_H

#include "od_base.h"
  /*! \defgroup system_specific_parm_D2 Commands: D2 (STM32) spezific System Commands
     The D2 (STM32) implementation specific commands are as follows, where the command is as P 1 x ...

     x is as :
     *  @{
   */

/* Values for PARAM_INFO - P 0 0 x */
#define VALUE_PARAM_INFO_ADC_POWER      		( 2 )	//!< Outputs the actual device power supply voltage
#define VALUE_PARAM_INFO_CPU_INFO 				( 3 )	//!< Outputs the actual CPU type
#define VALUE_PARAM_INFO_MEM_LOC  				( 4 )	//!< Outputs the Mem Loc
#define VALUE_PARAM_INFO_ROM_TABLE_LOC  		( 5 )	//!< Outputs the ROM Table Location
#define VALUE_PARAM_INFO_FREE_HEAP_SIZE			( 6 )	//!< Outputs the free Heap
#define VALUE_PARAM_INFO_CRC32					( 7 )	//!< Outputs the firmware CRC checksum result
#define VALUE_PARAM_INFO_BTM222_DEVICENAME 		( 8 )	//!< Outputs the BT Devicename
#define VALUE_PARAM_INFO_BTM222_UART_SPEED 		( 9 )	//!< Outputs the BT UART Speed
#define VALUE_PARAM_INFO_KLINE_FAST_INIT 		( 10 )	//!< Initiate K-Line Fast init procedure
#define VALUE_PARAM_INFO_KLINE					( 11 )	//!< Get K-Line status
#define VALUE_PARAM_INFO_LLINE					( 12 )	//!< Get L-Line status
#define VALUE_PARAM_INFO_KLINE_TX				( 13 )  //!< Get TX K-Line status

/*System specific IO- Pins */
#define IO_REL1 	(0) + SYS_SPECIFIC_IO_OFFSET
#define IO_REL2 	(1) + SYS_SPECIFIC_IO_OFFSET

  /*! @} */
// debugging macros so we can pin down message origin at a glance
#ifdef  DEBUG_SERIAL_STM32
#define DEBUGPRINT(_fmt, ...) DEBUGUARTPRINT(__VA_ARGS__)
#define DEBUGUARTPRINT(...)  uart1_puts(__VA_ARGS__)
#else
#define DEBUGPRINT(_fmt, ...)
#define DEBUGUARTPRINT(...)
#endif

uint16_t readADC1(uint8_t channel);
uint32_t CheckCrc32(void);



#endif
/* INC_MC_SYS_H */
