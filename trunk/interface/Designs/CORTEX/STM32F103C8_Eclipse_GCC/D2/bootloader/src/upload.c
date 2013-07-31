/**
 ******************************************************************************
 * @file    IAP/src/upload.c
 * @author  MCD Application Team
 * @version V3.3.0
 * @date    10/15/2010
 * @brief   This file provides the software which allows to upload an image
 *          from internal Flash.
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
extern uint8_t HardwareIdent;

/* Private function prototypes -----------------------------------------------*/
/* Private functions ---------------------------------------------------------*/

/**
 * @brief  Upload a file via serial port.
 * @param  None
 * @retval None
 */
void SerialUpload(void) {
	uint32_t status = 0;

	SerialPutString("\n\n\rSelect Receive File ... (press any key to abort)\n\r");
	if (HardwareIdent == 1) /* Original DXM1 */
		GPIO_ResetBits(GPIOB, GPIO_Pin_5); /* LED 1 - red ON */
	if (HardwareIdent == 4 || HardwareIdent == 5) /* OOBD-Cup v5 & OOBD CAN Invader */
	{
		GPIO_SetBits(GPIOB, GPIO_Pin_5);   /* Duo-LED 2 - red ON */
		GPIO_ResetBits(GPIOB, GPIO_Pin_4); /* Duo-LED 2 - green OFF */
	}

	if (GetKey() == CRC16) {
		/* Transmit the flash image through ymodem protocol */
		status = Ymodem_Transmit((uint8_t*) ApplicationAddress,
				(const uint8_t*) "UploadedFlashImage.bin", FLASH_IMAGE_SIZE);
		if (status != 0) {
			SerialPutString("\n\rError Occured while Transmitting File\n\r");
			if (HardwareIdent == 1) /* Original DXM1 */
				GPIO_SetBits(GPIOB, GPIO_Pin_5);   /* LED 1 - red OFF */
			if (HardwareIdent == 4 || HardwareIdent == 5) /* OOBD-Cup v5 & OOBD CAN Invader */
			{
				GPIO_ResetBits(GPIOB, GPIO_Pin_5); /* Duo-LED2 - red OFF */
				GPIO_SetBits(GPIOB, GPIO_Pin_4);   /* Duo-LED2 - green ON */
			}
		} else {
			SerialPutString("\n\rFile Trasmitted Successfully \n\r");
			if (HardwareIdent == 1) /* Original DXM1 */
				GPIO_SetBits(GPIOB, GPIO_Pin_5);   /* LED 1 - red OFF */
			if (HardwareIdent == 4 || HardwareIdent == 5) /* OOBD-Cup v5 & OOBD CAN Invader */
			{
				GPIO_ResetBits(GPIOB, GPIO_Pin_5); /* Duo-LED2 - red OFF */
				GPIO_SetBits(GPIOB, GPIO_Pin_4);   /* Duo-LED2 - green ON */
			}
		}
	} else {
		SerialPutString("\r\n\nAborted by user.\n\r");
		if (HardwareIdent == 1) /* Original DXM1 */
			GPIO_SetBits(GPIOB, GPIO_Pin_5);   /* LED 1 - red OFF */
		if (HardwareIdent == 4 || HardwareIdent == 5) /* OOBD-Cup v5 & OOBD CAN Invader */
		{
			GPIO_ResetBits(GPIOB, GPIO_Pin_5); /* Duo-LED2 - red OFF */
			GPIO_SetBits(GPIOB, GPIO_Pin_4);   /* Duo-LED2 - green ON */
		}
	}
}

/**
 * @}
 */

/*******************(C)COPYRIGHT 2010 STMicroelectronics *****END OF FILE******/
