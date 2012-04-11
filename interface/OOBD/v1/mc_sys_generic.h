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


#ifndef INC_MC_SYS_GENERIC_H
#define INC_MC_SYS_GENERIC_H

//! Global Names for standard IO-Pins
#define IO_LED_WHITE (0)
#define IO_LED_GREEN (1)
#define IO_LED_RED (2)
#define IO_BUZZER 	(3)
/*!
To seperate between OOBD standard IOs and implementation specific IOs, the identifiers for system specific IOs starts with the offset SYS_SPECIFIC_IO_OFFSET
*/
#define SYS_SPECIFIC_IO_OFFSET	(10000)
  /*! \defgroup system_generic_parm Command Line Parameter: Generic System Commands
     Generic (implementation independent) system commands, where the command is as P 1 x ...

     x is as :
     *  @{
   */

/* define parameter types */
#define PARAM_PROTOCOL 		    ( 0 )	//!< switch to protocol y, accourding to the available protocol table \ref protocol_table
#define PARAM_SET_OUTPUT 		    ( 1 )	//!< switch to protocol y, accourding to the available protocol table \ref protocol_table

  /*! @} */
void printParam_sys(portBASE_TYPE msgType, void *data,
		    printChar_cbf printchar);
void mc_init_sys_boot();
void mc_init_sys_tasks();
void mc_init_sys_shutdown();
void mc_sys_idlehook();

void printParam_sys_specific(portBASE_TYPE msgType, void *data,
			     printChar_cbf printchar);
void mc_init_sys_boot_specific();
void mc_init_sys_tasks_specific();
void mc_init_sys_shutdown_specific();



/** \brief handles input parameters
* \return pdTrue, if parameter was known and no futher handling is wanted ; pdFalse, if parameter is not not known and needs futher handling in other areas
*/

portBASE_TYPE eval_param_sys(param_data * args);
portBASE_TYPE eval_param_sys_specific(param_data * args);
portBASE_TYPE sysIoCtrl(portBASE_TYPE pinID, portBASE_TYPE lowerValue,
			portBASE_TYPE upperValue, portBASE_TYPE duration,
			portBASE_TYPE waveType);
portBASE_TYPE sysSound(portBASE_TYPE frequency, portBASE_TYPE volume);
#endif
/* INC_MC_SYS_GENERIC_H */
