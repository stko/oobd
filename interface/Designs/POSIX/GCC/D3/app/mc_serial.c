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
 * Inits the serial interface and starts the output task
 */

/* OOBD headers. */
#include "od_config.h"
#include "od_base.h"
#include "mc_serial_generic.h"
#include "mc_serial.h"

#include<pthread.h>


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>


/*---------------------------------------------------------------------------*/

void endProgram(const char *msg)
{
    perror(msg);
    exit(1);
}

/* file handle to communicate with the oobd side. */
int oobdIOHandle = -1;

pthread_t pTcpControlThread;

//callback routine to write a char to the output

void writeChar(char a)
{
    /* Echo it back to the sender. */
    //! \bug Echo off is not supported yet
    if (oobdIOHandle > -1) {
    (void) write(oobdIOHandle, &a, 1);
	DEBUGPRINT("%c", a);
    }
}

void *portControlThread(void *pvParameters)
{

    extern printChar_cbf printChar;
    extern QueueHandle_t internalSerialRxQueue;
    printChar = writeChar;

    DEBUGPRINT("PortControl Task started\n", 'a');
    struct timespec xTimeToSleep, xTimeSlept;
    /* Makes the process more agreeable when using the Posix simulator. */
    xTimeToSleep.tv_sec = 0;
    xTimeToSleep.tv_nsec = 5000;

    int keeprunning = 1;

    int sockfd, portno;
    int error = 0;
    int len = sizeof(error);
    socklen_t clilen;
    struct sockaddr_in serv_addr, cli_addr;
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
//    sockfd = socket(PF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) {
	endProgram("ERROR opening socket");
	keeprunning = 0;
    } else {
	/* setsockopt: Handy debugging trick that lets 
	 * us rerun the server immediately after we kill it; 
	 * otherwise we have to wait about 20 secs. 
	 * Eliminates "ERROR on binding: Address already in use" error. 
	 */
	int optval = 1;
	setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR,
//                 (const void *) &optval, sizeof(int));
		   (const void *) &optval, sizeof(optval));

	bzero((char *) &serv_addr, sizeof(serv_addr));
	portno = 3001;		//atoi(*pcDevice);
//      fcntl(sockfd, F_SETFL, O_NONBLOCK);
	serv_addr.sin_family = AF_INET;
	serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
	serv_addr.sin_port = htons((unsigned short) portno);
	if (bind(sockfd, (struct sockaddr *) &serv_addr,
		 sizeof(serv_addr)) < 0) {
	    endProgram("ERROR on binding");
	    keeprunning = 0;
	} else {
	    clilen = sizeof(cli_addr);
	    if (listen(sockfd, 5) < 0) {	/* allow 5 requests to queue up */
		endProgram("ERROR on listen");
	    }
	    for (; keeprunning;) {
		DEBUGPRINT("Waiting for connect sockethandle: %d\n",
			   sockfd);

		oobdIOHandle =
//                  accept(sockfd, (struct sockaddr *) &cli_addr, &clilen);
		    oobdIOHandle =
		    accept(sockfd, (struct sockaddr *) NULL, NULL);
		if (oobdIOHandle > -1) {
		    DEBUGPRINT("Got connected \n", 'a');
	(void) lAsyncIORegisterCallback(oobdIOHandle,
					vAsyncSerialIODataAvailableISR,
					internalSerialRxQueue);
		    while (getsockopt
			   (oobdIOHandle, SOL_SOCKET, SO_ERROR, &error,
			    &len) == 0) {
			nanosleep(&xTimeToSleep, &xTimeSlept);

    }

		    vAsyncIOUnregisterCallback(oobdIOHandle);
		    close(oobdIOHandle);
		} else {
		    printf("accept: Failed: %d\n", errno);
		    DEBUGPRINT("Invalid socket?? \n", 'a');
		    exit(1);

		}
	    }
	    close(sockfd);
	}
    }

    DEBUGPRINT("FATAL ERROR: No Output queue.\n", 'a');

    return NULL;

}

/*-----------------------------------------------------------*/



UBaseType_t serial_init_mc()
{

    extern QueueHandle_t internalSerialRxQueue;

    internalSerialRxQueue = xQueueCreate(10000, sizeof(unsigned char));	// uds buffer 4096 bytes = 9192 Hex chars + some extra..
    if (NULL == internalSerialRxQueue) {
	return pdFALSE;
    }

    /* Create a Task which controls the tcp Socket. */
    //portControlThread(NULL);
    int err =
	pthread_create(&pTcpControlThread, NULL, &portControlThread, NULL);
    if (err != 0) {
	printf("\ncan't create thread :[%s]", strerror(err));
	return pdFALSE;
    } else {
	printf("\n Thread created successfully\n");
	return pdTRUE;
    }


}
