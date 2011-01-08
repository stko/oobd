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
	      if (ucRx != 13 && ucRx !=10){
		printChar (ucRx);
		putchar(ucRx);
	      }
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

void sendParam(portBASE_TYPE key, portBASE_TYPE value){
  DEBUGPRINT("Sending Param %d Value %d\n",key,value);
}


/*-----------------------------------------------------------*/
#define crEOL ( 16 )
#define crBLANK ( 17 )
#define crCMD ( 18 )
#define crHEX ( 19 )
#define crUNKWN ( 20 )



char checkValidChar(char a){
  if (a== 13){
    return crEOL;
  }
  if (a== '\t' || a == ' '){
    return crBLANK;
  }
  if (a== 'p' || a == 'P'){
    return crCMD;
  }
  if (a== '$'){
    return crHEX;
  }
  if (a>47 && a<58){
    return a-48;
  }else{
    if (a>64 && a<71){
      return a-55;
    }else{
      if (a>96 && a<103){
	return a-87;
      }else{
	return crUNKWN;
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
  portBASE_TYPE msgType = 0, lastErr=0, processFurther = 1, hexInput=0;
  portBASE_TYPE cmdKey=0 ,cmdValue = 0; // the both possible params
  static data_packet dp;
  static unsigned char buffer[8];
  enum states //!< the possible states of the input parser state machine
{
  S_DATA,  //!< we just reading data to transmit
  S_PARAM, //!< we just interpreting parameters
  S_WAITEOL, //!< input error orrured or all params already given, so just waiting for EOL
  S_SLEEP, //!< do nothing, until data has finally transfered
  S_INIT  //!< initial state, waiting for input
};
  portBASE_TYPE actState=S_INIT, totalInCount=0;
  dp.data=&buffer;
  for (;;)
    {
      if (MSG_NONE != (msgType = waitMsg (inputQueue, &incomingMsg, portMAX_DELAY)))
	{
	DEBUGPRINT("totalInCount: %d\n",totalInCount);
	switch (msgType){
	  case MSG_SERIAL_IN:
	    processFurther = 1;
	    if (actState !=S_SLEEP ){ // if we not actual ignore any input 
	      inChar=checkValidChar(*(char*)incomingMsg->addr); 
	      DEBUGPRINT("charCode: %d \n",inChar);
	      if (actState == S_WAITEOL){ // just waiting for an end of line
		if (inChar == crEOL){
		  if (lastErr){
		    printser_string("Error:");
		    printser_int(lastErr,10);
		  }
		  actState = S_INIT;
		}
	      }
	      if (actState == S_INIT){
		cmdKey=0;
		cmdValue =0;
		lastErr=0;
		hexInput=0;
		totalInCount=0;
		if (inChar<16){ // first char is a valid hex char (0-F), so we switch into data line mode
		  actState = S_DATA;
		DEBUGPRINT ("Enter Data Mode\n",'a');
		}
		if (inChar == crCMD ){ // first char is a valid hex char (0-F), so we switch into data line mode
		  actState = S_PARAM;
		  processFurther = 0;
		DEBUGPRINT ("Enter Param Mode\n",'a');
		}
		if (inChar == crEOL ){ // just print the command prompt
		    printser_string("\r\n>");
		}
	      }
	      if (actState==S_DATA){
		if (inChar == crEOL){
		  if (dp.len > 0){
		  DEBUGPRINT ("EOL detected, send Buffer\n",'a');
		    printser_string("\r\n>");
		    sendData(&dp);
		  }
		  actState = S_INIT;

		}
		else{
		  //check for valid input 
		  if (inChar < 16){ // valid char, already as it's hex value
		    if (totalInCount == 0){
		      /* reset the protocol */
		      sendMsg (MSG_INIT, protocolQueue, NULL);
		      dp.recv=0xE0; 
		      dp.len=0;
		    }
		    totalInCount++;
		    DEBUGPRINT ("%d:%d\n",totalInCount,inChar);
		    if (totalInCount % 2 ){// just to see if totalInCount is just odd or even
		      dp.data[(++dp.len)-1]=inChar<<4;
		    }else{
		      dp.data[dp.len-1]+=inChar;		      
		    }
		    if ((dp.len > 7) && ((totalInCount & 1) == 0 )){// buffer full, transfer to protocol
		      DEBUGPRINT ("Receive buffer full, send Buffer\n",'a');
		      printser_string("\r\n>");
		      sendData(&dp);
		      dp.len=0;
		    }
		  }else{ // forbitten char, just waiting for end of line
		    actState=S_WAITEOL;
		    lastErr=1;
		  }
		}
	      }
	      
	      if (actState==S_PARAM && processFurther){
		if (inChar == crEOL){
		  if (totalInCount==3 || totalInCount==4){
		  DEBUGPRINT ("EOL detected, send Param\n",'a');
		    printser_string("\r\n>");
		    sendParam(cmdKey,cmdValue);
		  }else{
		    lastErr=2;
		  }
		  actState = S_INIT;
		}
		else{
		  /*check for valid input 
		  * totalInCount is used to define where we are in the input line:
		  * 0: at the beginning
		  * 1: just reading the first parameter
		  * 2: between the two numbers
		  * 3: reading the second parameter
		  * 4: input done, just waiting for EOL
		  *
		  */
		  
		  if (inChar==crHEX){
		    if(totalInCount == 0 || totalInCount == 2) {// we are before the 2 numbers
		      hexInput=1;
		      totalInCount++; //starting to input a number
		    }else{ // forbitten char, just waiting for end of line
		      actState=S_WAITEOL;
		      lastErr=2;
		    }
		  }else{
		  if (inChar==crBLANK){
		  DEBUGPRINT("Blank detected\n",'a');
		  if(totalInCount == 1 || totalInCount==3){ //finish the input of a param
		  totalInCount++; 
		  }
		  }else{
		    if (inChar < 16){ // valid char, already as it's hex value
		      if (inChar > 10 && !hexInput){ //wrong number format
			actState=S_WAITEOL;
			lastErr=2;
		      }else {
			if (totalInCount == 0 || totalInCount==1){
			  cmdKey=cmdKey*(10+(6*hexInput))+inChar;
			  totalInCount=1; //in case we were on 0 before
			}
			if (totalInCount == 2 || totalInCount==3){
			  cmdValue=cmdValue*(10+(6*hexInput))+inChar;
			  totalInCount=3; //in case we were on 2 before
			}
		      }
		      }else{ // forbitten char, just waiting for end of line
			actState=S_WAITEOL;
			lastErr=1;
		      }
		    }
		  }
		}
	      }
	      
	    }
	  break;
	}
	disposeMsg (incomingMsg);
	DEBUGPRINT("Actual State (out): %d \n",actState);
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
