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
 * MC specific system routines
 */

/* OOBD headers. */
#include "od_config.h"
#include "od_base.h"
#include "mc_sys_generic.h"
#include "mc_sys.h"
#include "mc_serial.h"

// STM headers
#include "stm32f10x.h"		/* ST Library v3.5.0 specific header files */
#include "SystemConfig.h"	/* STM32 hardware specific header file */

extern char *oobd_Error_Text_OS;

portBASE_TYPE mc_sys_get_startupProtocol() {
	return VALUE_PARAM_PROTOCOL_CAN_UDS;
}

portBASE_TYPE mc_sys_get_startupBus() {

	return ODB_CAN;
}

void printParam_sys_specific(portBASE_TYPE msgType, void *data,
		printChar_cbf printchar) {
	param_data *args;
	args = data;
	extern uint8_t BTM222_BtAddress[];
	extern unsigned char BTM222_DeviceName[];
	extern unsigned char BTM222_UartSpeed;

	DEBUGPRINT("sys specific parameter received: %ld / %ld\n",
			args->args[ARG_CMD], args->args[ARG_VALUE_1]);
	switch (args->args[ARG_CMD]) {
	case PARAM_INFO:
		switch (args->args[ARG_VALUE_1]) {

		case VALUE_PARAM_INFO_VERSION:
			printser_string("OOBD ");
			if (GPIO_HardwareLevel() == 1 || GPIO_HardwareLevel() == 4
					|| GPIO_HardwareLevel() == 5)
				printser_string("D2a");
			else
				printser_string("??");
			printser_string(" ");
			printser_string(SVNREV);
			printser_string(" ");
			if (GPIO_HardwareLevel() == 1)
				printser_string("dxm");
			else if (GPIO_HardwareLevel() == 4 || GPIO_HardwareLevel() == 5)
				printser_string("Lux-Wolf");
			else
				printser_string("unknown-HW");
			printser_string(" ");
			if (GPIO_HardwareLevel() == 1)
				printser_string("dxm");
			else if (GPIO_HardwareLevel() == 4)
				printser_string("Lux-Wolf");
			else if (GPIO_HardwareLevel() == 5)
				printser_string("CAN-Invader");
			else
				printser_string("FL?");
			printser_string(" ");
			printser_string(BUILDDATE);
			printLF();
			printEOT();
			break;

		case VALUE_PARAM_INFO_SERIALNUMBER:
			printser_string(BTM222_BtAddress);
			printLF();
			printEOT();
			break;
		case VALUE_PARAM_INFO_ADC_POWER:
			if (GPIO_HardwareLevel() == 4 || GPIO_HardwareLevel() == 1) /* OOBD Cup v5, R1=200k, R2=18k */
				printser_int((readADC1(8) * (3.15 / 4096) * 11 * 1000), 10); /* result in mV */
			else if (GPIO_HardwareLevel() == 5) /* OOBD CAN Invader, R1=130k, R2=27k */
				printser_int((readADC1(8) * (3.3 / 4096) * 5 * 1000), 10); /* result in mV */
			else
				printser_string("0"); /* result in mV */

			printser_string(" mV");
			printLF();
			printEOT();
			break;
		case VALUE_PARAM_INFO_CPU_INFO:
			sendCPUInfo(); /* send CPU Info */
			printLF();
			printEOT();
			break;
		case VALUE_PARAM_INFO_MEM_LOC:
			sendMemLoc(args[ARG_VALUE_2]); /* send Mem Location */
			printLF();
			printEOT();
			break;
		case VALUE_PARAM_INFO_ROM_TABLE_LOC:
			sendRomTable(); /* send ROM Table */
			printLF();
			printEOT();
			break;
		case VALUE_PARAM_INFO_FREE_HEAP_SIZE:
			printser_string("Total Heap (in byte): ");
			printser_int(configTOTAL_HEAP_SIZE, 10);
			printser_string("Free Heap (in byte): ");
			printser_int(xPortGetFreeHeapSize(), 10); /* send FreeRTOS free heap size */
			printLF();
			printEOT();
			break;
		case VALUE_PARAM_INFO_CRC32:
			if (CheckCrc32() == 0) {
				printser_string("CRC-32 application check passed!");
			} else {
				printser_string("CRC-32 application check failed");
			}
			printLF();
			printEOT();
			break;
		case VALUE_PARAM_INFO_BTM222_DEVICENAME:
			printser_string(BTM222_DeviceName);
			printLF();
			printEOT();
			break;
		case VALUE_PARAM_INFO_BTM222_UART_SPEED:
			switch (BTM222_UartSpeed) {
			case '0':
				printser_string("4800 bit/s");
				break;

			case '1':
				printser_string("9600 bit/s");
				break;

			case '2':
				printser_string("19200 bit/s");
				break;

			case '3':
				printser_string("38400 bit/s");
				break;

			case '4':
				printser_string("57600 bit/s");
				break;

			case '5':
				printser_string("115200 bit/s");
				break;

			case '6':
				printser_string("230400 bit/s");
				break;

			case '7':
				printser_string("460800 bit/s");
				break;

			default:
				printser_string("not detected");
				break;
			}
			printLF();
			printEOT();
			break;

		case VALUE_PARAM_INFO_KLINE_FAST_INIT:
			/* K-Line High for 300ms */
			GPIO_ResetBits(GPIOA, GPIO_Pin_2);
			vTaskDelay(300 / portTICK_RATE_MS);
			/* K-Line Low for 25ms */
			GPIO_SetBits(GPIOA, GPIO_Pin_2);
			vTaskDelay(25 / portTICK_RATE_MS);
			/* K-Line High for 25ms */
			GPIO_ResetBits(GPIOA, GPIO_Pin_2);
			vTaskDelay(25 / portTICK_RATE_MS);
			/* init UART2 to 10k4 baud, 8N1 */
			USART2_Configuration();

			/* send start communication request */
			USART_SendData(USART2, 0xc1);
			USART_SendData(USART2, 0x33);
			USART_SendData(USART2, 0xf1);
			USART_SendData(USART2, 0x81);
			USART_SendData(USART2, 0x66);

			printser_string("K-Line Fast Init completed!");
			printLF();
			printEOT();
			break;

		case VALUE_PARAM_INFO_KLINE:
			if (GPIO_ReadInputDataBit(GPIOA, GPIO_Pin_3) == Bit_SET)
				printser_string("active - low");
			else
				printser_string("inactive - high");
			printLF();
			printEOT();
			break;

		case VALUE_PARAM_INFO_LLINE:
			if (GPIO_ReadInputDataBit(GPIOA, GPIO_Pin_4) == Bit_SET)
				printser_string("active - low");
			else
				printser_string("inactive - high");
			printLF();
			printEOT();
			break;

		case VALUE_PARAM_INFO_KLINE_TX:
			if (GPIO_ReadInputDataBit(GPIOA, GPIO_Pin_2) == Bit_SET)
				printser_string("active - Receive mode");
			else
				printser_string("inactive - Transmit mode");
			printLF();
			printEOT();
			break;

		default:
			evalResult(FBID_SYS_SPEC, ERR_CODE_OS_UNKNOWN_COMMAND_TEXT, 0,
					ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
			break;
		}
	}
}

/* evaluation of p 0 x x x commands */
portBASE_TYPE eval_param_sys_specific(
		param_data * args) {

	uint32_t BTMpin;


	switch (args->args[ARG_CMD]) {
	case PARAM_INFO:
		CreateParamOutputMsg(args, printParam_sys_specific);
		return pdTRUE;
		break;

	case PARAM_SET_OUTPUT:
		if (sysIoCtrl(args->args[ARG_VALUE_1], 0, args->args[ARG_VALUE_2], 0, 0)
				== pdTRUE) {
			createCommandResultMsg(FBID_SYS_SPEC, ERR_CODE_NO_ERR, 0, NULL);
			return pdTRUE;
		} else {
			createCommandResultMsg(FBID_SYS_SPEC,
					ERR_CODE_OS_COMMAND_NOT_SUPPORTED, 0,
					ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
			return pdFALSE;
		}
		return pdTRUE;
		break;

	case PARAM_SET_BTM:
		if (args->args[ARG_VALUE_2] > 99999999) {
			createCommandResultMsg(FBID_SYS_SPEC, ERR_CODE_OS_UNKNOWN_COMMAND,
					0, ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
			return pdFALSE;
		}


		sysIoCtrl(6, 0, 0, 0, 0); /* Reset BT-Module */
		delay_ms(250);
		sysIoCtrl(6, 0, 1, 0, 0); /* Release Reset-Pin of BT-Module */
		delay_ms(2500);

		switch (args->args[ARG_VALUE_1]) {
		case BTM_PIN:

			BTM222_UART_Rx_Flag = pdFALSE;
			USART_SendData(USART1, '\r');
			btm_uart_delay_ms(100);
			USART_SendData(USART1, 'a');
			btm_uart_delay_ms(40);
			USART_SendData(USART1, 't');
			btm_uart_delay_ms(40);
			USART_SendData(USART1, '\r');
			btm_uart_delay_ms(100);

			USART_SendData(USART1, 'a');
			btm_uart_delay_ms(40);
			USART_SendData(USART1, 't');
			btm_uart_delay_ms(40);
			USART_SendData(USART1, 'p');
			btm_uart_delay_ms(40);
			USART_SendData(USART1, '=');
			btm_uart_delay_ms(40);

			BTMpin = 10000000;

			/* transmit PIN number from input stream to BT-Module */
			while ( 0 != BTMpin )
			{
				/* check if value is available on current position */
				/* BT-Module supports only character 0-9 with 4-8 digit for the BT-PIN */
				if ( 0 != args->args[ARG_VALUE_2] / BTMpin)
					USART_SendData(USART1, (args->args[ARG_VALUE_2] % (BTMpin*10) / BTMpin) + 48);
				BTMpin = BTMpin / 10;
				btm_uart_delay_ms(40);
			}

			USART_SendData(USART1, '\r');
			btm_uart_delay_ms(200);

			sysIoCtrl(6, 0, 0, 0, 0); /* Reset BT-Module */
			delay_ms(250);
			sysIoCtrl(6, 0, 1, 0, 0); /* Release Reset-Pin of BT-Module */
			delay_ms(500);
			printEOT();
			BTM222_UART_Rx_Flag = pdTRUE;
			return pdTRUE;
			break;

		default:
			createCommandResultMsg(FBID_SYS_SPEC, ERR_CODE_OS_UNKNOWN_COMMAND,
					0, ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
			return pdFALSE;
		}
		return pdTRUE;
		break;

	case PARAM_RESET:
		mc_init_sys_shutdown_specific(); // Reset
		return pdTRUE;
		break;

	case PARAM_SET_PROTOCOL_AND_BUS_DEFAULT:
		createCommandResultMsg(FBID_SYS_SPEC,
				ERR_CODE_OS_COMMAND_NOT_SUPPORTED, 0,
				ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
		return pdFALSE;

	default:
		createCommandResultMsg(FBID_SYS_SPEC, ERR_CODE_OS_UNKNOWN_COMMAND, 0,
				ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
		return pdFALSE;
	}
}

void mc_init_sys_boot_specific() {
	DEBUGPRINT("boot mc specific system\n", 'a');
	/*!< At this stage the microcontroller clock setting is already configured,
	 this is done through SystemInit() function which is called from startup
	 file (startup_stm32f10x_xx.s) before to branch to application main.
	 To reconfigure the default setting of SystemInit() function, refer to
	 system_stm32f10x.c file
	 */

	/* Buffer of data to be received by I2C1 */
	/*  uint8_t Buffer_Rx1[255]; */

	/* SystemInit(); *//* not needed as SystemInit() is called from startup */
	/* Initialize DXM1 hardware, i.e. GPIO, CAN, USART1 */
	System_Configuration();

	/* Initialize the I2C EEPROM driver ---------------------------------------- */
	sEE_Init();

}

void mc_init_sys_tasks_specific() {
	DEBUGPRINT("init system tasks\n", 'a');

#ifdef EEPROM_READ_WRITE_EXAMPLE

	uint8_t Tx1_Buffer[] = "EEPROM Check"; /* Page Write supports max. 16 bytes */
#define sEE_WRITE_ADDRESS1        0x00
#define sEE_READ_ADDRESS1         0x00
#define countof(a) (sizeof(a) / sizeof(*(a)))
#define BUFFER_SIZE1             (countof(Tx1_Buffer)-1)
	volatile uint16_t NumDataRead = 0;
	uint8_t Rx1_Buffer[BUFFER_SIZE1];

	printser_string("\r\nShow Tx1_buffer:");
	printser_string(Tx1_Buffer);

	/* First write in the memory followed by a read of the written data -------- */
	/* Write on I2C EEPROM from sEE_WRITE_ADDRESS1 */
	sEE_WriteBuffer(Tx1_Buffer, sEE_WRITE_ADDRESS1, BUFFER_SIZE1);

	/* Set the Number of data to be read */
	NumDataRead = BUFFER_SIZE1;

	/* Read from I2C EEPROM from sEE_READ_ADDRESS1 */
	sEE_ReadBuffer(Rx1_Buffer, sEE_READ_ADDRESS1,
			(uint16_t *) (&NumDataRead));

	/* Wait till DMA transfer is complete (Transfer complete interrupt handler
	 resets the variable holding the number of data to be read) */
	while (NumDataRead > 0) {
		/* Starting from this point, if the requested number of data is higher than 1,
		 then only the DMA is managing the data transfer. Meanwhile, CPU is free to
		 perform other tasks:

		 // Add your code here:
		 //...
		 //...

		 For simplicity reasons, this example is just waiting till the end of the
		 transfer. */
	}

	printser_string("\r\nShow Rx1_buffer:");
	printser_string(Rx1_Buffer);
#endif

	/* initialize Interrupt Vector table and activate interrupts */
	NVIC_Configuration();

}

void mc_init_sys_shutdown_specific() {
	DEBUGPRINT("shutdown systems\n", 'a');
	SCB->AIRCR = 0x05FA0604; /* soft reset */
}

portBASE_TYPE sysIoCtrl(portBASE_TYPE pinID, portBASE_TYPE lowerValue,
		portBASE_TYPE upperValue, portBASE_TYPE duration,
		portBASE_TYPE waveType) {
	DEBUGPRINT("Pin: %ld to value %ld\n", pinID, upperValue);
	switch (pinID) {
	case IO_LED_WHITE:
		DEBUGPRINT("IO_LED_BLUE set to %ld\n", upperValue);
		if (GPIO_HardwareLevel() == 4) { /* OOBD Cup v5 */
			upperValue ? GPIO_SetBits(GPIOB, GPIO_Pin_10) : GPIO_ResetBits(
					GPIOB, GPIO_Pin_10); /* LED1 - yellow  */
		} else if (GPIO_HardwareLevel() == 5) { /* OOBD CAN Invader */
			upperValue ? GPIO_SetBits(GPIOA, GPIO_Pin_7) : GPIO_ResetBits(
					GPIOA, GPIO_Pin_7); /* LED1 - yellow  */
		} else { /* DXM1 */
			upperValue ? GPIO_SetBits(GPIOB, GPIO_Pin_4) : GPIO_ResetBits(
					GPIOB, GPIO_Pin_4); /* LED2 - green */
		}
		return pdTRUE;
		break;
	case IO_LED_GREEN:
		DEBUGPRINT("IO_LED_GREEN set to %ld\n", upperValue);
		if (GPIO_HardwareLevel() == 4 || GPIO_HardwareLevel() == 5) { /* OOBD-Cup v5 or OOBD CAN Invader */
			upperValue ? GPIO_SetBits(GPIOB, GPIO_Pin_4) : GPIO_ResetBits(
					GPIOB, GPIO_Pin_4); /* Duo-LED2gr - green */
		} else if (GPIO_HardwareLevel() == 1) { /* DXM1 */
			upperValue ? GPIO_SetBits(GPIOB, GPIO_Pin_4) : GPIO_ResetBits(
					GPIOB, GPIO_Pin_4); /* LED2 - green */
		}
		return pdTRUE;
		break;
	case IO_LED_RED:
		DEBUGPRINT("IO_LED_RED set to %ld\n", upperValue);
		if (GPIO_HardwareLevel() == 4 || GPIO_HardwareLevel() == 5) { /* OOBD-Cup v5 or OOBD CAN Invader */
			upperValue ? GPIO_SetBits(GPIOB, GPIO_Pin_5) : GPIO_ResetBits(
					GPIOB, GPIO_Pin_5); /* Duo-LED2rd - red */
		} else if (GPIO_HardwareLevel() == 1) { /* DXM1 */
			upperValue ? GPIO_SetBits(GPIOB, GPIO_Pin_5) : GPIO_ResetBits(
					GPIOB, GPIO_Pin_5); /* LED1 - red */
		}
		return pdTRUE;
		break;

	case IO_REL1:
		if (GPIO_HardwareLevel() == 4) { /* OOBD-Cup v5 */
			upperValue ? GPIO_SetBits(GPIOC, GPIO_Pin_15) : GPIO_ResetBits(
					GPIOC, GPIO_Pin_15); /* Rel1 */
			DEBUGPRINT("IO_REL1 set to %ld\n", upperValue);
			return pdTRUE;
		} else if (GPIO_HardwareLevel() == 5) { /* OOBD CAN Invader */
			upperValue ? GPIO_SetBits(GPIOB, GPIO_Pin_3) : GPIO_ResetBits(
					GPIOB, GPIO_Pin_3); /* Rel1 */
			DEBUGPRINT("IO_REL1 set to %ld\n", upperValue);
			return pdTRUE;
		} else {
			return pdFALSE;
		}
		break;

	case IO_BUZZER:
		if (GPIO_HardwareLevel() == 4 || GPIO_HardwareLevel() == 5) { /* OOBD-Cup v5 or OOBD CAN Invader */
			sysSound(upperValue, portMAX_DELAY); /* Buzzer, full volume */
			DEBUGPRINT("Buzzer set to frequency of %ld\n", upperValue);
			return pdTRUE;
		} else {
			return pdFALSE;
		}
		break;

	case IO_KLINE:
		upperValue ? GPIO_SetBits(GPIOA, GPIO_Pin_2) : GPIO_ResetBits(GPIOA,
				GPIO_Pin_2); /* set K-Line */
		return pdTRUE;
		break;

	case IO_LLINE:
		upperValue ? GPIO_SetBits(GPIOA, GPIO_Pin_4) : GPIO_ResetBits(GPIOA,
				GPIO_Pin_4); /* set L-Line */
		return pdTRUE;
		break;

	case IO_BTM_RESET:
		if (GPIO_HardwareLevel() == 4) /* OOBD-Cup v5 */
			upperValue ? GPIO_SetBits(GPIOC, GPIO_Pin_13) : GPIO_ResetBits(
					GPIOC, GPIO_Pin_13); /* reset BT-Module */
		else if (GPIO_HardwareLevel() == 5) /* OOBD CAN Invader */
			upperValue ? GPIO_SetBits(GPIOA, GPIO_Pin_15) : GPIO_ResetBits(
					GPIOA, GPIO_Pin_15); /* reset BT-Module */
		return pdTRUE;
		break;

	default:
		DEBUGPRINT("unknown output pin\n", upperValue);
		return pdFALSE;
		break;
	}
}

portBASE_TYPE sysSound(portBASE_TYPE frequency, portBASE_TYPE volume) {

	/* if frequency=0 => disable buzzer, otherwise enable buzzer with frequency */
	if (GPIO_HardwareLevel() == 4) { /* OOBD-Cup v5 */
		if (frequency != 0)
			TIM2_Configuration(frequency);

		frequency ? TIM_Cmd(TIM2, ENABLE) : TIM_Cmd(TIM2, DISABLE); /* Buzzer */
	} else if (GPIO_HardwareLevel() == 5) { /* OOBD CAN Invader */
		if (frequency != 0)
			TIM3_Configuration(frequency);

		frequency ? TIM_Cmd(TIM3, ENABLE) : TIM_Cmd(TIM3, DISABLE); /* Buzzer */
	}

}

/*---------------------------------------------------------------------------*/

uint16_t readADC1(uint8_t channel) {
	ADC_RegularChannelConfig(ADC1, channel, 1, ADC_SampleTime_1Cycles5);
	/* Start the conversion */
	ADC_SoftwareStartConvCmd(ADC1, ENABLE);
	/* Wait until conversion completion */
	while (ADC_GetFlagStatus(ADC1, ADC_FLAG_EOC) == RESET)
		;
	/* Get the conversion value */
	return ADC_GetConversionValue(ADC1);
}

/*---------------------------------------------------------------------------*/

extern uint32_t _edata[], _etext[], _sdata[];
uint32_t CheckCrc32(void) {
	uint32_t size;
	uint32_t crc;
	RCC_AHBPeriphClockCmd(RCC_AHBPeriph_CRC, ENABLE);

	/* size is the calculation result of the linker minus application start address offset */
	size = (uint32_t) ((uint32_t) &_etext + (uint32_t) &_edata
			- (uint32_t) &_sdata) - 0x8002500;
	CRC_ResetDR();
	/* 0x8002500 is the application start address and size = application code size */
	crc = CRC_CalcBlockCRC((uint32_t *) 0x8002500, size / 4 + 1);
	RCC_AHBPeriphClockCmd(RCC_AHBPeriph_CRC, DISABLE);

	return crc;
}

void mc_sys_idlehook() {
	/* The co-routines are executed in the idle task using the idle task hook. */
	//  vCoRoutineSchedule();        /* Comment this out if not using Co-routines. */
}
