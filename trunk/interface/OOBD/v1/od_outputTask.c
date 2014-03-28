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
 * Send data and other outputs to the serial line
 */

/* OOBD headers. */
#include "od_config.h"
#include "od_base.h"
#include "mc_serial_generic.h"


/*-----------------------------------------------------------*/

void outputTask(void *pvParameters)
{
    DEBUGUARTPRINT("\r\n*** outputTask entered! ***");

    extern printChar_cbf printChar;
    extern QueueHandle_t outputQueue;
    MsgData *msg;
    print_cbf printdata;
    UBaseType_t msgType;

    if (NULL != outputQueue) {
	for (;;) {
	    DEBUGUARTPRINT("\r\n*** outputTask is running! ***");

	    if (MSG_NONE !=
		(msgType = waitMsg(outputQueue, &msg, portMAX_DELAY)))
		/* handle message */
	    {
		switch (msgType) {
		case MSG_BUS_RECV:
		    {
			DEBUGUARTPRINT
			    ("\r\n*** outputTask: outputQueue msgType MSG_BUS_RECV ***");
			/* use callback function to output data */
			printdata = msg->print;
			printdata(msgType, msg->addr, printChar);
			break;
		    }
		case MSG_DUMP_BUFFER:
		    {
			DEBUGUARTPRINT
			    ("\r\n*** outputTask: outputQueue msgType MSG_DUMP_BUFFER ***");
			/* use callback function to output data */
			printdata = msg->print;
			printdata(msgType, msg->addr, printChar);
			break;
		    }
		case MSG_INPUT_FEEDBACK:
		    {
			DEBUGUARTPRINT
			    ("\r\n*** outputTask: outputQueue msgType MSG_INPUT_FEEDBACK ***");
			/* use callback function to output data */
			printdata = msg->print;
			printdata(msgType, msg->addr, printChar);
			break;
		    }
		case MSG_HANDLE_PARAM:
		    {
			DEBUGUARTPRINT
			    ("\r\n*** outputTask: outputQueue msgType MSG_HANDLE_PARAM ***");
			/* use callback function to output data */
			printdata = msg->print;
			printdata(msgType, msg->addr, printChar);
			break;
		    }
		default:
		    {
			DEBUGUARTPRINT
			    ("\r\n*** outputTask: outputQueue msgType default ***");
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

void initOutput()
{
    DEBUGUARTPRINT("\r\n*** initOutput() entered! ***");

    extern QueueHandle_t outputQueue;
    outputQueue = xQueueCreate(QUEUE_SIZE_OUTPUT, sizeof(struct OdMsg));

    /* Create a Task which waits to receive bytes. */
    if (pdPASS ==
	xTaskCreate(outputTask, "Output", configMINIMAL_STACK_SIZE, NULL,
		    TASK_PRIO_LOW, NULL))
	DEBUGUARTPRINT("\r\n*** outputQueue created! ***");

    DEBUGUARTPRINT("\r\n*** initOutput() finished! ***");
}
