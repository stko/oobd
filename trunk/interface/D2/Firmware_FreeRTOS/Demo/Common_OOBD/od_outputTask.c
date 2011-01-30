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
 * Send data and other outputs to the serial line
 */

/* OOBD headers. */
#include "od_config.h"
#include "od_serial.h"
#include "od_base.h"

/*-----------------------------------------------------------*/

void
outputTask (void *pvParameters)
{
  extern printChar_cbf printChar;
  extern xQueueHandle outputQueue;
  extern printChar_cbf printChar;
  MsgData *msg;
  print_cbf printdata;
  portBASE_TYPE msgType;
  if (NULL != outputQueue)
    {
      for (;;)
	{
	  if (MSG_NONE !=
	      (msgType = waitMsg (outputQueue, &msg, portMAX_DELAY)))
	    //handle message
	    {
	      switch (msgType)
		{
		case MSG_BUS_RECV:
		case MSG_DUMP_BUFFER:
		case MSG_INPUT_FEEDBACK:
		  // use callback function to output data
		  printdata = msg->print;
		  printdata (msgType, msg->addr, printChar);
		  break;
		}
	      disposeMsg (msg);
	    }
	}
    }

  /* Port wasn't opened. */
  DEBUGPRINT ("FATAL ERROR: No Output queue.\n", 'a');
  vTaskDelete (NULL);
}

/*-----------------------------------------------------------*/

void
initOutput ()
{
  extern xQueueHandle outputQueue;
  outputQueue = xQueueCreate (QUEUE_SIZE_OUTPUT, sizeof (struct OdMsg));

  /* Create a Task which waits to receive bytes. */
  xTaskCreate (outputTask, "Output", configMINIMAL_STACK_SIZE,
	       NULL, TASK_PRIO_LOW, NULL);
}
