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
 * implementation of the UDS protocol
 */


#ifndef INC_ODP_UDS_H
#define INC_ODP_UDS_H

void obd_uds_init();

//! UDS protocol Error constants and texts
#define ERR_CODE_UDS_DATA_TOO_LONG_ERR 1
#define ERR_CODE_UDS_DATA_TOO_LONG_ERR_TEXT "<- Data block longer 4095 Bytes"
#define ERR_CODE_UDS_MISSING_FLOW_CONTROL 2
#define ERR_CODE_UDS_MISSING_FLOW_CONTROL_TEXT "<- received instead expected Flow Control PCI 0x30"
#define ERR_CODE_UDS_WRONG_SEQUENCE_COUNT 3
#define ERR_CODE_UDS_WRONG_SEQUENCE_COUNT_TEXT "<- wrong sequence count received"
#define ERR_CODE_UDS_MISSING_FIRST_FRAME 4
#define ERR_CODE_UDS_MISSING_FIRST_FRAME_TEXT "<- received instead expected First Frame PCI 0x20"
#define ERR_CODE_UDS_TIMEOUT 5
#define ERR_CODE_UDS_TIMEOUT_TEXT "Answer time exeeded"

/* store all parameter in one single struct to maybe later store such param sets in EEPROM */
struct UdsConfig {
    portBASE_TYPE recvID,	//!< Module ID
     sendID,			//!< sender ID, used when the expected answer ID <> recvID || 8
     timeout,			//!< timeout in systemticks
     listen,			//!< listen level
     timeoutPending,		//!< timeout for response pending delays in system ticks
     blockSize,			//!< max. number of frames to send, overwrites the values received from Module, if > 0.
     separationTime,		//!< delay between two frames,overwrites the values received from Module, if > 0
     tpFreq;			//!< time between two tester presents in systemticks
};

#endif				/* INC_ODP_UDS_H */
