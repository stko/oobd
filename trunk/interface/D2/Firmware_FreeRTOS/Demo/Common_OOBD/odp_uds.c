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
  data_packet *dp;

/* function pointers to the bus interface */
  extern bus_init actBus_init;
  extern bus_send actBus_send;
  extern bus_flush actBus_flush;
  extern bus_param actBus_param;
  extern bus_close actBus_close;
  extern xQueueHandle protocolQueue;
  extern xQueueHandle outputQueue;
  extern xQueueHandle inputQueue;
  extern print_cbf printdata_CAN;
  MsgData *msg;
  portBASE_TYPE *paramData;
  int i;
  unsigned char telegram[8];
  portBASE_TYPE msgType;
  portBASE_TYPE recvID = 0x7E0;
  portBASE_TYPE configTimeout = 6;
  portBASE_TYPE timeout = 0;
  /* select the can bus as output */
  odbarr[ODB_CAN] ();
  actBus_init ();
  // tell the Rx-ISR about the function to use for received data
  busControl (ODB_CMD_RECV, recvdata);
  for (; keeprunning;)
    {

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
		  DEBUGPRINT ("FATAL ERROR: output queue is full!\n", 'a');

		}
	      if (pdPASS != sendMsg (MSG_SERIAL_RELEASE, inputQueue, NULL))
		{
		  DEBUGPRINT ("FATAL ERROR: input queue is full!\n", 'a');

		}
	      timeout = 0;
	      break;
	    case MSG_SERIAL_DATA:
	      // make sure that the telegram fulfills the UDS spec.
	      dp = (data_packet *) msg->addr;
	      dp->recv = recvID;
	      dp->len &= 0x7;	// limit the length to max 8 
	      for (i = dp->len; i < 8; i++)
		{		//fill unused bytes with 0
		  dp->data[i] = 0;
		}
	      actBus_send ((data_packet *) msg->addr);
	      // forward the data to the output task
	      msg->print = printdata_CAN;
	      if (pdPASS != sendMsg (MSG_BUS_RECV, outputQueue, msg))
		{
		  DEBUGPRINT ("FATAL ERROR: output queue is full!\n", 'a');

		}

	      //disposeMsg (msg);
	      timeout = configTimeout;
	      break;
	    case MSG_SERIAL_PARAM:
	      paramData = (portBASE_TYPE *) msg->addr;
	      DEBUGPRINT ("parameter received %d %d\n", paramData[0],
			  paramData[1]);
	      switch (paramData[0])
		{
		case PARAM_TIMEOUT:
		  configTimeout = paramData[1] + 1;
		  break;
		case PARAM_RECVID:
		  recvID = paramData[1];
		  break;
		}
	      disposeMsg (msg);
	      break;
	    case MSG_INIT:
	      DEBUGPRINT ("Reset Protocol\n", 'a');
	      disposeMsg (msg);
	      break;
	    case MSG_TICK:
	      disposeMsg (msg);
	      if (timeout > 0)
		{		// we just waiting for an answer
		  if (timeout == 1)
		    {		// time's gone...
		      if (pdPASS !=
			  sendMsg (MSG_SERIAL_RELEASE, inputQueue, NULL))
			{
			  DEBUGPRINT ("FATAL ERROR: input queue is full!\n",
				      'a');

			}
		    }
		  timeout--;
		}
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
