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


void mc_init_sys_boot (){
  DEBUGPRINT ("boot system\n", 'a');
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
  System_Configuration ();

  /* Initialize the I2C EEPROM driver ----------------------------------------*/
  sEE_Init();

}



void
printParam_sys (portBASE_TYPE msgType, void *data, printChar_cbf printchar)
{
  static param_data *pd;
  pd = data;
  portBASE_TYPE cmdKey = pd->key, cmdValue = pd->value;	/* the both possible params */
  switch (cmdKey) {
      case PARAM_INFO:
	  // \bug Clean up!
	  /*
	    *  case statt elseif -ketten
	    * Ausgaben syncronisiert statt printser
	    *  can- Ausgaben in den CAN-Handler verschieben
	    *  Mc-spezische Ausgaben in mc-spezifischen Code verschieben
	    */
	  eval_param_sys(cmdKey, cmdValue);
	  switch (cmdValue) {

	  case VALUE_PARAM_INFO_VERSION:	/* p 0 0 */
	      printser_string("OOBD ");
	      printser_string(OOBDDESIGN);
	      printser_string(" ");
	      printser_string(SVNREV);
	      printser_string(" ");
	      printser_string(BUILDDATE);
	      break;
	  case VALUE_PARAM_INFO_SERIALNUMBER:	/* p 0 1 */
	      printser_string(BTM222_BtAddress);
	      break;
	  case VALUE_PARAM_INFO_PROTOCOL:	/* p 0 3 */
	      printser_string
		  ("1 - UDS (ISO14229-1)");
	      break;
	  case VALUE_PARAM_INFO_BUS_MODE:	/* p 0 4 */
	      switch (config.bus) {
	      case VALUE_BUS_SILENT_MODE:
		  printser_string
		      ("0 - CAN Transceiver in 'Silent Mode'");
		  break;
	      case VALUE_BUS_LOOP_BACK_MODE:
		  printser_string
		      ("1 - CAN Transceiver in 'Loop Back Mode'");
		  break;
	      case VALUE_BUS_LOOP_BACK_WITH_SILENT_MODE:
		  printser_string
		      ("2 - CAN Transceiver in 'Loop Back combined with Silent Mode'");
		  break;
	      case VALUE_BUS_NORMAL_MODE:
		  printser_string
		      ("3 - CAN Transceiver in 'Normal Mode'");
		  break;
	      }
	      break;
	  case VALUE_PARAM_INFO_BUS_CONFIG:	/* p 0 5 */
	      switch (config.busConfig) {
	      case VALUE_BUS_CONFIG_11bit_125kbit:
		  printser_string
		      ("1 = ISO 15765-4, CAN 11bit ID/125kBaud");
		  break;
	      case VALUE_BUS_CONFIG_11bit_250kbit:
		  printser_string
		      ("2 = ISO 15765-4, CAN 11bit ID/250kBaud");
		  break;
	      case VALUE_BUS_CONFIG_11bit_500kbit:
		  printser_string
		      ("3 = ISO 15765-4, CAN 11bit ID/500kBaud");
		  break;
	      case VALUE_BUS_CONFIG_11bit_1000kbit:
		  printser_string
		      ("4 - ISO 15765-4, CAN 11bit ID/1000kBaud");
		  break;
	      case VALUE_BUS_CONFIG_29bit_125kbit:
		  printser_string
		      ("5 - ISO 15765-4, CAN 29bit ID/125kBaud");
		  break;
	      case VALUE_BUS_CONFIG_29bit_250kbit:
		  printser_string
		      ("6 - ISO 15765-4, CAN 29bit ID/250kBaud");
		  break;
	      case VALUE_BUS_CONFIG_29bit_500kbit:
		  printser_string
		      ("7 - ISO 15765-4, CAN 29bit ID/500kBaud");
		  break;
	      case VALUE_BUS_CONFIG_29bit_1000kbit:
		  printser_string
		      ("8 - ISO 15765-4, CAN 29bit ID/1000kBaud");
		  break;
	      }
#ifdef OOBD_PLATFORM_STM32
	  case VALUE_PARAM_INFO_ADC_POWER:	/* p 0 6 */
	      printser_int((readADC1(8) * (3.15 / 4096)) * 10000, 10);	/* result in mV */
	      printser_string(" mV");
	      break;
	  case VALUE_PARAM_INFO_CPU_INFO:	/* p 0 10 */
	      sendCPUInfo();	/* send CPU Info */
	      break;
	  case VALUE_PARAM_INFO_MEM_LOC:	/* p 0 11 */
	      sendMemLoc(0x8002400);	/* send Mem Location */
	      break;
	  case VALUE_PARAM_INFO_ROM_TABLE_LOC:	/* p 0 12 */
	      sendRomTable();	/* send ROM Table */
	      break;
	  case VALUE_PARAM_INFO_FREE_HEAP_SIZE:	/* p 0 13 */
	      printser_string
		  ("Total Heap (in byte): ");
	      printser_int(configTOTAL_HEAP_SIZE,
			    10);
	      printser_string
		  ("Free Heap (in byte): ");
	      printser_int(xPortGetFreeHeapSize(), 10);	/* send FreeRTOS free heap size */
	      break;
	  case VALUE_PARAM_INFO_CRC32:	/* p 0 14 */
	      if (CheckCrc32() == 0) {
		  printser_string
		      ("CRC-32 application check passed!");
	      } else {
		  printser_string
		      ("CRC-32 application check failed");
	      }
	  case VALUE_PARAM_INFO_BTM222_DEVICENAME:	/* p 0 20 */
	      printser_string
		  (BTM222_DeviceName);
	      break;
	  case VALUE_PARAM_INFO_BTM222_UART_SPEED:	/* p 0 21 */
	      switch (BTM222_UartSpeed)
	      {
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
		  printser_string
		      ("115200 bit/s");
		  break;

	      case '6':
		  printser_string
		      ("230400 bit/s");
		  break;

	      case '7':
		  printser_string
		      ("460800 bit/s");
		  break;

	      default:
		  printser_string
		      ("not detected");
		  break;
	      }
	      break;

#endif
	  }
	  break;

      case PARAM_ECHO:
	  createCommandResultMsg
	      (ERR_CODE_SOURCE_SERIALIN,
		ERR_CODE_NO_ERR, 0, NULL);
	  break;

      case PARAM_LINEFEED:
	  lfType = cmdValue;
	  createCommandResultMsg
	      (ERR_CODE_SOURCE_SERIALIN,
		ERR_CODE_NO_ERR, 0, NULL);
	  break;

#ifdef OOBD_PLATFORM_STM32
	  // \todo to be moved into mc specific file
      case PARAM_RESET:
	  if (1 == cmdValue) {
	      DEBUGUARTPRINT
		  ("\r\n*** Softreset performed !!!");
	      SCB->AIRCR = 0x05FA0604;	/* soft reset */
	  }
	  if (2 == cmdValue) {
	      DEBUGUARTPRINT
		  ("\r\n*** Hardreset performed !!!");
	      SCB->AIRCR = 0x05FA0004;	/* hard reset */
	  }
	  break;
#endif
      case PARAM_PROTOCOL:
	  // \todo this kind of task switching is not design intent
	  // \todo no use of protocol table, its hardcoded instead
	  if (VALUE_PARAM_PROTOCOL_CAN_RAW == cmdValue) {	/* p 4 1 */
	      printser_string
		  ("Protocol CAN RAW activated!");
	      vTaskDelete(xTaskProtHandle);
	      vTaskDelay(100 / portTICK_RATE_MS);
	      /* */
	      if (pdPASS ==
		  xTaskCreate(odparr[0],
			      (const signed
				portCHAR *)
			      "prot",
			      configMINIMAL_STACK_SIZE,
			      (void *) NULL,
			      TASK_PRIO_LOW,
			      &xTaskProtHandle))
		  DEBUGUARTPRINT
		      ("\r\n*** 'prot' Task created ***");
	      else
		  DEBUGUARTPRINT
		      ("\r\n*** 'prot' Task NOT created ***");
	  }
	  if (VALUE_PARAM_PROTOCOL_CAN_UDS == cmdValue) {	/* p 4 2 */
	      printser_string
		  ("Protocol CAN UDS activated!");
	      vTaskDelete(xTaskProtHandle);
	      vTaskDelay(100 / portTICK_RATE_MS);
	      /* */
	      if (pdPASS ==
		  xTaskCreate(odparr[1],
			      (const signed
				portCHAR *)
			      "prot",
			      configMINIMAL_STACK_SIZE,
			      (void *) NULL,
			      TASK_PRIO_LOW,
			      &xTaskProtHandle))
	      {
		  DEBUGUARTPRINT
		      ("\r\n*** 'prot' Task created ***");
		  createCommandResultMsg
		      (ERR_CODE_SOURCE_SERIALIN,
			ERR_CODE_NO_ERR, 0, NULL);
	      } else {
		  createCommandResultMsg
		      (ERR_CODE_SOURCE_SERIALIN,
			ERR_CODE_OS_NO_PROTOCOL_TASK,
			0,
			ERR_CODE_OS_NO_PROTOCOL_TASK_TEXT);
		  DEBUGUARTPRINT
		      ("\r\n*** 'prot' Task NOT created ***");
	      }
	  }
	  break;


      default:
	  //sendParam(cmdKey, cmdValue);
	  break;
      }

  printLF();
  printEOT();
}

portBASE_TYPE eval_param_sys (portBASE_TYPE param, portBASE_TYPE value){
CreateParamOutputMsg(param, value,  printParam_sys);
}

void mc_init_sys_tasks (){
  DEBUGPRINT ("init system tasks\n", 'a');

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

    /* First write in the memory followed by a read of the written data --------*/
    /* Write on I2C EEPROM from sEE_WRITE_ADDRESS1 */
    sEE_WriteBuffer(Tx1_Buffer, sEE_WRITE_ADDRESS1, BUFFER_SIZE1);

    /* Set the Number of data to be read */
    NumDataRead = BUFFER_SIZE1;

    /* Read from I2C EEPROM from sEE_READ_ADDRESS1 */
    sEE_ReadBuffer(Rx1_Buffer, sEE_READ_ADDRESS1, (uint16_t *)(&NumDataRead));

    /* Wait till DMA transfer is complete (Transfer complete interrupt handler
      resets the variable holding the number of data to be read) */
    while (NumDataRead > 0)
    {
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

  if (pdPASS ==
      xTaskCreate (Led1Task, (const signed portCHAR *) "LED1",
		   configMINIMAL_STACK_SIZE, (void *) NULL, TASK_PRIO_LOW,
		   (xTaskHandle *) NULL))
    DEBUGUARTPRINT ("\r\n*** 'LED1' Task created ***");
  else
    DEBUGUARTPRINT ("\r\n*** 'LED1' Task NOT created ***");

  if (pdPASS ==
      xTaskCreate (Led2Task, (const signed portCHAR *) "LED2",
		   configMINIMAL_STACK_SIZE, (void *) NULL, TASK_PRIO_LOW,
		   (xTaskHandle *) NULL))
    DEBUGUARTPRINT ("\r\n*** 'LED2' Task created ***");
  else
    DEBUGUARTPRINT ("\r\n*** 'LED2' Task NOT created ***");


  /* initialize Interrupt Vector table and activate interrupts */
  NVIC_Configuration ();

}

void mc_init_sys_shutdown (){
  DEBUGPRINT ("shutdown systems\n", 'a');
  SCB->AIRCR = 0x05FA0604;	/* soft reset */
}


void
Led1Task (void *pvParameters)
{
  uint16_t LedBlinkDuration = 0;
  uint16_t Led1Duration = 250; /* 250ms default value */
  extern xQueueHandle Led1Queue;

  DEBUGUARTPRINT ("\r\n*** prvLedTask entered! ***");

  for (;;)
  {
	/* xQueueReceive waits for max. Led1Duration time for a new received values = LED OFF Time */
	if( pdTRUE == xQueueReceive( Led1Queue, &LedBlinkDuration, (portTickType) Led1Duration / portTICK_RATE_MS))
    {
	  /* data received from queue */
	  Led1Duration = LedBlinkDuration;
    }

	GPIO_ResetBits (GPIOB, GPIO_Pin_4); /* LED1 ON - green */
    vTaskDelay ((portTickType) Led1Duration / portTICK_RATE_MS); /* ON time */
    GPIO_SetBits (GPIOB, GPIO_Pin_4); /* LED1 OFF - green */
  }
}
/*---------------------------------------------------------------------------*/

void
Led2Task (void *pvParameters)
{
  uint16_t LedDuration = 0;
  extern xQueueHandle Led2Queue;

  DEBUGUARTPRINT ("\r\n*** prvLedTask entered! ***");

  for (;;)
  {
	/* wait indefinitely till value received => depends on portMAX_DELAY */
	if( pdTRUE == xQueueReceive( Led2Queue, &LedDuration, portMAX_DELAY ))
    {
      /* data received from queue */
      GPIO_ResetBits (GPIOB, GPIO_Pin_5); /* LED1 ON - red */
      vTaskDelay ((portTickType) LedDuration / portTICK_RATE_MS); /* ON time */
      GPIO_SetBits (GPIOB, GPIO_Pin_5); /* LED1 OFF - red */
    }
  }
}
/*---------------------------------------------------------------------------*/

uint16_t readADC1(uint8_t channel)
{  
  ADC_RegularChannelConfig(ADC1, channel, 1, ADC_SampleTime_1Cycles5);
  /* Start the conversion */
  ADC_SoftwareStartConvCmd(ADC1, ENABLE);  
  /* Wait until conversion completion */
  while(ADC_GetFlagStatus(ADC1, ADC_FLAG_EOC) == RESET);  
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
  size = (uint32_t)((uint32_t)&_etext + (uint32_t)&_edata - (uint32_t)&_sdata) - 0x8002400;
  CRC_ResetDR();
  /* 0x8002400 is the application start address and size = application code size */
  crc= CRC_CalcBlockCRC((uint32_t*)0x8002400, size/4+1);
  RCC_AHBPeriphClockCmd(RCC_AHBPeriph_CRC, DISABLE);

  return crc ;
}
