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

static void prvBlinkLedTask( void *pvParameters );
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
	uint32_t tmp;

	/* Initialize PLL and some basic stuff */
	SystemInit();

	/* Enable PORTC Clock */
	RCC->APB2ENR |= RCC_APB2ENR_IOPCEN;
	tmp = GPIOC->CRH;
	tmp &= ~((GPIO_CRH_CNF13 | GPIO_CRH_MODE13));
	tmp |= GPIO_CRH_MODE13_0 | GPIO_CRH_MODE13_1;
	GPIOC->CRH = tmp;

	/* Initialize i.e. GPIO, CAN, USART1 */
	System_Configuration();

	/* CAN configuration */
//	Configuration_CAN();
//	CAN_ITConfig(CAN1, CAN_IT_FMP0, ENABLE);

	InitSerialComm();
	Timer4Init();

	xTaskCreate(prvBlinkLedTask,
			(const signed portCHAR *) "BlinkLed",
			configMINIMAL_STACK_SIZE,
			(void *)NULL,
			mainBlinkLed_TASK_PRIORITY,
			(xTaskHandle *)NULL );

	uart1_puts("*** Starting RTOS ***\r\n");

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

static void prvBlinkLedTask( void *pvParameters )
{
	portTickType xLastExecutionTime;
	char sbuf[16];

	/* Initialise the xLastExecutionTime variable on task entry. */
	xLastExecutionTime = xTaskGetTickCount();

	RTC_Init();

    for( ;; )
	{
    	SerialSendStr("RTC: ");

    	itoa(RTC_GetCounter(), sbuf, 10);
    	SerialSendStr(sbuf);
    	SerialSendStr("s\n");

		/* Simple toggle the LED periodically.  This just provides some timing
		verification. */
		vTaskDelayUntil( &xLastExecutionTime, mainBlinkLed_DELAY );
		/* Output low -> LED on */
//		GPIO_ResetBits(GPIOB,GPIO_Pin_4);
//		GPIO_ResetBits(GPIOB,GPIO_Pin_5);

		vTaskDelayUntil( &xLastExecutionTime, mainBlinkLed_DELAY );
		/* Output Open Drain = high -> LED off */
//		GPIO_SetBits(GPIOB,GPIO_Pin_4);
//		GPIO_SetBits(GPIOB,GPIO_Pin_5);
	}
}

void Timer4Init(void) {
	/* Enable TIM4 Clock */
	RCC->APB1ENR |= RCC_APB1ENR_TIM4EN;

	/* Generate interrupt all 123 cycles -> 1016 Hz*/
	TIM4->ARR = (uint16_t) 122;
	/* Prescaler ( 36MHz / 288 = 125 kHz) */
	TIM4->PSC = (uint16_t) 287;
	/* Reset counter */
	TIM4->CNT = 0;
	/* clear status bits*/
	TIM4->SR = 0;
	/* enable TIM4 interrupt */
	TIM4->DIER = TIM_DIER_UIE;
	/* enable TIM4, upcounting */
	TIM4->CR1 = TIM_CR1_CEN;
}

void TIM4_IRQHandler(void) {
	/* clear status bits */
	TIM4->SR &= ~(TIM_DIER_UIE);

}

uint32_t RTC_GetCounter() {
	uint32_t count = 0;

	do {
		count = (RTC->CNTH << 16);
		count |= RTC->CNTL;
	} while ((count >> 16) != RTC->CNTH);

	return count;
}

void RTC_Init(void) {
	char sbuf[32];
	char *pstr;
	portTickType xLastExecutionTime;
	portTickType ticks;

	/* Initialize the xLastExecutionTime variable on function entry. */
	xLastExecutionTime = xTaskGetTickCount();

	// Enable Power and Backup interface
	RCC->APB1ENR |= RCC_APB1ENR_BKPEN | RCC_APB1ENR_PWREN;

	// Disbale backup domain write protection
	PWR->CR |= PWR_CR_DBP;

	if ((RCC->BDCR & RCC_BDCR_RTCEN) == 0) {
		// Enable external LSE crystal
		RCC->BDCR |= RCC_BDCR_LSEON;

		while ((RCC->BDCR & RCC_BDCR_LSERDY) == 0) {
			vTaskDelayUntil( &xLastExecutionTime, ( ( portTickType ) 20 / portTICK_RATE_MS ) );
		}

		ticks = xTaskGetTickCount();
		strcpy(sbuf, "LSE ready (");
		pstr = sbuf + strlen(sbuf);
		itoa(ticks, pstr, 10);
		strcat(sbuf," ms)\n");
		SerialSendStr(sbuf);

		// Use LSE Clock as RTC clock and enable RTC
		RCC->BDCR |= RCC_BDCR_RTCSEL_LSE | RCC_BDCR_RTCEN;

		while ((RTC->CRL & RTC_CRL_RTOFF) == 0) {
			__asm volatile ("nop");
		}

		RTC->CRL |= RTC_CRL_CNF;
		RTC->PRLL = 0x7FFF;
		RTC->CNTH = 0;
		RTC->CNTL = 0;
		RTC->CRL = 0;

		while ((RTC->CRL & RTC_CRL_RTOFF) == 0) {
			__asm volatile ("nop");
		}

		SerialSendStr("RTC enabled with LSE Clock and reset.\n");
	} else {
		SerialSendStr("RTC already enabled.\n");
	}

	// Clear all pending flags
	RTC->CRL = 0;

	// Wait for sync
	while ((RTC->CRL & RTC_CRL_RSF) == 0) {
		vTaskDelayUntil( &xLastExecutionTime, ( ( portTickType ) 20 / portTICK_RATE_MS ) );
	}
	SerialSendStr("RTC synchronized.\n");
}
