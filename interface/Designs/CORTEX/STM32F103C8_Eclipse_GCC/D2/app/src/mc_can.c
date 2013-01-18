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


 OOBD C source files requirement:
 Unix EOL file format
 UTF-8
 formated with "indent -kr"

 Please ensure to read the configuration and relevant port sections of the
 online documentation.


 OOBD is using FreeRTOS (www.FreeRTOS.org)

 */

/**
 * MC specific can routines
 */

/* OOBD headers. */
#include "od_base.h"
#include "od_protocols.h"
#include "odb_can.h"
#include "mc_can.h"
#include "mc_sys.h"
#include "stm32f10x.h"
#include "SystemConfig.h"

extern char *oobd_Error_Text_OS;
extern struct CanConfig *canConfig;

/* callback function for received data */
recv_cbf reportReceivedData = NULL;

portBASE_TYPE rxCount;
portBASE_TYPE txCount;
portBASE_TYPE errCount;
portBASE_TYPE stCANBusOffErr;
portBASE_TYPE stCANBusWarningErr;
portBASE_TYPE stCANBusPassiveErr;

portBASE_TYPE bus_init_can() {
	NVIC_InitTypeDef NVIC_InitStructure;
	extern startupProtocol;

	rxCount = 0;
	txCount = 0;
	errCount = 0;
	stCANBusOffErr = 0;
	stCANBusWarningErr = 0;
	stCANBusPassiveErr = 0;

	canConfig = pvPortMalloc(sizeof(struct CanConfig));
	if (canConfig == NULL) {
		DEBUGPRINT("Fatal error: Not enough heap to allocate CanConfig!\n",
				'a');
		return pdFAIL;
	}
	canConfig->bus = VALUE_BUS_MODE_SILENT; /* default */
	canConfig->busConfig = VALUE_BUS_CONFIG_11bit_500kbit; /* default */

	if (startupProtocol == VALUE_PARAM_PROTOCOL_CAN_UDS) {
		/* Enable CAN1 Receive interrupts for UDS protocol */
		NVIC_InitStructure.NVIC_IRQChannel = USB_LP_CAN1_RX0_IRQn;
		NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority
				= (configMAX_SYSCALL_INTERRUPT_PRIORITY >> 4) + 1;
		NVIC_InitStructure.NVIC_IRQChannelSubPriority = 1;
		NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
		NVIC_Init(&NVIC_InitStructure);

		/* Enable CAN1 Receive interrupts for UDS protocol */
		NVIC_InitStructure.NVIC_IRQChannel = CAN1_SCE_IRQn;
		NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority
				= (configMAX_SYSCALL_INTERRUPT_PRIORITY >> 4) + 1;
		NVIC_InitStructure.NVIC_IRQChannelSubPriority = 2;
		NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
		NVIC_Init(&NVIC_InitStructure);
	}

	return pdPASS;
}

/*----------------------------------------------------------------------------*/

portBASE_TYPE bus_send_can(data_packet * data) {

	DEBUGUARTPRINT("\r\n*** bus_send_can entered! ***");

	CanTxMsg TxMessage;

	if (canConfig->busConfig == VALUE_BUS_CONFIG_29bit_125kbit
			|| canConfig->busConfig == VALUE_BUS_CONFIG_29bit_250kbit
			|| canConfig->busConfig == VALUE_BUS_CONFIG_29bit_500kbit
			|| canConfig->busConfig == VALUE_BUS_CONFIG_29bit_1000kbit) {
		TxMessage.ExtId = data->recv; /* Extended CAN identifier 29bit */
		TxMessage.IDE = CAN_ID_EXT; /* IDE=1 for Extended CAN identifier 29 bit */
	} else {
		TxMessage.StdId = data->recv; /* Standard CAN identifier 11bit */
		TxMessage.IDE = CAN_ID_STD; /* IDE=0 for Standard CAN identifier 11 bit */
	}

	TxMessage.RTR = CAN_RTR_DATA; /* Data frame */
	TxMessage.DLC = data->len; /* Data length code */

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

	txCount++;
	if (txCount > 100000) {
		rxCount /= 2;
		txCount /= 2;
		errCount /= 2;
	}

	DEBUGUARTPRINT("\r\n*** bus_send_can finished! ***");
	return pdPASS;
}

/*----------------------------------------------------------------------------*/

void bus_flush_can() {
	DEBUGPRINT("Flush CAN\n", 'a');
}

/*----------------------------------------------------------------------------*/

void bus_param_can_spec_Print(portBASE_TYPE msgType, void *data,
		printChar_cbf printchar) {
	param_data *args;
	args = data;
	DEBUGPRINT
	("Bus Parameter received via Outputtask param %ld value %ld\n",
			args->args[ARG_RECV], args->args[ARG_CMD]);

}

/*----------------------------------------------------------------------------*/

portBASE_TYPE bus_param_can_spec(param_data * args) {
	switch (args->args[ARG_CMD]) {
	case PARAM_BUS_CONFIG:
		rxCount = 0;
		txCount = 0;
		errCount = 0;
		if (args->args[ARG_VALUE_1] != 0)
			CAN1_Configuration(args->args[ARG_VALUE_1], CAN_Mode_Silent); /* reinitialization of CAN interface */
		canConfig->busConfig = args->args[ARG_VALUE_1];
		createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0, NULL);
		break;

	case PARAM_BUS_MODE:
		rxCount = 0;
		txCount = 0;
		errCount = 0;
		switch (args->args[ARG_VALUE_1]) {
		case VALUE_BUS_MODE_SILENT:
			CAN1_Configuration((uint8_t) canConfig->busConfig, CAN_Mode_Silent); /* set CAN interface to silent mode */
			canConfig->bus = args->args[ARG_VALUE_1]; /* set config.bus to current value of Paramter */
			// send event information to the ILM task
			CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_OFF);
			createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0, NULL);
			break;
		case VALUE_BUS_MODE_LOOP_BACK:
			CAN1_Configuration((uint8_t) canConfig->busConfig,
					CAN_Mode_LoopBack); /* set CAN interface to loop back mode */
			canConfig->bus = args->args[ARG_VALUE_1]; /* set config.bus to current value of Paramter */
			// send event information to the ILM task
			CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_ON);
			createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0, NULL);
			break;
		case VALUE_BUS_MODE_LOOP_BACK_WITH_SILENT:
			CAN1_Configuration((uint8_t) canConfig->busConfig,
					CAN_Mode_Silent_LoopBack); /* set CAN interface to loop back combined with silent mode */
			canConfig->bus = args->args[ARG_VALUE_1]; /* set config.bus to current value of Paramter */
			// send event information to the ILM task
			CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_ON);
			createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0, NULL);
			break;
		case VALUE_BUS_MODE_NORMAL:
			CAN1_Configuration((uint8_t) canConfig->busConfig, CAN_Mode_Normal); /* set CAN interface to normal mode */
			canConfig->bus = args->args[ARG_VALUE_1]; /* set config.bus to current value of Paramter */
			// send event information to the ILM task
			CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_ON);
			createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0, NULL);
			break;

		default:
			createCommandResultMsg(FBID_BUS_SPEC,
					ERR_CODE_OS_COMMAND_NOT_SUPPORTED, args->args[ARG_VALUE_1],
					ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
			break;
		}
		break;

	case PARAM_BUS_OUTPUT_ACTIVATE:
		rxCount = 0;
		txCount = 0;
		errCount = 0;
		switch (args->args[ARG_VALUE_1]) {
		case 0:
		case 1:
			CreateEventMsg(MSG_EVENT_BUS_MODE,
					MSG_EVENT_BUS_MODE_OFF);
			CreateEventMsg(MSG_EVENT_BUS_CHANNEL,
					args->args[ARG_VALUE_1] == 1 ? 1 : 2);
			sysIoCtrl(IO_REL1, 0, args->args[ARG_VALUE_1], 0, 0);
			//! \bug this delay causes the protocol task to sleep for this time, but dring that his message queue runs full
			//vTaskDelay( 250 / portTICK_RATE_MS ); // wait to give the mechanic relay time to switch
			createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0, NULL);
			break;
		default:
			createCommandResultMsg(FBID_BUS_SPEC,
					ERR_CODE_OS_COMMAND_NOT_SUPPORTED, args->args[ARG_VALUE_1],
					ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
			break;
		}
		break;

	default:
		createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_OS_UNKNOWN_COMMAND, 0,
				ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
		break;
	}

	return pdPASS;
}

/*----------------------------------------------------------------------------*/

void bus_close_can() {

	NVIC_InitTypeDef NVIC_InitStructure;

	reportReceivedData = NULL;

	/* Disable CAN1 Receive interrupt generally */
	NVIC_InitStructure.NVIC_IRQChannel = USB_LP_CAN1_RX0_IRQn;

	/* NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0; */
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority
			= (configMAX_SYSCALL_INTERRUPT_PRIORITY >> 4) + 1;
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = 1;
	NVIC_InitStructure.NVIC_IRQChannelCmd = DISABLE;
	NVIC_Init(&NVIC_InitStructure);
}

/*----------------------------------------------------------------------------*/

portBASE_TYPE busControl(portBASE_TYPE cmd, void *param) {
	switch (cmd) {
	case ODB_CMD_RECV:
		reportReceivedData = param;
		return pdPASS;
		break;
	default:
		return pdFAIL;
		break;
	}
	return pdFAIL;
}

/*----------------------------------------------------------------------------*/
void USB_LP_CAN1_RX0_IRQHandler(void) {
	DEBUGUARTPRINT("\r\n*** USB_LP_CAN1_RX0_IRQHandler entered ***");
	portBASE_TYPE xHigherPriorityTaskWoken = pdFALSE;
	uint8_t i;
	uint16_t LedDuration;
	CanRxMsg RxMessage;
	static data_packet dp;

	/* initialize RxMessage CAN frame */
	RxMessage.StdId = 0x00;
	RxMessage.ExtId = 0x00;
	RxMessage.IDE = CAN_ID_STD;
	RxMessage.DLC = 0;
	RxMessage.FMI = 0;
	for (i = 0; i < 8; i++) {
		RxMessage.Data[i] = 0x00;
	}

	CAN_Receive(CAN1, CAN_FIFO0, &RxMessage);

	if (RxMessage.StdId != 0 || RxMessage.ExtId != 0) {
		rxCount++;	/* increment counter for valid received CAN message */
		if (rxCount > 100000) {
		  rxCount /= 2;
		  txCount /= 2;
		  errCount /= 2;
		}

		/* Data received. Process it. */
		if (RxMessage.IDE == CAN_ID_STD)
			dp.recv = RxMessage.StdId; /* Standard CAN frame 11bit received */
		else
			dp.recv = RxMessage.ExtId; /* Extended CAN frame 29bit received */
		/* CAN-Frame values which are independent on standard or extended identifiers */
		dp.len = RxMessage.DLC;
		dp.err = 0x00; /* use received value for error simulations */
		dp.data = &RxMessage.Data[0]; /* data starts here */
		if (reportReceivedData)
			reportReceivedData(&dp);
	}
	//  portEND_SWITCHING_ISR( xHigherPriorityTaskWoken );
	DEBUGUARTPRINT("\r\n*** USB_LP_CAN1_RX0_IRQHandler finished ***");
}

/*----------------------------------------------------------------------------*/
void CAN1_SCE_IRQHandler(void) {
	DEBUGUARTPRINT("\r\n*** CAN1_SCE_IRQHandler entered ***");
	portBASE_TYPE xHigherPriorityTaskWoken = pdFALSE;

	/* check for receive errors */
	if (CAN_GetLastErrorCode(CAN1) == CAN_ErrorCode_StuffErr
			|| CAN_GetLastErrorCode(CAN1) == CAN_ErrorCode_FormErr
			|| CAN_GetLastErrorCode(CAN1) == CAN_ErrorCode_ACKErr
			|| CAN_GetLastErrorCode(CAN1) == CAN_ErrorCode_BitRecessiveErr
			|| CAN_GetLastErrorCode(CAN1) == CAN_ErrorCode_BitDominantErr
			|| CAN_GetLastErrorCode(CAN1) == CAN_ErrorCode_CRCErr) {
		errCount ++; // increment err Counter if CAN error occurs
		/* clear all interrupt Flags */
		CAN_ClearITPendingBit(CAN1, CAN_IT_ERR);
		CAN_ClearITPendingBit(CAN1, CAN_IT_LEC);
		CAN_ClearITPendingBit(CAN1, CAN_IT_BOF);
		CAN_ClearITPendingBit(CAN1, CAN_IT_EPV);
		CAN_ClearITPendingBit(CAN1, CAN_IT_EWG);
	}

	DEBUGUARTPRINT("\r\n*** CAN1_SCE_IRQHandler finished ***");
}

/*----------------------------------------------------------------------------*/

portBASE_TYPE bus_rx_error_can() {
	return errCount;
}

/*----------------------------------------------------------------------------*/

portBASE_TYPE bus_tx_error_can() {
	return 0;
}

/*----------------------------------------------------------------------------*/

void bus_clear_rx_error_can() {
	errCount = 0;
}

/*----------------------------------------------------------------------------*/

void bus_clear_tx_error_can() {
}

/*----------------------------------------------------------------------------*/

portBASE_TYPE bus_rx_count_can() {
	return rxCount;
}

/*----------------------------------------------------------------------------*/

portBASE_TYPE bus_tx_count_can() {
	return txCount;
}

/*----------------------------------------------------------------------------*/

void bus_clear_rx_count_can() {
	rxCount = 0;
}

/*----------------------------------------------------------------------------*/

void bus_clear_tx_count_can() {
	txCount = 0;
}

portBASE_TYPE bus_busoff_error_can() {
	/* check for Bus-off flag */
	stCANBusOffErr = CAN_GetFlagStatus(CAN1, CAN_FLAG_BOF);
	return stCANBusOffErr;
}

portBASE_TYPE bus_passive_error_can() {
	/* check for Error passive flag */
	stCANBusPassiveErr = CAN_GetFlagStatus(CAN1, CAN_FLAG_EPV);
	return stCANBusPassiveErr;
}

portBASE_TYPE bus_warning_error_can() {
	/* check for Error Warning flag */
	stCANBusWarningErr = CAN_GetFlagStatus(CAN1, CAN_FLAG_EWG);
	return stCANBusWarningErr;
}
