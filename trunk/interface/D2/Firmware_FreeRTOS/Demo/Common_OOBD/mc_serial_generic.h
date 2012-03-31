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


/*!
 * \defgroup serial The serial Communication with the Application
 
  \section Introduction
  
  The OOBD acts like a transmitter between a application, normal connected via a serial I/O stream, and a module network, connected to the bus
  
  The serial routines couple the firmware to the serial I/O stream.
  
  
\todo der serielle Teil muß noch sauber getrennt werden in das reine Handling des I/O- Streams und die Verarbeitung der Eingabe...



 */


/**
 * generic part of the serial line
 */


#ifndef INC_MC_SERIAL_GENERIC_H
#define INC_MC_SERIAL_GENERIC_H

#include "od_config.h"
  /*! \defgroup serial_generic_parm Command Line Parameter: Generic Serial Input Commands
     The serial input commands are as follows, where the command is as P 2 value ...

     x is as :
     *  @{
   */

/* define parameter types */
/*! \brief switch serial echo on or off

Default: Echo on

  \param value 0= echo off, !=0 Echo on 
*/
#define PARAM_ECHO    		    ( 1 )
/*! \brief set type of EndOfLile

Default: Carriage Return CR hex. 0x0D dec. 10

  \param value see VALUE - defs
*/
#define PARAM_LINEFEED    	    ( 2 )

  /*! @} */


//! Serial Input Error constants and texts

#define ERR_CODE_SERIAL_SYNTAX_ERR 1
#define ERR_CODE_SERIAL_SYNTAX_ERR_TEXT "Syntax Error"

portBASE_TYPE serial_init_mc();

portBASE_TYPE serial_init();

void initOutput();



#endif				/* INC_MC_SERIAL_GENERIC_H */
