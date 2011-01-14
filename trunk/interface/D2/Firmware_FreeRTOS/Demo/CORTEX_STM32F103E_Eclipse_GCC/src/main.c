/**
  ******************************************************************************
  * @file main.c
  * @author  DBr
  * @version  V0.0.1
  * @date  04/24/2009
  * @brief  Main program body.
  ******************************************************************************
  *
  */


/* Includes ------------------------------------------------------------------*/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "stm32f10x.h"

/* Scheduler includes. */
#include "portmacro.h"
#include "FreeRTOS.h"
#include "task.h"
#include "queue.h"
#include "SerialComm.h"
#include "SystemConfig.h"

/* Exported types ------------------------------------------------------------*/
/* Exported constants --------------------------------------------------------*/
/* Exported macro ------------------------------------------------------------*/
/* Exported functions ------------------------------------------------------- */
  CanTxMsg TxMessage;

/* The rate at which the flash task toggles the LED. */
#define mainBlinkLed_DELAY					( ( portTickType ) 1000 / portTICK_RATE_MS )
#define mainBlinkLed_TASK_PRIORITY			( tskIDLE_PRIORITY + 2 )
#define mainDcfInterptreter_TASK_PRIORITY	( tskIDLE_PRIORITY + 3 )

// static void prvBlinkLedTask( void *pvParameters );
void Timer4Init(void);
void RTC_Init(void);
uint32_t RTC_GetCounter();
void CAN_Debug_Send_All_Primer_Data(void);

/**
  * @brief  Inserts a delay time.
  * @param  nCount: specifies the delay time length.
  * @retval None
  */
void Delay(__IO uint32_t nCount)
{
  for(; nCount != 0; nCount--);
}

/**
  * @brief  Initializes a Rx Message.
  * @param  CanRxMsg *RxMessage
  * @retval None
  */
void Init_RxMes(CanRxMsg *RxMessage)
{
  uint8_t i = 0;

  RxMessage->StdId = 0x00;
  RxMessage->ExtId = 0x00;
  RxMessage->IDE = CAN_ID_STD;
  RxMessage->DLC = 0;
  RxMessage->FMI = 0;
  for (i = 0;i < 8;i++)
    RxMessage->Data[i] = 0x00;
}

int main (void)
{
//	uint32_t tmp;

	/*!< At this stage the microcontroller clock setting is already configured,
	       this is done through SystemInit() function which is called from startup
	       file (startup_stm32f10x_xx.s) before to branch to application main.
	       To reconfigure the default setting of SystemInit() function, refer to
	       system_stm32f10x.c file
	*/

		/* Output low -> LED on */
//		GPIO_ResetBits(GPIOB,GPIO_Pin_4);
//		GPIO_ResetBits(GPIOB,GPIO_Pin_5);
//		Delay(1000000);
		/* Output open drain - high -> LED off */
//		GPIO_SetBits(GPIOB,GPIO_Pin_4);
//	    GPIO_SetBits(GPIOB,GPIO_Pin_5);
//		Delay(1000000);

	/* Initialize i.e. GPIO, CAN, USART1 */
	System_Configuration();

	/* CAN configuration */
//	Configuration_CAN();
//	CAN_ITConfig(CAN1, CAN_IT_FMP0, ENABLE);

	uart1_puts("\r\n*** Starting RTOS ***");

	/* Output low -> LED on */
	GPIO_ResetBits(GPIOB,GPIO_Pin_4);
	GPIO_ResetBits(GPIOB,GPIO_Pin_5);
	Delay(10000000);
	/* Output open drain - high -> LED off */
	GPIO_SetBits(GPIOB,GPIO_Pin_4);
    GPIO_SetBits(GPIOB,GPIO_Pin_5);
	Delay(10000000);

	/*
	if (0 == GPIO_ReadInputDataBit(GPIOC,GPIO_Pin_14))
		uart1_puts("*** PortC-Input, PC14 = low, DXM Pin 29 ***\r\n");
	else
		uart1_puts("*** PortC-Input, PC14 = high, DXM Pin 29 ***\r\n");

	if (0 == GPIO_ReadInputDataBit(GPIOC,GPIO_Pin_15))
		uart1_puts("*** PortC-Input, PC15 = low, DXM Pin 28 ***\r\n");
	else
		uart1_puts("*** PortC-Input, PC15 = high, DXM Pin 28 ***\r\n");
*/

	/* Transmit */
/*
	TxMessage.StdId = 0x7E8;
   TxMessage.ExtId = 0x01;
   TxMessage.RTR = CAN_RTR_DATA;
   TxMessage.IDE = CAN_ID_STD;
   TxMessage.DLC = 8;

	TxMessage.Data[0] = 0x55;
	TxMessage.Data[1] = CAN_GetITStatus(CAN1, CAN_IT_FMP0);
    CAN_Transmit(CAN1, &TxMessage);
	Delay(10000000);
	TxMessage.Data[0] = 0x44;
	TxMessage.Data[1] = CAN_GetITStatus(CAN1, CAN_IT_FMP0);
	CAN_Transmit(CAN1, &TxMessage);
	Delay(10000000);
	TxMessage.Data[0] = 0x33;
	TxMessage.Data[1] = CAN_GetITStatus(CAN1, CAN_IT_FMP0);
	CAN_Transmit(CAN1, &TxMessage);
	Delay(10000000);
*/
//	CAN_Debug_Send_All_Primer_Data();

	/* Start the scheduler. */
	vTaskStartScheduler();

	uart1_puts("Something got wrong, RTOS terminated !!!\r\n");

	while (1)
	{
	}
}
