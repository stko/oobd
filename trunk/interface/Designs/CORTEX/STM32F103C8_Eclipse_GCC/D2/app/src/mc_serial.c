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
 * Inits the serial interface and starts the output task
 */

/* OOBD headers. */
#include "mc_sys.h"
#include "mc_serial_generic.h"
#include "mc_serial.h"
#include "stm32f10x.h"

#define TX_QUEUE_SIZE	2
#define RX_QUEUE_SIZE	2

#define ONE_SECOND_DELAY					( ( portTickType ) 1000 / portTICK_RATE_MS )

#define SERIAL_COMM_TASK_PRIORITY			( tskIDLE_PRIORITY + 3 )

void uart1_putc(char c);

/*---------------------------------------------------------------------------*/
portBASE_TYPE serial_init_mc()
{

    extern printChar_cbf printChar;	/* callback function */

    extern xQueueHandle internalSerialRxQueue;

    printChar = uart1_putc;
    ;

    /* Set-up the Serial Console FreeRTOS Echo task */
    internalSerialRxQueue =
	xQueueCreate(RX_QUEUE_SIZE, sizeof(unsigned char));

    DEBUGUARTPRINT("\r\n*** serial_init_mc() - finished ***");

    return pdPASS;

}

/*---------------------------------------------------------------------------*/
void uart1_puts(char const *str)
{

    if (str) {

	/* transmit characters until 0 character */
	while (*str) {

	    /* wait for transmit buffer empty */
	    while (0 == ((USART1->SR) & (USART_SR_TXE)));

	    /* write character to buffer and increment pointer */ USART1->DR
		= *str++;

	}

    }

}

/*---------------------------------------------------------------------------*/
void uart1_putc(char c)
{

    /* wait for transmit buffer empty */
    while (0 == ((USART1->SR) & (USART_SR_TXE)));

    /* write character to buffer and increment pointer */ USART1->DR
	= (uint16_t) c;

}

void BTM222_Rx_getc(char c)
{
    extern unsigned char BTM222_BtAddress[];
    extern unsigned char BTM222_DeviceName[];
    extern unsigned char BTM222_UartSpeed;

    if (c == '\r') {		/* char CR Carriage return */

	/* check if response depends on request "atb?" */
	if (BTM222_RespBuffer[0] == 'a' && BTM222_RespBuffer[1] == 't'
	    && BTM222_RespBuffer[2] == 'b' && BTM222_RespBuffer[3]
	    == '?') {

	    /* create Bluetooth MAC-Address of BTM222 Response of "atb?" request */
	    BTM222_BtAddress[0] = BTM222_RespBuffer[BufCnt - 12];
	    BTM222_BtAddress[1] = BTM222_RespBuffer[BufCnt - 11];
	    BTM222_BtAddress[2] = ':';
	    BTM222_BtAddress[3] = BTM222_RespBuffer[BufCnt - 10];
	    BTM222_BtAddress[4] = BTM222_RespBuffer[BufCnt - 9];
	    BTM222_BtAddress[5] = ':';
	    BTM222_BtAddress[6] = BTM222_RespBuffer[BufCnt - 8];
	    BTM222_BtAddress[7] = BTM222_RespBuffer[BufCnt - 7];
	    BTM222_BtAddress[8] = ':';
	    BTM222_BtAddress[9] = BTM222_RespBuffer[BufCnt - 6];
	    BTM222_BtAddress[10] = BTM222_RespBuffer[BufCnt - 5];
	    BTM222_BtAddress[11] = ':';
	    BTM222_BtAddress[12] = BTM222_RespBuffer[BufCnt - 4];
	    BTM222_BtAddress[13] = BTM222_RespBuffer[BufCnt - 3];
	    BTM222_BtAddress[14] = ':';
	    BTM222_BtAddress[15] = BTM222_RespBuffer[BufCnt - 2];
	    BTM222_BtAddress[16] = BTM222_RespBuffer[BufCnt - 1];
	    BTM222_BtAddress[17] = '\0';	/* add termination of a string */
	}

	else if (BTM222_RespBuffer[0] == 'a'
		 && BTM222_RespBuffer[1] == 't'
		 && BTM222_RespBuffer[2] == 'l' && BTM222_RespBuffer[3]
		 == '?') {

	    BTM222_UartSpeed = BTM222_RespBuffer[BufCnt - 1];
	}

	else if (BTM222_RespBuffer[0] == 'a'
		 && BTM222_RespBuffer[1] == 't'
		 && BTM222_RespBuffer[2] == 'q' && BTM222_RespBuffer[3]
		 == '?') {
	    /* disable status messages like OK, ERROR, CONNECT, DISCONNECT of BT-Module */
	    if (BTM222_RespBuffer[BufCnt - 5] == '0') {
		USART_SendData(USART1, 'a');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, 't');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, 'q');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, '1');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, '\r');
		btm_uart_delay_ms(100);
	    }
	}

	else if (BTM222_RespBuffer[0] == 'a'
		 && BTM222_RespBuffer[1] == 't'
		 && BTM222_RespBuffer[2] == 'n' && BTM222_RespBuffer[3]
		 == '?') {

	    /* verify name */
	    if (BTM222_RespBuffer[BufCnt - 15] == 'O'
		&& BTM222_RespBuffer[BufCnt - 14] == 'O'
		&& BTM222_RespBuffer[BufCnt - 13] == 'B'
		&& BTM222_RespBuffer[BufCnt - 12] == 'D'
		&& BTM222_RespBuffer[BufCnt - 11] == '-'
		&& BTM222_RespBuffer[BufCnt - 10] == 'C'
		&& BTM222_RespBuffer[BufCnt - 9] == 'u'
		&& BTM222_RespBuffer[BufCnt - 8] == 'p'
		&& BTM222_RespBuffer[BufCnt - 7] == ' '
		&& BTM222_RespBuffer[BufCnt - 6]
		== BTM222_BtAddress[9]
		&& BTM222_RespBuffer[BufCnt - 5]
		== BTM222_BtAddress[10]
		&& BTM222_RespBuffer[BufCnt - 4]
		== BTM222_BtAddress[12]
		&& BTM222_RespBuffer[BufCnt - 3]
		== BTM222_BtAddress[13]
		&& BTM222_RespBuffer[BufCnt - 2]
		== BTM222_BtAddress[15]
		&& BTM222_RespBuffer[BufCnt - 1]
		== BTM222_BtAddress[16]) {

		BTM222_DeviceNameFlag = pdTRUE;

		/* store BTM222 device name "OOBD-Cup xxxxxx" local */
		BTM222_DeviceName[0] = BTM222_RespBuffer[BufCnt - 15];
		BTM222_DeviceName[1] = BTM222_RespBuffer[BufCnt - 14];
		BTM222_DeviceName[2] = BTM222_RespBuffer[BufCnt - 13];
		BTM222_DeviceName[3] = BTM222_RespBuffer[BufCnt - 12];
		BTM222_DeviceName[4] = BTM222_RespBuffer[BufCnt - 11];
		BTM222_DeviceName[5] = BTM222_RespBuffer[BufCnt - 10];
		BTM222_DeviceName[6] = BTM222_RespBuffer[BufCnt - 9];
		BTM222_DeviceName[7] = BTM222_RespBuffer[BufCnt - 8];
		BTM222_DeviceName[8] = BTM222_RespBuffer[BufCnt - 7];
		BTM222_DeviceName[9] = BTM222_RespBuffer[BufCnt - 6];
		BTM222_DeviceName[10] = BTM222_RespBuffer[BufCnt - 5];
		BTM222_DeviceName[11] = BTM222_RespBuffer[BufCnt - 4];
		BTM222_DeviceName[12] = BTM222_RespBuffer[BufCnt - 3];
		BTM222_DeviceName[13] = BTM222_RespBuffer[BufCnt - 2];
		BTM222_DeviceName[14] = BTM222_RespBuffer[BufCnt - 1];
		BTM222_DeviceName[15] = '\0';	/* add termination of a string */

	    }

	    else

		BTM222_DeviceNameFlag = pdFALSE;

	}
	BTM222_RespBuffer[BufCnt++] = c;

    }

    else

	BTM222_RespBuffer[BufCnt++] = c;

}

/*---------------------------------------------------------------------------*/

typedef enum {
    RX_STATE_IDLE = 0, RX_STATE_CMD, RX_STATE_CRLF
} E_RX_STATE;

/*---------------------------------------------------------------------------*/

void sendMemLoc(uint32_t * ptr)
{

    printLF();

    printser_string("Address [0x");

    printser_uint32ToHex((uint32_t) ptr);

    printser_string("] = Value 0x");

    printser_uint32ToHex(&ptr);

}

/*---------------------------------------------------------------------------*/

void sendCPUInfo()
{

    printLF();

    printser_string("CPU ID: 0x");

    printser_uint32ToHex(SCB->CPUID);

}

/*---------------------------------------------------------------------------*/

void sendRomTable()
{

    uint32_t *pRomTable = (uint32_t *) 0xE00FF000;

    while (*pRomTable != 0) {
	sendMemLoc(pRomTable++);
    }

    sendMemLoc(pRomTable);

}

/*---------------------------------------------------------------------------*/

void USART1_IRQHandler(void)
{

    DEBUGUARTPRINT("\r\n*** USART1_IRQHandler starting ***");

    extern xQueueHandle internalSerialRxQueue;

    char ch;

    static portBASE_TYPE xHigherPriorityTaskWoken = pdFALSE;

    /* Check for received Data */
    if (USART_GetITStatus(USART1, USART_IT_RXNE) != RESET) {

	ch = USART_ReceiveData(USART1);

	if (BTM222_UART_Rx_Flag == pdFALSE) {	/* check for BTM222 communication during powerup */
	    BTM222_Rx_getc(ch);
	}

	else {

	    if (pdPASS ==
		xQueueSendToBackFromISR(internalSerialRxQueue, &ch,
					&xHigherPriorityTaskWoken)) {

		DEBUGUARTPRINT
		    ("\r\n*** internalSerialRxQueue Zeichen geschrieben ***");

		/* Switch context if necessary */
		if (xHigherPriorityTaskWoken) {
		    taskYIELD();
		}
	    }

	    else {
		DEBUGUARTPRINT
		    ("\r\n*** internalSerialRxQueue Zeichen NICHT geschrieben ***");
	    }
	}

    }

    DEBUGUARTPRINT("\r\n*** USART1_IRQHandler finished ***");

}

/*---------------------------------------------------------------------------*/
