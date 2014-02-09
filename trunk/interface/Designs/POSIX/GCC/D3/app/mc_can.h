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
 * MC specific CAN header
 */


#ifndef INC_MC_CAN_H
#define INC_MC_CAN_H

#include "mc_sys.h"
#include "AsyncIO/AsyncIO.h"
#include "AsyncIO/AsyncIOSocketCan.h"
#include "AsyncIO/PosixMessageQueueIPC.h"


//#include <linux/can.h>
//#include <linux/can/raw.h>
#include <string.h>

/**
 * call back function which simply delivers the received packet 
 * @param iSocket A socket desicriptor to receive the packet from.
 */
void vCANReceiveAndDeliverCallbackOOBD(int iSocket, void *pvContext);

#endif				/* INC_MC_CAN_H */
