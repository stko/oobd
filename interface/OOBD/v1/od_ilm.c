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
 * ILM: Industrial Light and Magic: Controls Light and Sound effects :-)
 */

/* OOBD headers. */
#include "od_base.h"
#include "mc_sys_generic.h"
#include "od_ilm.h"

typedef struct {
    portBASE_TYPE freq;
    portBASE_TYPE duration;
} Tone;
  /* this f**cking trick of how to nest arrays took me two days of trying and googleing. It calls "anonymous arrays"
     thanks to http://www.run.montefiore.ulg.ac.be/~martin/resources/kung-f00.html !!
   */
Tone *sounds[2] =
    { (Tone[]){{3000, 50}, {0, 50}, {2000, 80}, {0, 0}}, (Tone[]){{4, 10},
								  {5, 10},
								  {6,
								   10},
								  {7, 10},
								  {8,
								   10},
								  {0, 0}}
};

/*-----------------------------------------------------------*/

void ilmTask(void *pvParameters)
{
    DEBUGPRINT("ILM Task started\n", 'a');

    extern xQueueHandle ilmQueue;
    MsgData *msg;
    portBASE_TYPE msgType;
    portBASE_TYPE activeBus = 2;
    Tone *tone = NULL;
    portBASE_TYPE toneDuration;
    enum ledMode {
	LED_OFF,
	LED_FLASH,
	LED_ON
    };

    typedef struct {
	portBASE_TYPE actualOnStatus;
	portBASE_TYPE prevOnStatus;	//this just to avoid to set the outputs new with each tick
	portBASE_TYPE mode;
	portBASE_TYPE flickerTime;
	portBASE_TYPE lostConnectionFlag;
	portBASE_TYPE foundConnectionFlag;
	portBASE_TYPE backwardTickCounterSinceLastIncomingEvent;
    } LEDStatus;
    LEDStatus Leds[3];
    int ledTick = 0;
    int ledStatus = pdFALSE;
    int i;

    if (NULL != ilmQueue) {
	// init LEDStatus
	Leds[IO_LED_WHITE].mode = LED_FLASH;
	Leds[IO_LED_GREEN].mode = LED_OFF;
	Leds[IO_LED_RED].mode = LED_OFF;
	for (i = 0; i < 3; i++) {
	    Leds[i].actualOnStatus = pdFALSE;
	    Leds[i].flickerTime = 0;
	}
	// test only
	tone = sounds[0];
	toneDuration = tone->duration;
	sysSound(tone->freq, tone->duration);
	for (;;) {
	    if (MSG_NONE !=
		(msgType = waitMsg(ilmQueue, &msg, portMAX_DELAY)))
		/* handle message */
	    {
		switch (msgType) {
		case MSG_TICK:
		    ledTick++;
		    for (i = 0; i < 3; i++) {
			if (Leds[i].backwardTickCounterSinceLastIncomingEvent > 0) {	// reduce the tick counter since the last event
			    Leds[i].
				backwardTickCounterSinceLastIncomingEvent--;
			    if (Leds[i].backwardTickCounterSinceLastIncomingEvent == 0 && Leds[i].mode == LED_ON) {	// timeout reached to switch back from permanent Light to flashing
				Leds[i].mode = LED_FLASH;
				Leds[i].lostConnectionFlag = pdTRUE;
			    }
			}
			if (Leds[i].flickerTime > 0) {	// reduce the tick counter for flickering
			    Leds[i].flickerTime--;
			}
		    }
		    if (ledTick > LEDFLASHTIME) {
			ledTick = 0;
			ledStatus = ledStatus == pdTRUE ? pdFALSE : pdTRUE;
			for (i = 0; i < 3; i++) {
			    if (Leds[i].mode == LED_FLASH && Leds[i].flickerTime == 0) {	// just flashing and not just just flickering?
				Leds[i].actualOnStatus = ledStatus;
			    }
			}
		    }
		    for (i = 0; i < 3; i++) {	// after modifying all the flags, setting finally the outputs accourdingly
			if ((Leds[i].actualOnStatus
			     && !(Leds[i].flickerTime != 0)) !=
			    Leds[i].prevOnStatus) {
			    sysIoCtrl(i, 0, Leds[i].actualOnStatus
				      && !(Leds[i].flickerTime != 0), 0,
				      0);
			    Leds[i].prevOnStatus = Leds[i].actualOnStatus
				&& !(Leds[i].flickerTime != 0);

			}
			if (Leds[i].lostConnectionFlag == pdTRUE) {
			    // do something special to indicate lost connection
			    Leds[i].lostConnectionFlag = pdFALSE;
			}
			if (Leds[i].foundConnectionFlag == pdTRUE) {
			    // do something special to indicate found connection
			    Leds[i].foundConnectionFlag = pdFALSE;
			}

		    }
		    // manage the beep
		    if (tone != NULL) {
			toneDuration--;
			if (toneDuration < 1) {
			    tone++;
			    if (tone->duration == 0) {
				sysSound(0, 0);	//stop sound
				tone = NULL;
			    } else {
				toneDuration = tone->duration;	//load next tone
				sysSound(tone->freq, tone->duration);
			    }
			}
		    }
		    break;
		case MSG_EVENT_PROTOCOL_RECEIVED:
		    DEBUGPRINT
			("ILM Handler: MSG_EVENT_PROTOCOL_RECEIVED event received\n",
			 'a');
		    Leds[activeBus].mode = LED_ON;
		    Leds[activeBus].actualOnStatus = pdTRUE;
		    Leds[activeBus].flickerTime = LEDFLASHTIMESHORT;
		    Leds[activeBus].backwardTickCounterSinceLastIncomingEvent = LED_SERIAL_TIMEOUT;
		    break;
		case MSG_EVENT_BUS_MODE:
		    DEBUGPRINT
			("ILM Handler: MSG_EVENT_BUS_MODE event received\n",
			 'a');
		    Leds[activeBus].mode =
			(*(portBASE_TYPE *) msg->addr) ==
			MSG_EVENT_BUS_MODE_ON ? LED_ON : LED_OFF;
		    break;
		case MSG_EVENT_BUS_CHANNEL:
		    DEBUGPRINT
			("ILM Handler: MSG_EVENT_BUS_CHANNEL event received\n",
			 'a');
		    activeBus = (*(portBASE_TYPE *) msg->addr);
		    break;
		case MSG_EVENT_CMDLINE:
		    DEBUGPRINT
			("ILM Handler: MSG_EVENT_CMDLINE event received\n",
			 'a');
		    Leds[IO_LED_WHITE].mode = LED_ON;
		    Leds[IO_LED_WHITE].actualOnStatus = pdTRUE;
		    Leds[IO_LED_WHITE].flickerTime = LEDFLASHTIMESHORT;
		    Leds[IO_LED_WHITE].backwardTickCounterSinceLastIncomingEvent = LED_SERIAL_TIMEOUT;
		    break;
		default:
		    {
			DEBUGPRINT
			    ("ilmTask: outputQueue msgType default\n",
			     'a');
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
