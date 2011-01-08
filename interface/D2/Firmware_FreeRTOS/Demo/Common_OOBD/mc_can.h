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


	OOBD is using FreeTROS (www.FreeRTOS.org)

*/

/**
 * MC spezific CAN header
 */


#ifndef INC_MC_CAN_H
#define INC_MC_CAN_H

portBASE_TYPE bus_init_can ();
portBASE_TYPE bus_send_can (data_packet * data);
void bus_flush_can ();
portBASE_TYPE bus_param_can (portBASE_TYPE cmd, void *param);
void bus_close_can ();

#endif /* INC_MC_CAN_H */
