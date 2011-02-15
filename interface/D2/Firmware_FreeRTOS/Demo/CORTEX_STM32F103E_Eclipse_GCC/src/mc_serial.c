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
 * Inits the serial interface and starts the output task
 */

/* OOBD headers. */
#include "od_config.h"
#include "mc_serial.h"
//#include "SerialComm.h"

/*
#include <string.h>
#include <stdlib.h>
*/

#include "stm32f10x.h"
/*
#include "portmacro.h"
#include "FreeRTOS.h"
#include "task.h"
#include "queue.h"
*/

xTaskHandle hSerialTask;
/* file handle to communicate with the oobd side. */
static int oobdIOHandle = 0;

//callback routine to write a char to the output

#define RX_CMD_BUFFER_SIZE	32
#define TX_QUEUE_SIZE	256
#define RX_QUEUE_SIZE	256

#define ONE_SECOND_DELAY					( ( portTickType ) 1000 / portTICK_RATE_MS )

//static void prvSerialTxTask( void *pvParameters );
#define SERIAL_COMM_TASK_PRIORITY			( tskIDLE_PRIORITY + 3 )

static uint8_t rxCmdBuffer[RX_CMD_BUFFER_SIZE+1];

void uart1_putc(char c);

//static xQueueHandle hSerialRx;
//static xQueueHandle hSerialTx;

portBASE_TYPE
serial_init_mc ()
{
  extern printChar_cbf printChar; /* callback function */
  extern xQueueHandle internalSerialRxQueue;
//  printChar = writeChar;;
  printChar = uart1_putc;;

#ifdef DEBUG_SERIAL
  uart1_puts("\r\n*** serial_init_mc() - entered ***");
#endif

  // Set-up the Serial Console Echo task
  internalSerialRxQueue = xQueueCreate (2, sizeof (unsigned char));

#ifdef DEBUG_SERIAL
  uart1_puts("\r\n*** serial_init_mc() - finished ***");
#endif

  return pdPASS;
}

void uart1_puts(char const *str) {
	if (str) {
		/* transmit characters until 0 character */
		while (*str) {
			/* wait for transmit buffer empty */
			while (0 == ((USART1->SR) & (USART_SR_TXE))) ;
			/* write character to buffer and increment pointer*/
			USART1->DR = *str++;
		}
	}
}

void uart1_putc(char c) {
	/* wait for transmit buffer empty */
	while (0 == ((USART1->SR) & (USART_SR_TXE))) ;
	/* write character to buffer and increment pointer*/
	USART1->DR = (uint16_t) c;
}

int uart1_getc() {
	return -1;
}
/*
void SerialSendStr(char const *str) {
	if (str) {
		while (*str) {
			xQueueSendToBack(hSerialTx, (void *)str, portMAX_DELAY);
			str++;
		}
		// enable TXE interrupt
		USART1->CR1 |= USART_CR1_TXEIE;
	}
}
*/
typedef enum {
	RX_STATE_IDLE = 0,
	RX_STATE_CMD,
	RX_STATE_CRLF
} E_RX_STATE;
/*
void sendMemLoc(uint32_t *ptr) {
	char sbuf[32];

	strcpy(sbuf,"[0x");
	char *pstr = sbuf + strlen(sbuf);
	uint32ToHex(pstr, (uint32_t)ptr);
	strcat(pstr, "] = 0x");
	pstr = sbuf + strlen(sbuf);
	uint32ToHex(pstr, *ptr);
	strcat(pstr, "\r\n");
	SerialSendStr(sbuf);
}
*/
/*
void sendCPUInfo() {
	char sbuf[32];

	strcpy(sbuf, "\r\nCPU ID: 0x");
	char *pstr = sbuf + strlen(sbuf);
	uint32ToHex(pstr, SCB->CPUID);
	pstr = sbuf + strlen(sbuf);
	strcpy(pstr, "\r\n");
	SerialSendStr(sbuf);
}
*/
/*
void sendRomTable() {
	uint32_t *pRomTable = (uint32_t *)0xE00FF000;

	while (*pRomTable != 0) {
		sendMemLoc(pRomTable++);
	}
	sendMemLoc(pRomTable);
}
*/
/*
static void prvSerialTxTask( void *pvParameters )
{
	char rxChar;
	E_RX_STATE rxState = RX_STATE_IDLE;
	int rxCmdIndex = 0;
uart1_puts("\r\nprvSerialTxTask entered!!!");

	for(;;) {
		while (pdFALSE == xQueueReceive(hSerialRx, &rxChar, portMAX_DELAY)) ;
		switch (rxState) {
			case RX_STATE_IDLE:
				if ((rxChar != 0x0a) && (rxChar != 0x0d)) {
					rxState = RX_STATE_CMD;
					rxCmdIndex = 0;
					rxCmdBuffer[rxCmdIndex++] = rxChar;
				}
				break;

			case RX_STATE_CMD:
				uart1_puts("\r\nprvSerialTxTask - received character!!!");
				if ((rxChar == 0x0a) || (rxChar == 0x0d)) {
					rxState = RX_STATE_IDLE;
					rxCmdBuffer[rxCmdIndex] = 0;

					sendCPUInfo();
					sendRomTable();
				} else {
					if (rxCmdIndex < RX_CMD_BUFFER_SIZE) {
						rxCmdBuffer[rxCmdIndex++] = rxChar;
						xQueueSendToBack(hSerialTx, &rxChar, portMAX_DELAY);
					}
				}
				break;

			default:
				rxState = RX_STATE_IDLE;
				rxCmdIndex = 0;
				break;
		}
	}
}
*/
void USART1_IRQHandler(void) {
#ifdef DEBUG_SERIAL
	uart1_puts("\r\n*** USART1_IRQHandler starting ***");
#endif
	extern xQueueHandle internalSerialRxQueue;
	uint16_t sr = USART1->SR;
	char ch;
	static portBASE_TYPE xHigherPriorityTaskWoken = pdFALSE;
	static portBASE_TYPE xTaskWokenByReceive  = pdFALSE;

	// Check for received Data
	if (sr & USART_SR_RXNE) {
		ch = USART_ReceiveData(USART1);

		if (pdPASS == xQueueSendToBackFromISR(internalSerialRxQueue, &ch, &xHigherPriorityTaskWoken))
		{
			#ifdef DEBUG_SERIAL
				uart1_puts("\r\n*** internalSerialRxQueue Zeichen geschrieben ***");
			#endif
			// Switch context if necessary.
			if( xHigherPriorityTaskWoken )
			{
			  taskYIELD ();
			}
		}
		else
		{
			#ifdef DEBUG_SERIAL
				uart1_puts("\r\n*** internalSerialRxQueue Zeichen NICHT geschrieben ***");
			#endif
		}
	}

	/*
	// Check if transmit buffer is empty
	if (sr & USART_SR_TXE) {
#ifdef DEBUG_SERIAL
	uart1_puts("\r\n*** USART1_IRQHandler TRANSMIT interrupt detected ***");
#endif
		// Send character if available
		if (pdTRUE == xQueueReceiveFromISR(hSerialTx, &ch, &xTaskWokenByReceive)) {
				USART1->DR = ch;
			    // Switch context if necessary.
			    if( xTaskWokenByReceive )
			    {
			        taskYIELD ();
			    }
		} else {
			// disable TXE interrupt
			USART1->CR1 &= ~USART_CR1_TXEIE;
		}
	}
*/
#ifdef DEBUG_SERIAL
	uart1_puts("\r\n*** USART1_IRQHandler finished ***");
#endif
}

/*
void strreverse(char* begin, char* end) {

	char aux;

	while(end>begin) {
		aux=*end;
		*end--=*begin;
		*begin++=aux;
	}
}
*/
/*
void uint8ToHex(char *buf, uint8_t value) {
	static const char num[] = "0123456789abcdef";

	// write upper nibble
	buf[0] = num[value >> 4];
	// write lower nibble
	buf[1] = num[value & 0x0F];
	buf[2] = 0;
}

void uint16ToHex(char *buf, uint16_t value) {
	uint8ToHex(buf, (uint8_t)(value >> 8));
	buf += 2;
	uint8ToHex(buf, (uint8_t)value);
	buf += 2;
	*buf = 0;
}

void uint32ToHex(char *buf, uint32_t value) {
	uint16ToHex(buf, (uint16_t)(value >> 16));
	buf += 4;
	uint16ToHex(buf, (uint16_t)value);
	buf += 4;
	*buf = 0;
}

void itoa(int value, char* str, int base) {

	static const char num[] = "0123456789abcdefghijklmnopqrstuvwxyz";
	char* wstr=str;
	int sign;

	div_t res;

	// Validate base
	if (base<2 || base>35) {
		*wstr='\0';
		return;
	}

	// Take care of sign
	if ((sign=value) < 0) value = -value;

	// Conversion. Number is reversed.
	do {
		res = div(value,base);
		*wstr++ = num[res.rem];
		value=res.quot;
	} while (value != 0);

	if(sign<0) *wstr++='-';

	*wstr='\0';

	// Reverse string
	strreverse(str,wstr-1);
}
*/
