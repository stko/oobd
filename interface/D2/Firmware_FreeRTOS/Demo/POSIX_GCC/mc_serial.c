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
 * Inits the serial interface and starts the output task
 */

/* OOBD headers. */
#include "od_config.h"
#include "mc_serial_generic.h"
#include "mc_serial.h"

xTaskHandle hSerialTask;
/* file handle to communicate with the oobd side. */
static int oobdIOHandle = 0;


//callback routine to write a char to the output

void writeChar(char a)
{
    /* Echo it back to the sender. */
    (void) write(oobdIOHandle, &a, 1);
}




portBASE_TYPE serial_init_mc()
{

    extern printChar_cbf printChar;
    extern xQueueHandle internalSerialRxQueue;
    printChar = writeChar;

    // Set-up the Serial Console Echo task
    if (pdTRUE == lAsyncIOSerialOpen("/tmp/OOBD", &oobdIOHandle)) {
	internalSerialRxQueue = xQueueCreate(2, sizeof(unsigned char));
	(void) lAsyncIORegisterCallback(oobdIOHandle,
					vAsyncSerialIODataAvailableISR,
					internalSerialRxQueue);
    }
    return pdPASS;

}
