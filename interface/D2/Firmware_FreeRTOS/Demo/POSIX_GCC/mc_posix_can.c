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
 * MC specific can routines
 */

/* OOBD headers. */
#include "od_config.h"
#include "od_protocols.h"
#include "odb_can.h"
#include "mc_can.h"


/* UDP Packet size to send/receive. */
#define mainUDP_SEND_ADDRESS		"127.0.0.1"
#define UDP_PORT_SEND			( 9998 )
#define UDP_PORT_RECEIVE		( 9999 )

/* global vars */
struct sockaddr_in xReceiveAddress;
int iSocketReceive = 0;
xQueueHandle xUDPReceiveQueue = NULL;
struct sockaddr_in xSendAddress;
int iSocketSend = 0, iReturn = 0, iSendTaskList = pdTRUE;

//callback function for received data
recv_cbf reportReceicedData = NULL;
/* Send/Receive UDP packets. */
void prvUDPTask (void *pvParameters);


/*-----------------------------------------------------------*/

portBASE_TYPE
bus_init_can ()
{
  // Initialise Receives sockets. 
  xReceiveAddress.sin_family = AF_INET;
  xReceiveAddress.sin_addr.s_addr = INADDR_ANY;
  xReceiveAddress.sin_port = htons (UDP_PORT_RECEIVE);

  // Set-up the Receive Queue and open the socket ready to receive. 
  xUDPReceiveQueue = xQueueCreate (2, sizeof (xUDPPacket));
  iSocketReceive =
    iSocketOpenUDP (vUDPReceiveAndDeliverCallback, xUDPReceiveQueue,
		    &xReceiveAddress);

  // Remember to open a whole in your Firewall to be able to receive!!!


  iSocketSend = iSocketOpenUDP (NULL, NULL, NULL);

  if (iSocketSend != 0)
    {
      xSendAddress.sin_family = AF_INET;
      /* Set the UDP main address to reflect your local subnet. */
      iReturn =
	!inet_aton (mainUDP_SEND_ADDRESS,
		    (struct in_addr *) &(xSendAddress.sin_addr.s_addr));
      xSendAddress.sin_port = htons (UDP_PORT_SEND);
      /* Create a Task which waits to receive messages and sends its own when it times out. */
      xTaskCreate (prvUDPTask, "UDPRxTx", configMINIMAL_STACK_SIZE, NULL,
		   TASK_PRIO_MID, NULL);

      /* Remember to open a whole in your Firewall to be able to receive!!! */

      return pdPASS;
    }
  else
    {

      vSocketClose (iSocketSend);
      DEBUGPRINT ("UDP Task: Unable to open a socket.\n", 'a');
      return pdFAIL;
    }

}





/*-----------------------------------------------------------*/

portBASE_TYPE
bus_send_can (data_packet * data)
{
  static xUDPPacket xPacket;
  xPacket.ucPacket[0] = data->recv & 0xFF;	//just use the LByte
  xPacket.ucPacket[1] = data->len;
  xPacket.ucPacket[2] = 0;	// err not used here
  xPacket.ucPacket[3] = data->data[0];
  xPacket.ucPacket[4] = data->data[1];
  xPacket.ucPacket[5] = data->data[2];
  xPacket.ucPacket[6] = data->data[3];
  xPacket.ucPacket[7] = data->data[4];
  xPacket.ucPacket[8] = data->data[5];
  xPacket.ucPacket[9] = data->data[6];
  xPacket.ucPacket[10] = data->data[7];
  iReturn = iSocketUDPSendTo (iSocketSend, &xPacket, &xSendAddress);
  if (sizeof (xUDPPacket) != iReturn)
    {
      DEBUGPRINT ("UDP Failed to send whole packet: %d.\n", errno);
    }

  return pdPASS;
}


/*-----------------------------------------------------------*/
void
bus_flush_can ()
{
  DEBUGPRINT ("Flush CAN\n", 'a');
}


/*-----------------------------------------------------------*/

void ParamPrint (portBASE_TYPE msgType, void *data, printChar_cbf printchar)
{
	param_data pData;

	pData = *(param_data *) data;
	 DEBUGPRINT ("Bus Parameter received via Outputtask param %d value %d\n", pData.key,pData.value);

}


/*-----------------------------------------------------------*/
portBASE_TYPE
bus_param_can (portBASE_TYPE param, portBASE_TYPE value)
{
  DEBUGPRINT ("Bus Parameter received param %d value %d\n", param,value);
  CreateParamOutputMsg(param, value, ParamPrint);
  return pdPASS;
}


/*-----------------------------------------------------------*/
void
bus_close_can ()
{


}



/*-----------------------------------------------------------*/

void
prvUDPTask (void *pvParameters)
{
  static xUDPPacket xPacket;
  static data_packet dp;
  //struct sockaddr_in xSendAddress;
  // int iSocketSend, iReturn = 0, iSendTaskList = pdTRUE;
  //xQueueHandle xUDPReceiveQueue = (xQueueHandle) pvParameters;

  /* Open a socket for sending. */

  for (;;)
    {
      if (pdPASS ==
	  xQueueReceive (xUDPReceiveQueue, &xPacket, 2500 / portTICK_RATE_MS))
	{
	  /* Data received. Process it. */
	  dp.recv = xPacket.ucPacket[0] + 0x700;	// add the HByte again
	  dp.len = xPacket.ucPacket[1];
	  dp.err = xPacket.ucPacket[2];	// use received value for error simulations
	  dp.data = &xPacket.ucPacket[3];	// data starts here
	  //xPacket.ucNull = 0; /* Ensure the string is terminated. */
	  //DEBUGPRINT ("--%s", xPacket.ucPacket);
	  reportReceicedData (&dp);
	}
    }


  /* Unable to open the socket. Bail out. */
  vTaskDelete (NULL);
}



portBASE_TYPE
busControl (portBASE_TYPE cmd, void *param)
{
  switch (cmd)
    {
    case ODB_CMD_RECV:
      reportReceicedData = param;
      return pdPASS;
      break;
    default:
      return pdFAIL;
      break;
    }
}
