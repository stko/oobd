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


  1 tab == 2 spaces!

  Please ensure to read the configuration and relevant port sections of the
  online documentation.


  OOBD is using FreeTROS (www.FreeRTOS.org)

*/

/* Includes ------------------------------------------------------------------*/
#include "FreeRTOSConfig.h"
#include "SystemConfig.h"
#include "stm32f10x.h"
#include "SerialComm.h"
#include "od_config.h"
#include "odp_uds.h"

/* -------- Used Global variables --------------------------------------------*/

/* Private typedef -----------------------------------------------------------*/

/* Private define ------------------------------------------------------------*/

/* Private macro -------------------------------------------------------------*/

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
  /* GPIO configuration */
  GPIO_Configuration();

  /* USART1 configuration */
  USART1_Configuration();

  /* USART1 configuration */
  CAN1_Configuration(VALUE_BUS_CONFIG_11bit_500kbit); /* default initialization */

  /* NVIC configuration */
//  NVIC_Configuration();
}

/*----------------------------------------------------------------------------*/
/**
  * @brief  Configure the nested vectored interrupt controller
  * @param  None
  * @retval None
  */
void NVIC_Configuration(void)
{
  NVIC_InitTypeDef NVIC_InitStructure;

  /* Set the Vector Table base location at 0x3000 */
  NVIC_SetVectorTable(NVIC_VectTab_FLASH, 0x3000);

  /* Setting the priority grouping bits length */
  NVIC_PriorityGroupConfig(NVIC_PriorityGroup_4);
  
  /* Enable the USART1 Interrupt */
  NVIC_InitStructure.NVIC_IRQChannel = USART1_IRQn;
  /* NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0; */
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = (configMAX_SYSCALL_INTERRUPT_PRIORITY >> 4) + 1;
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0;
  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
  NVIC_Init(&NVIC_InitStructure);

  /* Enable CAN1 interrupt */
  NVIC_InitStructure.NVIC_IRQChannel = USB_LP_CAN1_RX0_IRQn;
  /* NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0; */
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = (configMAX_SYSCALL_INTERRUPT_PRIORITY >> 4) + 1;
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 1;
  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
  NVIC_Init(&NVIC_InitStructure);

  NVIC_InitStructure.NVIC_IRQChannel = I2C1_EV_IRQn;
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0;
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0;;
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
  * @brief  Configures the different GPIO ports
  * @param  None
  * @retval None
  */
void GPIO_Configuration(void)
{
  GPIO_InitTypeDef GPIO_InitStructure;

  /* GPIOx clocks enable */
  RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA | RCC_APB2Periph_GPIOB |
		  RCC_APB2Periph_GPIOC | RCC_APB2Periph_AFIO, ENABLE);

  /* Configure all unused GPIO port pins in Analog Input mode (floating input
     trigger OFF), this will reduce the power consumption and increase the
     device immunity against EMI/EMC ******************************************/
  GPIO_InitStructure.GPIO_Pin  = GPIO_Pin_All;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AIN;
  GPIO_Init(GPIOA, &GPIO_InitStructure);
  GPIO_Init(GPIOB, &GPIO_InitStructure);
  GPIO_Init(GPIOC, &GPIO_InitStructure);

  /** --------------------------------------------------------------------------
    * PORTA configuration
    * PA 0 = ???             = GPIO_Mode_AIN - Input floating for low power consumption
    * PA 1 = ???             = GPIO_Mode_AIN - Input floating for low power consumption
    * PA 2 = L9637D RX, Pin1 = GPIO_Mode_AIN - currently unused
    * PA 3 = L9637D TX, Pin4 = GPIO_Mode_AIN - currently unused
    * PA 4 = ???             = GPIO_Mode_AIN - Input floating for low power consumption
    * PA 5 = ???             = GPIO_Mode_AIN - Input floating for low power consumption
    * PA 6 = LM393 Out, Pin1 = GPIO_Mode_AIN - currently unused
    * PA 7 = LM393 Out, Pin7 = GPIO_Mode_AIN - currently unused
    * --------------------------------------------------------------------------
    * PA 8 = DXM1 IO Pin 21  = GPIO_Mode_AIN - Input floating for low power consumption
    * PA 9 = USART1_TX       = GPIO_Mode_AF_PP - Alternate Function output Push Pull
    * PA10 = USART1_RX       = GPIO_Mode_AIN - Input floating for low power consumption
    * PA11 = USART1_CTS      = GPIO_Mode_AIN - currently unused
    * PA12 = USART1_RTS      = GPIO_Mode_AIN - currently unused
    * PA13 = ???             = GPIO_Mode_AIN - Input floating for low power consumption
    * PA14 = ???             = GPIO_Mode_AIN - Input floating for low power consumption
    * PA15 = ???             = GPIO_Mode_AIN - Input floating for low power consumption
    * --------------------------------------------------------------------------
    */

  /* Enable USART1 Clock */
  RCC_APB2PeriphClockCmd(RCC_APB2Periph_USART1, ENABLE);

  /* initialize USART1 on PA9 (USART1_TX) and PA10 (USART1_RX) */
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_InitStructure.GPIO_Mode  = GPIO_Mode_AF_PP;
  GPIO_InitStructure.GPIO_Pin   = GPIO_Pin_9;
  GPIO_Init(GPIOA, &GPIO_InitStructure);

  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN_FLOATING;
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_10;
  GPIO_Init(GPIOA, &GPIO_InitStructure);

  /** --------------------------------------------------------------------------
    * PB 0 = ??? = GPIO_Mode_AIN - Input floating for low power consumption
    * PB 1 = ??? = GPIO_Mode_AIN - Input floating for low power consumption
    * PB 2 = ??? = GPIO_Mode_AIN - Input floating for low power consumption
    * PB 3 = ??? = GPIO_Mode_AIN - Input floating for low power consumption
    * PB 4 = DXM1 IO Pin 13, LED2 - red = GPIO_Mode_Out_OD - Output open drain
    * PB 5 = DXM1 IO Pin 12, LED1 - green = GPIO_Mode_Out_OD - Output open drain
    * PB 6 = 24LC16, SCL = GPIO_Mode_AF_OD = Alternate Function output open drain
    * PB 7 = 24LC16, SDA = GPIO_Mode_AF_OD = Alternate Function output open drain
    * --------------------------------------------------------------------------
    * PB 8 = TJA1050 Pin 4 (RxD)  = GPIO_Mode_IPU - Input Pullup
    * PB 9 = TJA1050 Pin 1 (TxD)  = GPIO_Mode_AF_PP - Alternate Function output Push Pull
    * PB10 = DXM1 IO Pin 15, LCD out  = GPIO_Mode_AIN - currently unused
    * PB11 = DXM1 IO Pin 18, A0 out   = GPIO_Mode_AIN - currently unused
    * PB12 = DXM1 IO Pin 19, SPI_CS   = GPIO_Mode_AIN - currently unused
    * PB13 = DXM1 IO Pin 20, SPI_SCK  = GPIO_Mode_AIN - currently unused
    * PB14 = DXM1 IO Pin 22, SPI_MISO = GPIO_Mode_AIN - currently unused
    * PB15 = DXM1 IO Pin 23, SPI_MOSI = GPIO_Mode_AIN - currently unused
    *---------------------------------------------------------------------------
    */

  /* configure Output (open drain) of LED1 - green (PB5) and LED2 - red (PB4) */
  GPIO_PinRemapConfig(GPIO_Remap_SWJ_NoJTRST, ENABLE); /* release alternative GPIO function of PB4 */
  GPIO_InitStructure.GPIO_Pin   = GPIO_Pin_4 | GPIO_Pin_5;
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_InitStructure.GPIO_Mode  = GPIO_Mode_Out_OD;
  GPIO_Init(GPIOB, &GPIO_InitStructure);

  /* start CAN configuration */
  /* GPIO clock enable */
  GPIO_PinRemapConfig(GPIO_Remap1_CAN1, ENABLE);
  /* Configure CAN pin: RX */
  GPIO_InitStructure.GPIO_Pin   = GPIO_Pin_8;
  GPIO_InitStructure.GPIO_Mode  = GPIO_Mode_IPU;
  GPIO_Init(GPIOB, &GPIO_InitStructure);
  /* Configure CAN pin: TX */
  GPIO_InitStructure.GPIO_Pin   = GPIO_Pin_9;
  GPIO_InitStructure.GPIO_Mode  = GPIO_Mode_AF_PP;
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(GPIOB, &GPIO_InitStructure);
  /* CAN1 Periph clock enable */
  RCC_APB1PeriphClockCmd(RCC_APB1Periph_CAN1, ENABLE);

  /* Configure I2C pins: SCL and SDA ----------------------------------------*/
//  GPIO_InitStructure.GPIO_Pin   = GPIO_Pin_6 | GPIO_Pin_7;
//  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
//  GPIO_InitStructure.GPIO_Mode  = GPIO_Mode_AF_OD;
//  GPIO_Init(GPIOB, &GPIO_InitStructure);

  /** --------------------------------------------------------------------------
    * PC13 = ??? = GPIO_Mode_AIN - Input floating for low power consumption
    * PC14 = DXM1 IO Pin 29 = GPIO_Mode_IPU - Input with Pullup
    * PC15 = DXM1 IO Pin 28 = GPIO_Mode_IPU - Input with Pullup
    * --------------------------------------------------------------------------
    */

  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU;
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_14 | GPIO_Pin_15;
  /* initialize new PortC settings  */
  GPIO_Init(GPIOC, &GPIO_InitStructure);
}
/*----------------------------------------------------------------------------*/

/**
  * @brief  Configures the different GPIO ports
  * @param  None
  * @retval None
  */
void USART1_Configuration(void)
{
  USART_InitTypeDef USART_InitStructure;

  /** USARTx configuration -----------------------------------------------------
    * USARTx configured as follow:
    *      - BaudRate = 115200 baud
    *      - Word Length = 8 Bits
    *      - One Stop Bit
    *      - None parity
    *      - Hardware flow control disabled (RTS and CTS signals)
    *      - Receive and transmit enabled
    */
  USART_InitStructure.USART_BaudRate            = USART1_BAUDRATE_DEFAULT;
  USART_InitStructure.USART_WordLength          = USART_WordLength_8b;
  USART_InitStructure.USART_StopBits            = USART_StopBits_1;
  USART_InitStructure.USART_Parity              = USART_Parity_No;
  USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None;
  USART_InitStructure.USART_Mode                = USART_Mode_Rx | USART_Mode_Tx;

  /* Configure USART1 */
  USART_Init(USART1, &USART_InitStructure);

  /* Enable the USART1-Transmit interrupt: this interrupt is generated when the
     USART1 transmit data register is empty */
  /* USART_ITConfig(USART1, USART_IT_TXE, ENABLE); */

  /* Enable the USART1-Receive interrupt: this interrupt is generated when the
     USART1 receive data register is not empty */
//  USART_ITConfig(USART1, USART_IT_RXNE, ENABLE);

  /* Enable the USART1 */
  USART_Cmd(USART1, ENABLE);
#ifdef todo_autobaud
    char  ReceivedData;
    uint8_t   AutobaudControl;
    uint32_t  nCount;
    uint8_t TxBuffer[] = "atl?\r\n";
    uint8_t RxBuffer[6];
#define TxBufferSize   (countof(TxBuffer))
#define countof(a)   (sizeof(a) / sizeof(*(a)))

    __IO uint8_t TxCounter, RxCounter;
/*
    TxCounter = 0;
    RxCounter = 0;
    while(TxCounter < TxBufferSize)
    {
*/
      /* Send one byte from USARTy to USARTz */
//      USART_SendData(USART1, TxBuffer[TxCounter++]);

      /* Loop until USARTy DR register is empty */
//      while(USART_GetFlagStatus(USART1, USART_FLAG_TXE) == RESET)
//      {
//      }
//    }

    for (AutobaudControl=0; AutobaudControl<=9; AutobaudControl++)
    {
/*
      TxCounter = 0;
      RxCounter = 0;
      while(TxCounter < TxBufferSize)
      {
*/
        /* Send one byte from USARTy to USARTz */
//        USART_SendData(USART1, TxBuffer[TxCounter++]);

        /* Loop until USARTy DR register is empty */
//        while(USART_GetFlagStatus(USART1, USART_FLAG_TXE) == RESET)
//        {
//        }
//      }
        uart1_puts ("atl?\r");


      /* Loop until the USARTz Receive Data Register is not empty */
      while(USART_GetFlagStatus(USART1, USART_FLAG_RXNE) == RESET)
      {
          RxBuffer[RxCounter++] = (USART_ReceiveData(USART1) & 0x7F);
      }

      AutobaudControl++;

      switch (RxBuffer[0])
      {
       case '1':
//          DEBUGUARTPRINT("\r\n*** BTM222 - L1 = 9600bps detected! ***");
         *TxBuffer = "atl5\r\n";
//         USART_SendData(USART1, "atl5"); /* set BTM222 to default baudrate */
         AutobaudControl = 9;
          break;

        case '2':
//          DEBUGUARTPRINT("\r\n*** BTM222 - L2 = 19200bps detected! ***");
//          USART_SendData(USART1, "atl5"); /* set BTM222 to default baudrate */
          *TxBuffer = "atl5\r\n";
          AutobaudControl = 9;
          break;

        case '3':
//          DEBUGUARTPRINT("\r\n*** BTM222 - L3 = 38400bps detected! ***");
//          USART_SendData(USART1, "atl5"); /* set BTM222 to default baudrate */
          *TxBuffer = "atl5\r\n";
          AutobaudControl = 9;
          break;

        case '4':
//          DEBUGUARTPRINT("\r\n*** BTM222 - L4 = 57600bps detected! ***");
//          USART_SendData(USART1, "atl5"); /* set BTM222 to default baudrate */
          *TxBuffer = "atl5\r\n”";
          AutobaudControl = 9;
          break;

        case '5':
//          DEBUGUARTPRINT("\r\n*** BTM222 - L5 = 115200bps detected! ***");
          AutobaudControl = 9;
          break;

        case '6':
//          DEBUGUARTPRINT("\r\n*** BTM222 - L6 = 230400bps detected! ***");
//          USART_SendData(USART1, "atl5"); /* set BTM222 to default baudrate */
          *TxBuffer = "atl5\r\n";
          AutobaudControl = 9;
          break;

        case '7':
//          DEBUGUARTPRINT("\r\n*** BTM222 - L7 = 460800bps detected! ***");
//          USART_SendData(USART1, "atl5"); /* set BTM222 to default baudrate */
          *TxBuffer = "atl5\r\n";
          AutobaudControl = 9;
          break;

        default:
//          DEBUGUARTPRINT("\r\n*** BTM222 - baudrate not detected! ***");
          AutobaudControl++; /* increment AutobaudControl counter */
          break;
      }

      if (AutobaudControl == 9)
      {
          TxCounter = 0;
          while(TxCounter < TxBufferSize)
          {
            /* Send one byte from USARTy to USARTz */
            USART_SendData(USART1, TxBuffer[TxCounter++]);

            /* Loop until USARTy DR register is empty */
            while(USART_GetFlagStatus(USART1, USART_FLAG_TXE) == RESET)
            {
            }
          }
      }


      switch (AutobaudControl)
      {
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
          USART_SendData(USART1, "atl5"); /* set BTM222 to default baudrate */
          /* fallback to default baudrate */
          USART_InitStructure.USART_BaudRate = USART1_BAUDRATE_DEFAULT;
          break;

        default:
          break;
      }
      USART_Init(USART1, &USART_InitStructure); /* reinitialization of USART */
    } /* end of for */

//  DEBUGUARTPRINT("\r\n*** Autobaud SCAN for BTM222 finished! ***");

//  USART_Init(USART1, &USART_InitStructure);
#endif
  /* Enable the USART1-Receive interrupt: this interrupt is generated when the
     USART1 receive data register is not empty */
  USART_ITConfig(USART1, USART_IT_RXNE, ENABLE);

}
/*----------------------------------------------------------------------------*/

/**
  * @brief  Configures the different GPIO ports
  * @param  None
  * @retval None
  */
void CAN1_Configuration(uint8_t CAN_BusConfig)
{
  DEBUGUARTPRINT("\r\n*** CANx_Configuration (CAN1) entered! ***");

  CAN_InitTypeDef       CAN_InitStructure;
  CAN_FilterInitTypeDef CAN_FilterInitStructure;

  /* CAN register init */
  CAN_DeInit(CAN1);

  CAN_StructInit(&CAN_InitStructure);

  /* CAN cell init */
  CAN_InitStructure.CAN_TTCM = DISABLE; /* Time triggered communication mode */
  CAN_InitStructure.CAN_ABOM = DISABLE; /* Automatic bus-off management */
  CAN_InitStructure.CAN_AWUM = DISABLE; /* Automatic wakeup mode */
  CAN_InitStructure.CAN_NART = DISABLE; /* No automatic retransmission */
  CAN_InitStructure.CAN_RFLM = DISABLE; /* Receive FIFO locked mode */
  CAN_InitStructure.CAN_TXFP = ENABLE;  /* Transmit FIFO priority */
  CAN_InitStructure.CAN_Mode = CAN_Mode_Normal;
  CAN_InitStructure.CAN_SJW  = CAN_SJW_1tq;

  if (CAN_BusConfig == VALUE_BUS_CONFIG_11bit_125kbit ||
      CAN_BusConfig == VALUE_BUS_CONFIG_29bit_125kbit)
  {
    CAN_InitStructure.CAN_BS1 = CAN_BS1_3tq;
    CAN_InitStructure.CAN_BS2 = CAN_BS2_5tq;
    CAN_InitStructure.CAN_Prescaler = 32; /* BRP Baudrate prescaler */
  }
  else if ( CAN_BusConfig == VALUE_BUS_CONFIG_11bit_250kbit ||
            CAN_BusConfig == VALUE_BUS_CONFIG_29bit_250kbit)
  {
    CAN_InitStructure.CAN_BS1 = CAN_BS1_3tq;
    CAN_InitStructure.CAN_BS2 = CAN_BS2_5tq;
    CAN_InitStructure.CAN_Prescaler = 16; /* BRP Baudrate prescaler */
  }
  else if ( CAN_BusConfig == VALUE_BUS_CONFIG_11bit_500kbit ||
            CAN_BusConfig == VALUE_BUS_CONFIG_29bit_500kbit)
  {
    CAN_InitStructure.CAN_BS1 = CAN_BS1_3tq;
    CAN_InitStructure.CAN_BS2 = CAN_BS2_5tq;
    CAN_InitStructure.CAN_Prescaler = 8; /* BRP Baudrate prescaler */
  }
  else if ( CAN_BusConfig == VALUE_BUS_CONFIG_11bit_1000kbit ||
            CAN_BusConfig == VALUE_BUS_CONFIG_29bit_1000kbit)
  {
    CAN_InitStructure.CAN_BS1 = CAN_BS1_3tq;
    CAN_InitStructure.CAN_BS2 = CAN_BS2_5tq;
    CAN_InitStructure.CAN_Prescaler = 4; /* BRP Baudrate prescaler */
  }
  else
  {
    CAN_InitStructure.CAN_BS1 = CAN_BS1_3tq;
    CAN_InitStructure.CAN_BS2 = CAN_BS2_5tq;
    CAN_InitStructure.CAN_Prescaler = 8; /* BRP Baudrate prescaler */
  }

  CAN_Init(CAN1, &CAN_InitStructure);


  //! \todo Filter must be configurable via parameter to use also 29bit CAN-ID
  /* CAN filter init */
  CAN_FilterInitStructure.CAN_FilterNumber          = 0;
  CAN_FilterInitStructure.CAN_FilterMode            = CAN_FilterMode_IdMask;
  CAN_FilterInitStructure.CAN_FilterScale           = CAN_FilterScale_32bit;
  CAN_FilterInitStructure.CAN_FilterIdHigh          = 0x0700 << 5; /* CAN-ID 0x700 */
  CAN_FilterInitStructure.CAN_FilterIdLow           = 0x0000;
  CAN_FilterInitStructure.CAN_FilterMaskIdHigh      = 0x0700 << 5; /* Range 0x700-0x7FF) */
  CAN_FilterInitStructure.CAN_FilterMaskIdLow       = 0x0000;
  CAN_FilterInitStructure.CAN_FilterFIFOAssignment  = 0;
  CAN_FilterInitStructure.CAN_FilterActivation      = ENABLE;
  CAN_FilterInit(&CAN_FilterInitStructure);

  CAN_ITConfig(CAN1, CAN_IT_FMP0, ENABLE);

  DEBUGUARTPRINT("\r\n*** CANx_Configuration (CAN1) finished***");
}
/*----------------------------------------------------------------------------*/
