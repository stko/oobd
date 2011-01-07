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

/* OOBD headers. */
#include "od_config.h"
#include "od_base.h"
#include "od_protocols.h"
#include "odp_uds.h"



/*-----------------------------------------------------------*/
// callback function, called from CAN Rx ISR
// transfers received telegrams into Msgqueue

void
recvdata (data_packet * p)
{
  MsgData *msg;
  extern xQueueHandle protocolQueue;
  if (NULL != (msg = createDataMsg (p)))
    {
      if (pdPASS != sendMsg (MSG_BUS_RECV, protocolQueue, msg))
	{
	  DEBUGPRINT ("FATAL ERROR: protocol queue is full!\n", 'a');

	}
     }
  else
    {
      DEBUGPRINT ("FATAL ERROR: Out of Heap space!l\n", 'a');
    }
}

/*-----------------------------------------------------------*/


void
obp_uds (void *pvParameters)
{
  int keeprunning = 1;
  data_packet dp;

/* function pointers to the bus interface */
  extern bus_init actBus_init;
  extern bus_send actBus_send;
  extern bus_flush actBus_flush;
  extern bus_param actBus_param;
  extern bus_close actBus_close;
  extern xQueueHandle protocolQueue;
  extern xQueueHandle outputQueue;
  extern print_cbf printdata_CAN;
  MsgData *msg;
  unsigned char telegram[8];
  portBASE_TYPE msgType;
  /* select the can bus as output */
  odbarr[ODB_CAN] ();
  actBus_init ();
  // tell the Rx-ISR about the function to use for received data
  busControl (ODB_CMD_RECV, recvdata);
  for (; keeprunning;)
    {
/*
      //actBus_flush ();
      dp.len = 8;
      dp.recv = 0x7E0;
      dp.data = &telegram;
      telegram[0] = 11;
      telegram[1] = 22;
      telegram[2] = 33;
      telegram[3] = 44;
      telegram[4] = 55;
      telegram[5] = 66;
      telegram[6] = 77;
      telegram[7] = 88;
      actBus_send (&dp);
*/
	if (MSG_NONE != (msgType = waitMsg (protocolQueue, &msg, portMAX_DELAY)))	// portMAX_DELAY
	//handle message
	{
	  switch (msgType)
	    {
	    case MSG_BUS_RECV:
	      // add correct print routine;
	      msg->print = printdata_CAN;
	      // forward data to the output task
	      if (pdPASS != sendMsg (MSG_BUS_RECV, outputQueue, msg))
		{
		  DEBUGPRINT ("FATAL ERROR: protocol queue is full!\n", 'a');

		}
	      break;
	    case MSG_SERIAL_DATA:
	       actBus_send ((data_packet*)msg->addr);
	      disposeMsg (msg);
	      break;
	    case MSG_SERIAL_PARAM:
	      disposeMsg (msg);
	      break;
	    case MSG_INIT:
	      DEBUGPRINT("Reset Protocol\n",'a');
	      disposeMsg (msg);
	      break;
	    default:
	      disposeMsg (msg);
	    }

	}
      //vTaskDelay (5000 / portTICK_RATE_MS);

    }

  /* Do all cleanup here to finish task */
  vTaskDelete (NULL);


}

/*-----------------------------------------------------------*/


void
obd_uds_init ()
{
  odparr[ODP_UDS] = obp_uds;
}
