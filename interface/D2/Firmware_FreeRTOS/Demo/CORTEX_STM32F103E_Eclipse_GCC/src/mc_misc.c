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
 * MC specific miscellaneous routines
 */

/* OOBD headers. */
#include "od_config.h"
#include "stm32f10x.h"
#include "SystemConfig.h"

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
