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

/* Define to prevent recursive inclusion -------------------------------------*/
#ifndef __SystemConfig_H
#define __SystemConfig_H


#include "portmacro.h"

void Led1Task(void *pvParameters);
void Led2Task(void *pvParameters);


/* -------- Create Global variables ---------------------------------------------------*/
void System_Configuration(void);
void RCC_Configuration(void);
void GPIO_Configuration(void);
void NVIC_Configuration(void);
void SysTick_Configuration(void);
void ADC_Configuration(void);
portBASE_TYPE GPIO_HardwareLevel(void);

/* void        SPI_Configuration(void);  */

#define USART1_BAUDRATE_DEFAULT USART1_BAUDRATE_115200
#define USART1_BAUDRATE_4800    4800
#define USART1_BAUDRATE_9600    9600
#define USART1_BAUDRATE_19200   19200
#define USART1_BAUDRATE_38400   38400
#define USART1_BAUDRATE_57600   57600
#define USART1_BAUDRATE_115200  115200
#define USART1_BAUDRATE_230400  230400
#define USART1_BAUDRATE_460800  460800

/**
  * @}
  */

/** @addtogroup STM3210C_EVAL_LOW_LEVEL_I2C_EE
  * @{
  */
/**
  * @brief  I2C EEPROM Interface pins
  */
#define sEE_I2C                          I2C1
#define sEE_I2C_CLK                      RCC_APB1Periph_I2C1
#define sEE_I2C_SCL_PIN                  GPIO_Pin_6	/* PB.06 */
#define sEE_I2C_SCL_GPIO_PORT            GPIOB	/* GPIOB */
#define sEE_I2C_SCL_GPIO_CLK             RCC_APB2Periph_GPIOB
#define sEE_I2C_SDA_PIN                  GPIO_Pin_7	/* PB.07 */
#define sEE_I2C_SDA_GPIO_PORT            GPIOB	/* GPIOB */
#define sEE_I2C_SDA_GPIO_CLK             RCC_APB2Periph_GPIOB
#define sEE_M24C64_32

#define sEE_I2C_DMA                      DMA1
#define sEE_I2C_DMA_CHANNEL_TX           DMA1_Channel6
#define sEE_I2C_DMA_CHANNEL_RX           DMA1_Channel7
#define sEE_I2C_DMA_FLAG_TX_TC           DMA1_IT_TC6
#define sEE_I2C_DMA_FLAG_TX_GL           DMA1_IT_GL6
#define sEE_I2C_DMA_FLAG_RX_TC           DMA1_IT_TC7
#define sEE_I2C_DMA_FLAG_RX_GL           DMA1_IT_GL7
#define sEE_I2C_DMA_CLK                  RCC_AHBPeriph_DMA1
#define sEE_I2C_DR_Address               ((uint32_t)0x40005410)
#define sEE_USE_DMA

#define sEE_I2C_DMA_TX_IRQn              DMA1_Channel6_IRQn
#define sEE_I2C_DMA_RX_IRQn              DMA1_Channel7_IRQn
#define sEE_I2C_DMA_TX_IRQHandler        DMA1_Channel6_IRQHandler
#define sEE_I2C_DMA_RX_IRQHandler        DMA1_Channel7_IRQHandler
#define sEE_I2C_DMA_PREPRIO              0
#define sEE_I2C_DMA_SUBPRIO              0

#define sEE_DIRECTION_TX                 0
#define sEE_DIRECTION_RX                 1

/* Time constant for the delay caclulation allowing to have a millisecond
   incrementing counter. This value should be equal to (System Clock / 1000).
   ie. if system clock = 72MHz then sEE_TIME_CONST should be 72. */
#define sEE_TIME_CONST                   72


#endif				/* __SystemConfig_H */
