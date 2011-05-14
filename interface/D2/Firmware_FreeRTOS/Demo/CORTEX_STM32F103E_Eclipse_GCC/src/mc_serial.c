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
 * Inits the serial interface and starts the output task
 */

/* OOBD headers. */
#include "od_config.h"
#include "mc_serial.h"
#include "stm32f10x.h"

#define TX_QUEUE_SIZE	2
#define RX_QUEUE_SIZE	2

#define ONE_SECOND_DELAY					( ( portTickType ) 1000 / portTICK_RATE_MS )

#define SERIAL_COMM_TASK_PRIORITY			( tskIDLE_PRIORITY + 3 )

void uart1_putc(char c);

/*---------------------------------------------------------------------------*/
portBASE_TYPE
serial_init_mc ()
{
  extern printChar_cbf printChar; /* callback function */
  extern xQueueHandle internalSerialRxQueue;
  printChar = uart1_putc;;

  /* Set-up the Serial Console FreeRTOS Echo task */
  internalSerialRxQueue = xQueueCreate (RX_QUEUE_SIZE, sizeof (unsigned char));

  DEBUGUARTPRINT("\r\n*** serial_init_mc() - finished ***");

  return pdPASS;
}

/*---------------------------------------------------------------------------*/
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

/*---------------------------------------------------------------------------*/
void uart1_putc(char c) {
	/* wait for transmit buffer empty */
	while (0 == ((USART1->SR) & (USART_SR_TXE))) ;
	/* write character to buffer and increment pointer*/
	USART1->DR = (uint16_t) c;
}

int uart1_getc() {
	return -1;
}
/*---------------------------------------------------------------------------*/

typedef enum {
	RX_STATE_IDLE = 0,
	RX_STATE_CMD,
	RX_STATE_CRLF
} E_RX_STATE;
/*---------------------------------------------------------------------------*/
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
/*---------------------------------------------------------------------------*/

void sendMemLoc(uint32_t *ptr) {
  printLF();
  printser_string("Address [0x");
  printser_uint32ToHex((uint32_t)ptr);
  printser_string("] = Value 0x");
  printser_uint32ToHex(&ptr);
}
/*---------------------------------------------------------------------------*/

void sendCPUInfo() {
  printLF();
  printser_string("CPU ID: 0x");
  printser_uint32ToHex(SCB->CPUID);
}
/*---------------------------------------------------------------------------*/

void sendRomTable() {
	uint32_t *pRomTable = (uint32_t *)0xE00FF000;

	while (*pRomTable != 0) {
		sendMemLoc(pRomTable++);
	}
	sendMemLoc(pRomTable);
}
/*---------------------------------------------------------------------------*/

void USART1_IRQHandler(void) {
	DEBUGUARTPRINT("\r\n*** USART1_IRQHandler starting ***");

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
			DEBUGUARTPRINT("\r\n*** internalSerialRxQueue Zeichen geschrieben ***");

			// Switch context if necessary.
			if( xHigherPriorityTaskWoken )
			{
			  taskYIELD ();
			}
		}
		else
		{
			DEBUGUARTPRINT("\r\n*** internalSerialRxQueue Zeichen NICHT geschrieben ***");
		}
	}

	/*
	// Check if transmit buffer is empty
	if (sr & USART_SR_TXE) {
     	DEBUGUARTPRINT("\r\n*** USART1_IRQHandler TRANSMIT interrupt detected ***");
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
	DEBUGUARTPRINT("\r\n*** USART1_IRQHandler finished ***");
}
/*---------------------------------------------------------------------------*/
