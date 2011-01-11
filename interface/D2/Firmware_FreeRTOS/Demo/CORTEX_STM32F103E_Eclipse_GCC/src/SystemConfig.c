/**
  ******************************************************************************
  * @file OOBD/src/SystemConfig.c 
  * @author  MCD Application Team
  * @version  V0.01
  * @date  13/12/2010
  * @brief  System configuration for OOBD firmware 
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
  * <h2><center>&copy; COPYRIGHT 2009 STMicroelectronics</center></h2>
  */ 


/* Includes ------------------------------------------------------------------*/
#include "SystemConfig.h"
#include "stm32f10x.h"

/* -------- Used Global variables -----------------------------------------------------*/

/* Private typedef -----------------------------------------------------------*/

/* Private define ------------------------------------------------------------*/
/* CAN BAUD RATE SELECTION */
#define  CAN_BAUDRATE_500KB /* _125KB, _250KB, _500KB, _1000KB */

/* Private macro ------------------------------------------------------------*/

/* Private variables ---------------------------------------------------------*/ 

/* Private function prototypes -----------------------------------------------*/

/* Private functions ---------------------------------------------------------*/


/**
  * @brief  Configures the system.
  * @param None.
  * @retval : None.
  */
void System_Configuration(void)
{
  /* System Clocks Configuration */
//  RCC_Configuration();

  /* NVIC Configuration */
//  NVIC_Configuration();
  
  /* GPIO configuration */
  GPIO_Configuration();

  /* Configure EXTI Line0 to generate an interrupt on falling edge */
/*  EXTI_Configuration(); */

  /* GPIO configuration */
//  CAN_Configuration();

  /* Timer configuration */
/*  TIMx_Configuration(TIMER_LCD); */
}


/**
  * @brief  Configures the different system clocks.
  * @param None.
  * @retval : None.
  */
void RCC_Configuration(void)
{   
  /* RCC system reset(for debug purpose) */
  RCC_DeInit();

  /* HCLK = SYSCLK/4 = 2MHz */
  RCC_HCLKConfig(RCC_SYSCLK_Div4);
  
  /* PCLK2 = HCLK = 2MHz */
  RCC_PCLK2Config(RCC_HCLK_Div1);

  /* PCLK1 = HCLK = 2MHz */
  RCC_PCLK1Config(RCC_HCLK_Div1);
    
  /* Flash 2 wait state */
/*  FLASH_SetLatency(FLASH_Latency_0); */
    
  /* Enable Flash half cycle */
/*  FLASH_HalfCycleAccessCmd(FLASH_HalfCycleAccess_Enable); */

  /* Enable Prefetch Buffer */
/*  FLASH_PrefetchBufferCmd(FLASH_PrefetchBuffer_Enable); */
    
  /* Select HSI as system clock source */
/*  RCC_SYSCLKConfig(RCC_SYSCLKSource_HSI); */
    
  /* TIM to be used clock enable */
/*  RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIMx, ENABLE); */

  /* GPIOx clocks enable */
//  RCC_APB2PeriphClockCmd(RCC_APB2Periph_Used_GPIO | RCC_APB2Periph_AFIO, ENABLE);
}

/**
  * @brief  Configure the nested vectored interrupt controller.
  * @param ne.          
  * @retval : None.
  */
void NVIC_Configuration(void)
{
  NVIC_InitTypeDef NVIC_InitStructure;

  /* Setting the priority grouping bits length */
//  NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
  NVIC_PriorityGroupConfig(NVIC_PriorityGroup_0);
  
  /* Enable the TIM_LCD global Interrupt */

//  NVIC_InitStructure.NVIC_IRQChannel = TIM_LCD_IRQChannel;
//  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = LCD_Priority_Value;
//  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 1;
//  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
//  NVIC_Init(&NVIC_InitStructure);

  /* Enable CAN1 interrupt */
#ifndef STM32F10X_CL
  NVIC_InitStructure.NVIC_IRQChannel = USB_LP_CAN1_RX0_IRQn;
#else
 NVIC_InitStructure.NVIC_IRQChannel = CAN1_RX0_IRQn;
#endif /* STM32F10X_CL*/
 NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0x0;
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0x0;
  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
  NVIC_Init(&NVIC_InitStructure);

  /* Enable the EXTI0 Interrupt */
//  NVIC_InitStructure.NVIC_IRQChannel = EXTI0_IRQn;
//  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0;
//  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0;
//  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
//  NVIC_Init(&NVIC_InitStructure);
}


/**
  * @brief  Configures the different GPIO ports.
  * @param None.
  * @retval : None.
  */
void GPIO_Configuration(void)
{
  GPIO_InitTypeDef GPIO_InitStructure;

  /* Configure all unused GPIO port pins in Analog Input mode (floating input
     trigger OFF), this will reduce the power consumption and increase the
     device immunity against EMI/EMC ******************************************/
  RCC_APB2PeriphClockCmd(RCC_APB2Periph_AFIO | RCC_APB2Periph_GPIOA |
		  	  	  	  	 RCC_APB2Periph_GPIOB | RCC_APB2Periph_GPIOC, ENABLE);

  GPIO_InitStructure.GPIO_Pin  = GPIO_Pin_All;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AIN;
  GPIO_Init(GPIOA, &GPIO_InitStructure);
  GPIO_Init(GPIOB, &GPIO_InitStructure);
  GPIO_Init(GPIOC, &GPIO_InitStructure);

   //-------------------------------------------------------------------------------------------------------------------//
   // PORTA configuration
   // PA 0 = ???             = GPIO_Mode_AIN - Input floating for low power consumption (see above)
   // PA 1 = ???             = GPIO_Mode_AIN - Input floating for low power consumption (see above)
   // PA 2 = L9637D RX, Pin1 = GPIO_Mode_AIN - currently unused
   // PA 3 = L9637D TX, Pin4 = GPIO_Mode_AIN - currently unused
   // PA 4 = ???             = GPIO_Mode_AIN - Input floating for low power consumption (see above)
   // PA 5 = ???             = GPIO_Mode_AIN - Input floating for low power consumption (see above)
   // PA 6 = LM393 Out, Pin1 = GPIO_Mode_AIN - currently unused
   // PA 7 = LM393 Out, Pin7 = GPIO_Mode_AIN - currently unused
   //-------------------------------------------------------------------------------------------------------------------//
   // PA 8 = DXM1 IO Pin 21  = GPIO_Mode_AIN - Input floating for low power consumption (see above)
   // PA 9 = USART1_TX       = GPIO_Mode_AF_PP - Alternate Function output Push Pull
   // PA10 = USART1_RX       = GPIO_Mode_AIN - Input floating for low power consumption (see above)
   // PA11 = USART1_CTS      = GPIO_Mode_AIN - currently unused
   // PA12 = USART1_RTS      = GPIO_Mode_AIN - currently unused
   // PA13 = ???             = GPIO_Mode_AIN - Input floating for low power consumption (see above)
   // PA14 = ???             = GPIO_Mode_AIN - Input floating for low power consumption (see above)
   // PA15 = ???             = GPIO_Mode_AIN - Input floating for low power consumption (see above)
   //-------------------------------------------------------------------------------------------------------------------//

  /* Enable USART1 Clock */
	RCC->APB2ENR |= RCC_APB2ENR_USART1EN;

	/* Enable USART1 */
	USART1->CR1 = USART_CR1_UE;

	/* Set baudrate divider to 39,0625 -> 115200 baud @ 72MHz */
	USART1->BRR = (39 << 4) | (1);

	USART1->CR2 = 0;
	USART1->CR3 = 0;
	USART1->CR1 = USART_CR1_UE | USART_CR1_TE | USART_CR1_RE | USART_CR1_RXNEIE;


  /* initialize USART1 on PA9 (USART1_TX) and PA10 (USART1_RX) */
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_InitStructure.GPIO_Mode  = GPIO_Mode_AF_PP;
  GPIO_InitStructure.GPIO_Pin   = GPIO_Pin_9;
  GPIO_Init(GPIOA, &GPIO_InitStructure);

  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN_FLOATING;
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_10;
  GPIO_Init(GPIOA, &GPIO_InitStructure);

  //-------------------------------------------------------------------------------------------------------------------//
   // PB 0 = ??? = GPIO_Mode_AIN - Input floating for low power consumption (see above)
   // PB 1 = ??? = GPIO_Mode_AIN - Input floating for low power consumption (see above)
   // PB 2 = ??? = GPIO_Mode_AIN - Input floating for low power consumption (see above)
   // PB 3 = ??? = GPIO_Mode_AIN - Input floating for low power consumption (see above)
   // PB 4 = DXM1 IO Pin 13, LED2    = B Output
   // PB 5 = DXM1 IO Pin 12, LED1    = B Output
   // PB 6 = ??? = GPIO_Mode_AIN - Input floating for low power consumption (see above)
   // PB 7 = ??? = GPIO_Mode_AIN - Input floating for low power consumption (see above)
   //-------------------------------------------------------------------------------------------------------------------//
   // PB 8 = TJA1050 Pin 4 (RxD)  = GPIO_Mode_IPU - Input Pullup
   // PB 9 = TJA1050 Pin 1 (TxD)  = GPIO_Mode_AF_PP - Alternate Function output Push Pull
   // PB10 = DXM1 IO Pin 15, LCD out  = GPIO_Mode_AIN - currently unused
   // PB11 = DXM1 IO Pin 18, A0 out   = GPIO_Mode_AIN - currently unused
   // PB12 = DXM1 IO Pin 19, SPI_CS   = GPIO_Mode_AIN - currently unused
   // PB13 = DXM1 IO Pin 20, SPI_SCK  = GPIO_Mode_AIN - currently unused
   // PB14 = DXM1 IO Pin 22, SPI_MISO = GPIO_Mode_AIN - currently unused
   // PB15 = DXM1 IO Pin 23, SPI_MOSI = GPIO_Mode_AIN - currently unused
   //-------------------------------------------------------------------------------------------------------------------//

  /* configure Output (open drain) of LED1 - green (PB5) and LED2 - red (PB4) */
  GPIO_PinRemapConfig(GPIO_Remap_SWJ_NoJTRST, ENABLE); /* release alternative GPIO function of PB4 */
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_4 | GPIO_Pin_5;
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_OD;
  GPIO_Init(GPIOB, &GPIO_InitStructure);

  /* start CAN configuration */
  /* GPIO clock enable */
/*
  RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA | RCC_APB2Periph_GPIOB |
		                 RCC_APB2Periph_GPIOB | RCC_APB2Periph_AFIO, ENABLE);
*/
  GPIO_PinRemapConfig(GPIO_Remap1_CAN1, ENABLE);

  /* Configure CAN pin: RX */
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_8;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU;
  GPIO_Init(GPIOB, &GPIO_InitStructure);

  /* Configure CAN pin: TX */
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_9;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP;
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(GPIOB, &GPIO_InitStructure);

  /* CAN1 Periph clock enable */
  RCC_APB1PeriphClockCmd(RCC_APB1Periph_CAN1, ENABLE);

   //-------------------------------------------------------------------------------------------------------------------//
   // PC13 = ??? = GPIO_Mode_AIN - Input floating for low power consumption (see above)
   // PC14 = DXM1 IO Pin 29 = GPIO_Mode_IPU - Input with Pullup
   // PC15 = DXM1 IO Pin 28 = GPIO_Mode_IPU - Input with Pullup
   //-------------------------------------------------------------------------------------------------------------------//
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU;
    /* set GPIO Push-Pull output */
    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_14 | GPIO_Pin_15;
    /* initialize new PortC settings  */
    GPIO_Init(GPIOC, &GPIO_InitStructure);

   //-------------------------------------------------------------------------------------------------------------------//
   //                  .--------------------------- SWJ_CFG[2:0]     000: Full SWJ (JTAG-DP + SW-DP): Reset State
   //                  |.-------------------------- SWJ_CFG[2:0]     001: Full SWJ (JTAG-DP + SW-DP) but without JNTRST                        100: JTAG-DP Disabled and SW-DP Disabled
   //                  ||.------------------------- SWJ_CFG[2:0]     010: JTAG-DP Disabled and SW-DP Enabled
   //                  |||        .---------------- PD01_REMAP         0: No remapping of PD0 and PD1                                            1: PD0 remapped on OSC_IN, PD1 remapped on OSC_OUT,
   //                  |||        |.--------------- CAN_REMAP[1:0]    00: CANRX mapped to PA11, CANTX mapped to PA12                            10: CANRX mapped to PB8, CANTX mapped to PB9 (not available on 36-pin package)
   //                  |||        ||.-------------- CAN_REMAP[1:0]    01: Not used                                                              11: CANRX mapped to PD0, CANTX mapped to PD1
   //                  |||        |||.------------- TIM4_REMAP         0: No remap (TIM4_CH1/PB6, TIM4_CH2/PB7, TIM4_CH3/PB8, TIM4_CH4/PB9)      1: Full remap (TIM4_CH1/PD12, TIM4_CH2/PD13, TIM4_CH3/PD14, TIM4_CH4/PD15)
   //                  |||        ||||.------------ TIM3_REMAP[1:0]   00: No remap (CH1/PA6, CH2/PA7, CH3/PB0, CH4/PB1)                         10: Partial remap (CH1/PB4, CH2/PB5, CH3/PB0, CH4/PB1)
   //                  |||        |||||.----------- TIM3_REMAP[1:0]   01: Not used                                                              11: Full remap (CH1/PC6, CH2/PC7, CH3/PC8, CH4/PC9)
   //                  |||        ||||||.---------- TIM2_REMAP[1:0]   00: No remap (CH1/ETR/PA0, CH2/PA1, CH3/PA2, CH4/PA3)                     10: Partial remap (CH1/ETR/PA0, CH2/PA1, CH3/PB10, CH4/PB11)
   //                  |||        |||||||.--------- TIM2_REMAP[1:0]   01: Partial remap (CH1/ETR/PA15, CH2/PB3, CH3/PA2, CH4/PA3)               11: Full remap (CH1/ETR/PA15, CH2/PB3, CH3/PB10, CH4/PB11)
   //                  |||        ||||||||.-------- TIM1_REMAP[1:0]   00: No remap (ETR/PA12,CH1/PA8,CH2/PA9,CH3/PA10,CH4/PA11,BKIN/PB12,CH1N/PB13,CH2N/P14,CH3N/PB15) 10: not used
   //                  |||        |||||||||.------- TIM1_REMAP[1:0]   01: Partial  (ETR/PA12,CH1/PA8,CH2/PA9,CH3/PA10,CH4/PA11,BKIN/PA6, CH1N/PA7, CH2N/PB0,CH3N/PB1)  11: Full remap (ETR/PE7,CH1/PE9,CH2/PE11,CH3/PE13,CH4/PE14,BKIN/PE15,CH1N/PE8,CH2N/PE10,CH3N/PE12)
   //                  |||        ||||||||||.------ USART3_REMAP[1:0] 00: No remap (TX/PB10, RX/PB11, CK/PB12, CTS/PB13, RTS/PB14)              10: not used
   //                  |||        |||||||||||.----- USART3_REMAP[1:0] 01: Partial remap (TX/PC10, RX/PC11, CK/PC12, CTS/PB13, RTS/PB14)         11: Full remap (TX/PD8, RX/PD9, CK/PD10, CTS/PD11, RTS/PD12)
   //                  |||        ||||||||||||.---- USART2_REMAP       0: No remap (CTS/PA0, RTS/PA1, TX/PA2, RX/PA3, CK/PA4)                    1: Remap (CTS/PD3, RTS/PD4, TX/PD5, RX/PD6, CK/PD7)
   //                  |||        |||||||||||||.--- USART1_REMAP       0: No remap (TX/PA9, RX/PA10)                                             1: Remap (TX/PB6, RX/PB7)
   //                  |||        ||||||||||||||.-- I2C1_REMAP         0: No remap (SCL/PB6, SDA/PB7)                                            1: Remap (SCL/PB8, SDA/PB9)
   //                  |||        |||||||||||||||.- SPI1_REMAP         0: No remap (NSS/PA4, SCK/PA5, MISO/PA6, MOSI/PA7)                        1: Remap (NSS/PA15, SCK/PB3, MISO/PB4, MOSI/PB5)
   //                  |||        ||||||||||||||||
//      AFIO->MAPR = 0b00000000000000000100000000000000;
   //             33222222222111111111119876543210
   //             1098765432109876543210
}


/**
  * @brief  Configures EXTI Line9.
  * @param None.
  * @retval : None.
  */
void EXTI_Configuration(void)
{
//  EXTI_InitTypeDef EXTI_InitStructure;
  
  /* Connect EXTI Line0 to PA.00 */
//  GPIO_EXTILineConfig(GPIO_PortSourceGPIOA, GPIO_PinSource0);

  /* Configure EXTI Line0 to generate an interrupt on falling edge */  
//  EXTI_InitStructure.EXTI_Line = EXTI_Line0;
//  EXTI_InitStructure.EXTI_Mode = EXTI_Mode_Interrupt;
//  EXTI_InitStructure.EXTI_Trigger = EXTI_Trigger_Falling;
//  EXTI_InitStructure.EXTI_LineCmd = ENABLE;
//  EXTI_Init(&EXTI_InitStructure);
}

/**
  * @brief Configures CAN interface
  * @param None.
  * @retval : None.
*/ 
void CAN_Configuration(void)
{
   CAN_InitTypeDef        CAN_InitStructure;
   CAN_FilterInitTypeDef  CAN_FilterInitStructure;

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
   CAN_InitStructure.CAN_Mode = CAN_Mode_Normal;
   CAN_InitStructure.CAN_SJW  = CAN_SJW_1tq;

	#ifdef  CAN_BAUDRATE_125KB
   	   CAN_InitStructure.CAN_BS1 = CAN_BS1_3tq;
   	   CAN_InitStructure.CAN_BS2 = CAN_BS2_5tq;
   	   CAN_InitStructure.CAN_Prescaler = 32; /* BRP Baudrate prescaler */
	#endif
	#ifdef CAN_BAUDRATE_250KB
   	   CAN_InitStructure.CAN_BS1 = CAN_BS1_3tq;
   	   CAN_InitStructure.CAN_BS2 = CAN_BS2_5tq;
   	   CAN_InitStructure.CAN_Prescaler = 16; /* BRP Baudrate prescaler */
	#endif
	#ifdef CAN_BAUDRATE_500KB
   	   CAN_InitStructure.CAN_BS1 = CAN_BS1_3tq;
   	   CAN_InitStructure.CAN_BS2 = CAN_BS2_5tq;
   	   CAN_InitStructure.CAN_Prescaler = 8; /* BRP Baudrate prescaler */
	#endif
	#ifdef CAN_BAUDRATE_1000KB
   	   CAN_InitStructure.CAN_BS1 = CAN_BS1_3tq;
   	   CAN_InitStructure.CAN_BS2 = CAN_BS2_5tq;
   	   CAN_InitStructure.CAN_Prescaler = 4; /* BRP Baudrate prescaler */
	#endif

    CAN_Init(CAN1, &CAN_InitStructure);

   /* CAN filter init */
    CAN_FilterInitStructure.CAN_FilterNumber = 0;
    CAN_FilterInitStructure.CAN_FilterMode = CAN_FilterMode_IdMask;
    CAN_FilterInitStructure.CAN_FilterScale = CAN_FilterScale_32bit;
    CAN_FilterInitStructure.CAN_FilterIdHigh = 0x0000;
    CAN_FilterInitStructure.CAN_FilterIdLow = 0x0000;
    CAN_FilterInitStructure.CAN_FilterMaskIdHigh = 0x0000;
    CAN_FilterInitStructure.CAN_FilterMaskIdLow = 0x0000;
    CAN_FilterInitStructure.CAN_FilterFIFOAssignment = 0;
    CAN_FilterInitStructure.CAN_FilterActivation = ENABLE;
    CAN_FilterInit(&CAN_FilterInitStructure);

    CAN_ITConfig(CAN1, CAN_IT_FMP0, ENABLE);
}
 
/******************* (C) COPYRIGHT 2009 STMicroelectronics *****END OF FILE****/
