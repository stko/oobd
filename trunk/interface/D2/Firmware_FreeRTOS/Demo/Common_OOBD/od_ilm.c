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
 * ILM: Industrial Light and Magic: Controls Light and Sound effects :-)
 */

/* OOBD headers. */
#include "od_base.h"
#include "mc_sys_generic.h"
#include "od_ilm.h"


/*-----------------------------------------------------------*/

void ilmTask(void *pvParameters)
{
    DEBUGPRINT("ILM Task started\n",'a');

    extern xQueueHandle ilmQueue;
    MsgData *msg;
    portBASE_TYPE msgType;

    int ledTick=0;
    int ledStatus=0;
    
    if (NULL != ilmQueue) {
	for (;;) {
	    if (MSG_NONE !=
		(msgType = waitMsg(ilmQueue, &msg, portMAX_DELAY)))
		/* handle message */
	    {
		switch (msgType) {
		case MSG_TICK:
		    {
			    ledTick++;
			    if (ledTick>100){
			      ledTick=0;
			      ledStatus=ledStatus?0:1;
/*			  sysIoCtrl(IO_LED_RED, 0,
				ledStatus, 0,
				0);
			  sysIoCtrl(IO_LED_GREEN, 0,
				ledStatus, 0,
				0);
*/			  sysIoCtrl(IO_LED_WHITE, 0,
				ledStatus, 0,
				0);
			    }
			    
			/* use callback function to output data */
			break;
		    }
		default:
		    {
			DEBUGPRINT
			    ("ilmTask: outputQueue msgType default\n",'a');
			break;
		    }
		}
		disposeMsg(msg);
	    }
	}
    }
    /* Port wasn't opened. */
    DEBUGPRINT("FATAL ERROR: No Output queue.\n", 'a');
    vTaskDelete(NULL);
}

/*-----------------------------------------------------------*/

void initILM()
{
    extern xQueueHandle ilmQueue;
    ilmQueue = xQueueCreate(QUEUE_SIZE_ILM, sizeof(struct OdMsg));

    /* Create a Task which waits to receive bytes. */
    if (pdPASS ==
	xTaskCreate(ilmTask, "ILM", configMINIMAL_STACK_SIZE, NULL,
		    TASK_PRIO_LOW, NULL))
	DEBUGUARTPRINT("\r\n*** ilmQueue created! ***");

    DEBUGUARTPRINT("\r\n*** initILM() finished! ***");
}
