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
 * generic part for the serial line
 */

/* OOBD headers. */
#include "od_serial.h"
#include "mc_serial.h"
#include "od_base.h"

/* global message queues */
xQueueHandle internalSerialRxQueue = NULL;
xQueueHandle inputQueue = NULL;
xQueueHandle outputQueue = NULL;
xQueueHandle protocolQueue = NULL;


//! pointer to writeChar() function
printChar_cbf printChar = NULL;

/* input buffer */

/*
 * This is just a workaround, as the Posix vAsyncSerialIODataAvailableISR fills only one single Quere, but as the
 * receving tasks uses the multiMessage-Interface, we need the inputRedirectTask to shuffle the received chars from one
 * queue to the next
 */


void
inputRedirectTask (void *pvParameters)
{
  extern xQueueHandle internalSerialRxQueue;
  unsigned char ucRx;
  MsgData *msg;
  if (NULL != internalSerialRxQueue)
    {
      for (;;)
	{
	  if (pdTRUE ==
	      xQueueReceive (internalSerialRxQueue, &ucRx, portMAX_DELAY))
	    {
	      printChar (ucRx);
	      putchar(ucRx);
	      msg = createMsg (&ucRx, 1);
	      if (pdPASS !=sendMsg (MSG_SERIAL_IN, inputQueue, msg)){
		DEBUGPRINT ("Serial Redirect queue full!!\n", 'a');	      }
	      
	    }
	}
    }

  /* Port wasn't opened. */
  DEBUGPRINT ("Serial Redirect Task exiting.\n", 'a');
  vTaskDelete (NULL);
}


/*-----------------------------------------------------------*/
void sendData(data_packet *dp){
  MsgData *msg;
  int i;
  for (i=dp->len;i<8;i++){
    dp->data[i]=0;
  }
  if (NULL != (msg = createDataMsg (dp)))
			{
			  if (pdPASS != sendMsg (MSG_SERIAL_DATA, protocolQueue, msg))
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

char checkValidChar(char a){
  if (a>47 && a<58){
    return a-48;
  }else{
    if (a>64 && a<71){
      return a-55;
    }else{
      if (a>96 && a<103){
	return a-87;
      }else{
	if (a==13){
	  return 16;
	}else{
	  return 17;
	}
      }
    }
  }
}

/*-----------------------------------------------------------*/

void
inputParserTask (void *pvParameters)
{

  //extern xQueueHandle inputQueue;
  MsgData *incomingMsg;
  char inChar;
  portBASE_TYPE msgType = 0;
  data_packet dp;
  portBASE_TYPE packetInCount=0, totalInCount=0;
  for (;;)
    {
      if (MSG_NONE != (msgType = waitMsg (inputQueue, &incomingMsg, portMAX_DELAY)))
	{
	  switch (msgType){
	    case MSG_SERIAL_IN:
		inChar=*(char*)incomingMsg->addr;
		disposeMsg (incomingMsg);
		//handle input
		if (inChar == 13 && totalInCount > 0){
		  DEBUGPRINT ("EOL detected, send Buffer\n",'a');
		    if (dp.len > 0){
		     printser_string("\r\n");
		        sendData(&dp);
		    }
		    totalInCount=0;
		    
		}
		else{
		  //check for valid input 
		  inChar= checkValidChar(inChar);
		  if (inChar < 16){ // valid char, already as it's hex value
		    if (totalInCount == 0){
		      /* reset the protocol */
		      sendMsg (MSG_INIT, protocolQueue, NULL);
		      dp.recv=0xE0; 
		      dp.len=0;
		    }
		    totalInCount++;
		     DEBUGPRINT ("%d:%d\n",totalInCount,inChar);
		    if (totalInCount & 1 ){// just to see if totalInCount is just odd or even
		      dp.data[++dp.len-1]=inChar<<4;
		    }else{
		      dp.data[dp.len-1]+=inChar;		      
		    }
		    if ((dp.len > 7) && ((totalInCount & 1) == 0 )){// buffer full, transfer to protocol
		     DEBUGPRINT ("Receive buffer full, send Buffer\n",'a');
		     printser_string("\n");
		      sendData(&dp);
		      dp.len=0;
		    }
		  }
		}
	    break;
	    default:
	      disposeMsg(incomingMsg);
	  }
	}
    }
}



portBASE_TYPE
serial_init ()
{

  // extern xQueueHandle protocolQueue;
  protocolQueue = xQueueCreate (QUEUE_SIZE_PROTOCOL, sizeof (struct OdMsg));
  inputQueue = xQueueCreate (QUEUE_SIZE_INPUT, sizeof (struct OdMsg));
  serial_init_mc ();
  xTaskCreate (inputRedirectTask, "SerialRedirect", configMINIMAL_STACK_SIZE,
	       NULL, TASK_PRIO_LOW, NULL);
  xTaskCreate (inputParserTask, "InputParser", configMINIMAL_STACK_SIZE,
	       NULL, TASK_PRIO_MID, NULL);
  return pdPASS;

}
