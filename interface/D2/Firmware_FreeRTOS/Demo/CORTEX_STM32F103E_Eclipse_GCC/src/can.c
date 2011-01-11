/****************** (C) Matthias *******************************************************/
/* optimized for PRIMER2, extended and amended by Matthias email webmaster@tg-info.de  */
/* -------- Includes ------------------------------------------------------------------*/
#include    "stm32f10x_can.h"

/* -------- Used Global variables -----------------------------------------------------*/

/* -------- Create Global variables ---------------------------------------------------*/
void        USB_LP_CAN1_RX0_IRQHandler(void);  // Interrupt when message is received can

/* -------- Private define ------------------------------------------------------------*/

/* -------- Private variables ---------------------------------------------------------*/

/* -------- Prototyp local ------------------------------------------------------------*/

/* -------- Code ----------------------------------------------------------------------*/
void USB_LP_CAN1_RX0_IRQHandler(void)
{
   /*
   CanTxMsg TxMessage;
   CanRxMsg RxMessage;
   
   CAN_Receive(CAN1, CAN_FIFO0, &RxMessage);
   
   TxMessage.StdId   = 0x7eF;
   TxMessage.ExtId   = 0x01;
   TxMessage.RTR     = CAN_RTR_DATA;
   TxMessage.IDE     = CAN_ID_STD;
   TxMessage.DLC     = 8;
   TxMessage.Data[0] = (RxMessage.StdId >> 8) & 0xFF;
   TxMessage.Data[1] =  RxMessage.StdId       & 0xFF;
   TxMessage.Data[2] =  RxMessage.DLC;
   TxMessage.Data[3] =  RxMessage.Data[0];
   TxMessage.Data[4] =  RxMessage.Data[1];
   TxMessage.Data[5] =  RxMessage.Data[2];
   TxMessage.Data[6] =  RxMessage.Data[3];
   TxMessage.Data[7] =  RxMessage.Data[4];
   CAN_Transmit(CAN1, &TxMessage);
   */
}
