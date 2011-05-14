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
 * generic part for the serial line
 */

/* OOBD headers. */
#include "od_serial.h"
#include "mc_serial.h"
#include "od_base.h"
#ifdef OOBD_PLATFORM_STM32
#include "stm32f10x.h"
#endif

/* global message queues */
xQueueHandle internalSerialRxQueue = NULL;
xQueueHandle inputQueue = NULL;
xQueueHandle outputQueue = NULL;
xQueueHandle protocolQueue = NULL;

//! pointer to writeChar() function
printChar_cbf printChar = NULL;

/* input buffer */

/*
 * This is just a workaround, as the Posix vAsyncSerialIODataAvailableISR fills only one single Queue, but as the
 * receiving tasks uses the multiMessage-Interface, we need the inputRedirectTask to shuffle the received chars from one
 * queue to the next
 */

void
inputRedirectTask (void *pvParameters)
{
  extern xQueueHandle internalSerialRxQueue;
  unsigned char ucRx;
  MsgData *msg;

  DEBUGUARTPRINT ("\r\n*** inputRedirectTask entered! ***");

  if (NULL != internalSerialRxQueue)
    {
      for (;;)
	{
	  DEBUGUARTPRINT ("\r\n*** inputRedirectTask is running! ***");

	  if (pdTRUE ==
	      xQueueReceive (internalSerialRxQueue, &ucRx, portMAX_DELAY))
	    {
	      if (ucRx != 13 && ucRx != 10)
		{
		  printChar (ucRx);
#ifdef OOBD_PLATFORM_POSIX
		  putchar (ucRx);
#endif
		}
	      msg = createMsg (&ucRx, 1);
	      if (pdPASS != sendMsg (MSG_SERIAL_IN, inputQueue, msg))
		{
		  DEBUGPRINT ("FATAL ERROR: Serial Redirect queue full!!\n",
			      'a');
		}

	    }
	}
    }

  /* Port wasn't opened. */
  DEBUGPRINT ("FATAL ERROR: No internalSerialRxQueue!\n", 'a');
  vTaskDelete (NULL);
}


/*-----------------------------------------------------------*/
void
sendData (data_packet * dp)
{
  MsgData *msg;
  int i;
  for (i = dp->len; i < 8; i++)
    {
      dp->data[i] = 0;
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

void
sendParam (portBASE_TYPE key, portBASE_TYPE value)
{
  MsgData *msg;
  portBASE_TYPE p[2];
  p[0] = key;
  p[1] = value;
  if (NULL != (msg = createMsg (&p, sizeof (p))))
    {
      if (pdPASS != sendMsg (MSG_SERIAL_PARAM, protocolQueue, msg))
	{
	  DEBUGPRINT ("FATAL ERROR: protocol queue is full!\n", 'a');

	}
    }
  else
    {
      DEBUGPRINT ("FATAL ERROR: Out of Heap space!l\n", 'a');
    }

}

void
printSerData (portBASE_TYPE msgType, void *data, printChar_cbf printchar)
{
  portBASE_TYPE err;
  err = *(portBASE_TYPE *) data;
  if (err)
    {
      printLF();
     printser_string (":Error: ");
      printser_int (err, 10);
    }
  printLF();
  printser_string (">");
}

void
createFeedbackMsg (portBASE_TYPE err)
{
  MsgData *msg;
  msg = createMsg (&err, sizeof (err));
  msg->print = printSerData;
  if (pdPASS != sendMsg (MSG_INPUT_FEEDBACK, outputQueue, msg))
    {
      DEBUGPRINT ("FATAL ERROR: Output queue full!!\n", 'a');
    }
}

/*-----------------------------------------------------------*/
#define crEOL ( 16 )
#define crBLANK ( 17 )
#define crCMD ( 18 )
#define crHEX ( 19 )
#define crUNKWN ( 20 )



char
checkValidChar (char a)
{
  if (a == 13) /* char CR carriage return */
    {
      return crEOL;
    }
  if (a == '\t' || a == ' ')
    {
      return crBLANK;
    }
  if (a == 'p' || a == 'P')
    {
      return crCMD;
    }
  if (a == '$')
    {
      return crHEX;
    }
  if (a > 47 && a < 58)  /* char 0 - 9 */
    {
      return a - 48;
    }
  else
    {
      if (a > 64 && a < 71) /* char A, B, C, D, E, F */
	{
	  return a - 55;
	}
      else
	{
	  if (a > 96 && a < 103) /* char a, b, c, d, e,f */
	    {
	      return a - 87;
	    }
	  else
	    {
	      return crUNKWN;
	    }
	}
    }
}

/*-----------------------------------------------------------*/

void
inputParserTask (void *pvParameters)
{
  DEBUGUARTPRINT ("\r\n*** inputParserTask entered! ***");

  extern xQueueHandle inputQueue;
  extern xQueueHandle protocolQueue;
  extern portBASE_TYPE lfType;
  extern struct UdsConfig config;

  MsgData *incomingMsg;
  char inChar;
  portBASE_TYPE msgType = 0, lastErr = 0, processFurther = 1, hexInput = 0;
  portBASE_TYPE cmdKey = 0, cmdValue = 0;	/* the both possible params */
  static data_packet dp;
  static unsigned char buffer[8];
  enum states			/* !< the possible states of the input parser state machine */
  {
    S_DATA,			/* !< we just reading data to transmit */
    S_PARAM,			/* !< we just interpreting parameters */
    S_WAITEOL,			/* !< input error orrured or all params already given, so just waiting for EOL */
    S_SLEEP,			/* !< do nothing, until data has finally transfered */
    S_INIT			/* !< initial state, waiting for input */
  };
  portBASE_TYPE actState = S_INIT, totalInCount = 0;
  dp.data = &buffer;
  for (;;)
    {
      DEBUGUARTPRINT ("\r\n*** inputRedirectTask is running! ***");

      if (MSG_NONE !=
	  (msgType = waitMsg (inputQueue, &incomingMsg, portMAX_DELAY)))
	{
	  switch (msgType)
	    {
	    case MSG_SERIAL_IN:
	      processFurther = 1;
	      if (actState != S_SLEEP)
		{		/* if we not actual ignore any input */
		  inChar = checkValidChar (*(char *) incomingMsg->addr);
		  if (actState == S_WAITEOL)
		    {		/* just waiting for an end of line */
		      if (inChar == crEOL)
			{
			  if (lastErr)
			    {
			      createFeedbackMsg (1);
			    }
			  actState = S_INIT;
			}
		    }
		  if (actState == S_INIT)
		    {
		      cmdKey = 0;
		      cmdValue = 0;
		      lastErr = 0;
		      hexInput = 0;
		      totalInCount = 0;
		      dp.len = 0;
		      if (inChar < 16)
			{	/* first char is a valid hex char (0-F), so we switch into data line mode */
			  actState = S_DATA;
			}
		      if (inChar == crCMD)
			{	/* first char is a valid hex char (0-F), so we switch into data line mode */
			  actState = S_PARAM;
			  processFurther = 0;
			}
		      if (inChar == crEOL)
			{	/* in case we filled the buffer already previously */
			  sendMsg (MSG_SEND_BUFFER, protocolQueue, NULL);
			  actState = S_SLEEP;
			}
		    }
		  if (actState == S_DATA)
		    {
		      if (inChar == crEOL)
			{
			  if (dp.len > 0)
			    {
			      sendData (&dp);
			      /* tells the  protocol to send the buffer */
			      printLF();
			      sendMsg (MSG_SEND_BUFFER, protocolQueue, NULL);
			      actState = S_SLEEP;
			    }
			  else
			    {
			      actState = S_INIT;
			    }
			}
		      else
			{	/* check for valid input */
			  if (inChar < 16)
			    {	/* valid char, already as it's hex value */
			      if (totalInCount == 0)
				{	/* reset the protocol */
				  sendMsg (MSG_INIT, protocolQueue, NULL);
				  dp.len = 0;
				}
			      totalInCount++;
			      if (totalInCount % 2)
				{	/* just to see if totalInCount is just odd or even */
				  dp.data[(++dp.len) - 1] = inChar << 4;
				}
			      else
				{
				  dp.data[dp.len - 1] += inChar;
				}
			      if ((dp.len > 7) && ((totalInCount & 1) == 0))
				{	/* buffer full, transfer to protocol */
				  sendData (&dp);
				  dp.len = 0;
				}
			    }
			  else
			    {	/* forbitten char, just waiting for end of line */
			      actState = S_WAITEOL;
			      lastErr = 1;
			    }
			}
		    }

		  if (actState == S_PARAM && processFurther)
		    {
		      if (inChar == crEOL)
			{
			  if (totalInCount == 3 || totalInCount == 4)
			    {
			      switch (cmdKey)
				{
				case PARAM_INFO:
				  if (cmdValue == VALUE_PARAM_INFO_VERSION) /* p 0 0 */
				  {
					printLF();
					printser_string ("OOBD ");
					printser_string (OOBDDESIGN);
					printser_string (" ");
					printser_string (SVNREV);
					printser_string (" ");
					printser_string (BUILDDATE);
				  }
			      else if (cmdValue == VALUE_PARAM_INFO_SERIALNUMBER) /* p 0 1 */
			      {
			    	printLF();
			    	printser_string (BTM222_RespBuffer);
			      }
				  else if (cmdValue == VALUE_PARAM_INFO_PROTOCOL) /* p 0 3 */
				  {
					printLF();
					printser_string ("1 - UDS (ISO14229-1)");
				  }
				  else if (cmdValue == VALUE_PARAM_INFO_BUS) /* p 0 4 */
				  {
					  if (config.bus == VALUE_BUS_CAN)
					  {
				 		printLF();
				 		printser_string("3 - CAN");
					  }
				  }
				  else if (cmdValue == VALUE_PARAM_INFO_BUS_CONFIG) /* p 0 5 */
				  {
				    if (config.busConfig == VALUE_BUS_CONFIG_11bit_125kbit)
				    {
				      printLF();
				 	  printser_string("1 = ISO 15765-4, CAN 11bit ID/125kBaud");
				 	}
				 	else if (config.busConfig == VALUE_BUS_CONFIG_11bit_250kbit)
				 	{
				 	  printLF();
				 	  printser_string("2 = ISO 15765-4, CAN 11bit ID/250kBaud");
				 	}
				 	else if (config.busConfig == VALUE_BUS_CONFIG_11bit_500kbit)
				 	{
				 	  printLF();
				 	  printser_string("3 = ISO 15765-4, CAN 11bit ID/500kBaud");
				 	}
				 	else if (config.busConfig == VALUE_BUS_CONFIG_11bit_1000kbit)
				 	{
				 	  printLF();
				 	  printser_string("4 - ISO 15765-4, CAN 11bit ID/1000kBaud");
				 	}
				 	else if (config.busConfig == VALUE_BUS_CONFIG_29bit_125kbit)
				 	{
				 	  printLF();
				 	  printser_string("5 - ISO 15765-4, CAN 29bit ID/125kBaud");
				 	}
				 	else if (config.busConfig == VALUE_BUS_CONFIG_29bit_250kbit)
				 	{
				 	  printLF();
				 	  printser_string("6 - ISO 15765-4, CAN 29bit ID/250kBaud");
				 	}
				 	else if (config.busConfig == VALUE_BUS_CONFIG_29bit_500kbit)
				 	{
				 	  printLF();
				 	  printser_string("7 - ISO 15765-4, CAN 29bit ID/500kBaud");
				 	}
				 	else if (config.busConfig == VALUE_BUS_CONFIG_29bit_1000kbit)
				 	{
				 	  printLF();
				 	  printser_string("8 - ISO 15765-4, CAN 29bit ID/1000kBaud");
				 	}
				  }
				  #ifdef OOBD_PLATFORM_STM32
				  else if (cmdValue == VALUE_PARAM_INFO_ADC_POWER) /* p 0 6 */
				  {
					printLF();
					printser_int ((readADC1(8)*(3.15/4096))*10000, 10); /* result in mV */
					printser_string (" mV");
				  }
				  else if (cmdValue == VALUE_PARAM_INFO_CPU_INFO) /* p 0 10 */
			        sendCPUInfo();			/* send CPU Info */
			      else if (cmdValue == VALUE_PARAM_INFO_MEM_LOC) /* p 0 11 */
			    	sendMemLoc(0x8003000); /* send Mem Location */
			      else if (cmdValue == VALUE_PARAM_INFO_ROM_TABLE_LOC) /* p 0 12 */
			    	sendRomTable();      	/* send ROM Table */
			      else if (cmdValue == VALUE_PARAM_INFO_FREE_HEAP_SIZE) /* p 0 13 */
			      {
			    	printLF();
			    	printser_string ("Total Heap (in byte): ");
			    	printser_int (configTOTAL_HEAP_SIZE, 10);
			    	printLF();
			    	printser_string ("Free Heap (in byte): ");
			    	printser_int (xPortGetFreeHeapSize(), 10);	/* send FreeRTOS free heap size */
			      }
			      else {
			        sendParam (cmdKey, cmdValue);
			      }
				  createFeedbackMsg (0);
				  #endif
				  break;
				case PARAM_ECHO:
				  break;
				case PARAM_LINEFEED:
				  lfType=cmdValue;
				  break;
				#ifdef OOBD_PLATFORM_STM32
				case PARAM_RESET:
				  if (1 == cmdValue)
				  {
					DEBUGUARTPRINT ("\r\n*** Softreset performed !!!");
					SCB->AIRCR = 0x05FA0604;	/* soft reset */
				  }
				  if (2 == cmdValue)
				  {
					DEBUGUARTPRINT ("\r\n*** Hardreset performed !!!");
					SCB->AIRCR = 0x05FA0004;	/* hard reset */
				  }
				  break;
 	 	 	 	 #endif

				default:
				  sendParam (cmdKey, cmdValue);
				  break;
				}				
				}
			  else
			    {
			      lastErr = 2;
			    }
			  actState = S_INIT;
			}
		      else
			{
			  /*check for valid input 
			   * totalInCount is used to define where we are in the input line:
			   * 0: at the beginning
			   * 1: just reading the first parameter
			   * 2: between the two numbers
			   * 3: reading the second parameter
			   * 4: input done, just waiting for EOL
			   *
			   */

			  if (inChar == crHEX)
			    {
			      if (totalInCount == 0 || totalInCount == 2)
				{	/* we are before the 2 numbers */
				  hexInput = 1;
				  totalInCount++;	/* starting to input a number */
				}
			      else
				{	/* forbitten char, just waiting for end of line */
				  actState = S_WAITEOL;
				  lastErr = 2;
				}
			    }
			  else
			    {
			      if (inChar == crBLANK)
				{
				  if (totalInCount == 1 || totalInCount == 3)
				    {	/* finish the input of a param */
				      hexInput = 0;	/* reset the number format */
				      totalInCount++;
				    }
				}
			      else
				{
				  if (inChar < 16)
				    {	/* valid char, already as it's hex value */
				      if (inChar > 10 && !hexInput)
					{	/* wrong number format */
					  actState = S_WAITEOL;
					  lastErr = 2;
					}
				      else
					{
					  if (totalInCount == 0
					      || totalInCount == 1)
					    {
					      cmdKey =
						cmdKey * (10 +
							  (6 * hexInput)) +
						inChar;
					      totalInCount = 1;	/* in case we were on 0 before */
					    }
					  if (totalInCount == 2
					      || totalInCount == 3)
					    {
					      cmdValue =
						cmdValue * (10 +
							    (6 * hexInput)) +
						inChar;
					      totalInCount = 3;	/* in case we were on 2 before */
					    }
					}
				    }
				  else
				    {	/* forbitten char, just waiting for end of line */
				      actState = S_WAITEOL;
				      lastErr = 1;
				    }
				}
			    }
			}
		    }

		}
	      break;
	    case MSG_SERIAL_RELEASE:
	      if (actState == S_SLEEP)
		{		/* do we just waiting for an answer? */
		  createFeedbackMsg (0);
		  actState = S_INIT;	/* start again */
		}
	      break;
	    default:
	      break;
	    }

	  disposeMsg (incomingMsg);
	}
    }
}

/*-----------------------------------------------------------*/

portBASE_TYPE
serial_init ()
{
  DEBUGUARTPRINT ("\r\n*** serial_init() entered! ***");

  extern xQueueHandle protocolQueue;
  extern xQueueHandle inputQueue;

  if (pdPASS ==
      (protocolQueue =
       xQueueCreate (QUEUE_SIZE_PROTOCOL, sizeof (struct OdMsg))))
    DEBUGUARTPRINT ("\r\n*** protocolQueue created ***");

  if (pdPASS ==
      (inputQueue = xQueueCreate (QUEUE_SIZE_INPUT, sizeof (struct OdMsg))))
    DEBUGUARTPRINT ("\r\n*** inputQueue created ***");

  serial_init_mc ();

  if (pdPASS ==
      xTaskCreate (inputRedirectTask,
		   (const signed portCHAR *) "SerialRedirect",
		   configMINIMAL_STACK_SIZE, (void *) NULL, TASK_PRIO_LOW,
		   (xTaskHandle *) NULL))
    DEBUGUARTPRINT ("\r\n*** inputRedirectTask created ***");

  if (pdPASS ==
      xTaskCreate (inputParserTask, (const signed portCHAR *) "InputParser",
		   configMINIMAL_STACK_SIZE, (void *) NULL, TASK_PRIO_MID,
		   (xTaskHandle *) NULL))
    DEBUGUARTPRINT ("\r\n*** inputParserTask created ***");

  DEBUGUARTPRINT ("\r\n*** serial_init() finished! ***");

  return pdPASS;

}
