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
 * generic part of the serial line
 */


#ifndef INC_MC_SERIAL_GENERIC_H
#define INC_MC_SERIAL_GENERIC_H

#include "od_config.h"


//! Serial Input Error constants and texts

#define ERR_CODE_SERIAL_SYNTAX_ERR 1
#define ERR_CODE_SERIAL_SYNTAX_ERR_TEXT "Syntax Error"

portBASE_TYPE serial_init_mc();

portBASE_TYPE serial_init();

void initOutput();



#endif				/* INC_MC_SERIAL_GENERIC_H */
