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
#include "odp_uds.h"
#include "mc_can.h"
#include "stm32f10x.h"
#include "SystemConfig.h"

/* callback function for received data */
recv_cbf  reportReceivedData = NULL;
uint8_t   CAN_BusConfig;

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

  if (CAN_BusConfig == VALUE_BUS_CONFIG_29bit_125kbit ||
      CAN_BusConfig == VALUE_BUS_CONFIG_29bit_250kbit ||
      CAN_BusConfig == VALUE_BUS_CONFIG_29bit_500kbit ||
      CAN_BusConfig == VALUE_BUS_CONFIG_29bit_1000kbit)
  {
    TxMessage.ExtId = data->recv;   /* Extended CAN identifier 29bit */
    TxMessage.IDE   = CAN_ID_EXT;   /* IDE=1 for Extended CAN identifier 29 bit */
  }
  else
  {
    TxMessage.StdId = data->recv;   /* Standard CAN identifier 11bit */
    TxMessage.IDE   = CAN_ID_STD;   /* IDE=0 for Standard CAN identifier 11 bit */
  }

  TxMessage.RTR     = CAN_RTR_DATA; /* Data frame */
  TxMessage.DLC     = data->len;    /* Data length code */

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
bus_param_can (portBASE_TYPE param, portBASE_TYPE value)
{
  switch (param)
  {
    case PARAM_BUS_CONFIG:
      if (value != 0)
        CAN1_Configuration(value); /* reinitialization of CAN interface */
        CAN_BusConfig = value;
      break;

    default:
      break;
  }

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
      reportReceivedData = param;
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

  if (RxMessage.StdId != 0 || RxMessage.ExtId != 0)
    {
      /* Data received. Process it. */
      if (RxMessage.IDE = CAN_ID_STD)
        dp.recv = RxMessage.StdId; /* Standard CAN frame 11bit received */
      else
        dp.recv = RxMessage.ExtId; /* Extended CAN frame 29bit received */
      dp.len  = RxMessage.DLC;
      dp.err  = 0x00; /* use received value for error simulations */
      dp.data = &RxMessage.Data[0]; /* data starts here */
      reportReceivedData (&dp);
    }

  DEBUGUARTPRINT("\r\n*** USB_LP_CAN1_RX0_IRQHandler finished ***");
}
/*----------------------------------------------------------------------------*/
