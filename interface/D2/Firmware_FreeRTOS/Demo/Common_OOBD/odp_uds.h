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
 * implementation of the UDS protocol
 */


#ifndef INC_ODP_UDS_H
#define INC_ODP_UDS_H

void obd_uds_init ();

/* store all parameter in one single struct to maybe later store such param sets in EEPROM */
struct UdsConfig
{
  portBASE_TYPE recvID, //!< Module ID
    timeout,      //!< timeout in systemticks
    listen,     //!< listen level
    bus,      //!< id of actual used bus
    busConfig,    //!< nr of actual used bus configuration
    timeoutPending,   //!< timeout for response pending delays in system ticks
    blockSize,    //!< max. number of frames to send, overwrites the values received from Module, if > 0.
    separationTime,   //!< delay between two frames,overwrites the values received from Module, if > 0
    tpFreq      //!< time between two tester presents in systemticks
} config;

#endif /* INC_ODP_UDS_H */
