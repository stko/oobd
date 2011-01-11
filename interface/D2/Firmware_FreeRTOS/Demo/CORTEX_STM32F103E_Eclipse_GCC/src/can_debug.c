/****************** (C) Matthias *******************************************************/
/* optimized for PRIMER2, extended and amended by Matthias email webmaster@tg-info.de  */
/* -------- Includes ------------------------------------------------------------------*/
#include    "stm32f10x_can.h"

/* -------- Used Global variables -----------------------------------------------------*/

/* -------- Create Global variables ---------------------------------------------------*/
void        CAN_Debug_Send_All_Primer_Data(void);       /* Sends several can messages with the sensors values */

/* -------- Private define ------------------------------------------------------------*/
u8          SendZyklus = 0;

/* -------- Private variables ---------------------------------------------------------*/

/* -------- Prototyp local ------------------------------------------------------------*/


/* -------- Code ----------------------------------------------------------------------*/
void CAN_Debug_Send_All_Primer_Data(void)
{
   SendZyklus++;
   CanTxMsg CanMsg;
   
   SendZyklus = 1;

   CanMsg.StdId   = 0x600 + SendZyklus;
   CanMsg.ExtId   = 0x01;
   CanMsg.RTR     = CAN_RTR_DATA;
   CanMsg.IDE     = CAN_ID_STD;
   CanMsg.DLC     = 8;
   
   switch (SendZyklus)
   {
      case (0):
      {
         CanMsg.Data[0] = 0x01;
         CanMsg.Data[1] = 0x02;
         CanMsg.Data[2] = 0x03;
         CanMsg.Data[3] = 0x04;
         CanMsg.Data[4] = 0x05;
         CanMsg.Data[5] = 0x06;
         CanMsg.Data[6] = 0x07;
         CanMsg.Data[7] = 0x08;
         break;
      }
      case (1):
      {
         CanMsg.Data[0] = 0x10;
         CanMsg.Data[1] = 0x20;
         CanMsg.Data[2] = 0x30;
         CanMsg.Data[3] = 0x40;
         CanMsg.Data[4] = 0x50;
         CanMsg.Data[5] = 0x60;
         CanMsg.Data[6] = 0x70;
         CanMsg.Data[7] = 0x80;
         break;
      }
      case (2):
      {
         CanMsg.Data[0] = 0x11;
         CanMsg.Data[1] = 0x22;
         CanMsg.Data[2] = 0x33;
         CanMsg.Data[3] = 0x44;
         CanMsg.Data[4] = 0x55;
         CanMsg.Data[5] = 0x66;
         CanMsg.Data[6] = 0x77;
         CanMsg.Data[7] = 0x88;
         SendZyklus = -1;
         break;
      }
      default:
      {
         SendZyklus = -1;
         break;
      }
   }
   
   CAN_Transmit(CAN1, &CanMsg);
   
}
