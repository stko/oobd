/*
 * SerialTask.c
 *
 *  Created on: 20.06.2009
 *      Author: Dirki
 */

#include <string.h>
#include <stdlib.h>

#include "stm32f10x.h"
#include "portmacro.h"
#include "FreeRTOS.h"
#include "task.h"
#include "queue.h"
#include "SerialComm.h"

#define RX_CMD_BUFFER_SIZE	32
#define TX_QUEUE_SIZE	256
#define RX_QUEUE_SIZE	256

#define ONE_SECOND_DELAY					( ( portTickType ) 1000 / portTICK_RATE_MS )

static void prvSerialTxTask( void *pvParameters );

static uint8_t rxCmdBuffer[RX_CMD_BUFFER_SIZE+1];

static xQueueHandle hSerialRx;
static xQueueHandle hSerialTx;

void InitSerialComm() {
//	uint32_t tmp;

	/* Enable PORTA Clock */
	RCC->APB2ENR |= RCC_APB2ENR_IOPAEN;

	/* Enable USART1 Clock */
	RCC->APB2ENR |= RCC_APB2ENR_USART1EN;

	/* Enable USART1 */
	USART1->CR1 = USART_CR1_UE;

	/* Set baudrate divider to 39,0625 -> 115200 baud @ 72MHz */
	USART1->BRR = (39 << 4) | (1);

	USART1->CR2 = 0;
	USART1->CR3 = 0;
	USART1->CR1 = USART_CR1_UE | USART_CR1_TE | USART_CR1_RE | USART_CR1_RXNEIE;

	/* configure PA9 input (RX), PA10 alternate push/pull output (TX) */
/*
	tmp = GPIOA->CRH;
	tmp &= ~((GPIO_CRH_CNF9 | GPIO_CRH_CNF10) | (GPIO_CRH_MODE9 | GPIO_CRH_MODE10));
	tmp |= GPIO_CRH_CNF10_0 | GPIO_CRH_CNF9_1 | GPIO_CRH_MODE9_1 | GPIO_CRH_MODE9_0;
	GPIOA->CRH = tmp;
*/

	hSerialTx = xQueueCreate( TX_QUEUE_SIZE, sizeof( uint8_t ) );
	hSerialRx = xQueueCreate( RX_QUEUE_SIZE, sizeof( uint8_t ) );

	if ((hSerialTx == 0) || (hSerialRx == 0)) {
		uart1_puts("ERROR: Can not create Rx/Tx queue !");
	} else {
		xTaskCreate(prvSerialTxTask,
				(const signed portCHAR *) "SerialComm",
				configMINIMAL_STACK_SIZE,
				(void *)NULL,
				SERIAL_COMM_TASK_PRIORITY,
				(xTaskHandle *)NULL );

		/* Set USART1 interrupt to low priority */
		NVIC->IP[USART1_IRQn] = (uint8_t)0xE0;
		/* Clear pending USART1 interrupt */
		NVIC_ClearPendingIRQ(USART1_IRQn);
		/* Enable USART1 interrupt */
		NVIC_EnableIRQ(USART1_IRQn);
	}
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

int putChar(char c){
uart1_putc( c);
}


int uart1_getc() {
	return -1;
}

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

typedef enum {
	RX_STATE_IDLE = 0,
	RX_STATE_CMD,
	RX_STATE_CRLF
} E_RX_STATE;

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

void sendCPUInfo() {
	char sbuf[32];

	strcpy(sbuf, "\r\nCPU ID: 0x");
	char *pstr = sbuf + strlen(sbuf);
	uint32ToHex(pstr, SCB->CPUID);
	pstr = sbuf + strlen(sbuf);
	strcpy(pstr, "\r\n");
	SerialSendStr(sbuf);
}

void sendRomTable() {
	uint32_t *pRomTable = (uint32_t *)0xE00FF000;

	while (*pRomTable != 0) {
		sendMemLoc(pRomTable++);
	}
	sendMemLoc(pRomTable);
}

static void prvSerialTxTask( void *pvParameters )
{
	char rxChar;
	E_RX_STATE rxState = RX_STATE_IDLE;
	int rxCmdIndex = 0;

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

void USART1_IRQHandler(void) {
	uint16_t sr = USART1->SR;
	char ch;
	static portBASE_TYPE xHigherPriorityTaskWoken = pdFALSE;
	static portBASE_TYPE xTaskWokenByReceive  = pdFALSE;

	// Check for received Data
	if (sr & USART_SR_RXNE) {
		ch = (char) USART1->DR;
		xQueueSendToBackFromISR(hSerialRx, &ch, &xHigherPriorityTaskWoken);

	    // Switch context if necessary.
	    if( xHigherPriorityTaskWoken )
	    {
	        taskYIELD ();
	    }
	}

	// Check if transmit buffer is empty
	if (sr & USART_SR_TXE) {
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
}

void strreverse(char* begin, char* end) {

	char aux;

	while(end>begin) {
		aux=*end;
		*end--=*begin;
		*begin++=aux;
	}
}

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
