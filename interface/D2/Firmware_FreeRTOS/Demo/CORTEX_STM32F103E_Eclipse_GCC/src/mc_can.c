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

/**
 * MC specific can routines
 */

/* OOBD headers. */
#include "od_config.h"
#include "od_protocols.h"
#include "odb_can.h"
#include "mc_can.h"
#include "stm32f10x.h"

/* callback function for received data */
recv_cbf reportReceicedData = NULL;

portBASE_TYPE
bus_init_can ()
{
  return pdPASS;
}
/*----------------------------------------------------------------------------*/

portBASE_TYPE
bus_send_can (data_packet * data)
{
  DEBUGUARTPRINT("\r\n*** bus_send_can entered! ***");

  CanTxMsg TxMessage;

  TxMessage.StdId = 0x7E8;        /* CAN - ID */
  TxMessage.ExtId = 0x01;         /* Standard CAN identifier 11bit */
  TxMessage.RTR   = CAN_RTR_DATA; /* Data frame */
  TxMessage.IDE   = CAN_ID_STD;   /* IDE=0 for Standard CAN identifier 11 bit */
  TxMessage.DLC   = 8;            /* Data length code, default 8 byte */

  TxMessage.Data[0] = data->data[0];
  TxMessage.Data[1] = data->data[1];
  TxMessage.Data[2] = data->data[2];
  TxMessage.Data[3] = data->data[3];
  TxMessage.Data[4] = data->data[4];
  TxMessage.Data[5] = data->data[5];
  TxMessage.Data[6] = data->data[6];
  TxMessage.Data[7] = data->data[7];

  /* transmit whole CAN frame as specified above on CAN1 */
  CAN_Transmit(CAN1, &TxMessage);

  DEBUGUARTPRINT("\r\n*** bus_send_can finished! ***");
  return pdPASS;
}
/*----------------------------------------------------------------------------*/

void
bus_flush_can ()
{
  DEBUGPRINT ("Flush CAN\n", 'a');
}
/*----------------------------------------------------------------------------*/

portBASE_TYPE
bus_param_can (portBASE_TYPE cmd, void *param)
{
  return pdPASS;
}
/*----------------------------------------------------------------------------*/

void
bus_close_can ()
{

}
/*----------------------------------------------------------------------------*/

portBASE_TYPE
busControl (portBASE_TYPE cmd, void *param)
{
  switch (cmd)
    {
    case ODB_CMD_RECV:
      reportReceicedData = param;
      return pdPASS;
      break;
    default:
      return pdFAIL;
      break;
    }
}
/*----------------------------------------------------------------------------*/

void USB_LP_CAN1_RX0_IRQHandler(void)
{
  DEBUGUARTPRINT("\r\n*** USB_LP_CAN1_RX0_IRQHandler entered ***");

  uint8_t i;
  CanRxMsg RxMessage;
  static data_packet dp;
  /* CanTxMsg TxMessage; */

  /* initialize RxMessage CAN frame */
  RxMessage.StdId = 0x00;
  RxMessage.ExtId = 0x00;
  RxMessage.IDE   = CAN_ID_STD;
  RxMessage.DLC   = 0;
  RxMessage.FMI   = 0;
  for (i = 0; i < 8; i++)
  {
    RxMessage.Data[i] = 0x00;
  }

  CAN_Receive(CAN1, CAN_FIFO0, &RxMessage);

  if (RxMessage.StdId != 0)
    {
      /* Data received. Process it. */
      dp.recv = RxMessage.StdId;
      dp.len  = RxMessage.DLC;
      dp.err  = 0x00; /* use received value for error simulations */
      dp.data = &RxMessage.Data[0]; /* data starts here */
      reportReceicedData (&dp);
    }

  DEBUGUARTPRINT("\r\n*** USB_LP_CAN1_RX0_IRQHandler finished ***");
}
/*----------------------------------------------------------------------------*/
