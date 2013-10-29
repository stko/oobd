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

/* Includes ------------------------------------------------------------------*/
#include "FreeRTOSConfig.h"
#include "stm32f10x.h"
#include "SystemConfig.h"
#include "od_config.h"
#include "mc_serial.h"
#include "odp_uds.h"
#include "odb_can.h"

/* -------- Used Global variables --------------------------------------------*/

uint8_t BTM222_BtAddress[18] = "00:00:00:00:00:00";

uint8_t BTM222_DeviceName[17] = "not detected";

unsigned char BTM222_UartSpeed;

/* Private typedef -----------------------------------------------------------*/

/* Private define ------------------------------------------------------------*/

/* Private macro -------------------------------------------------------------*/

/* Private variables ---------------------------------------------------------*/
DMA_InitTypeDef sEEDMA_InitStructure;

/* Private function prototypes -----------------------------------------------*/

/* Private functions ---------------------------------------------------------*/

/**
 * @brief  Configures the system.
 * @param None.
 * @retval : None.
 */
void System_Configuration(void) {

	/* GPIO configuration */
	GPIO_Configuration();

	/* USART1 configuration */
	USART1_Configuration();

	/* USART1 configuration */
	//    USART2_Configuration();

	/* USART1 configuration */
	CAN1_Configuration(VALUE_BUS_CONFIG_11bit_500kbit, CAN_Mode_Silent); /* default initialization */

	/* Analog digitial converter configuration */
	ADC_Configuration();

	if (GPIO_HardwareLevel() == 4)
		/* Timer 2 configuration for PWM output, 3100Hz/50% duty cycle */
		TIM2_Configuration(3100); /* OOBD-Cup v5 only */
	else if (GPIO_HardwareLevel() == 5)
		/* Timer 3 configuration for PWM output, 3100Hz/50% duty cycle */
		TIM3_Configuration(3100); /* OOBD CAN Invader only */

	/* NVIC configuration */
	//  NVIC_Configuration();

	// move to hardware init function as well as LEDTask
	//xQueueCreate = xQueueCreate (2, sizeof (boolean));
	extern xQueueHandle Led1Queue;

	extern xQueueHandle Led2Queue;

}

/*----------------------------------------------------------------------------*/
/**
 * @brief  Configure the nested vectored interrupt controller
 * @param  None
 * @retval None
 */
void NVIC_Configuration(void) {
	NVIC_InitTypeDef NVIC_InitStructure;

	/* Set the Vector Table base location to FLASH */
	NVIC_SetVectorTable(NVIC_VectTab_FLASH, 0x2500);

	/* Setting the priority grouping bits length */
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_4);

	/* Enable the USART1 Interrupt */
	NVIC_InitStructure.NVIC_IRQChannel = USART1_IRQn;

	/* NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0; */
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority
			= (configMAX_SYSCALL_INTERRUPT_PRIORITY >> 4) + 1;
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0;
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
	NVIC_Init(&NVIC_InitStructure);

	/* Enable CAN1 interrupt */
	NVIC_InitStructure.NVIC_IRQChannel = USB_LP_CAN1_RX0_IRQn;
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority
			= (configMAX_SYSCALL_INTERRUPT_PRIORITY >> 4) + 1;
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = 1;
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
	NVIC_Init(&NVIC_InitStructure);

	NVIC_InitStructure.NVIC_IRQChannel = I2C1_EV_IRQn;
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0;
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0;
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
	NVIC_Init(&NVIC_InitStructure);

	NVIC_InitStructure.NVIC_IRQChannel = I2C1_ER_IRQn;
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0;
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = 1;
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
	NVIC_Init(&NVIC_InitStructure);
}

/*----------------------------------------------------------------------------*/
/**
 * @brief  returns peripherals hardware ID
 * @param  None
 * @retval 1 = standard DXM, 4 = OOBD D2-V5, 5 = OOBD CAN Invader
 */portBASE_TYPE GPIO_HardwareLevel(void) {
	portBASE_TYPE HardwareVariant = 0; /* default no valid hardware detected */

	if ((GPIO_ReadInputDataBit(GPIOA, GPIO_Pin_8) == Bit_SET)
			&& (GPIO_ReadInputDataBit(GPIOA, GPIO_Pin_13) == Bit_SET)
			&& (GPIO_ReadInputDataBit(GPIOC, GPIO_Pin_14) == Bit_SET))
		HardwareVariant = 1; /* DXM1 */
	else if ((GPIO_ReadInputDataBit(GPIOA, GPIO_Pin_8) == Bit_RESET)
			&& (GPIO_ReadInputDataBit(GPIOA, GPIO_Pin_13) == Bit_SET))
		//              (GPIO_ReadInputDataBit(GPIOC, GPIO_Pin_14) == Bit_RESET))
		HardwareVariant = 4; /* OOBD-Cup v5 */
	else if ((GPIO_ReadInputDataBit(GPIOA, GPIO_Pin_8) == Bit_SET)
			&& (GPIO_ReadInputDataBit(GPIOA, GPIO_Pin_13) == Bit_SET)
			&& (GPIO_ReadInputDataBit(GPIOA, GPIO_Pin_14) == Bit_RESET))
		HardwareVariant = 5; /* OOBD CAN Invader */

	return HardwareVariant;
}

/*----------------------------------------------------------------------------*/
/**
 * @brief  Configures the different GPIO ports
 * @param  None
 * @retval None
 */
void GPIO_Configuration(void) {

	GPIO_InitTypeDef GPIO_InitStructure;

	/* release alternative GPIO function of PA13/14/15 and PB4 */
	GPIO_PinRemapConfig(GPIO_Remap_SWJ_Disable, ENABLE);

	/* GPIOx clocks enable */
	RCC_APB2PeriphClockCmd(
			RCC_APB2Periph_GPIOA | RCC_APB2Periph_GPIOB | RCC_APB2Periph_GPIOC
					| RCC_APB2Periph_AFIO, ENABLE);

	/* Configure all unused GPIO port pins in Analog Input mode (floating input
	 trigger OFF), this will reduce the power consumption and increase the
	 device immunity against EMI/EMC ***************************************** */
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_All;
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AIN;

	GPIO_Init(GPIOA, &GPIO_InitStructure);
	GPIO_Init(GPIOB, &GPIO_InitStructure);
	GPIO_Init(GPIOC, &GPIO_InitStructure);

	/** --------------------------------------------------------------------------
	 * PORTA configuration
	 * PA 0 =              	  = GPIO_Mode_AIN - Input floating for low power consumption
	 * PA 1 = ???             = GPIO_Mode_AIN - Input floating for low power consumption
	 * PA 2 = USART2_Tx 	  = GPIO_Mode_AF_PP - Alternate Function output Push Pull (L9637D RX, Pin1)
	 * PA 3 = USART2_Rx 	  = GPIO_Mode_AIN - Input floating for low power consumption (L9637D TX, Pin4)
	 * PA 4 = L-LINE Output   = GPIO_Mode_Out_PP - Output Push Pull
	 * PA 5 = ???             = GPIO_Mode_AIN - Input floating for low power consumption
	 * PA 6 = LM393 Out, Pin1 = GPIO_Mode_AIN - currently unused
	 * PA 7 = LM393 Out, Pin7 = GPIO_Mode_AIN - currently unused
	 * --------------------------------------------------------------------------
	 * PA 8 = DXM1 IO Pin 21  = GPIO_Mode_AIN - OOBD-Cup v5 hardware identification
	 * PA 9 = USART1_Tx       = GPIO_Mode_AF_PP - Alternate Function output Push Pull
	 * PA10 = USART1_Rx       = GPIO_Mode_AIN - Input floating for low power consumption
	 * PA11 = USART1_CTS      = GPIO_Mode_AIN - CAN-RxD for STM32F103T8 (i.e. OOBD CAN Invader)
	 * PA12 = USART1_RTS      = GPIO_Mode_AIN - CAN-TxD for STM32F103T8 (i.e. OOBD CAN Invader)
	 * PA13 = HardwareIdent   = GPIO_Mode_AIN - OOBD CAN Invader hardware identification
	 * PA14 = HardwareIdent   = GPIO_Mode_AIN - OOBD CAN Invader hardware identification
	 * PA15 = ???             = GPIO_Mode_AIN - Input floating for low power consumption
	 * --------------------------------------------------------------------------
	 */

	/* Enable USART1 Clock */
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_USART1, ENABLE);

	/* General settings for GPIOs of PORT A */
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;

	/* Initialize USART2 on PA2 (USART2_Tx) and PA3 (USART2_Rx) for K-Line interface */
	//    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP;
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_2;
	GPIO_Init(GPIOA, &GPIO_InitStructure);
	GPIO_ResetBits(GPIOA, GPIO_Pin_2);

	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN_FLOATING;
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_3;
	GPIO_Init(GPIOA, &GPIO_InitStructure);

	/* configure L-Line output */
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_4;
	GPIO_Init(GPIOA, &GPIO_InitStructure);
	GPIO_ResetBits(GPIOA, GPIO_Pin_4);

	/* configure PA8, PA13, PA14 as Input for Hardwareidentifikaton
	 * PA8 & PA13 & PA14 = 1 - Original DXM1
	 * PA8 = 0, PA13 & PA14 = 1 - OOBD-Cup v5
	 */
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU;
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_8;
	GPIO_Init(GPIOA, &GPIO_InitStructure);

	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU;
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_13;
	GPIO_Init(GPIOA, &GPIO_InitStructure);

	/* Initialize USART1 on PA9 (USART1_Tx) and PA10 (USART1_Rx) for RS232 interface */
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP;
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_9;
	GPIO_Init(GPIOA, &GPIO_InitStructure);

	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN_FLOATING;
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_10;
	GPIO_Init(GPIOA, &GPIO_InitStructure);

	/** --------------------------------------------------------------------------
	 * PB 0 = ADC12_IN8 = GPIO_Mode_AIN - Input floating for low power consumption
	 * PB 1 = ??? = GPIO_Mode_AIN - Input floating for low power consumption
	 * PB 2 = ??? = GPIO_Mode_AIN - Input floating for low power consumption
	 * PB 3 = ??? = GPIO_Mode_AIN - Input floating for low power consumption
	 * PB 4 = DXM1 IO Pin 13, LED2 - red = GPIO_Mode_Out_OD - Output open drain
	 *        OOBD-Cup v5 Duo-LED2 - red
	 * PB 5 = DXM1 IO Pin 12, LED1 - green = GPIO_Mode_Out_OD - Output open drain
	 * 		  OOBD-Cup v5 Duo-LED2 - green
	 * PB 6 = 24LC16, SCL = GPIO_Mode_AF_OD = Alternate Function output open drain
	 * PB 7 = 24LC16, SDA = GPIO_Mode_AF_OD = Alternate Function output open drain
	 * --------------------------------------------------------------------------
	 * PB 8 = TJA1050 Pin 4 (RxD)  = GPIO_Mode_IPU - Input Pullup
	 * PB 9 = TJA1050 Pin 1 (TxD)  = GPIO_Mode_AF_PP - Alternate Function output Push Pull
	 * PB10 = DXM1 IO Pin 15, LCD out  = GPIO_Mode_AIN - currently unused
	 * 		  OOBD-Cup v5 LED1 - yellow
	 * PB11 = DXM1 IO Pin 18, A0 out   = GPIO_Mode_AIN - currently unused
	 *        OOBD-Cup v5 PWM out for buzzer = GPIO_Mode_AF_PP - Output Push Pull
	 * PB12 = DXM1 IO Pin 19, SPI_CS   = GPIO_Mode_AIN - currently unused
	 * PB13 = DXM1 IO Pin 20, SPI_SCK  = GPIO_Mode_AIN - currently unused
	 * PB14 = DXM1 IO Pin 22, SPI_MISO = GPIO_Mode_AIN - currently unused
	 * PB15 = DXM1 IO Pin 23, SPI_MOSI = GPIO_Mode_AIN - currently unused
	 *---------------------------------------------------------------------------
	 */

	/* General settings for GPIOs of PORT B */
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;

	/* Configure I2C pins: SCL and SDA ---------------------------------------- */
	//  GPIO_InitStructure.GPIO_Pin   = GPIO_Pin_6 | GPIO_Pin_7;
	//  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
	//  GPIO_InitStructure.GPIO_Mode  = GPIO_Mode_AF_OD;
	//  GPIO_Init(GPIOB, &GPIO_InitStructure);

	/** --------------------------------------------------------------------------
	 * PC13 = ??? = GPIO_Mode_AIN - Input floating for low power consumption
	 * PC14 = DXM1 IO Pin 29 = GPIO_Mode_AIN - currently unused
	 * PC15 = DXM1 IO Pin 28 = GPIO_Mode_AIN - currently unused
	 *        OOBD-Cup v5    = GPIO_Mode_Out_PP - Plush-Pull, REL1
	 * --------------------------------------------------------------------------
	 */

	/* General settings for GPIOs of PORT C */
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;

	/* configure PC14 as Input for Hardwareidentifikaton
	 * PC14 = 1 - Reserved for further variants of OOBD-Cup v5 (default)
	 * PC14 = 0 - Reserved for further variants of OOBD-Cup v5
	 */
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU;
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_14;
	GPIO_Init(GPIOC, &GPIO_InitStructure);

	/** --------------------------------------------------------------------------
	 * Port specific configuration depending on hardware ident
	 * --------------------------------------------------------------------------
	 */

	/* General settings for GPIO_Speed */
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;

	/* CAN-Relay switching depending on hardware ident */
	if (GPIO_HardwareLevel() == 4) { /* OOBD-Cup v5 */
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;
		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_15;
		/* initialize new PortC settings  */
		GPIO_Init(GPIOC, &GPIO_InitStructure);
		GPIO_ResetBits(GPIOC, GPIO_Pin_15); /* default: REL1 - OFF */
	} else if (GPIO_HardwareLevel() == 5) { /* OOBD CAN Invader */
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;
		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_3;
		/* initialize new PortB settings  */
		GPIO_Init(GPIOB, &GPIO_InitStructure);
		GPIO_ResetBits(GPIOB, GPIO_Pin_3); /* default PB3: REL1 - OFF */
	}

	/* LED configuration depending on hardware ident */
	if (GPIO_HardwareLevel() == 4) { /* OOBD-Cup v5 or OOBD-Cup v5 */
		/* configure DXM1-Output (push pull) of Duo-LED2 - green (PB5) and LED2 - red (PB4)
		 * and LED2 - blue (PB10) */
		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_4 | GPIO_Pin_5 | GPIO_Pin_10;
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;
		GPIO_Init(GPIOB, &GPIO_InitStructure);
	} else if (GPIO_HardwareLevel() == 5) { /* OOBD CAN Invader */
		/* configure of Duo-LED - green (PB5) and LED2 - red (PB4) */
		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_4 | GPIO_Pin_5;
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;
		GPIO_Init(GPIOB, &GPIO_InitStructure);

		/* configure of LED2 - blue (PA7) */
		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_7;
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;
		GPIO_Init(GPIOA, &GPIO_InitStructure);
	} else if (GPIO_HardwareLevel() == 1) { /* Original DXM1 */
		/* configure DXM1-Output (open drain) of LED1 - green (PB5) and LED2 - red (PB4) */
		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_4 | GPIO_Pin_5;
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_OD;
		GPIO_Init(GPIOB, &GPIO_InitStructure);
	}

	/* configure of BT-Module reset */
	if (GPIO_HardwareLevel() == 4) { /* OOBD-Cup v5 or OOBD-Cup v5 */
		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_13;
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;
		GPIO_Init(GPIOC, &GPIO_InitStructure);
		GPIO_SetBits(GPIOC, GPIO_Pin_13); /* default PC13: Release BT-Module from reset */
	} else if (GPIO_HardwareLevel() == 5) { /* OOBD CAN Invader */
		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_15;
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;
		GPIO_Init(GPIOA, &GPIO_InitStructure);
		GPIO_SetBits(GPIOA, GPIO_Pin_15); /* default PC13: Release BT-Module from reset */
	}

	/* CAN configuration depending on hardware ident */
	/* GPIO clock enable */
	if (GPIO_HardwareLevel() == 5) { /* OOBD CAN Invader */
		GPIO_PinRemapConfig(GPIO_Remap1_CAN1, DISABLE);
		GPIO_PinRemapConfig(GPIO_Remap2_CAN1, DISABLE);
		/* Configure CAN pin: RX */
		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_11;
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU;
		GPIO_Init(GPIOA, &GPIO_InitStructure);
		/* Configure CAN pin: TX */
		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_12;
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP;
		GPIO_Init(GPIOA, &GPIO_InitStructure);
	} else { /* DXM1 or OOBD-Cup v5 */
		GPIO_PinRemapConfig(GPIO_Remap1_CAN1, ENABLE);
		/* Configure CAN pin: RX */
		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_8;
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU;
		GPIO_Init(GPIOB, &GPIO_InitStructure);
		/* Configure CAN pin: TX */
		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_9;
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP;
		GPIO_Init(GPIOB, &GPIO_InitStructure);
	}
	/* CAN1 Periph clock enable */
	RCC_APB1PeriphClockCmd(RCC_APB1Periph_CAN1, ENABLE);

	/* Identify on which hardware the firmware is running */
	if (GPIO_HardwareLevel() == 4) { /* OOBD-Cup v5 */
		/* TIM2 clock enable */
		RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM2, ENABLE);
		/* configure PB11 PWM output for buzzer, TIM2_CH4 source for OOBD-Cup v5 */
		GPIO_PinRemapConfig(GPIO_PartialRemap2_TIM2, ENABLE); /* OOBD-Cup v5, use TIM2, Ch3 */
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP;
		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_11;
		GPIO_Init(GPIOB, &GPIO_InitStructure);
	} else if (GPIO_HardwareLevel() == 5) { /* OOBD CAN Invader */
		/* TIM3 clock enable */
		RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM3, ENABLE);
		/* configure PA6 PWM output for buzzer, TIM3_CH1 source for OOBD CAN Invader */
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP;
		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_6;
		GPIO_Init(GPIOA, &GPIO_InitStructure);
	}
}

/*----------------------------------------------------------------------------*/

/**
 * @brief  Configures the different GPIO ports
 * @param  None
 * @retval None
 */
void USART1_Configuration(void) {

	volatile unsigned long nCount, nLength = 300000;

	USART_InitTypeDef USART_InitStructure;

	uint8_t BTM222_UartAutobaudControl;

	extern uint8_t BTM222_BtAddress[];

	USART_DeInit(USART1); /* Reset Uart to default */

	/** USARTx configuration -----------------------------------------------------
	 * USARTx configured as follow:
	 *      - BaudRate = 115200 baud
	 *      - Word Length = 8 Bits
	 *      - One Stop Bit
	 *      - None parity
	 *      - Hardware flow control disabled (RTS and CTS signals)
	 *      - Receive and transmit enabled
	 */
	USART_InitStructure.USART_BaudRate = USART1_BAUDRATE_DEFAULT;

	USART_InitStructure.USART_WordLength = USART_WordLength_8b;

	USART_InitStructure.USART_StopBits = USART_StopBits_1;

	USART_InitStructure.USART_Parity = USART_Parity_No;

	USART_InitStructure.USART_HardwareFlowControl
			= USART_HardwareFlowControl_None;

	USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;

	/* Configure USART1 */
	USART_Init(USART1, &USART_InitStructure);

	/* Enable the USART1 */
	USART_Cmd(USART1, ENABLE);

	/* Enable the USART1-Transmit interrupt: this interrupt is generated when the
	 USART1 transmit data register is empty */
	/* USART_ITConfig(USART1, USART_IT_TXE, ENABLE); */

	/* Enable the USART1-Receive interrupt: this interrupt is generated when the
	 USART1 receive data register is not empty */
	USART_ITConfig(USART1, USART_IT_RXNE, ENABLE);

	NVIC_Configuration();

	/* set UartAutobaudControl to default UART value 115200kbit/s */
	BTM222_UartAutobaudControl = 0;

	USART_SendData(USART1, '\r'); /* clear command line from Bootloader String first */
	delay_ms(1500); /* delay for waiting of BTM182/BTM222 power up - 1500ms */

	while (BTM222_UartAutobaudControl != 9) {
		USART_SendData(USART1, 'a');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, 't');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, '\r');
		btm_uart_delay_ms(100);

		BufCnt = 0; /* set BTM222_RespBuffer back to zero */
		BTM222_UART_Rx_Flag = pdFALSE;

		USART_SendData(USART1, 'a');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, 't');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, 'q');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, '?');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, '\r');
		btm_uart_delay_ms(100);

		BufCnt = 0; /* set BTM222_RespBuffer back to zero */
		BTM222_UART_Rx_Flag = pdFALSE;

		USART_SendData(USART1, 'a');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, 't');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, 'l');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, '?');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, '\r');
		btm_uart_delay_ms(100);

		/* check received response character from BTM222 */
		switch (BTM222_UartSpeed) {

		case '0':
			DEBUGUARTPRINT("\r\n*** BTM222 - L1 = 4800bps detected! ***");

			btm_uart_speed('5'); /* set BT-Module UART to default 115200 bit/s */

			BTM222_UartAutobaudControl = 6;
			break;

		case '1':

			DEBUGUARTPRINT("\r\n*** BTM222 - L1 = 9600bps detected! ***");

			btm_uart_speed('5'); /* set BT-Module UART to default 115200 bit/s */

			BTM222_UartAutobaudControl = 6;
			break;

		case '2':

			DEBUGUARTPRINT("\r\n*** BTM222 - L2 = 19200bps detected! ***");

			btm_uart_speed('5'); /* set BT-Module UART to default 115200 bit/s */

			BTM222_UartAutobaudControl = 6;

			break;

		case '3':

			DEBUGUARTPRINT("\r\n*** BTM222 - L3 = 38400bps detected! ***");

			BTM222_UART_Rx_Flag = pdFALSE;

			btm_uart_speed('5'); /* set BT-Module UART to default 115200 bit/s */

			BTM222_UartAutobaudControl = 6;

			BTM222_UART_Rx_Flag = pdTRUE;

			break;

		case '4':

			DEBUGUARTPRINT("\r\n*** BTM222 - L4 = 57600bps detected! ***");

			btm_uart_speed('5'); /* set BT-Module UART to default 115200 bit/s */

			BTM222_UartAutobaudControl = 6;

			break;

		case '5':

			DEBUGUARTPRINT
			("\r\n*** BTM222 - L5 = 115200bps detected! ***");

			BTM222_UartAutobaudControl = 9;

			break;

		case '6':

			DEBUGUARTPRINT
			("\r\n*** BTM222 - L6 = 230400bps detected! ***");

			btm_uart_speed('5'); /* set BT-Module UART to default 115200 bit/s */

			BTM222_UartAutobaudControl = 6;

			break;

		case '7':

			DEBUGUARTPRINT
			("\r\n*** BTM222 - L7 = 460800bps detected! ***");

			btm_uart_speed('5'); /* set BT-Module UART to default 115200 bit/s */

			BTM222_UartAutobaudControl = 6;

			break;

		default:

			DEBUGUARTPRINT("\r\n*** BTM222 - baudrate not detected! ***");

			BTM222_UartAutobaudControl++; /* increment AutobaudControl counter */

			break;

		}

		switch (BTM222_UartAutobaudControl) {

		case 1:

			/* Initialize USART1 with next possible baudrate of BTM222 */
			USART_InitStructure.USART_BaudRate = USART1_BAUDRATE_4800;

			break;

		case 2:

			/* Initialize USART1 with next possible baudrate of BTM222 */
			USART_InitStructure.USART_BaudRate = USART1_BAUDRATE_9600;

			break;

		case 3:

			/* Initialize USART1 with next possible baudrate of BTM222 */
			USART_InitStructure.USART_BaudRate = USART1_BAUDRATE_19200;

			break;

		case 4:

			/* Initialize USART1 with next possible baudrate of BTM222 */
			USART_InitStructure.USART_BaudRate = USART1_BAUDRATE_38400;

			break;

		case 5:

			/* Initialize USART1 with next possible baudrate of BTM222 */
			USART_InitStructure.USART_BaudRate = USART1_BAUDRATE_57600;

			break;

		case 6:

			/* Initialize USART1 with next possible baudrate of BTM222 */
			USART_InitStructure.USART_BaudRate = USART1_BAUDRATE_115200;

			break;

		case 7:

			/* Initialize USART1 with next possible baudrate of BTM222 */
			USART_InitStructure.USART_BaudRate = USART1_BAUDRATE_230400;

			break;

		case 8:

			/* Initialize USART1 with next possible baudrate of BTM222 */
			USART_InitStructure.USART_BaudRate = USART1_BAUDRATE_460800;

			break;

		case 9:

			/* fallback to default baudrate */
			USART_InitStructure.USART_BaudRate = USART1_BAUDRATE_DEFAULT;

			break;

		default:

			break;

		}

		USART_Init(USART1, &USART_InitStructure); /* reinitialization of USART */

	} /* end of for */

	DEBUGUARTPRINT("\r\n*** Autobaud SCAN for BTM222 finished! ***");

	DEBUGUARTPRINT("\r\n*** Starting to get BTM222 BT address! ***");

	//  BTM222_BtAddress[18] = {'0','0',':','0','0',':','0','0',':','0','0',':','0','0',':','0','0','\0'};

	BTM222_UART_Rx_Flag = pdFALSE;

	USART_SendData(USART1, '\r');
	btm_uart_delay_ms(40);

	BufCnt = 0; /* set BTM222_RespBuffer back to zero */

	/* send "atb?"-command to get Bluetooth MAC address of BTM222 */
	USART_SendData(USART1, 'a');
	btm_uart_delay_ms(40);
	USART_SendData(USART1, 't');
	btm_uart_delay_ms(40);
	USART_SendData(USART1, 'b');
	btm_uart_delay_ms(40);
	USART_SendData(USART1, '?');
	btm_uart_delay_ms(40);
	USART_SendData(USART1, '\r');
	btm_uart_delay_ms(100);

	BTM222_UART_Rx_Flag = pdTRUE;

	DEBUGUARTPRINT("\r\n*** Get BTM222 BT address finished! ***");

	DEBUGUARTPRINT("\r\n*** Get BTM222 BT device name! ***");

	BTM222_UART_Rx_Flag = pdFALSE;
	BufCnt = 0; /* set BTM222_RespBuffer back to zero */

	/* send "atn?"-command to get BTM222 device name */
	USART_SendData(USART1, 'a');
	btm_uart_delay_ms(40);
	USART_SendData(USART1, 't');
	btm_uart_delay_ms(40);
	USART_SendData(USART1, 'n');
	btm_uart_delay_ms(40);
	USART_SendData(USART1, '?');
	btm_uart_delay_ms(40);
	USART_SendData(USART1, '\r');
	btm_uart_delay_ms(100);

	BTM222_UART_Rx_Flag = pdTRUE;

	DEBUGUARTPRINT("\r\n*** Get BTM222 BT device name finished! ***");

	/* set "OOBD-Cup" as BTM222 Bluetooth device name if needed */
	if (BTM222_DeviceNameFlag == pdFALSE) {

		USART_SendData(USART1, 'a');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, 't');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, 'n');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, '=');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, 'O');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, 'O');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, 'B');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, 'D');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, '-');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, 'C');
		btm_uart_delay_ms(40);
		if (GPIO_HardwareLevel() == 4)
			USART_SendData(USART1, 'u');
		else if (GPIO_HardwareLevel() == 5)
			USART_SendData(USART1, 'I');

		btm_uart_delay_ms(40);
		if (GPIO_HardwareLevel() == 4)
			USART_SendData(USART1, 'p');
		else if (GPIO_HardwareLevel() == 5)
			USART_SendData(USART1, 'V');

		btm_uart_delay_ms(40);
		USART_SendData(USART1, ' ');

		for (nCount = 0; nCount < nLength; nCount++) {
		}; /* delay */
		USART_SendData(USART1, BTM222_BtAddress[9]);
		btm_uart_delay_ms(40);
		USART_SendData(USART1, BTM222_BtAddress[10]);
		btm_uart_delay_ms(40);
		USART_SendData(USART1, BTM222_BtAddress[12]);
		btm_uart_delay_ms(40);
		USART_SendData(USART1, BTM222_BtAddress[13]);
		btm_uart_delay_ms(40);
		USART_SendData(USART1, BTM222_BtAddress[15]);
		btm_uart_delay_ms(40);
		USART_SendData(USART1, BTM222_BtAddress[16]);
		btm_uart_delay_ms(40);
		USART_SendData(USART1, '\r');
		btm_uart_delay_ms(100);
		DEBUGUARTPRINT("\r\n*** BTM222 device name set to 'OOBD-Cup' ***");

		BufCnt = 0; /* set BTM222_RespBuffer back to zero */
		BTM222_UART_Rx_Flag = pdFALSE;

		/* send "atn?"-command to get BTM222 device name */
		USART_SendData(USART1, 'a');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, 't');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, 'n');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, '?');
		btm_uart_delay_ms(40);
		USART_SendData(USART1, '\r');
		btm_uart_delay_ms(100);

		BTM222_UART_Rx_Flag = pdTRUE;

	}

}

/*----------------------------------------------------------------------------*/

/**
 * @brief  Configures the different GPIO ports
 * @param  None
 * @retval None
 */
void USART2_Configuration(void) {

	USART_InitTypeDef USART_InitStructure;

	USART_DeInit(USART2); /* Reset Uart to default */

	/** USARTx configuration -----------------------------------------------------
	 * USARTx configured as follow:
	 *      - BaudRate = 10400 baud
	 *      - Word Length = 8 Bits
	 *      - One Stop Bit
	 *      - None parity
	 *      - Hardware flow control disabled (RTS and CTS signals)
	 *      - Receive and transmit enabled
	 */
	USART_InitStructure.USART_BaudRate = 10400;

	USART_InitStructure.USART_WordLength = USART_WordLength_8b;

	USART_InitStructure.USART_StopBits = USART_StopBits_1;

	USART_InitStructure.USART_Parity = USART_Parity_No;

	USART_InitStructure.USART_HardwareFlowControl
			= USART_HardwareFlowControl_None;

	USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;

	/* Configure USART2 */
	USART_Init(USART2, &USART_InitStructure);

	/* Enable the USART2 */
	USART_Cmd(USART2, ENABLE);

	/* Enable the USART2-Transmit interrupt: this interrupt is generated when the
	 USART1 transmit data register is empty */
	/* USART_ITConfig(USART2, USART_IT_TXE, ENABLE); */

	/* Enable the USART2-Receive interrupt: this interrupt is generated when the
	 USART2 receive data register is not empty */
	USART_ITConfig(USART2, USART_IT_RXNE, ENABLE);

	NVIC_Configuration();

}

/**
 * @brief  Configures the different GPIO ports
 * @param  None
 * @retval None
 */
void CAN1_Configuration(uint8_t CAN_BusConfig, uint8_t CAN_ModeConfig) {

	DEBUGUARTPRINT("\r\n*** CANx_Configuration (CAN1) entered! ***");

	CAN_InitTypeDef CAN_InitStructure;

	/* CAN register init */
	CAN_DeInit(CAN1);

	CAN_StructInit(&CAN_InitStructure);

	/* CAN cell init */
	CAN_InitStructure.CAN_TTCM = DISABLE; /* Time triggered communication mode */

	CAN_InitStructure.CAN_ABOM = DISABLE; /* Automatic bus-off management */

	CAN_InitStructure.CAN_AWUM = DISABLE; /* Automatic wakeup mode */

	CAN_InitStructure.CAN_NART = DISABLE; /* No automatic retransmission */

	CAN_InitStructure.CAN_RFLM = DISABLE; /* Receive FIFO locked mode */

	CAN_InitStructure.CAN_TXFP = ENABLE; /* Transmit FIFO priority */

	/* CAN_Mode_Normal, CAN_Mode_LoopBack, CAN_Mode_Silent, CAN_Mode_Silent_LoopBack */
	//  CAN_InitStructure.CAN_Mode = CAN_Mode_Normal;
	CAN_InitStructure.CAN_Mode = CAN_ModeConfig;

	CAN_InitStructure.CAN_SJW = CAN_SJW_1tq;

	if (CAN_BusConfig == VALUE_BUS_CONFIG_11bit_125kbit || CAN_BusConfig
			== VALUE_BUS_CONFIG_29bit_125kbit) {

		CAN_InitStructure.CAN_BS1 = CAN_BS1_3tq;

		CAN_InitStructure.CAN_BS2 = CAN_BS2_5tq;

		CAN_InitStructure.CAN_Prescaler = 32; /* BRP Baudrate prescaler */

	}

	else if (CAN_BusConfig == VALUE_BUS_CONFIG_11bit_250kbit || CAN_BusConfig
			== VALUE_BUS_CONFIG_29bit_250kbit) {

		CAN_InitStructure.CAN_BS1 = CAN_BS1_3tq;

		CAN_InitStructure.CAN_BS2 = CAN_BS2_5tq;

		CAN_InitStructure.CAN_Prescaler = 16; /* BRP Baudrate prescaler */

	}

	else if (CAN_BusConfig == VALUE_BUS_CONFIG_11bit_500kbit || CAN_BusConfig
			== VALUE_BUS_CONFIG_29bit_500kbit) {

		CAN_InitStructure.CAN_BS1 = CAN_BS1_3tq;

		CAN_InitStructure.CAN_BS2 = CAN_BS2_5tq;

		CAN_InitStructure.CAN_Prescaler = 8; /* BRP Baudrate prescaler */

	}

	else if (CAN_BusConfig == VALUE_BUS_CONFIG_11bit_1000kbit || CAN_BusConfig
			== VALUE_BUS_CONFIG_29bit_1000kbit) {

		CAN_InitStructure.CAN_BS1 = CAN_BS1_3tq;

		CAN_InitStructure.CAN_BS2 = CAN_BS2_5tq;

		CAN_InitStructure.CAN_Prescaler = 4; /* BRP Baudrate prescaler */

	}

	else {

		/* default CAN bus speed is set to 500kbaud */
		CAN_InitStructure.CAN_BS1 = CAN_BS1_3tq;

		CAN_InitStructure.CAN_BS2 = CAN_BS2_5tq;

		CAN_InitStructure.CAN_Prescaler = 8; /* BRP Baudrate prescaler */

	}

	CAN_Init(CAN1, &CAN_InitStructure);

	CAN_ITConfig(CAN1, CAN_IT_EWG, ENABLE); /* Enable Error warning interrupt */
	CAN_ITConfig(CAN1, CAN_IT_EPV, ENABLE); /* Enable Error passive interrupt */
	CAN_ITConfig(CAN1, CAN_IT_ERR, ENABLE); /* Enable Error interrupt */
	CAN_ITConfig(CAN1, CAN_IT_BOF, ENABLE); /* Enable Bus off interrupt */
	CAN_ITConfig(CAN1, CAN_IT_LEC, ENABLE); /* Enable Last Error code interrupt */
	CAN_ITConfig(CAN1, CAN_IT_FMP0, ENABLE); /* Enable FIFO message pending interrupt */
	CAN_ITConfig(CAN1, CAN_IT_TME, ENABLE); /* Enable CAN Transmit empty interrupt */

	DEBUGUARTPRINT("\r\n*** CANx_Configuration (CAN1) finished***");
}

/*----------------------------------------------------------------------------*/

void ADC_Configuration(void) {

	ADC_InitTypeDef ADC_InitStructure;

	/* PCLK2 is the APB2 clock */
	/* ADCCLK = PCLK2/6 = 72/6 = 12MHz */
	RCC_ADCCLKConfig(RCC_PCLK2_Div6);

	/* Enable ADC1 clock so that we can talk to it */
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_ADC1, ENABLE);

	/* Put everything back to power-on defaults */
	ADC_DeInit(ADC1);

	/* ADC1 Configuration ------------------------------------------------------ */
	/* ADC1 and ADC2 operate independently */
	ADC_InitStructure.ADC_Mode = ADC_Mode_Independent;

	/* Disable the scan conversion so we do one at a time */
	ADC_InitStructure.ADC_ScanConvMode = DISABLE;

	/* Don't do continuous conversions - do them on demand */
	ADC_InitStructure.ADC_ContinuousConvMode = DISABLE;

	/* Start conversion by software, not on external trigger */
	ADC_InitStructure.ADC_ExternalTrigConv = ADC_ExternalTrigConv_None;

	/* Conversions are 12 bit - put them in the lower 12 bits of the result */
	ADC_InitStructure.ADC_DataAlign = ADC_DataAlign_Right;

	/* Say how many channels would be used by the sequencer */
	ADC_InitStructure.ADC_NbrOfChannel = 1; /* Now do the setup */

	ADC_Init(ADC1, &ADC_InitStructure); /* Enable ADC1 */

	ADC_Cmd(ADC1, ENABLE); /* Enable ADC1 reset calibaration register */

	ADC_ResetCalibration(ADC1); /* Check the end of ADC1 reset calibration register */

	while (ADC_GetResetCalibrationStatus(ADC1))
		;

	/* Start ADC1 calibaration */
	ADC_StartCalibration(ADC1);

	/* Check the end of ADC1 calibration */
	while (ADC_GetCalibrationStatus(ADC1))
		;

}

void TIM2_Configuration(int Frequency) {
	TIM_TimeBaseInitTypeDef TIM_TimeBaseStructure;
	TIM_OCInitTypeDef TIM_OCInitStructure;
	uint16_t PrescalerValue = 0;
	uint16_t CCR4_Val = 322; /* TIM2 Channel4 duty cycle = (TIM2_CCR4/ TIM2_ARR)* 100 = 50% */

	/* Compute the prescaler value, set TIM2CLK to 2MHz */
	PrescalerValue = (uint16_t) (SystemCoreClock / 2000000) - 1;
	/* PWM Time base configuration based on TIM2CLK of 2MHz */
	TIM_TimeBaseStructure.TIM_Period = 2000000 / Frequency;
	TIM_TimeBaseStructure.TIM_Prescaler = PrescalerValue;
	TIM_TimeBaseStructure.TIM_ClockDivision = 0;
	TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up;

	TIM_TimeBaseInit(TIM2, &TIM_TimeBaseStructure);

	/* PWM1 Mode configuration: Channel4 */
	TIM_OCInitStructure.TIM_OCMode = TIM_OCMode_PWM1;
	TIM_OCInitStructure.TIM_OutputState = TIM_OutputState_Enable;
	TIM_OCInitStructure.TIM_Pulse = CCR4_Val;
	TIM_OCInitStructure.TIM_OCPolarity = TIM_OCPolarity_High; /* default: PWM Output high */
	TIM_OC4Init(TIM2, &TIM_OCInitStructure);

	TIM_OC4PreloadConfig(TIM2, TIM_OCPreload_Enable);

	/* TIM2 enable counter */
	TIM_Cmd(TIM2, DISABLE); /* default */

	TIM_CtrlPWMOutputs(TIM2, ENABLE);
}

void TIM3_Configuration(int Frequency) {
	TIM_TimeBaseInitTypeDef TIM_TimeBaseStructure;
	TIM_OCInitTypeDef TIM_OCInitStructure;
	uint16_t PrescalerValue = 0;
	uint16_t CCR1_Val = 322; /* TIM3 Channel1 duty cycle = (TIM3_CCR1/ TIM3_ARR)* 100 = 50% */

	/* Compute the prescaler value, set TIM3CLK to 2MHz */
	PrescalerValue = (uint16_t) (SystemCoreClock / 2000000) - 1;
	/* PWM Time base configuration based on TIM3CLK of 2MHz */
	TIM_TimeBaseStructure.TIM_Period = 2000000 / Frequency;
	TIM_TimeBaseStructure.TIM_Prescaler = PrescalerValue;
	TIM_TimeBaseStructure.TIM_ClockDivision = 0;
	TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up;

	TIM_TimeBaseInit(TIM3, &TIM_TimeBaseStructure);

	/* PWM2 Mode configuration: Channel1 */
	TIM_OCInitStructure.TIM_OCMode = TIM_OCMode_PWM1;
	TIM_OCInitStructure.TIM_OutputState = TIM_OutputState_Enable;
	TIM_OCInitStructure.TIM_Pulse = CCR1_Val;
	TIM_OCInitStructure.TIM_OCPolarity = TIM_OCPolarity_High; /* default: PWM Output high */
	TIM_OC1Init(TIM3, &TIM_OCInitStructure); /* Channel1 init */

	TIM_OC1PreloadConfig(TIM3, TIM_OCPreload_Enable); /* Channel1 Preload */

	/* TIM3 enable counter */
	TIM_Cmd(TIM3, DISABLE); /* default */

	TIM_CtrlPWMOutputs(TIM3, ENABLE);
}

/**
 * @brief  DeInitializes peripherals used by the I2C EEPROM driver.
 * @param  None
 * @retval None
 */
void sEE_LowLevel_DeInit(void) {

	GPIO_InitTypeDef GPIO_InitStructure;
	NVIC_InitTypeDef NVIC_InitStructure;

	/* sEE_I2C Peripheral Disable */
	I2C_Cmd(sEE_I2C, DISABLE);

	/* sEE_I2C DeInit */
	I2C_DeInit(sEE_I2C);

	/*!< sEE_I2C Periph clock disable */
	RCC_APB1PeriphClockCmd(sEE_I2C_CLK, DISABLE);

	/*!< GPIO configuration */
	/*!< Configure sEE_I2C pins: SCL */
	GPIO_InitStructure.GPIO_Pin = sEE_I2C_SCL_PIN;
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN_FLOATING;
	GPIO_Init(sEE_I2C_SCL_GPIO_PORT, &GPIO_InitStructure);

	/*!< Configure sEE_I2C pins: SDA */
	GPIO_InitStructure.GPIO_Pin = sEE_I2C_SDA_PIN;
	GPIO_Init(sEE_I2C_SDA_GPIO_PORT, &GPIO_InitStructure);

	/* Configure and enable I2C DMA TX Channel interrupt */
	NVIC_InitStructure.NVIC_IRQChannel = sEE_I2C_DMA_TX_IRQn;
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = sEE_I2C_DMA_PREPRIO;
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = sEE_I2C_DMA_SUBPRIO;
	NVIC_InitStructure.NVIC_IRQChannelCmd = DISABLE;
	NVIC_Init(&NVIC_InitStructure);

	/* Configure and enable I2C DMA RX Channel interrupt */
	NVIC_InitStructure.NVIC_IRQChannel = sEE_I2C_DMA_RX_IRQn;
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = sEE_I2C_DMA_PREPRIO;
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = sEE_I2C_DMA_SUBPRIO;
	NVIC_Init(&NVIC_InitStructure);

	/* Disable and Deinitialize the DMA channels */
	DMA_Cmd(sEE_I2C_DMA_CHANNEL_TX, DISABLE);
	DMA_Cmd(sEE_I2C_DMA_CHANNEL_RX, DISABLE);
	DMA_DeInit(sEE_I2C_DMA_CHANNEL_TX);
	DMA_DeInit(sEE_I2C_DMA_CHANNEL_RX);
}

/**
 * @brief  Initializes peripherals used by the I2C EEPROM driver.
 * @param  None
 * @retval None
 */
void sEE_LowLevel_Init(void) {

	GPIO_InitTypeDef GPIO_InitStructure;
	NVIC_InitTypeDef NVIC_InitStructure;

	/*!< sEE_I2C_SCL_GPIO_CLK and sEE_I2C_SDA_GPIO_CLK Periph clock enable */
	RCC_APB2PeriphClockCmd(sEE_I2C_SCL_GPIO_CLK | sEE_I2C_SDA_GPIO_CLK, ENABLE);

	/*!< sEE_I2C Periph clock enable */
	RCC_APB1PeriphClockCmd(sEE_I2C_CLK, ENABLE);

	/*!< GPIO configuration */
	/*!< Configure sEE_I2C pins: SCL */
	GPIO_InitStructure.GPIO_Pin = sEE_I2C_SCL_PIN;
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_OD;
	GPIO_Init(sEE_I2C_SCL_GPIO_PORT, &GPIO_InitStructure);

	/*!< Configure sEE_I2C pins: SDA */
	GPIO_InitStructure.GPIO_Pin = sEE_I2C_SDA_PIN;
	GPIO_Init(sEE_I2C_SDA_GPIO_PORT, &GPIO_InitStructure);

	/* Configure and enable I2C DMA TX Channel interrupt */
	NVIC_InitStructure.NVIC_IRQChannel = sEE_I2C_DMA_TX_IRQn;
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = sEE_I2C_DMA_PREPRIO;
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = sEE_I2C_DMA_SUBPRIO;
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
	NVIC_Init(&NVIC_InitStructure);

	/* Configure and enable I2C DMA RX Channel interrupt */
	NVIC_InitStructure.NVIC_IRQChannel = sEE_I2C_DMA_RX_IRQn;
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = sEE_I2C_DMA_PREPRIO;
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = sEE_I2C_DMA_SUBPRIO;
	NVIC_Init(&NVIC_InitStructure);

	/*!< I2C DMA TX and RX channels configuration */
	/* Enable the DMA clock */
	RCC_AHBPeriphClockCmd(sEE_I2C_DMA_CLK, ENABLE);

	/* I2C TX DMA Channel configuration */
	DMA_DeInit(sEE_I2C_DMA_CHANNEL_TX);
	sEEDMA_InitStructure.DMA_PeripheralBaseAddr = (uint32_t) sEE_I2C_DR_Address;
	sEEDMA_InitStructure.DMA_MemoryBaseAddr = (uint32_t) 0; /* This parameter will be configured durig communication */
	sEEDMA_InitStructure.DMA_DIR = DMA_DIR_PeripheralDST; /* This parameter will be configured durig communication */
	sEEDMA_InitStructure.DMA_BufferSize = 0xFFFF; /* This parameter will be configured durig communication */
	sEEDMA_InitStructure.DMA_PeripheralInc = DMA_PeripheralInc_Disable;
	sEEDMA_InitStructure.DMA_MemoryInc = DMA_MemoryInc_Enable;
	sEEDMA_InitStructure.DMA_PeripheralDataSize = DMA_MemoryDataSize_Byte;
	sEEDMA_InitStructure.DMA_MemoryDataSize = DMA_MemoryDataSize_Byte;
	sEEDMA_InitStructure.DMA_Mode = DMA_Mode_Normal;
	sEEDMA_InitStructure.DMA_Priority = DMA_Priority_VeryHigh;
	sEEDMA_InitStructure.DMA_M2M = DMA_M2M_Disable;
	DMA_Init(sEE_I2C_DMA_CHANNEL_TX, &sEEDMA_InitStructure);

	/* I2C RX DMA Channel configuration */
	DMA_DeInit(sEE_I2C_DMA_CHANNEL_RX);
	DMA_Init(sEE_I2C_DMA_CHANNEL_RX, &sEEDMA_InitStructure);

	/* Enable the DMA Channels Interrupts */
	DMA_ITConfig(sEE_I2C_DMA_CHANNEL_TX, DMA_IT_TC, ENABLE);
	DMA_ITConfig(sEE_I2C_DMA_CHANNEL_RX, DMA_IT_TC, ENABLE);
}

/**
 * @brief  Initializes DMA channel used by the I2C EEPROM driver.
 * @param  None
 * @retval None
 */
void sEE_LowLevel_DMAConfig(uint32_t pBuffer, uint32_t BufferSize,
		uint32_t Direction) {
	/* Initialize the DMA with the new parameters */
	if (Direction == sEE_DIRECTION_TX) {
		/* Configure the DMA Tx Channel with the buffer address and the buffer size */
		sEEDMA_InitStructure.DMA_MemoryBaseAddr = (uint32_t) pBuffer;
		sEEDMA_InitStructure.DMA_DIR = DMA_DIR_PeripheralDST;
		sEEDMA_InitStructure.DMA_BufferSize = (uint32_t) BufferSize;
		DMA_Init(sEE_I2C_DMA_CHANNEL_TX, &sEEDMA_InitStructure);
	} else {
		/* Configure the DMA Rx Channel with the buffer address and the buffer size */
		sEEDMA_InitStructure.DMA_MemoryBaseAddr = (uint32_t) pBuffer;
		sEEDMA_InitStructure.DMA_DIR = DMA_DIR_PeripheralSRC;
		sEEDMA_InitStructure.DMA_BufferSize = (uint32_t) BufferSize;
		DMA_Init(sEE_I2C_DMA_CHANNEL_RX, &sEEDMA_InitStructure);
	}

}

void btm_uart_delay_ms(uint16_t mSecs) {
	while (USART_GetFlagStatus(USART1, USART_FLAG_TXE) == RESET) {
	}
	delay_ms(mSecs);
}

void btm_uart_speed(char speed) {
	USART_SendData(USART1, 'a');
	btm_uart_delay_ms(40);
	USART_SendData(USART1, 't');
	btm_uart_delay_ms(40);
	USART_SendData(USART1, 'l');
	btm_uart_delay_ms(40);
	USART_SendData(USART1, speed);
	btm_uart_delay_ms(40);
	USART_SendData(USART1, '\r');
	btm_uart_delay_ms(100);
}

/* 72MHz = each clock cycle is 14ns. Loop is always 6 clock cycles?
 * These can get clock stretched if we have interrupts in the background.
 */
void delay_ms(const uint16_t ms) {
	uint32_t i = ms * 11905;
	while (i-- > 0) {
		asm("nop");
	}
}

void delay_us(const uint16_t us) {
	uint32_t i = us * 12;
	while (i-- > 0) {
		asm("nop");
	}
}
