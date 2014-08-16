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
#include <termios.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <string.h>
#include "common.h"
#include "st-compatibility.h"

#define BAUDRATE B115200
#define _POSIX_SOURCE 1		/* POSIX compliant source */
#define FALSE 0
#define TRUE 1
#define CMDSIZE 100
volatile int STOP = FALSE;
int fd;

uint8_t virtualFlash[FLASH_SIZE];

#define NROFCMDS 4

char *cmds[NROFCMDS] = { "end", "p 0 0", "p 99 2", "1" };


int getCmdNr(char *input)
{
    int i = 0;
    int found = 0;
    while (!found && i < NROFCMDS) {
	if (strcmp(input, cmds[i]) == 0) {
	    found = i + 1;
	}
	i++;
    }
    return found;
}

void showCommands()
{
    int i;
    printf
	("The following commands are currently simulated (case-sensitive!):\n");
    for (i = 0; i < NROFCMDS; i++)
	printf("\t%s\n", cmds[i]);
}

void sendStr(char *output)
{
    write(fd, output, strlen(output));
}

// --------------------ST Compatibility functions, taken from the ST bootloader library  COPYRIGHT 2010 STMicroelectronics

void Int2Str(uint8_t * str, int32_t intnum)
{
    uint32_t i, Div = 1000000000, j = 0, Status = 0;

    for (i = 0; i < 10; i++) {
	str[j++] = (intnum / Div) + 48;

	intnum = intnum % Div;
	Div /= 10;
	if ((str[j - 1] == '0') & (Status == 0)) {
	    j = 0;
	} else {
	    Status++;
	}
    }
}

/**
 * @brief  Convert a string to an integer
 * @param  inputstr: The string to be converted
 * @param  intnum: The integer value
 * @retval 1: Correct
 *         0: Error
 */
uint32_t Str2Int(uint8_t * inputstr, int32_t * intnum)
{
    uint32_t i = 0, res = 0;
    uint32_t val = 0;

    if (inputstr[0] == '0' && (inputstr[1] == 'x' || inputstr[1] == 'X')) {
	if (inputstr[2] == '\0') {
	    return 0;
	}
	for (i = 2; i < 11; i++) {
	    if (inputstr[i] == '\0') {
		*intnum = val;
		/* return 1; */
		res = 1;
		break;
	    }
	    if (ISVALIDHEX(inputstr[i])) {
		val = (val << 4) + CONVERTHEX(inputstr[i]);
	    } else {
		/* return 0, Invalid input */
		res = 0;
		break;
	    }
	}
	/* over 8 digit hex --invalid */
	if (i >= 11) {
	    res = 0;
	}
    } else {			/* max 10-digit decimal input */

	for (i = 0; i < 11; i++) {
	    if (inputstr[i] == '\0') {
		*intnum = val;
		/* return 1 */
		res = 1;
		break;
	    } else if ((inputstr[i] == 'k' || inputstr[i] == 'K')
		       && (i > 0)) {
		val = val << 10;
		*intnum = val;
		res = 1;
		break;
	    } else if ((inputstr[i] == 'm' || inputstr[i] == 'M')
		       && (i > 0)) {
		val = val << 20;
		*intnum = val;
		res = 1;
		break;
	    } else if (ISVALIDDEC(inputstr[i])) {
		val = val * 10 + CONVERTDEC(inputstr[i]);
	    } else {
		/* return 0, Invalid input */
		res = 0;
		break;
	    }
	}
	/* Over 10 digit decimal --invalid */
	if (i >= 11) {
	    res = 0;
	}
    }

    return res;
}

//-------------------------------------------


/**
 * @brief  Test to see if a key has been pressed on the HyperTerminal
 * @param  key: The key pressed
 * @retval 1: Correct
 *         0: Error
 */
uint32_t SerialKeyPressed(uint8_t * key)
{
    int len;
    len = read(fd, key, 1);
    if (len == 1) {
	return 1;
    } else {
	return 0;
    }
}

/**
 * @brief  Print a character on the HyperTerminal
 * @param  c: The character to be printed
 * @retval None
 */
void SerialPutChar(uint8_t c)
{
    write(fd, &c, 1);
}

int main(int argc, char *argv[])
{
    if (argc != 2) {
	printf
	    ("\nOOBD Bootloader SIM - \ntries to simulate the OOBD Bootloader to test the YModem Flash Upload process\n\n");
	printf("Usage:\n\t%s /dev/myComPort\n\n", argv[0]);
	printf("The port settings are fixed to 115200 8 N 1\n\n");
	printf
	    ("Hint: use socat to create virtual com ports, e.g.:\n\n\tsocat PTY,link=/tmp/DXM PTY,link=/tmp/OOBD\n\n");
	showCommands();
	printf("\n%s is part of the OOBD tool chain (www.oobd.org)\n\n",
	       argv[0]);
	exit(0);

    }

    int res;
    struct termios oldtio, newtio;
    char buf[255];
    char cmdLine[CMDSIZE];
    int inPos = 0;
    int i;
    int32_t Size = 0;
    fd = open(argv[1], O_RDWR | O_NOCTTY);
    if (fd < 0) {
	perror(argv[1]);
	exit(-1);
    }

    tcgetattr(fd, &oldtio);	/* save current port settings */
    /* set new port settings for canonical input processing */
    newtio.c_cflag = BAUDRATE | CRTSCTS | CS8 | CLOCAL | CREAD;
    newtio.c_iflag = IGNPAR;
    newtio.c_oflag = 0;
    newtio.c_lflag = 0;
    newtio.c_cc[VMIN] = 0;
    newtio.c_cc[VTIME] = 1;
    tcflush(fd, TCIFLUSH);
    tcsetattr(fd, TCSANOW, &newtio);
    while (STOP == FALSE) {
	res = read(fd, buf, 255);
	if (res >= -1) {
	    buf[res] = 0;
	}
	//copy input into cmdLine
	for (i = 0; i < res && inPos < CMDSIZE; i++) {
	    write(fd, &buf[i], 1);
	    if (buf[i] == 10)
		buf[i] = 13;	//dirty trick, as all terminal programs seems to like send \n instead \r, although if so configured..
	    if (buf[i] != 10) {
		cmdLine[inPos++] = buf[i];
		cmdLine[inPos] = 0;	// add leading Null byte
		if (cmdLine[inPos - 1] == 13) {
		    cmdLine[inPos - 1] = 0;	// remove trailing \n
		    printf("Line:%s len:%d\n", cmdLine, inPos);
		    switch (getCmdNr(cmdLine)) {
		    case 1:	//finish the sim
			STOP = TRUE;	/* stop loop if only a CR was input */
			printf("end..\n");
			sendStr("bye\r\n");
			break;
		    case 2:
			printf("send identification string..\n");
			sendStr("OOBD D2 100 Lux-Wolf 12. May 2001\r");
			break;
		    case 3:
			printf("simulate Reset..\n");
			sleep(2);	//sleep 1000 msec to simulate reboot
			sendStr("\r\nOOBD-Flashloader>");
			break;
		    case 4:
			printf("starting YMODEM upload..\n");
			sendStr
			    ("\r\nWaiting for the file to be sent ... (press 'a' to abort)\n\r");
			Size = Ymodem_Receive(virtualFlash);
			printf("YModem returns size: %d\n", Size);

			if (Size > 0) {
			    sendStr
				("\n\n\r Programming Completed Successfully!\n\r--------------------------------\r\n ");
			    sendStr
				("--------------------------------\n\r");
			    sendStr("\r\nOOBD-Flashloader>");
			} else {
			    sendStr("\r\nError\r\nOOBD-Flashloader>");
			}
			break;
		    default:
			printf("unknown or empty input..\n");
			sendStr(".\r>");
			break;
		    }
		    inPos = 0;
		    cmdLine[inPos] = 0;	// add leading Null byte
		}
	    }
	}
    }
    /* restore old port settings */
    tcsetattr(fd, TCSANOW, &oldtio);
    return 0;
}
