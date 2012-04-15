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

// STM headers
#include "stm32f10x.h"		/* ST Library v3.4..0 specific header files */
#include "SystemConfig.h"	/* STM32 hardware specific header file */

extern char *oobd_Error_Text_OS;


void printParam_sys_specific(portBASE_TYPE msgType, void *data,
			     printChar_cbf printchar)
{
    param_data *args;
    args = data;
    extern uint8_t BTM222_BtAddress[];
    extern unsigned char BTM222_DeviceName[];
    extern unsigned char BTM222_UartSpeed;

    DEBUGPRINT("sys specific parameter received: %ld / %ld\n",
	       args->args[ARG_CMD], args->args[ARG_VALUE_1]);
    switch (args->args[ARG_RCV]) {
    case FBID_SYS_GENERIC:
    switch (args->args[ARG_CMD]) {
    case PARAM_INFO:
	switch (args->args[ARG_VALUE_1]) {

	case VALUE_PARAM_INFO_VERSION:	
	    printser_string("OOBD ");
	    printser_string(OOBDDESIGN);
	    printser_string(" ");
	    printser_string(SVNREV);
	    printser_string(" ");
	    if (GPIO_HardwareLevel() > 0) {
		printser_string("Lux-Wolf");
	    } else {
		printser_string("dxm");
	    }
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
	default:
	    evalResult(FBID_SYS_SPEC, ERR_CODE_OS_UNKNOWN_COMMAND_TEXT, 0,
		       ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
	    break;
	}
	break;

    default:
	break;
    }
    break;
    case FBID_SYS_SPEC:
    switch (args->args[ARG_CMD]) {
    case PARAM_INFO:
	switch (args->args[ARG_VALUE_1]) {

	case VALUE_PARAM_INFO_ADC_POWER:	
	    printser_int((readADC1(8) * (3.15 / 4096)) * 10000, 10);	/* result in mV */
	    printser_string(" mV");
	    printLF();
	    printEOT();
	    break;
	case VALUE_PARAM_INFO_CPU_INFO:	
	    sendCPUInfo();	/* send CPU Info */
	    printLF();
	    printEOT();
	    break;
	case VALUE_PARAM_INFO_MEM_LOC:	
	    sendMemLoc(0x8002400);	/* send Mem Location */
	    printLF();
	    printEOT();
	    break;
	case VALUE_PARAM_INFO_ROM_TABLE_LOC:	
	    sendRomTable();	/* send ROM Table */
	    printLF();
	    printEOT();
	    break;
	case VALUE_PARAM_INFO_FREE_HEAP_SIZE:	
	    printser_string("Total Heap (in byte): ");
	    printser_int(configTOTAL_HEAP_SIZE, 10);
	    printser_string("Free Heap (in byte): ");
	    printser_int(xPortGetFreeHeapSize(), 10);	/* send FreeRTOS free heap size */
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

	default:
	    evalResult(FBID_SYS_SPEC, ERR_CODE_OS_UNKNOWN_COMMAND_TEXT, 0,
		       ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
	    break;
	}
	break;

    default:
	break;
    }
    break;
    default:
	break;
    }
}

portBASE_TYPE eval_param_sys_specific(param_data * args)
{
    switch (args->args[ARG_CMD]) {
    case PARAM_INFO:
	CreateParamOutputMsg(args, printParam_sys_specific);
	return pdTRUE;
	break;

    case PARAM_SET_OUTPUT:
	if (sysIoCtrl(args->args[ARG_VALUE_1], 0,
		      args->args[ARG_VALUE_2], 0, 0) == pdTRUE) {
	    createCommandResultMsg(FBID_SYS_SPEC, ERR_CODE_NO_ERR, 0,
				   NULL);
	    return pdTRUE;
	} else {
	    createCommandResultMsg(FBID_SYS_SPEC,
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED, 0,
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
	    return pdFALSE;
	}
	return pdTRUE;
	break;
    case PARAM_RESET:
	mc_init_sys_shutdown_specific();	// Reset
	return pdTRUE;
	break;


    default:
	createCommandResultMsg(FBID_SYS_SPEC, ERR_CODE_OS_UNKNOWN_COMMAND, 0,
		   ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
	return pdFALSE;
    }
}

void mc_init_sys_boot_specific()
{
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

void mc_init_sys_tasks_specific()
{
    DEBUGPRINT("init system tasks\n", 'a');

#ifdef EEPROM_READ_WRITE_EXAMPLE

    uint8_t Tx1_Buffer[] = "EEPROM Check";	/* Page Write supports max. 16 bytes */
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

void mc_init_sys_shutdown_specific()
{
    DEBUGPRINT("shutdown systems\n", 'a');
    SCB->AIRCR = 0x05FA0604;	/* soft reset */
}



portBASE_TYPE sysIoCtrl(portBASE_TYPE pinID, portBASE_TYPE lowerValue,
			portBASE_TYPE upperValue, portBASE_TYPE duration,
			portBASE_TYPE waveType)
{
    DEBUGPRINT("Pin: %ld to value %ld\n", pinID, upperValue);
    switch (pinID) {
    case IO_LED_WHITE:
	DEBUGPRINT("IO_LED_WHITE set to %ld\n", upperValue);
	if (GPIO_HardwareLevel() == 1) {
	    upperValue ? GPIO_SetBits(GPIOB, GPIO_Pin_10) : GPIO_ResetBits(GPIOB, GPIO_Pin_10);	/* LED1 - yellow  */
	} else {
	    upperValue ? GPIO_SetBits(GPIOB, GPIO_Pin_4) : GPIO_ResetBits(GPIOB, GPIO_Pin_4);	/* LED2 - green */
	}
	return pdTRUE;
	break;
    case IO_LED_GREEN:
	DEBUGPRINT("IO_LED_GREEN set to %ld\n", upperValue);
	if (GPIO_HardwareLevel() == 1) {
	    upperValue ? GPIO_SetBits(GPIOB, GPIO_Pin_4) : GPIO_ResetBits(GPIOB, GPIO_Pin_4);	/* Duo-LED2gr - green */
	} else {
	    upperValue ? GPIO_SetBits(GPIOB, GPIO_Pin_4) : GPIO_ResetBits(GPIOB, GPIO_Pin_4);	/* LED2 - green */
	}
	return pdTRUE;
	break;
    case IO_LED_RED:
	DEBUGPRINT("IO_LED_RED set to %ld\n", upperValue);
	if (GPIO_HardwareLevel() == 1) {
	    upperValue ? GPIO_SetBits(GPIOB, GPIO_Pin_5) : GPIO_ResetBits(GPIOB, GPIO_Pin_5);	/* Duo-LED2rd - red */
	} else {
	    upperValue ? GPIO_SetBits(GPIOB, GPIO_Pin_5) : GPIO_ResetBits(GPIOB, GPIO_Pin_5);	/* LED1 - red */
	}
	return pdTRUE;
	break;


    case IO_REL1:
	if (GPIO_HardwareLevel() == 1) {
	    upperValue ? GPIO_SetBits(GPIOC, GPIO_Pin_15) : GPIO_ResetBits(GPIOC, GPIO_Pin_15);	/* Rel1 */
	    DEBUGPRINT("IO_REL1 set to %ld\n", upperValue);
	    return pdTRUE;
	} else {
	    return pdFALSE;
	}
	break;

    case IO_BUZZER:
	if (GPIO_HardwareLevel() == 1) {
	    sysSound(upperValue, portMAX_DELAY);	/* Buzzer, full volume */
	    DEBUGPRINT("Buzzer set to frequency of %ld\n", upperValue);
	    return pdTRUE;
	} else {
	    return pdFALSE;
	}
    default:
	DEBUGPRINT("unknown output pin\n", upperValue);
	return pdFALSE;
	break;
    }
}


portBASE_TYPE sysSound(portBASE_TYPE frequency, portBASE_TYPE volume)
{
    frequency ? TIM_Cmd(TIM2, ENABLE) : TIM_Cmd(TIM2, DISABLE);	/* Buzzer */
}



/*---------------------------------------------------------------------------*/

uint16_t readADC1(uint8_t channel)
{
    ADC_RegularChannelConfig(ADC1, channel, 1, ADC_SampleTime_1Cycles5);
    /* Start the conversion */
    ADC_SoftwareStartConvCmd(ADC1, ENABLE);
    /* Wait until conversion completion */
    while (ADC_GetFlagStatus(ADC1, ADC_FLAG_EOC) == RESET);
    /* Get the conversion value */
    return ADC_GetConversionValue(ADC1);
}

/*---------------------------------------------------------------------------*/

extern uint32_t _edata[], _etext[], _sdata[];
uint32_t CheckCrc32(void)
{
    uint32_t size;
    uint32_t crc;
    RCC_AHBPeriphClockCmd(RCC_AHBPeriph_CRC, ENABLE);

    /* size is the calculation result of the linker minus application start address offset */
    size = (uint32_t) ((uint32_t) & _etext + (uint32_t) & _edata
		       - (uint32_t) & _sdata) - 0x8002400;
    CRC_ResetDR();
    /* 0x8002400 is the application start address and size = application code size */
    crc = CRC_CalcBlockCRC((uint32_t *) 0x8002400, size / 4 + 1);
    RCC_AHBPeriphClockCmd(RCC_AHBPeriph_CRC, DISABLE);

    return crc;
}

void mc_sys_idlehook()
{
    /* The co-routines are executed in the idle task using the idle task hook. */
    //  vCoRoutineSchedule();        /* Comment this out if not using Co-routines. */
}
