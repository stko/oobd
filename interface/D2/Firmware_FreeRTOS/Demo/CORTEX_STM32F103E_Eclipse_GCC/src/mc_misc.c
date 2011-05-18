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
blinkLedTask (void *pvParameters)
{
  portTickType xLastExecutionTime;

  DEBUGUARTPRINT ("\r\n*** prvBlinkLedTask entered! ***");

  /* Initialise the xLastExecutionTime variable on task entry. */
  xLastExecutionTime = xTaskGetTickCount ();

  for (;;)
    {
      /* Simple toggle the LED periodically.  This just provides some timing
         verification. */
      vTaskDelayUntil (&xLastExecutionTime,
           (portTickType) 1000 / portTICK_RATE_MS);
      /* Output high -> LED off for 1000ms = 1sec */
      GPIO_SetBits (GPIOB, GPIO_Pin_4); /* LED2 - green */
      GPIO_SetBits (GPIOB, GPIO_Pin_5); /* LED1 - red */

      vTaskDelayUntil (&xLastExecutionTime,
           (portTickType) 1000 / portTICK_RATE_MS);
      /* Output low -> LED on for 1000ms = 1sec */
      GPIO_ResetBits (GPIOB, GPIO_Pin_4); /* LED2 - green */
      GPIO_ResetBits (GPIOB, GPIO_Pin_5); /* LED1 - red */
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
  size = (uint32_t)((uint32_t)&_etext + (uint32_t)&_edata - (uint32_t)&_sdata) - 0x8003000;
  CRC_ResetDR();
  /* 0x8003000 is the application start address and size = application code size */
  crc= CRC_CalcBlockCRC((uint32_t*)0x8003000, size/4+1);
  RCC_AHBPeriphClockCmd(RCC_AHBPeriph_CRC, DISABLE);

  return crc ;
}