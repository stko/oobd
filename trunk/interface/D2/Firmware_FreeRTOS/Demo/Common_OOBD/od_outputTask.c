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

  OOBD is using FreeTROS (www.FreeRTOS.org)

*/

/**
 * Send data and other outputs to the serial line
 */

/* OOBD headers. */
#include "od_config.h"
#include "od_serial.h"
#include "od_base.h"
#ifdef OOBD_PLATFORM_STM32
#include "stm32f10x.h"
#endif

/*-----------------------------------------------------------*/

void
outputTask (void *pvParameters)
{
  DEBUGUARTPRINT ("\r\n*** outputTask entered! ***");

  extern printChar_cbf printChar;
  extern xQueueHandle outputQueue;
  MsgData *msg;
  print_cbf printdata;
  portBASE_TYPE msgType;

  if (NULL != outputQueue)
    {
      for (;;)
	{
	  DEBUGUARTPRINT ("\r\n*** outputTask is running! ***");

	  if (MSG_NONE !=
	      (msgType = waitMsg (outputQueue, &msg, portMAX_DELAY)))
	    /* handle message */
	    {
	      switch (msgType)
		{
		case MSG_BUS_RECV:
		  {
		    DEBUGUARTPRINT
		      ("\r\n*** outputTask: outputQueue msgType MSG_BUS_RECV ***");
		    /* use callback function to output data */
		    printdata = msg->print;
		    printdata (msgType, msg->addr, printChar);
		    break;
		  }
		case MSG_DUMP_BUFFER:
		  {
		    DEBUGUARTPRINT
		      ("\r\n*** outputTask: outputQueue msgType MSG_DUMP_BUFFER ***");
		    /* use callback function to output data */
		    printdata = msg->print;
		    printdata (msgType, msg->addr, printChar);
		    break;
		  }
		case MSG_INPUT_FEEDBACK:
		  {
		    DEBUGUARTPRINT
		      ("\r\n*** outputTask: outputQueue msgType MSG_INPUT_FEEDBACK ***");
		    /* use callback function to output data */
		    printdata = msg->print;
		    printdata (msgType, msg->addr, printChar);
		    break;
		  }
		default:
		  {
		    DEBUGUARTPRINT
		      ("\r\n*** outputTask: outputQueue msgType default ***");
		    break;
		  }
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
  DEBUGUARTPRINT ("\r\n*** initOutput() entered! ***");

  extern xQueueHandle outputQueue;
  outputQueue = xQueueCreate (QUEUE_SIZE_OUTPUT, sizeof (struct OdMsg));

  /* Create a Task which waits to receive bytes. */
  if (pdPASS == xTaskCreate (outputTask, "Output", configMINIMAL_STACK_SIZE,
			     NULL, TASK_PRIO_LOW, NULL))
    DEBUGUARTPRINT ("\r\n*** outputQueue created! ***");

  DEBUGUARTPRINT ("\r\n*** initOutput() finished! ***");
}
