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
 * MC generic System header
 */


#ifndef INC_MC_SYS_H
#define INC_MC_SYS_H

#include "od_base.h"
  /*! \addtogroup param
   *  Additional documentation for group 'param'
   *  @{
   */

#define VALUE_PARAM_INFO_ADC_POWER      		( 6 )
#define VALUE_PARAM_INFO_CPU_INFO 				( 10 )
#define VALUE_PARAM_INFO_MEM_LOC  				( 11 )
#define VALUE_PARAM_INFO_ROM_TABLE_LOC  		( 12 )
#define VALUE_PARAM_INFO_FREE_HEAP_SIZE			( 13 )
#define VALUE_PARAM_INFO_CRC32					( 14 )
#define VALUE_PARAM_INFO_BTM222_DEVICENAME 		( 20 )
#define VALUE_PARAM_INFO_BTM222_UART_SPEED 		( 21 )
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
