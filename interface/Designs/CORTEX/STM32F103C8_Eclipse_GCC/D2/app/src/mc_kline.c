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
 * MC specific K-Line routines
 */

#include "stm32f10x.h"

/* OOBD headers. */
#include "SystemConfig.h"
#include "od_base.h"
#include "od_protocols.h"
#include "odb_kline.h"
#include "mc_kline.h"
#include "mc_sys.h"
#include "mc_sys_generic.h"

extern char *oobd_Error_Text_OS;

/* callback function for received data */
// recv_cbf reportReceivedData = NULL;

portBASE_TYPE rxCount;
portBASE_TYPE txCount;
portBASE_TYPE errCount;
portBASE_TYPE stKlineBusOffErr;
portBASE_TYPE stKlineBusWarningErr;
portBASE_TYPE stKlineBusPassiveErr;
portBASE_TYPE ctrKlineTec;
portBASE_TYPE ctrKlineRec;

portBASE_TYPE bus_init_kline()
{
    NVIC_InitTypeDef NVIC_InitStructure;
    extern startupProtocol;

    /* USART2 Interrupt handdling must be added */

    return pdPASS;
}

/*----------------------------------------------------------------------------*/

portBASE_TYPE bus_send_kline(data_packet * data)
{
    DEBUGUARTPRINT("\r\n*** bus_send_kline entered! ***");


    DEBUGUARTPRINT("\r\n*** bus_send_kline finished! ***");
    return pdPASS;
}

/*----------------------------------------------------------------------------*/

void bus_flush_kline()
{
    DEBUGPRINT("Flush K-Line\n", 'a');
}

/*----------------------------------------------------------------------------*/

void bus_param_kline_spec_Print(portBASE_TYPE msgType, void *data,
			      printChar_cbf printchar)
{
    param_data *args;
    args = data;
    DEBUGPRINT
	("Bus Parameter received via Outputtask param %ld value %ld\n",
	 args->args[ARG_RECV], args->args[ARG_CMD]);

}

/*----------------------------------------------------------------------------*/

portBASE_TYPE bus_param_kline_spec(param_data * args)
{

    uint8_t i;

    switch (args->args[ARG_CMD]) {
/*
    case PARAM_BUS_CONFIG:
	break;

    case PARAM_BUS_MODE:
	switch (args->args[ARG_VALUE_1]) {
	case VALUE_BUS_MODE_SILENT:
	    break;
	case VALUE_BUS_MODE_LOOP_BACK:
	    break;
	case VALUE_BUS_MODE_LOOP_BACK_WITH_SILENT:
	    break;
	case VALUE_BUS_MODE_NORMAL:
	    break;

	default:
	    createCommandResultMsg(FBID_BUS_SPEC,
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED,
				   args->args[ARG_VALUE_1],
				   ERR_CODE_OS_COMMAND_NOT_SUPPORTED_TEXT);
	    break;
	}
	break;
*/
    case PARAM_BUS_KLINE_MODE:
	switch (args->args[ARG_VALUE_1]) {
	case 0:
	case 1:
/*
	    CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_OFF);
	    CreateEventMsg(MSG_EVENT_BUS_CHANNEL,
			   args->args[ARG_VALUE_1] == 1 ? 1 : 2);
*/
	    sysIoCtrl(IO_KLINE, 0, args->args[ARG_VALUE_1], 0, 0);

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

    case PARAM_BUS_LLINE_ACTIVATE:
	switch (args->args[ARG_VALUE_1]) {
	case 0:
	case 1:
/*
		CreateEventMsg(MSG_EVENT_BUS_MODE, MSG_EVENT_BUS_MODE_OFF);
	    CreateEventMsg(MSG_EVENT_BUS_CHANNEL,
			   args->args[ARG_VALUE_1] == 1 ? 1 : 2);
*/
	    sysIoCtrl(IO_LLINE, 0, args->args[ARG_VALUE_1], 0, 0);
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


    default:
	createCommandResultMsg(FBID_BUS_SPEC, ERR_CODE_OS_UNKNOWN_COMMAND,
			       0, ERR_CODE_OS_UNKNOWN_COMMAND_TEXT);
	break;
    }

    return pdPASS;
}

/*----------------------------------------------------------------------------*/

void bus_close_kline()
{
    NVIC_InitTypeDef NVIC_InitStructure;

//    reportReceivedData = NULL;

    /* USART2 Interrupt hand ling must be added */
}

/*----------------------------------------------------------------------------*/
/*
portBASE_TYPE busControl(portBASE_TYPE cmd, void *param)
{
    switch (cmd) {
    case ODB_CMD_RECV:
//	reportReceivedData = param;
	return pdPASS;
	break;
    default:
	return pdFAIL;
	break;
    }
    return pdFAIL;
}
*/
/*----------------------------------------------------------------------------*/
/* Implementation of USART2 interrupt for K-Line interface */
void USART2_IRQHandler(void)
{
    DEBUGUARTPRINT("\r\n*** USART2_IRQHandler starting ***");
    portBASE_TYPE xHigherPriorityTaskWoken = pdFALSE;

    char ch;
//    KlineRxMsg RxMessage;
//    static data_packet dp;

    /* Check for received Data */
    if (USART_GetITStatus(USART2, USART_IT_RXNE) != RESET) {
    	ch = USART_ReceiveData(USART2);
    }

    DEBUGUARTPRINT("\r\n*** USART2_IRQHandler finished ***");
}

/*----------------------------------------------------------------------------*/

portBASE_TYPE bus_rx_error_kline()
{
    return errCount;
}

/*----------------------------------------------------------------------------*/

portBASE_TYPE bus_tx_error_kline()
{
    return 0;
}

/*----------------------------------------------------------------------------*/

void bus_clear_rx_error_kline()
{
    errCount = 0;
}

/*----------------------------------------------------------------------------*/

void bus_clear_tx_error_kline()
{
}

/*----------------------------------------------------------------------------*/

portBASE_TYPE bus_rx_count_kline()
{
    return rxCount;
}

/*----------------------------------------------------------------------------*/

portBASE_TYPE bus_tx_count_kline()
{
    return txCount;
}

/*----------------------------------------------------------------------------*/

void bus_clear_rx_count_kline()
{
    rxCount = 0;
}

/*----------------------------------------------------------------------------*/

void bus_clear_tx_count_kline()
{
    txCount = 0;
}

portBASE_TYPE bus_busoff_error_kline()
{
    /* check for Bus-off flag */

    return stKlineBusOffErr;
}

portBASE_TYPE bus_passive_error_kline()
{
    /* check for Error passive flag */

    return stKlineBusPassiveErr;
}

portBASE_TYPE bus_warning_error_kline()
{
    /* check for Error Warning flag */

    return stKlineBusWarningErr;
}
