/**
 ******************************************************************************
 * @file    IAP/src/common.c
 * @author  MCD Application Team
 * @version V3.3.0
 * @date    10/15/2010
 * @brief   This file provides all the common functions.
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
#include "ymodem.h"
#include "stm32f10x_conf.h"

/* Private typedef -----------------------------------------------------------*/
/* Private define ------------------------------------------------------------*/
/* Private macro -------------------------------------------------------------*/
/* Private variables ---------------------------------------------------------*/
pFunction Jump_To_Application;
uint32_t JumpAddress;
uint8_t HardwareIdent;
uint32_t BlockNbr = 0, UserMemoryMask = 0;
__IO uint32_t FlashProtection = 0;
extern uint32_t FlashDestination;

/* Private function prototypes -----------------------------------------------*/
/* Private functions ---------------------------------------------------------*/

/**
 * @brief  Convert an Integer to a string
 * @param  str: The string
 * @param  intnum: The intger to be converted
 * @retval None
 */
void Int2Str(uint8_t* str, int32_t intnum) {
	uint32_t i, Div = 1000000000, j = 0, Status = 0;

	for (i = 0; i < 10; i++) {
		str[j++] = (intnum / Div) + 48;

		intnum = intnum % Div;
		Div /= 10;
		if ((str[j - 1] == '0') & (Status == 0)) {
			j = 0;
		} else {
			Status++;
		}
	}
}

/**
 * @brief  Convert a string to an integer
 * @param  inputstr: The string to be converted
 * @param  intnum: The integer value
 * @retval 1: Correct
 *         0: Error
 */
uint32_t Str2Int(uint8_t *inputstr, int32_t *intnum) {
	uint32_t i = 0, res = 0;
	uint32_t val = 0;

	if (inputstr[0] == '0' && (inputstr[1] == 'x' || inputstr[1] == 'X')) {
		if (inputstr[2] == '\0') {
			return 0;
		}
		for (i = 2; i < 11; i++) {
			if (inputstr[i] == '\0') {
				*intnum = val;
				/* return 1; */
				res = 1;
				break;
			}
			if (ISVALIDHEX(inputstr[i])) {
				val = (val << 4) + CONVERTHEX(inputstr[i]);
			} else {
				/* return 0, Invalid input */
				res = 0;
				break;
			}
		}
		/* over 8 digit hex --invalid */
		if (i >= 11) {
			res = 0;
		}
	} else /* max 10-digit decimal input */
	{
		for (i = 0; i < 11; i++) {
			if (inputstr[i] == '\0') {
				*intnum = val;
				/* return 1 */
				res = 1;
				break;
			} else if ((inputstr[i] == 'k' || inputstr[i] == 'K') && (i > 0)) {
				val = val << 10;
				*intnum = val;
				res = 1;
				break;
			} else if ((inputstr[i] == 'm' || inputstr[i] == 'M') && (i > 0)) {
				val = val << 20;
				*intnum = val;
				res = 1;
				break;
			} else if (ISVALIDDEC(inputstr[i])) {
				val = val * 10 + CONVERTDEC(inputstr[i]);
			} else {
				/* return 0, Invalid input */
				res = 0;
				break;
			}
		}
		/* Over 10 digit decimal --invalid */
		if (i >= 11) {
			res = 0;
		}
	}

	return res;
}

/**
 * @brief  Get an integer from the HyperTerminal
 * @param  num: The integer
 * @retval 1: Correct
 *         0: Error
 */
uint32_t GetIntegerInput(int32_t * num) {
	uint8_t inputstr[16];

	while (1) {
		GetInputString(inputstr);
		if (inputstr[0] == '\0')
			continue;
		if ((inputstr[0] == 'a' || inputstr[0] == 'A') && inputstr[1] == '\0') {
			SerialPutString("User Cancelled \r\n");
			return 0;
		}

		if (Str2Int(inputstr, num) == 0) {
			SerialPutString("Error, Input again: \r\n");
		} else {
			return 1;
		}
	}
}

/**
 * @brief  Test to see if a key has been pressed on the HyperTerminal
 * @param  key: The key pressed
 * @retval 1: Correct
 *         0: Error
 */
uint32_t SerialKeyPressed(uint8_t *key) {

	if (USART_GetFlagStatus(USART1, USART_FLAG_RXNE) != RESET) {
		*key = (uint8_t) USART1->DR;
		return 1;
	} else {
		return 0;
	}
}

/**
 * @brief  Get a key from the HyperTerminal
 * @param  None
 * @retval The Key Pressed
 */
uint8_t GetKey(void) {
	uint8_t key = 0;

	/* Waiting for user input */
	while (1) {
		if (SerialKeyPressed((uint8_t*) &key))
			break;
	}
	return key;

}

/**
 * @brief  Print a character on the HyperTerminal
 * @param  c: The character to be printed
 * @retval None
 */
void SerialPutChar(uint8_t c) {
	USART_SendData(USART1, c);
	while (USART_GetFlagStatus(USART1, USART_FLAG_TXE) == RESET) {
	}
}

/**
 * @brief  Print a string on the HyperTerminal
 * @param  s: The string to be printed
 * @retval None
 */
void Serial_PutString(uint8_t *s) {
	while (*s != '\0') {
		SerialPutChar(*s);
		s++;
	}
}

/**
 * @brief  Get Input string from the HyperTerminal
 * @param  buffP: The input string
 * @retval None
 */
void GetInputString(uint8_t * buffP) {
	uint32_t bytes_read = 0;
	uint8_t c = 0;
	do {
		c = GetKey();
		if (c == '\r')
			break;
		if (c == '\b') /* Backspace */
		{
			if (bytes_read > 0) {
				SerialPutString("\b \b");
				bytes_read--;
			}
			continue;
		}
		if (bytes_read >= CMD_STRING_SIZE) {
			SerialPutString("Command string size overflow\r\n");
			bytes_read = 0;
			continue;
		}
		if (c >= 0x20 && c <= 0x7E) {
			buffP[bytes_read++] = c;
			SerialPutChar(c);
		}
	} while (1);
	SerialPutString(("\n\r"));
	buffP[bytes_read] = '\0';
}

/**
 * @brief  Calculate the number of pages
 * @param  Size: The image size
 * @retval The number of pages
 */
uint32_t FLASH_PagesMask(__IO uint32_t Size) {
	uint32_t pagenumber = 0x0;
	uint32_t size = Size;

	if ((size % PAGE_SIZE) != 0) {
		pagenumber = (size / PAGE_SIZE) + 1;
	} else {
		pagenumber = size / PAGE_SIZE;
	}
	return pagenumber;

}

/**
 * @brief  Disable the write protection of desired pages
 * @param  None
 * @retval None
 */
void FLASH_DisableWriteProtectionPages(void) {
	uint32_t useroptionbyte = 0, WRPR = 0;
	uint16_t var1 = OB_IWDG_SW, var2 = OB_STOP_NoRST, var3 = OB_STDBY_NoRST;
	FLASH_Status status = FLASH_BUSY;

	WRPR = FLASH_GetWriteProtectionOptionByte();

	/* Test if user memory is write protected */
	if ((WRPR & UserMemoryMask) != UserMemoryMask) {
		useroptionbyte = FLASH_GetUserOptionByte();

		UserMemoryMask |= WRPR;

		status = FLASH_EraseOptionBytes();

		if (UserMemoryMask != 0xFFFFFFFF) {
			status = FLASH_EnableWriteProtection((uint32_t) ~UserMemoryMask);
		}

		/* Test if user Option Bytes are programmed */
		if ((useroptionbyte & 0x07) != 0x07) {
			/* Restore user Option Bytes */
			if ((useroptionbyte & 0x01) == 0x0) {
				var1 = OB_IWDG_HW;
			}
			if ((useroptionbyte & 0x02) == 0x0) {
				var2 = OB_STOP_RST;
			}
			if ((useroptionbyte & 0x04) == 0x0) {
				var3 = OB_STDBY_RST;
			}

			FLASH_UserOptionByteConfig(var1, var2, var3);
		}

		if (status == FLASH_COMPLETE) {
			SerialPutString("\r\nWrite Protection disabled...");

			SerialPutString("\r\n...and a System Reset will be generated to re-load the new option bytes");

			/* Generate System Reset to load the new option byte values */
			NVIC_SystemReset();
		} else {
			SerialPutString("\r\nError: Flash write unprotection failed...");
		}
	} else {
		SerialPutString("\r\nFlash memory not write protected");
	}
}

/**
 * @brief  Display the Main Menu on to HyperTerminal
 * @param  None
 * @retval None
 */
void Main_Menu(void) {
	uint8_t key = 0;

	/* Get the number of block (4 or 2 pages) from where the user program will be loaded */
	BlockNbr = (FlashDestination - 0x08000000) >> 12;

	/* Compute the mask to test if the Flash memory, where the user program will be
	 loaded, is write protected */
#if defined (STM32F10X_MD) || defined (STM32F10X_MD_VL)
	UserMemoryMask = ((uint32_t) ~((1 << BlockNbr) - 1));
#else /* USE_STM3210E_EVAL */
	if (BlockNbr < 62)
	{
		UserMemoryMask = ((uint32_t)~((1 << BlockNbr) - 1));
	}
	else
	{
		UserMemoryMask = ((uint32_t)0x80000000);
	}
#endif /* (STM32F10X_MD) || (STM32F10X_MD_VL) */

	/* Test if any page of Flash memory where program user will be loaded is write protected */
	if ((FLASH_GetWriteProtectionOptionByte() & UserMemoryMask)
			!= UserMemoryMask) {
		FlashProtection = 1;
	} else {
		FlashProtection = 0;
	}

	while (1) {
		key = GetKey();

		if (key == 0x31) /* ASCII character "1" */
		{
			/* Download user application in the Flash */
			SerialDownload();
		} else if (key == 0x32) /* ASCII character "2" */
		{
			/* Upload user application from the Flash */
			SerialUpload();
		} else if (key == 0x33) /* ASCII character "3" */
		{
			/* Test if user code is programmed starting from address "ApplicationAddress" */
			if ((*(__IO uint32_t*) ApplicationAddress == 0x20005000)
				&& (CheckCrc32() == 0)) {
				JumpAddress = *(__IO uint32_t*) (ApplicationAddress + 4);
				/* Jump to user application */
				Jump_To_Application = (pFunction) JumpAddress;
				/* Initialize user application's Stack Pointer */
				__set_MSP(*(__IO uint32_t*) ApplicationAddress);
				Jump_To_Application();
			}
			else /* jump into flashloader if no valid application available */
			{
				SerialPutString("\r\nOOBD-Flashloader>");
				Main_Menu();
			}
		} else if (key == 0x56) /* ASCII character "V" */
		{
			OOBD_BL_Version();
			SerialPutString("\r\nOOBD-Flashloader>");
		} else if ((key == 0x34) && (FlashProtection == 1)) {
			/* Disable the write protection of desired pages */
			FLASH_DisableWriteProtectionPages();
		} else if ((key == 0x68) || (key == 0x48)) {
			SerialPutString("\r\n======================== Help ============================\r\n\n");
			SerialPutString("  Download Image To the STM32F10x Internal Flash ------- 1\r\n\n");
			SerialPutString("  Upload Image From the STM32F10x Internal Flash ------- 2\r\n\n");
			SerialPutString("  Execute The New Program ------------------------------ 3\r\n\n");
			SerialPutString("  Bootloader version string ---------------------------- V\r\n\n");
			if (FlashProtection != 0) {
				SerialPutString("  Disable the write protection ------------------------- 4\r\n\n");
			}
			SerialPutString("==========================================================\r\n\n");
			SerialPutString("OOBD-Flashloader>");
		} else {
			SerialPutString("\r\nOOBD-Flashloader>");
		}
	}
}

/**
 * @brief  Resets the CRC Data register (DR).
 * @param  None
 * @retval None
 */
void CRC_ResetDR(void) {
	/* Reset CRC generator */CRC->CR = CRC_CR_RESET;
}

/**
 * @brief  Computes the 32-bit CRC of a given buffer of data word(32-bit).
 * @param  pBuffer: pointer to the buffer containing the data to be computed
 * @param  BufferLength: length of the buffer to be computed
 * @retval 32-bit CRC
 */
uint32_t CRC_CalcBlockCRC(uint32_t pBuffer[], uint32_t BufferLength) {
	uint32_t index = 0;

	for (index = 0; index < BufferLength; index++) {
		CRC->DR = pBuffer[index];
	}
	return (CRC->DR);
}

/**
 * @brief  Check Flash-ROM against CRC-32 checksum
 * @param  None
 * @retval crc - CRC-32 checksum
 */

uint32_t CheckCrc32(void) {
	uint32_t size;
	uint32_t crc;

	RCC_AHBPeriphClockCmd(RCC_AHBPeriph_CRC, ENABLE);

	/* application size (4byte) for CRC checking is stored on flash in front of the application */
	size = *(__IO uint32_t*) (ApplicationAddress -4);

	CRC_ResetDR();
	/* 0x8002500 is the application start address and size = application code size */
	crc = CRC_CalcBlockCRC((uint32_t*) ApplicationAddress, size / 4 + 1);
	RCC_AHBPeriphClockCmd(RCC_AHBPeriph_CRC, DISABLE);

	return crc;
}

/**
 * @brief  Check Flash-ROM against CRC-32 checksum
 * @param  None
 * @retval crc - CRC-32 checksum
 */
void OOBD_BL_Version(void) {
	/* send OOBD-Flashloader Version string on USART1 */SerialPutString("\r\nOOBD-Flashloader ");
	SerialPutString(OOBDDESIGN);
	SerialPutString(" ");
	SerialPutString(SVNREV);
	SerialPutString(" ");
	SerialPutString(BUILDDATE);
}

/**
 * @}
 */

/*******************(C)COPYRIGHT 2010 STMicroelectronics *****END OF FILE******/
