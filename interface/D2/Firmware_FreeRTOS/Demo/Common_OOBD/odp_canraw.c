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


	1 tab == 2 spaces!

	Please ensure to read the configuration and relevant port sections of the
	online documentation.


	OOBD is using FreeRTOS (www.FreeRTOS.org)

*/

/**
 * implementation of the UDS protocol
 */

/* OOBD headers. */
#include "od_config.h"
#include "od_base.h"
#include "od_protocols.h"
#include "odp_canraw.h"
#ifdef OOBD_PLATFORM_STM32
#include "stm32f10x.h"
#endif

void
obp_canraw (void *pvParameters)
{
	for( ;; )
	{
		/* for debug purposes only => must be deleted with next release */
		vTaskDelay( 100 / portTICK_RATE_MS );
		printser_string ("CAN_Raw Task running!");
	}
	/* Do all cleanup here to finish task */
	vTaskDelete (NULL);
}

void
obd_canraw_init ()
{
	odparr[ODP_CANRAW] = obp_canraw;
}
