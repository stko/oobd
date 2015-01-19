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
 * MC specific CAN routines
 */

#include "stm32f10x.h"

/* OOBD headers. */
#include "SystemConfig.h"
#include "od_base.h"
#include "od_protocols.h"
#include "odb_can.h"
#include "mc_can.h"
#include "mc_sys.h"

extern char *oobd_Error_Text_OS;
extern struct CanConfig *canConfig;

/* callback function for received data */
recv_cbf reportReceivedData = NULL;

UBaseType_t rxCount;
UBaseType_t txCount;
UBaseType_t errCount;
UBaseType_t stCANBusOffErr;
UBaseType_t stCANBusWarningErr;
UBaseType_t stCANBusPassiveErr;
UBaseType_t ctrCANTec;
UBaseType_t ctrCANRec;

UBaseType_t bus_init_can()
{
    NVIC_InitTypeDef NVIC_InitStructure;
    extern startupProtocol;

    rxCount = 0;
    txCount = 0;
    errCount = 0;
    stCANBusOffErr = 0;
    stCANBusWarningErr = 0;
    stCANBusPassiveErr = 0;
    ctrCANTec = 0;
    ctrCANRec = 0;

    canConfig = pvPortMalloc(sizeof(struct CanConfig));
    if (canConfig == NULL) {
	DEBUGPRINT("Fatal error: Not enough heap to allocate CanConfig!\n",
		   'a');
	return pdFAIL;
    }
    canConfig->bus = VALUE_BUS_MODE_SILENT;	/* default */
    canConfig->busConfig = VALUE_BUS_CONFIG_11bit_500kbit;	/* default */

    /* Enable CAN1 Transmit interrupts for CAN messages */
    NVIC_InitStructure.NVIC_IRQChannel = USB_HP_CAN1_TX_IRQn;
    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority
	= (configMAX_SYSCALL_INTERRUPT_PRIORITY >> 4) + 1;
    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 3;
    NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
    NVIC_Init(&NVIC_InitStructure);

    /* Enable CAN1 Receive interrupts for CAN messages */
    NVIC_InitStructure.NVIC_IRQChannel = USB_LP_CAN1_RX0_IRQn;
    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority
	= (configMAX_SYSCALL_INTERRUPT_PRIORITY >> 4) + 1;
    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 1;
    NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
    NVIC_Init(&NVIC_InitStructure);

    /* Enable CAN1 Receive error interrupts for CAN messages */
    NVIC_InitStructure.NVIC_IRQChannel = CAN1_SCE_IRQn;
    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority
	= (configMAX_SYSCALL_INTERRUPT_PRIORITY >> 4) + 1;
    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 2;
    NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
    NVIC_Init(&NVIC_InitStructure);

    return pdPASS;
}

/*----------------------------------------------------------------------------*/

UBaseType_t bus_send_can(data_packet * data)
{

    DEBUGUARTPRINT("\r\n*** bus_send_can entered! ***");

    CanTxMsg TxMessage;

    if (canConfig->busConfig == VALUE_BUS_CONFIG_29bit_125kbit
	|| canConfig->busConfig == VALUE_BUS_CONFIG_29bit_250kbit
	|| canConfig->busConfig == VALUE_BUS_CONFIG_29bit_500kbit
	|| canConfig->busConfig == VALUE_BUS_CONFIG_29bit_1000kbit) {
	TxMessage.ExtId = data->recv;	/* Extended CAN identifier 29bit */
	TxMessage.IDE = CAN_ID_EXT;	/* IDE=1 for Extended CAN identifier 29 bit */
    } else {
	TxMessage.StdId = data->recv;	/* Standard CAN identifier 11bit */
	TxMessage.IDE = CAN_ID_STD;	/* IDE=0 for Standard CAN identifier 11 bit */
    }

    TxMessage.RTR = CAN_RTR_DATA;	/* Data frame */
    TxMessage.DLC = data->len;	/* Data length code */

    TxMessage.Data[0] = data->data[0];
    TxMessage.Data[1] = data->data[1];
    TxMessage.Data[2] = data->data[2];
    TxMessage.Data[3] = data->data[3];
    TxMessage.Data[4] = data->data[4];
    TxMessage.Data[5] = data->data[5];
    TxMessage.Data[6] = data->data[6];
    TxMessage.Data[7] = data->data[7];

    /* transmit whole CAN frame as specified above on CAN1 */
    /* Tx-Counter is increased in CAN-Tx interrupt routine */
    /* repeat sending CAN messages until mailboxes is empty */
    while(CAN_Transmit(CAN1, &TxMessage)>3);

    DEBUGUARTPRINT("\r\n*** bus_send_can finished! ***");
    return pdPASS;
}

/*----------------------------------------------------------------------------*/

void bus_flush_can()
{
    DEBUGPRINT("Flush CAN\n", 'a');
}

/*----------------------------------------------------------------------------*/

void bus_param_can_spec_Print(UBaseType_t msgType, void *data,
			      printChar_cbf printchar)
{
    param_data *args;
    args = data;
    DEBUGPRINT
	("Bus Parameter received via Outputtask param %ld value %ld\n",
	 args->args[ARG_RECV], args->args[ARG_CMD]);

}

/*----------------------------------------------------------------------------*/

uint16_t CAN_GetFilterReg16(uint8_t FilterID, uint8_t FilterReg,
			    uint8_t FilterPos)
{
    /* Check the parameters */
    assert_param(IS_CAN_ALL_PERIPH(CANx));

    if (FilterPos == 0) {	/* IDLow */
	/* Get the LowID of the 32bit Filter register Fx.FR1 */
	if (FilterReg == 1)	/* FR1 */
	    return (uint16_t) (CAN1->sFilterRegister[FilterID].
			       FR1 & 0x0000FFFF)
		>> 5;
	else if (FilterReg == 2)	/* FR2 */
	    return (uint16_t) (CAN1->sFilterRegister[FilterID].
			       FR2 & 0x0000FFFF)
		>> 5;
	else
	    return NULL;
    } else if (FilterPos == 1) {	/* ID High */
	if (FilterReg == 1)	/* FR1 */
	    return (uint16_t) (CAN1->sFilterRegister[FilterID].FR1 >> 16 &
			       0x0000FFFF) >> 5;
	else if (FilterReg == 2)	/* FR2 */
	    return (uint16_t) (CAN1->sFilterRegister[FilterID].FR2 >> 16 &
			       0x0000FFFF) >> 5;
	else
	    return NULL;
    } else
	return NULL;
}

/*----------------------------------------------------------------------------*/

uint32_t CAN_GetFilterReg32(uint8_t FilterID, uint8_t FilterReg)
{
    /* Check the parameters */
    assert_param(IS_CAN_ALL_PERIPH(CANx));

    /* Get the LowID of the 32bit Filter register Fx.FR1 */
    if (FilterReg == 1)		/* FR1 */
	return (uint32_t) CAN1->sFilterRegister[FilterID].FR1 >> 3;
    else if (FilterReg == 2)	/* FR2 */
	return (uint32_t) CAN1->sFilterRegister[FilterID].FR2 >> 3;
    else
	return NULL;
}

/*----------------------------------------------------------------------------*/

UBaseType_t bus_param_can_spec(param_data * args)
{
    CAN_FilterInitTypeDef CAN_FilterInitStructure;
    uint8_t i;

    switch (args->args[ARG_CMD]) {
    case PARAM_BUS_CONFIG:
	rxCount = 0;
	txCount = 0;
	errCount = 0;
	if (args->args[ARG_VALUE_1] != 0)
	    CAN1_Configuration(args->args[ARG_VALUE_1], CAN_Mode_Silent);	/* reinitialization of CAN interface */
	canConfig->busConfig = args->args[ARG_VALUE_1];
	createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0, NULL);
	break;

    case PARAM_BUS_MODE:
	rxCount = 0;
	txCount = 0;
	errCount = 0;
	switch (args->args[ARG_VALUE_1]) {
	case VALUE_BUS_MODE_SILENT:
	    CAN1_Configuration((uint8_t) canConfig->busConfig, CAN_Mode_Silent);	/* set CAN interface to silent mode */
	    canConfig->bus = args->args[ARG_VALUE_1];	/* set config.bus to current value of Paramter */
	    // send event information to the ILM task
	    CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_OFF);
	    createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0,
				   NULL);
	    break;
	case VALUE_BUS_MODE_LOOP_BACK:
	    CAN1_Configuration((uint8_t) canConfig->busConfig, CAN_Mode_LoopBack);	/* set CAN interface to loop back mode */
	    canConfig->bus = args->args[ARG_VALUE_1];	/* set config.bus to current value of Paramter */
	    // send event information to the ILM task
	    CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_ON);
	    createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0,
				   NULL);
	    break;
	case VALUE_BUS_MODE_LOOP_BACK_WITH_SILENT:
	    CAN1_Configuration((uint8_t) canConfig->busConfig, CAN_Mode_Silent_LoopBack);	/* set CAN interface to loop back combined with silent mode */
	    canConfig->bus = args->args[ARG_VALUE_1];	/* set config.bus to current value of Paramter */
	    // send event information to the ILM task
	    CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_ON);
	    createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0,
				   NULL);
	    break;
	case VALUE_BUS_MODE_NORMAL:
	    CAN1_Configuration((uint8_t) canConfig->busConfig, CAN_Mode_Normal);	/* set CAN interface to normal mode */
	    canConfig->bus = args->args[ARG_VALUE_1];	/* set config.bus to current value of Paramter */
	    // send event information to the ILM task
	    CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_ON);
	    createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0,
				   NULL);
	    break;

	default:
	    createCommandResultMsg(FBID_BUS_SPEC,
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED,
				   args->args[ARG_VALUE_1],
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
	    CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_OFF);
	    CreateEventMsg(MSG_EVENT_BUS_CHANNEL,
			   args->args[ARG_VALUE_1] == 1 ? 1 : 2);
	    sysIoCtrl(IO_REL1, 0, args->args[ARG_VALUE_1], 0, 0);
	    //! \bug this delay causes the protocol task to sleep for this time, but dring that his message queue runs full
	    //vTaskDelay( 250 / portTICK_PERIOD_MS ); // wait to give the mechanic relay time to switch
	    createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0,
				   NULL);
	    break;
	default:
	    createCommandResultMsg(FBID_BUS_SPEC,
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED,
				   args->args[ARG_VALUE_1],
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
	    break;
	}
	break;

    case PARAM_BUS_Can11FilterID:	/* 11bit CAN filter ID reconfig */
	/* check CAN-ID */
	if (args->args[ARG_VALUE_2] < 0x7FF) {
	    /* check if Filter Number is odd */
	    if (args->args[ARG_VALUE_1] == 1)
		CAN_FilterInitStructure.CAN_FilterNumber =
		    args->args[ARG_VALUE_1] - 1;
	    else if (args->args[ARG_VALUE_1] == 3
		     || args->args[ARG_VALUE_1] == 2)
		CAN_FilterInitStructure.CAN_FilterNumber =
		    args->args[ARG_VALUE_1] - 2;
	    else if (args->args[ARG_VALUE_1] == 5
		     || args->args[ARG_VALUE_1] == 4)
		CAN_FilterInitStructure.CAN_FilterNumber =
		    args->args[ARG_VALUE_1] - 3;
	    else if (args->args[ARG_VALUE_1] == 7
		     || args->args[ARG_VALUE_1] == 6)
		CAN_FilterInitStructure.CAN_FilterNumber =
		    args->args[ARG_VALUE_1] - 4;
	    else if (args->args[ARG_VALUE_1] == 9
		     || args->args[ARG_VALUE_1] == 8)
		CAN_FilterInitStructure.CAN_FilterNumber =
		    args->args[ARG_VALUE_1] - 5;
	    else if (args->args[ARG_VALUE_1] == 10)
		CAN_FilterInitStructure.CAN_FilterNumber =
		    args->args[ARG_VALUE_1] - 6;
	    else {
		createCommandResultMsg(FBID_BUS_SPEC,
				       ERR_CODE_OS_COMMAND_NOT_SUPPORTED,
				       args->args[ARG_VALUE_1],
				       ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
		break;
	    }

	    if (args->args[ARG_VALUE_1] == 1 ||
		args->args[ARG_VALUE_1] == 3 ||
		args->args[ARG_VALUE_1] == 5 ||
		args->args[ARG_VALUE_1] == 7 ||
		args->args[ARG_VALUE_1] == 9) {
		CAN_FilterInitStructure.CAN_FilterIdLow =
		    args->args[ARG_VALUE_2] << 5;
		CAN_FilterInitStructure.CAN_FilterIdHigh =
		    (uint16_t) (CAN1->sFilterRegister
				[CAN_FilterInitStructure.CAN_FilterNumber].
				FR2 & 0x0000FFFF);
	    } else {
		CAN_FilterInitStructure.CAN_FilterIdLow =
		    (uint16_t) (CAN1->sFilterRegister
				[CAN_FilterInitStructure.CAN_FilterNumber].
				FR1 & 0x0000FFFF);
		CAN_FilterInitStructure.CAN_FilterIdHigh =
		    args->args[ARG_VALUE_2] << 5;
	    }

	    CAN_FilterInitStructure.CAN_FilterMaskIdLow =
		(uint16_t) (CAN1->sFilterRegister
			    [CAN_FilterInitStructure.CAN_FilterNumber].FR1
			    >> 16 & 0x0000FFFF);
	    CAN_FilterInitStructure.CAN_FilterMaskIdHigh =
		(uint16_t) (CAN1->sFilterRegister
			    [CAN_FilterInitStructure.CAN_FilterNumber].FR2
			    >> 16 & 0x0000FFFF);
	    CAN_FilterInitStructure.CAN_FilterScale =
		CAN_FilterScale_16bit;
	    CAN_FilterInitStructure.CAN_FilterMode = CAN_FilterMode_IdMask;
	    CAN_FilterInitStructure.CAN_FilterFIFOAssignment = 0;
	    CAN_FilterInitStructure.CAN_FilterActivation = ENABLE;
	    CAN_FilterInit(&CAN_FilterInitStructure);
	    createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0,
				   NULL);
	} else {
	    createCommandResultMsg(FBID_BUS_SPEC,
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED,
				   args->args[ARG_VALUE_2],
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
	}
	break;

    case PARAM_BUS_Can29FilterID:	/* 29bit CAN filter ID reconfig */
	CAN_FilterInitStructure.CAN_FilterNumber =
	    args->args[ARG_VALUE_1] - 1;
	CAN_FilterInitStructure.CAN_FilterIdLow =
	    (uint16_t) (args->args[ARG_VALUE_2] & 0x0000FFFF) << 3;
	CAN_FilterInitStructure.CAN_FilterIdHigh =
	    (uint16_t) (args->args[ARG_VALUE_2] >> 13) & 0x0000FFFF;
	CAN_FilterInitStructure.CAN_FilterMaskIdLow =
	    (uint16_t) (CAN1->sFilterRegister
			[CAN_FilterInitStructure.CAN_FilterNumber].FR2 &
			0x0000FFFF);
	CAN_FilterInitStructure.CAN_FilterMaskIdHigh =
	    (uint16_t) (CAN1->sFilterRegister
			[CAN_FilterInitStructure.CAN_FilterNumber].FR2 >>
			16 & 0x0000FFFF);
	CAN_FilterInitStructure.CAN_FilterScale = CAN_FilterScale_32bit;
	CAN_FilterInitStructure.CAN_FilterMode = CAN_FilterMode_IdMask;
	CAN_FilterInitStructure.CAN_FilterFIFOAssignment = 0;
	CAN_FilterInitStructure.CAN_FilterActivation = ENABLE;
	CAN_FilterInit(&CAN_FilterInitStructure);
	createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0, NULL);
	break;

    case PARAM_BUS_Can11MaskID:	/* 11bit CAN filter mask ID reconfig */
	/* check CAN-ID */
	if (args->args[ARG_VALUE_2] <= 0x7FF) {
	    /* check filter mask number */
	    if (args->args[ARG_VALUE_1] == 1)
		CAN_FilterInitStructure.CAN_FilterNumber =
		    args->args[ARG_VALUE_1] - 1;
	    else if (args->args[ARG_VALUE_1] == 3
		     || args->args[ARG_VALUE_1] == 2)
		CAN_FilterInitStructure.CAN_FilterNumber =
		    args->args[ARG_VALUE_1] - 2;
	    else if (args->args[ARG_VALUE_1] == 5
		     || args->args[ARG_VALUE_1] == 4)
		CAN_FilterInitStructure.CAN_FilterNumber =
		    args->args[ARG_VALUE_1] - 3;
	    else if (args->args[ARG_VALUE_1] == 7
		     || args->args[ARG_VALUE_1] == 6)
		CAN_FilterInitStructure.CAN_FilterNumber =
		    args->args[ARG_VALUE_1] - 4;
	    else if (args->args[ARG_VALUE_1] == 9
		     || args->args[ARG_VALUE_1] == 8)
		CAN_FilterInitStructure.CAN_FilterNumber =
		    args->args[ARG_VALUE_1] - 5;
	    else if (args->args[ARG_VALUE_1] == 10)
		CAN_FilterInitStructure.CAN_FilterNumber =
		    args->args[ARG_VALUE_1] - 6;
	    else {
		createCommandResultMsg(FBID_BUS_SPEC,
				       ERR_CODE_OS_COMMAND_NOT_SUPPORTED,
				       args->args[ARG_VALUE_1],
				       ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
		break;
	    }

	    /* CAN filter mask ID reconfig */
	    if (args->args[ARG_VALUE_1] == 1 ||
		args->args[ARG_VALUE_1] == 3 ||
		args->args[ARG_VALUE_1] == 5 ||
		args->args[ARG_VALUE_1] == 7 ||
		args->args[ARG_VALUE_1] == 9) {
		CAN_FilterInitStructure.CAN_FilterMaskIdLow =
		    (uint16_t) args->args[ARG_VALUE_2] << 5;
		CAN_FilterInitStructure.CAN_FilterMaskIdHigh =
		    (uint16_t) (CAN1->sFilterRegister
				[CAN_FilterInitStructure.CAN_FilterNumber].
				FR2 >> 16 & 0x0000FFFF);
	    } else {
		CAN_FilterInitStructure.CAN_FilterMaskIdLow =
		    (uint16_t) (CAN1->sFilterRegister
				[CAN_FilterInitStructure.CAN_FilterNumber].
				FR1 >> 16 & 0x0000FFFF);
		CAN_FilterInitStructure.CAN_FilterMaskIdHigh =
		    (uint16_t) args->args[ARG_VALUE_2] << 5;
	    }
	    CAN_FilterInitStructure.CAN_FilterIdLow =
		(uint16_t) (CAN1->sFilterRegister
			    [CAN_FilterInitStructure.CAN_FilterNumber].FR1
			    & 0x0000FFFF);
	    CAN_FilterInitStructure.CAN_FilterIdHigh =
		(uint16_t) (CAN1->sFilterRegister
			    [CAN_FilterInitStructure.CAN_FilterNumber].FR2
			    & 0x0000FFFF);
	    CAN_FilterInitStructure.CAN_FilterScale =
		CAN_FilterScale_16bit;
	    CAN_FilterInitStructure.CAN_FilterMode = CAN_FilterMode_IdMask;
	    CAN_FilterInitStructure.CAN_FilterFIFOAssignment = 0;
	    CAN_FilterInitStructure.CAN_FilterActivation = ENABLE;
	    CAN_FilterInit(&CAN_FilterInitStructure);
	    createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0,
				   NULL);
	} else {
	    createCommandResultMsg(FBID_BUS_SPEC,
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED,
				   args->args[ARG_VALUE_2],
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
	}
	break;

    case PARAM_BUS_Can29MaskID:	/* 29bit CAN filter mask ID reconfig */
	CAN_FilterInitStructure.CAN_FilterNumber =
	    args->args[ARG_VALUE_1] - 1;
	CAN_FilterInitStructure.CAN_FilterIdLow =
	    (uint16_t) (CAN1->sFilterRegister
			[CAN_FilterInitStructure.CAN_FilterNumber].FR1 &
			0x0000FFFF);
	CAN_FilterInitStructure.CAN_FilterIdHigh =
	    (uint16_t) (CAN1->sFilterRegister
			[CAN_FilterInitStructure.CAN_FilterNumber].FR1 >>
			16 & 0x0000FFFF);
	CAN_FilterInitStructure.CAN_FilterMaskIdLow =
	    (uint16_t) (args->args[ARG_VALUE_2] & 0x0000FFFF) << 3;
	CAN_FilterInitStructure.CAN_FilterMaskIdHigh =
	    (uint16_t) (args->args[ARG_VALUE_2] >> 13) & 0x0000FFFF;
	CAN_FilterInitStructure.CAN_FilterScale = CAN_FilterScale_32bit;
	CAN_FilterInitStructure.CAN_FilterMode = CAN_FilterMode_IdMask;
	CAN_FilterInitStructure.CAN_FilterFIFOAssignment = 0;
	CAN_FilterInitStructure.CAN_FilterActivation = ENABLE;
	CAN_FilterInit(&CAN_FilterInitStructure);
	createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0, NULL);
	break;

    case PARAM_BUS_CanFilterReset:	/* 11bit CAN filter mask ID reconfig */
	for (i = 0; i < 14; i++) {
	    CAN_FilterInitStructure.CAN_FilterNumber = i;
	    CAN_FilterInitStructure.CAN_FilterIdLow = 0x000 << 5;
	    CAN_FilterInitStructure.CAN_FilterIdHigh = 0x000 << 5;
	    CAN_FilterInitStructure.CAN_FilterMaskIdLow = 0x07FF << 5;
	    CAN_FilterInitStructure.CAN_FilterMaskIdHigh = 0x07FF << 5;
	    CAN_FilterInitStructure.CAN_FilterScale =
		CAN_FilterScale_16bit;
	    CAN_FilterInitStructure.CAN_FilterMode = CAN_FilterMode_IdMask;
	    CAN_FilterInitStructure.CAN_FilterFIFOAssignment = 0;
	    CAN_FilterInitStructure.CAN_FilterActivation = ENABLE;
	    CAN_FilterInit(&CAN_FilterInitStructure);
	}
	createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_NO_ERR, 0, NULL);
	break;

    default:
	createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_OS_UNKNOWN_COMMAND,
			       0, ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
	break;
    }

    return pdPASS;
}

/*----------------------------------------------------------------------------*/

void bus_close_can()
{
    NVIC_InitTypeDef NVIC_InitStructure;

    reportReceivedData = NULL;

    /* Disable CAN1 Receive interrupt generally */
    NVIC_InitStructure.NVIC_IRQChannel = USB_LP_CAN1_RX0_IRQn;
    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority
	= (configMAX_SYSCALL_INTERRUPT_PRIORITY >> 4) + 1;
    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 1;
    NVIC_InitStructure.NVIC_IRQChannelCmd = DISABLE;
    NVIC_Init(&NVIC_InitStructure);

    /* Disable CAN1 Receive interrupts for CAN messages */
    NVIC_InitStructure.NVIC_IRQChannel = USB_HP_CAN1_TX_IRQn;
    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority
	= (configMAX_SYSCALL_INTERRUPT_PRIORITY >> 4) + 1;
    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 3;
    NVIC_InitStructure.NVIC_IRQChannelCmd = DISABLE;
    NVIC_Init(&NVIC_InitStructure);

    /* Disable CAN1 Receive error interrupts for CAN messages */
    NVIC_InitStructure.NVIC_IRQChannel = CAN1_SCE_IRQn;
    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority
	= (configMAX_SYSCALL_INTERRUPT_PRIORITY >> 4) + 1;
    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 2;
    NVIC_InitStructure.NVIC_IRQChannelCmd = DISABLE;
    NVIC_Init(&NVIC_InitStructure);
}

/*----------------------------------------------------------------------------*/

UBaseType_t busControl(UBaseType_t cmd, void *param)
{
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
void USB_LP_CAN1_RX0_IRQHandler(void)
{
    DEBUGUARTPRINT("\r\n*** USB_LP_CAN1_RX0_IRQHandler entered ***");
//    portBASE_TYPE xHigherPriorityTaskWoken = pdFALSE;
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
	rxCount++;		/* increment counter for valid received CAN message */
	if (rxCount > 100000) {
	    rxCount /= 2;
	    txCount /= 2;
	    errCount /= 2;
	}

	/* Data received. Process it. */
	if (RxMessage.IDE == CAN_ID_STD) {
	    dp.recv = RxMessage.StdId;		/* Standard CAN frame 11bit received */
		dp.recv &= ~0x80000000;			/* set Bit 31=0 for Extended CAN identifier 29 bit */
	}
	else {
	    dp.recv = RxMessage.ExtId;		/* Extended CAN frame 29bit received */
		dp.recv |= 0x80000000;			/* set Bit 31=1 for Extended CAN identifier 29 bit */
	}
	/* CAN-Frame values which are independent on standard or extended identifiers */
	dp.len = RxMessage.DLC;
	
	if (CAN_MessagePending(CAN1, CAN_FIFO0) == 0x00)
		dp.err = 0x00;		/* use received value for error simulations */
	else
		dp.err = 0x01;		/* set error flag indication if CAN message is pending */
	dp.data = &RxMessage.Data[0];	/* data starts here */
	if (reportReceivedData)
	    reportReceivedData(&dp,pdTRUE);
    }
    DEBUGUARTPRINT("\r\n*** USB_LP_CAN1_RX0_IRQHandler finished ***");
	
//    portEND_SWITCHING_ISR( xHigherPriorityTaskWoken );
}

/*----------------------------------------------------------------------------*/
void CAN1_SCE_IRQHandler(void)
{
    DEBUGUARTPRINT("\r\n*** CAN1_SCE_IRQHandler entered ***");
//    UBaseType_t xHigherPriorityTaskWoken = pdFALSE;

    /* check for receive errors */
    if (CAN_GetLastErrorCode(CAN1) == CAN_ErrorCode_StuffErr
	|| CAN_GetLastErrorCode(CAN1) == CAN_ErrorCode_FormErr
	|| CAN_GetLastErrorCode(CAN1) == CAN_ErrorCode_ACKErr
	|| CAN_GetLastErrorCode(CAN1) == CAN_ErrorCode_BitRecessiveErr
	|| CAN_GetLastErrorCode(CAN1) == CAN_ErrorCode_BitDominantErr
	|| CAN_GetLastErrorCode(CAN1) == CAN_ErrorCode_CRCErr)
	errCount++;		// increment err Counter if CAN error occurs

    /* clear all interrupt Flags */
    CAN_ClearITPendingBit(CAN1, CAN_IT_ERR);
    CAN_ClearITPendingBit(CAN1, CAN_IT_LEC);
    CAN_ClearITPendingBit(CAN1, CAN_IT_BOF);
    CAN_ClearITPendingBit(CAN1, CAN_IT_EPV);
    CAN_ClearITPendingBit(CAN1, CAN_IT_EWG);

    DEBUGUARTPRINT("\r\n*** CAN1_SCE_IRQHandler finished ***");
}

/*----------------------------------------------------------------------------*/
void USB_HP_CAN1_TX_IRQHandler(void)
{
    DEBUGUARTPRINT("\r\n*** USB_HP_CAN1_TX_IRQHandler entered ***");

    txCount++;			// increment CAN-Tx Counter if interrupt occurs
    if (txCount > 100000) {
	rxCount /= 2;
	txCount /= 2;
	errCount /= 2;
    }

    /* clear Tranmit interrupt flag */
    CAN_ClearITPendingBit(CAN1, CAN_IT_TME);

    DEBUGUARTPRINT("\r\n*** CAN1_SCE_IRQHandler finished ***");
}

/*----------------------------------------------------------------------------*/

UBaseType_t bus_rx_error_can()
{
    return errCount;
}

/*----------------------------------------------------------------------------*/

UBaseType_t bus_tx_error_can()
{
    return 0;
}

/*----------------------------------------------------------------------------*/

void bus_clear_rx_error_can()
{
    errCount = 0;
}

/*----------------------------------------------------------------------------*/

void bus_clear_tx_error_can()
{
}

/*----------------------------------------------------------------------------*/

UBaseType_t bus_rx_count_can()
{
    return rxCount;
}

/*----------------------------------------------------------------------------*/

UBaseType_t bus_tx_count_can()
{
    return txCount;
}

/*----------------------------------------------------------------------------*/

void bus_clear_rx_count_can()
{
    rxCount = 0;
}

/*----------------------------------------------------------------------------*/

void bus_clear_tx_count_can()
{
    txCount = 0;
}

UBaseType_t bus_busoff_error_can()
{
    /* check for Bus-off flag */
    stCANBusOffErr = CAN_GetFlagStatus(CAN1, CAN_FLAG_BOF);
    return stCANBusOffErr;
}

UBaseType_t bus_passive_error_can()
{
    /* check for Error passive flag */
    stCANBusPassiveErr = CAN_GetFlagStatus(CAN1, CAN_FLAG_EPV);
    return stCANBusPassiveErr;
}

UBaseType_t bus_warning_error_can()
{
    /* check for Error Warning flag */
    stCANBusWarningErr = CAN_GetFlagStatus(CAN1, CAN_FLAG_EWG);
    return stCANBusWarningErr;
}

UBaseType_t bus_tec_can()
{
    /* read Transmit Error Counter of CAN hardware */
    ctrCANTec = CAN_GetLSBTransmitErrorCounter(CAN1);
    return ctrCANTec;
}

UBaseType_t bus_rec_can()
{
    /* read Receive Error Counter of CAN hardware */
    ctrCANRec = CAN_GetReceiveErrorCounter(CAN1);
    return ctrCANRec;
}
