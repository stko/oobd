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
 * MC specific system routines
 */

/* OOBD headers. */
#include "od_config.h"
#include "od_base.h"
#include "mc_sys.h"

void mc_init_sys_boot (){
  DEBUGPRINT ("boot system\n", 'a');
}



void
printParam_sys (portBASE_TYPE msgType, void *data, printChar_cbf printchar)
{
  static param_data *pd;
  pd = data;
  printser_string ("Moin");
  printLF();
  printEOT();
}

portBASE_TYPE bus_param_sys (portBASE_TYPE param, portBASE_TYPE value){
CreateParamOutputMsg(param, value,  printParam_sys);
}

void mc_init_sys_tasks (){
  DEBUGPRINT ("init system tasks\n", 'a');
}

void mc_init_sys_shutdown (){
  DEBUGPRINT ("shutdown systems\n", 'a');
}

void mc_sys_idlehook(){
  /* The co-routines are executed in the idle task using the idle task hook. */
//  vCoRoutineSchedule();        /* Comment this out if not using Co-routines. */

#ifdef __GCC_POSIX__
//  struct timespec xTimeToSleep, xTimeSlept;
  /* Makes the process more agreeable when using the Posix simulator. */
//  xTimeToSleep.tv_sec = 1;
//  xTimeToSleep.tv_nsec = 0;
//  nanosleep (&xTimeToSleep, &xTimeSlept);
#endif
}