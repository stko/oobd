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


	1 tab == 4 spaces!

	Please ensure to read the configuration and relevant port sections of the
	online documentation.


	OOBD is using FreeTROS (www.FreeRTOS.org)

*/

/**
 * all common routines
 */

/* OOBD headers. */
#include "od_config.h"
#include "od_base.h"
#include "od_protocols.h"


bus_init actBus_init = NULL;
bus_send actBus_send = NULL;
bus_flush actBus_flush = NULL;
bus_param actBus_param = NULL;
bus_close actBus_close = NULL;

char outputBuffer[100];


void
initProtocols ()
{
  obd_uds_init ();
}

void
initBusses ()
{
  odb_can_init ();
}


MsgData *
createMsg (void *data, size_t size)
{
  // is there any msgdata at all?
  if (data != NULL && size > 0)
    {
      MsgData *dataDescr;
      // get mem for the MsgData itself + the payload
      dataDescr = pvPortMalloc (sizeof (struct MsgData) + size);
      if (dataDescr != NULL)
	{
	  // store the payload size
	  dataDescr->len = size;
	  // store the payload address
	  dataDescr->addr = (void *) dataDescr + sizeof (struct MsgData);
	  // copy payload into the fresh mem
	  memcpy (dataDescr->addr, data, size);
	  return dataDescr;
	}
      else
	{
	  return NULL;
	}
    }
  else
    {
      return NULL;
    }
}


//! @todo data mem is here copied twice instead just once, that give place for improvements
MsgData *
createDataMsg (data_packet * data)
{
  // is there any msgdata at all?
  if (data != NULL)
    {
      data_packet *newDataDescr;
      MsgData *newMsg;
      // get mem for the new data_packet itself + the payload
      newDataDescr = pvPortMalloc (sizeof (struct data_packet) + data->len);
      if (newDataDescr != NULL)
	{
	  // copy the values of the source data_packet into the new one
	  memcpy (newDataDescr, data, sizeof (struct data_packet));
	  // store the payload address
	  newDataDescr->data =
	    (unsigned char *) newDataDescr + sizeof (struct data_packet);
	  // copy payload into the fresh mem
	  memcpy (newDataDescr->data, data->data, data->len);
	  newMsg =
	    createMsg (newDataDescr, sizeof (struct data_packet) + data->len);
	  vPortFree (newDataDescr);
	  return newMsg;
	}
      else
	{
	  return NULL;
	}
    }
  else
    {
      return NULL;
    }
}


void
disposeMsg (MsgData * p)
{
  if (p != NULL)
    {
      vPortFree (p);
    }
}

/*
void
disposeDataMsg (MsgData * p)
{
  data_packet * dp;
  dp=p->addr;
  vPortFree (dp->data);
  vPortFree (p);
}
*/

portBASE_TYPE
sendMsg (portBASE_TYPE msgType, xQueueHandle recv, MsgData * msg)
{
  OdMsg odMsg;
  odMsg.msgType = msgType;
  odMsg.msgPtr = msg;
  return xQueueSend (recv, &odMsg, 0);
}

portBASE_TYPE
sendMsgFromISR (portBASE_TYPE msgType, xQueueHandle recv, MsgData * msg)
{
  OdMsg odMsg;
  odMsg.msgType = msgType;
  odMsg.msgPtr = msg;
  return xQueueSendFromISR (recv, &odMsg, 0);
}

portBASE_TYPE
waitMsg (xQueueHandle recv, MsgData ** msgdata, portBASE_TYPE timeout)
{
  OdMsg odMsg;
  portBASE_TYPE recvStatus;
  if (pdPASS == (recvStatus = xQueueReceive (recv, &odMsg, timeout)))
    {
      *msgdata = odMsg.msgPtr;
      return odMsg.msgType;
    }
  else
    {
      return MSG_NONE;
    }

}



void
strreverse (char *begin, char *end)
{

  char aux;

  while (end > begin)
    {
      aux = *end;
      *end-- = *begin;
      *begin++ = aux;
    }
}

void
uint8ToHex (char *buf, uint8_t value)
{
  static const char num[] = "0123456789abcdef";

  // write upper nibble
  buf[0] = num[value >> 4];
  // write lower nibble
  buf[1] = num[value & 0x0F];
  buf[2] = 0;
}

void
uint16ToHex (char *buf, uint16_t value)
{
  uint8ToHex (buf, (uint8_t) (value >> 8));
  buf += 2;
  uint8ToHex (buf, (uint8_t) value);
  buf += 2;
  *buf = 0;
}

void
uint32ToHex (char *buf, uint32_t value)
{
  uint16ToHex (buf, (uint16_t) (value >> 16));
  buf += 4;
  uint16ToHex (buf, (uint16_t) value);
  buf += 4;
  *buf = 0;
}

void
itoa (int value, char *str, int base)
{

  static const char num[] = "0123456789abcdefghijklmnopqrstuvwxyz";
  char *wstr = str;
  int sign;

  div_t res;

  // Validate base
  if (base < 2 || base > 35)
    {
      *wstr = '\0';
      return;
    }

  // Take care of sign
  if ((sign = value) < 0)
    value = -value;

  // Conversion. Number is reversed.
  do
    {
      res = div (value, base);
      *wstr++ = num[res.rem];
      value = res.quot;
    }
  while (value != 0);

  if (sign < 0)
    *wstr++ = '-';

  *wstr = '\0';

  // Reverse string
  strreverse (str, wstr - 1);
}

void
printser_string (char const *str)
{
  extern printChar_cbf printChar;
  if (str)
    {
      /* transmit characters until 0 character */
      while (*str)
	{
	  /* write character to buffer and increment pointer */
	  putchar (*str);
	  printChar (*str++);
	}
    }

}

void
printser_int (int value, int base)
{
  itoa (value, (char *) &outputBuffer, base);
  printser_string ((char *) &outputBuffer);
}

void
printser_uint32ToHex (uint32_t value)
{
  uint32ToHex ((char *) &outputBuffer, value);
  printser_string ((char *) &outputBuffer);
}

void
printser_uint16ToHex (uint16_t value)
{
  uint16ToHex ((char *) &outputBuffer, value);
  printser_string ((char *) &outputBuffer);
}

void
printser_uint8ToHex (uint8_t value)
{
  uint8ToHex ((char *) &outputBuffer, value);
  printser_string ((char *) &outputBuffer);
}
