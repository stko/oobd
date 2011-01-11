/*
 * SerialTask.h
 *
 *  Created on: 20.06.2009
 *      Author: Dirki
 */

#ifndef SERIALTASK_H_
#define SERIALTASK_H_

#define SERIAL_COMM_TASK_PRIORITY			( tskIDLE_PRIORITY + 3 )

void InitSerialComm();
void SerialSendStr(char const *str);

void uart1_puts(char const *);
void uart1_putc(char);
int uart1_getc();

void itoa(int value, char* str, int base);

void uint8ToHex(char *buf, uint8_t value);
void uint16ToHex(char *buf, uint16_t value);
void uint32ToHex(char *buf, uint32_t value);

#endif /* SERIALTASK_H_ */
