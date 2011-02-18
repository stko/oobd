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
 * generic part of the CAN routines
 */

/* OOBD headers. */
/*-----------------------------------------------------------*/
#include "od_config.h"
#include "od_protocols.h"
#include "odb_can.h"

print_cbf printdata_CAN = NULL;

/*-----------------------------------------------------------*/

void
print_telegram (portBASE_TYPE msgType, void *data, printChar_cbf printchar)
{
  static data_packet *dp;
  dp = data;
  printser_string ("\r\n# 0x");
  printser_int (dp->recv, 16);
  printser_string ("  0x");
  printser_int (dp->err, 16);
  printser_string ("  ");
  printser_int (dp->len, 10);
  printser_string ("  ");
  int i;
  for (i = 0; i < 8; i++)
    {
      printser_uint8ToHex (dp->data[i]);
      printser_string ("  ");
    }
  printser_string ("\r\n");
}
/*-----------------------------------------------------------*/

void
odb_can_setup ()
{

  printdata_CAN = print_telegram;

  extern bus_init actBus_init;
  extern bus_send actBus_send;
  extern bus_flush actBus_flush;
  extern bus_param actBus_param;
  extern bus_close actBus_close;
  /* assign the actual can bus functions to the generic function pointers */
  actBus_init = bus_init_can;
  actBus_send = bus_send_can;
  actBus_flush = bus_flush_can;
  actBus_param = bus_param_can;
  actBus_close = bus_close_can;
}
/*-----------------------------------------------------------*/

void
odb_can_init ()
{
  odbarr[ODB_CAN] = odb_can_setup;
}
/*-----------------------------------------------------------*/
