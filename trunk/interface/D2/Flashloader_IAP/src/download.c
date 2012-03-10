/**
 ******************************************************************************
 * @file    IAP/src/download.c
 * @author  MCD Application Team
 * @version V3.3.0
 * @date    10/15/2010
 * @brief   This file provides the software which allows to download an image
 *          to internal Flash.
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
#include "common.h"
#include "stm32f10x_gpio.h"

/* Private typedef -----------------------------------------------------------*/
/* Private define ------------------------------------------------------------*/
/* Private macro -------------------------------------------------------------*/
/* Private variables ---------------------------------------------------------*/
extern uint8_t file_name[FILE_NAME_LENGTH];
uint8_t tab_1024[1024] = { 0 };
extern uint8_t HardwareIdent;

/* Private function prototypes -----------------------------------------------*/
/* Private functions ---------------------------------------------------------*/

/**
 * @brief  Download a file via serial port
 * @param  None
 * @retval None
 */
void SerialDownload(void) {
	uint8_t Number[10] = "          ";
	int32_t Size = 0;

	SerialPutString("\r\nWaiting for the file to be sent ... (press 'a' to abort)\n\r");
	if (HardwareIdent == 1) /* Original DXM1 */
		GPIO_ResetBits(GPIOB, GPIO_Pin_5); /* LED 1 - red ON */
	if (HardwareIdent == 2) /* OOBD-Cup2 */
	{
		GPIO_SetBits(GPIOB, GPIO_Pin_5); /* Duo-LED 2 - red ON */
		GPIO_ResetBits(GPIOB, GPIO_Pin_4); /* Duo-LED 2 - green OFF */
	}

	Size = Ymodem_Receive(&tab_1024[0]);
	if (Size > 0) {
		SerialPutString("\n\n\r Programming Completed Successfully!\n\r--------------------------------\r\n Name: ");
		SerialPutString(file_name);
		Int2Str(Number, Size);
		SerialPutString("\n\r Size: ");
		SerialPutString(Number);
		SerialPutString(" Bytes\r\n");
		SerialPutString("--------------------------------\n\r");
		SerialPutString("\r\nOOBD-Flashloader>");
		if (HardwareIdent == 1) /* Original DXM1 */
			GPIO_SetBits(GPIOB, GPIO_Pin_5); /* LED 1 - red OFF */
		if (HardwareIdent == 2) /* OOBD-Cup2 */
		{
			GPIO_ResetBits(GPIOB, GPIO_Pin_5); /* Duo-LED2 - red OFF */
			GPIO_SetBits(GPIOB, GPIO_Pin_4); /* Duo-LED2 - green ON */
		}
	} else if (Size == -1) {
		SerialPutString("\n\n\rThe image size is higher than the allowed space memory!\n\r");
		if (HardwareIdent == 1) /* Original DXM1 */
			GPIO_SetBits(GPIOB, GPIO_Pin_5); /* LED 1 - red OFF */
		if (HardwareIdent == 2) /* OOBD-Cup2 */
		{
			GPIO_ResetBits(GPIOB, GPIO_Pin_5); /* Duo-LED2 - red OFF */
			GPIO_SetBits(GPIOB, GPIO_Pin_4); /* Duo-LED2 - green ON */
		}
	} else if (Size == -2) {
		SerialPutString("\n\n\rVerification failed!\n\r");
		if (HardwareIdent == 1) /* Original DXM1 */
			GPIO_SetBits(GPIOB, GPIO_Pin_5); /* LED 1 - red OFF */
		if (HardwareIdent == 2) /* OOBD-Cup2 */
		{
			GPIO_ResetBits(GPIOB, GPIO_Pin_5); /* Duo-LED2 - red OFF */
			GPIO_SetBits(GPIOB, GPIO_Pin_4); /* Duo-LED2 - green ON */
		}
	} else if (Size == -3) {
		SerialPutString("\r\n\nAborted by user.\n\r");
		if (HardwareIdent == 1) /* Original DXM1 */
			GPIO_SetBits(GPIOB, GPIO_Pin_5); /* LED 1 - red OFF */
		if (HardwareIdent == 2) /* OOBD-Cup2 */
		{
			GPIO_ResetBits(GPIOB, GPIO_Pin_5); /* Duo-LED2 - red OFF */
			GPIO_SetBits(GPIOB, GPIO_Pin_4); /* Duo-LED2 - green ON */
		}
	} else {
		SerialPutString("\n\rFailed to receive the file!\n\r");
		if (HardwareIdent == 1) /* Original DXM1 */
			GPIO_SetBits(GPIOB, GPIO_Pin_5); /* LED 1 - red OFF */
		if (HardwareIdent == 2) /* OOBD-Cup2 */
		{
			GPIO_ResetBits(GPIOB, GPIO_Pin_5); /* Duo-LED2 - red OFF */
			GPIO_SetBits(GPIOB, GPIO_Pin_4); /* Duo-LED2 - green ON */
		}
	}
}
/**
 * @}
 */

/*******************(C)COPYRIGHT 2010 STMicroelectronics *****END OF FILE******/
