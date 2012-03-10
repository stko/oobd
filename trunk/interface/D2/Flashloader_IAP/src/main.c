/**
 ******************************************************************************
 * @file    IAP/src/main.c
 * @author  MCD Application Team
 * @version V3.3.0
 * @date    10/15/2010
 * @brief   Main program body
 ******************************************************************************
 * @copy
 *
 * THE PRESENT FIRMWARE WHICH IS FOR GUIDANCE ONLY AIMS AT PROVIDING CUSTOMERS
 * WITH CODING INFORMATION REGARDING THEIR PRODUCTS IN ORDER FOR THEM TO SAVE
 * TIME. AS A RESULT, STMICROELECTRONICS SHALL NOT BE HELD LIABLE FOR ANY
 * DIRECT, INDIRECT OR CONSEQUENTIAL DAMAGES WITH RESPECT TO ANY CLAIMS ARISING
 * FROM THE CONTENT OF SUCH FIRMWARE AND/OR THE USE MADE BY CUSTOMERS OF THE
 * CODING INFORMATION CONTAINED HEREIN IN CONNECTION WITH THEIR PRODUCTS.
 *
 * <h2><center>&copy; COPYRIGHT 2010 STMicroelectronics</center></h2>
 */

/** @addtogroup IAP
 * @{
 */

/* Includes ------------------------------------------------------------------*/
#include "stm32f10x_conf.h"
#include "common.h"

/* Private typedef -----------------------------------------------------------*/
/* Private define ------------------------------------------------------------*/
/* Private macro -------------------------------------------------------------*/
/* Private variables ---------------------------------------------------------*/
extern pFunction Jump_To_Application;
extern uint32_t JumpAddress;
extern uint8_t HardwareIdent;

/* Private function prototypes -----------------------------------------------*/
static void IAP_Init(void);

/* Private functions ---------------------------------------------------------*/

/**
 * @brief  Main program.
 * @param  None
 * @retval None
 */
int main(void) {
	uint32_t nCount, nLength = 1000000;

	/* Flash unlock */
	FLASH_Unlock();
	/* Execute the IAP driver in order to re-program the Flash */
	IAP_Init();

	/* send OOBD-Flashloader Version string on USART1 */SerialPutString("\r\nOOBD-Flashloader ");
	SerialPutString(OOBDDESIGN);
	SerialPutString(" ");
	SerialPutString(SVNREV);
	SerialPutString(" ");
	SerialPutString(BUILDDATE);

	while (1) {
		for (nCount = 0; nCount < nLength; nCount++) /* delay */
		{
			if (USART_GetFlagStatus(USART1, USART_FLAG_RXNE) != RESET) {
				if ((char) USART1->DR == 'f') {
					/* If Key is pressed */SerialPutString("\r\nOOBD-Flashloader>");
					Main_Menu();
				}
			}
		}

		/* Keep the user application running */
		/* Test if user code is programmed starting from address "ApplicationAddress" */
		if ((*(__IO uint32_t*) ApplicationAddress == 0x20005000)
				&& (CheckCrc32() == 0)) {
			/* Jump to user application */
			JumpAddress = *(__IO uint32_t*) (ApplicationAddress + 4);
			Jump_To_Application = (pFunction) JumpAddress;
			/* Initialize user application's Stack Pointer */
			__set_MSP(*(__IO uint32_t*) ApplicationAddress);
			Jump_To_Application();
		} else /* jump into flashloader if no valid application available */
		{
			SerialPutString("\r\nOOBD-Flashloader>");
			Main_Menu();
		}
	}
}

/**
 * @brief  Initialize the IAP: Configure RCC, USART and GPIOs.
 * @param  None
 * @retval None
 */
void IAP_Init(void) {
	GPIO_InitTypeDef GPIO_InitStructure;
	USART_InitTypeDef USART_InitStructure;

	/* GPIOx clocks enable */
	RCC_APB2PeriphClockCmd(
			RCC_APB2Periph_GPIOA | RCC_APB2Periph_GPIOB | RCC_APB2Periph_GPIOC
					| RCC_APB2Periph_AFIO, ENABLE);

	/* Configure all unused GPIO port pins in Analog Input mode (floating input
	 trigger OFF), this will reduce the power consumption and increase the
	 device immunity against EMI/EMC ******************************************/
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_All;
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AIN;
	GPIO_Init(GPIOA, &GPIO_InitStructure);
	GPIO_Init(GPIOB, &GPIO_InitStructure);
	GPIO_Init(GPIOC, &GPIO_InitStructure);

	/* Enable USART1 Clock */
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_USART1, ENABLE);

	/* initialize USART1 on PA9 (USART1_TX) and PA10 (USART1_RX) */
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP;
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_9;
	GPIO_Init(GPIOA, &GPIO_InitStructure);

	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN_FLOATING;
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_10;
	GPIO_Init(GPIOA, &GPIO_InitStructure);

	/* configure PA8 as Input for Hardwareidentifikaton
	 * PA8 = 1 - Original DXM1
	 * PA8 = 0 - OOBD-Cup v5
	 */
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN_FLOATING;
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_8;
	GPIO_Init(GPIOA, &GPIO_InitStructure);

	/* identify on which hardware the flashloader is running */
	if (GPIO_ReadInputDataBit(GPIOA, GPIO_Pin_8) == Bit_RESET) {
		/* configure DXM1-Output (open drain) of LED1 - green (PB5) and LED2 - red (PB4) */
		GPIO_PinRemapConfig(GPIO_Remap_SWJ_NoJTRST, ENABLE); /* release alternative GPIO function of PB4 */
		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_4 | GPIO_Pin_5;
		GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;
		GPIO_Init(GPIOB, &GPIO_InitStructure);
		HardwareIdent = 2; /* OOBD-Cup v5 */
	} else if (GPIO_ReadInputDataBit(GPIOA, GPIO_Pin_8) == Bit_SET) {
		/* configure DXM1-Output (open drain) of LED1 - green (PB5) and LED2 - red (PB4) */
		GPIO_PinRemapConfig(GPIO_Remap_SWJ_NoJTRST, ENABLE); /* release alternative GPIO function of PB4 */
		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_4 | GPIO_Pin_5;
		GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_OD;
		GPIO_Init(GPIOB, &GPIO_InitStructure);
		HardwareIdent = 1; /* Original DXM1 */
	} else
		HardwareIdent = 0;

	if (HardwareIdent == 1) {/* Original DXM1 */
		GPIO_SetBits(GPIOB, GPIO_Pin_5); /* LED 1 - red OFF */
		GPIO_ResetBits(GPIOB, GPIO_Pin_4); /* LED 2 - green ON */
	}
	if (HardwareIdent == 2) { /* OOBD-Cup v5 */
		GPIO_ResetBits(GPIOB, GPIO_Pin_5); /* Duo-LED 2 - red OFF */
		GPIO_SetBits(GPIOB, GPIO_Pin_4); /* Duo-LED 2 - green ON */
	}
	/* USART resources configuration (Clock, GPIO pins and USART registers) ----*/
	/* USART configured as follow:
	 - BaudRate = 115200 baud
	 - Word Length = 8 Bits
	 - One Stop Bit
	 - No parity
	 - Hardware flow control disabled (RTS and CTS signals)
	 - Receive and transmit enabled
	 */
	USART_InitStructure.USART_BaudRate = 115200;
	USART_InitStructure.USART_WordLength = USART_WordLength_8b;
	USART_InitStructure.USART_StopBits = USART_StopBits_1;
	USART_InitStructure.USART_Parity = USART_Parity_No;
	USART_InitStructure.USART_HardwareFlowControl
			= USART_HardwareFlowControl_None;
	USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;

	/* USART configuration */
	USART_Init(USART1, &USART_InitStructure);

	/* Enable the USART1 */
	USART_Cmd(USART1, ENABLE);
}

#ifdef USE_FULL_ASSERT
/**
 * @brief  Reports the name of the source file and the source line number
 *         where the assert_param error has occurred.
 * @param  file: pointer to the source file name
 * @param  line: assert_param error line source number
 * @retval None
 */
void assert_failed(uint8_t* file, uint32_t line)
{
	/* User can add his own implementation to report the file name and line number,
	 ex: printf("Wrong parameters value: file %s on line %d\r\n", file, line) */

	/* Infinite loop */
	while (1)
	{
	}
}
#endif

/**
 * @}
 */

/******************* (C) COPYRIGHT 2010 STMicroelectronics *****END OF FILE****/
