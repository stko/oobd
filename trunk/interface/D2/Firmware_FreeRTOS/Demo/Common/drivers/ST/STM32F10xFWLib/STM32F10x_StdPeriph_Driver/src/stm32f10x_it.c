/**
  ******************************************************************************
  * @file    Project/STM32F10x_StdPeriph_Template/stm32f10x_it.c 
  * @author  MCD Application Team
  * @version V3.4.0
  * @date    10/15/2010
  * @brief   Main Interrupt Service Routines.
  *          This file provides template for all exceptions handler and 
  *          peripherals interrupt service routine.
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

/* Includes ------------------------------------------------------------------*/
#include "stm32f10x_it.h"

/** @addtogroup STM32F10x_StdPeriph_Template
  * @{
  */

/* Private typedef -----------------------------------------------------------*/
/* Private define ------------------------------------------------------------*/
/* Private macro -------------------------------------------------------------*/
/* Private variables ---------------------------------------------------------*/
/* Private function prototypes -----------------------------------------------*/
/* Private functions ---------------------------------------------------------*/

/******************************************************************************/
/*            Cortex-M3 Processor Exceptions Handlers                         */
/******************************************************************************/

/**
  * @brief   This function handles NMI exception.
  * @param  None
  * @retval None
  */
void NMI_Handler(void)
{
}

/**
  * @brief  This function handles Hard Fault exception.
  * @param  None
  * @retval None
  */
void HardFault_Handler(void)
{
  /* Go to infinite loop when Hard Fault exception occurs */
  while (1)
  {
  }
}

/**
  * @brief  This function handles Memory Manage exception.
  * @param  None
  * @retval None
  */
void MemManage_Handler(void)
{
  /* Go to infinite loop when Memory Manage exception occurs */
  while (1)
  {
  }
}

/**
  * @brief  This function handles Bus Fault exception.
  * @param  None
  * @retval None
  */
void BusFault_Handler(void)
{
  /* Go to infinite loop when Bus Fault exception occurs */
  while (1)
  {
  }
}

/**
  * @brief  This function handles Usage Fault exception.
  * @param  None
  * @retval None
  */
void UsageFault_Handler(void)
{
  /* Go to infinite loop when Usage Fault exception occurs */
  while (1)
  {
  }
}

/**
  * @brief  This function handles SVCall exception.
  * @param  None
  * @retval None
  */
/** definition within FreeRTOS port.c
void SVC_Handler(void)
{
}
*/
/**
  * @brief  This function handles Debug Monitor exception.
  * @param  None
  * @retval None
  */
void DebugMon_Handler(void)
{
}

/**
  * @brief  This function handles PendSVC exception.
  * @param  None
  * @retval None
  */
/** definition within FreeRTOS port.c
void PendSV_Handler(void)
{
}
*/

/**
  * @brief  This function handles SysTick Handler.
  * @param  None
  * @retval None
  */
/** definition within FreeRTOS port.c
void SysTick_Handler(void)
{
}
*/

/**
  * @brief  This function handles PPP interrupt request.
  * @param  None
  * @retval None
  */
void USB_LP_CAN1_RX0_IRQHandler(void)
{
	uint8_t i = 0;
	CanRxMsg RxMessage;
	CanTxMsg TxMessage;

	#ifdef DEBUG_SERIAL
		uart1_puts("\r\n*** USB_LP_CAN1_RX0_IRQHandler starting ***");
	#endif

	RxMessage.StdId = 0x00;
	RxMessage.ExtId = 0x00;
	RxMessage.IDE = CAN_ID_STD;
	RxMessage.DLC = 0;
	RxMessage.FMI = 0;
	for (i = 0; i < 8; i++)
	{
	  RxMessage.Data[i] = 0x00;
	}

	CAN_Receive(CAN1, CAN_FIFO0, &RxMessage);

	if ((RxMessage.StdId == 0x321)&&(RxMessage.IDE == CAN_ID_STD)&&(RxMessage.DLC == 1)&&(RxMessage.Data[0] == 0x55))
	{
	    TxMessage.StdId   = 0x7eF;
	    TxMessage.ExtId   = 0x01;
	    TxMessage.RTR     = CAN_RTR_DATA;
	    TxMessage.IDE     = CAN_ID_STD;
	    TxMessage.DLC     = 4;
	    TxMessage.Data[0] = (RxMessage.StdId >> 8) & 0xFF;
	    TxMessage.Data[1] =  RxMessage.StdId       & 0xFF;
	    TxMessage.Data[2] =  RxMessage.DLC;
	    TxMessage.Data[3] =  RxMessage.Data[0];
	    CAN_Transmit(CAN1, &TxMessage);
	}
	else
	{
	    TxMessage.StdId   = 0x7eF;
	    TxMessage.ExtId   = 0x01;
	    TxMessage.RTR     = CAN_RTR_DATA;
	    TxMessage.IDE     = CAN_ID_STD;
	    TxMessage.DLC     = 4;
	    TxMessage.Data[0] = (RxMessage.StdId >> 8) & 0xFF;
	    TxMessage.Data[1] =  RxMessage.StdId       & 0xFF;
	    TxMessage.Data[2] =  RxMessage.DLC;
	    TxMessage.Data[3] =  RxMessage.Data[0];
	    CAN_Transmit(CAN1, &TxMessage);
	}

	#ifdef DEBUG_SERIAL
		uart1_puts("\r\n*** USB_LP_CAN1_RX0_IRQHandler finished ***");
	#endif
}

/******************************************************************************/
/*                 STM32F10x Peripherals Interrupt Handlers                   */
/*  Add here the Interrupt Handler for the used peripheral(s) (PPP), for the  */
/*  available peripheral interrupt handler's name please refer to the startup */
/*  file (startup_stm32f10x_xx.s).                                            */
/******************************************************************************/

/**
  * @brief  This function handles PPP interrupt request.
  * @param  None
  * @retval None
  */
/*void PPP_IRQHandler(void)
{
}*/

/**
  * @}
  */ 


/******************* (C) COPYRIGHT 2010 STMicroelectronics *****END OF FILE****/
