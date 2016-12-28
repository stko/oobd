/*
 * AsyncIOSocketCAN.c
 *
 * based on
 *  AsyncIOSocket.h
 *  Created on: 21 Sep 2009
 *      Author: William Davy
 *
 *  Created on: 01.04.2013
 *      Author: Steffen Köhler
 */

#define _GNU_SOURCE

#include <errno.h>
#include <unistd.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <stdio.h>
#include <netinet/in.h>

#include <sys/types.h>
#include <sys/socket.h>
#include <sys/ioctl.h>
#include <net/if.h>
#include <string.h>

#include "FreeRTOS.h"
#include "task.h"
#include "queue.h"
#include "AsyncIO.h"
#include "AsyncIOSocketCan.h"

#include "od_base.h"

/*---------------------------------------------------------------------------*/

int iSocketOpenCAN(char *name, void (*vSocketCallback) (int, void *),
		   void *pvContext, struct sockaddr_can *pxBindAddress)
{
    int iSocket = 0;

    taskENTER_CRITICAL();
    {
	/* Open a new socket. */
	iSocket = socket(PF_CAN, SOCK_RAW, CAN_RAW);
	if (iSocket < 0) {
	    printf("Failed to open socket: %d.\n", errno);
	    return 0;
	}
	/* Have we been passed a call back function that will deal with received messages? */
	struct ifreq ifr;
	strcpy(ifr.ifr_name, name);
	/* ifr.ifr_ifindex gets filled 
	 * with that device's index */
	if (ioctl(iSocket, SIOCGIFINDEX, &ifr)) {
	    printf("ioctl\n");
	    return 0;
	}

	if (pdTRUE ==
	    lAsyncIORegisterCallback(iSocket, vSocketCallback,
				     pvContext)) {
	    /* This is CAN so bind it passed listen address. */
	    if (NULL != pxBindAddress) {
		pxBindAddress->can_family = AF_CAN;
		pxBindAddress->can_ifindex = ifr.ifr_ifindex;
		if (0 !=
		    bind(iSocket, (struct sockaddr *) pxBindAddress,
			 sizeof(struct sockaddr_can))) {
		    printf("Bind error: %d\n", errno);
		}
	    }
	} else {
	    /* Socket is being used as polled IO or for sending data. */
	}
    }
    taskEXIT_CRITICAL();
    return iSocket;
}

/*---------------------------------------------------------------------------*/

void vSocketClose(int iSocket)
{
    close(iSocket);
    vAsyncIOUnregisterCallback(iSocket);
}

/*---------------------------------------------------------------------------*/

int iSocketCANSendTo(int iSocket, struct can_frame *pxPacket,
		     struct sockaddr_can *pxSendAddress)
{
    int iBytesSent = 0;
    if ((0 != iSocket) && (NULL != pxPacket)) {
	taskENTER_CRITICAL();
	iBytesSent = write(iSocket, pxPacket, sizeof(struct can_frame));
	taskEXIT_CRITICAL();
    }
    return iBytesSent;
}

/*---------------------------------------------------------------------------*/

int iSocketCANReceiveISR(int iSocket, struct can_frame *pxPacket,
			 struct sockaddr_can *pxReceiveAddress)
{
    int iBytesReceived = 0;
    socklen_t xSocketLength = sizeof(struct sockaddr_can);

    if (0 != iSocket) {
	iBytesReceived =
	    recvfrom(iSocket, pxPacket, sizeof(struct can_frame), 0,
		     (struct sockaddr *) pxReceiveAddress, &xSocketLength);
    }
    return iBytesReceived;
}

/*---------------------------------------------------------------------------*/

int iSocketCANReceiveFrom(int iSocket, struct can_frame *pxPacket,
			  struct sockaddr_can *pxReceiveAddress)
{
    int iBytesReceived = 0;
    socklen_t xSocketLength = sizeof(struct sockaddr_can);

    if (0 != iSocket) {
	taskENTER_CRITICAL();
	iBytesReceived =
	    recvfrom(iSocket, pxPacket, sizeof(struct can_frame), 0,
		     (struct sockaddr *) pxReceiveAddress, &xSocketLength);
	taskEXIT_CRITICAL();
    }
    return iBytesReceived;
}

/*---------------------------------------------------------------------------*/

void vCANReceiveAndDeliverCallback(int iSocket, void *pvContext)
{
    BaseType_t xHigherTaskWoken = pdFALSE;
    static struct can_frame xPacket;
    struct sockaddr_can xReceiveAddress;

    if (sizeof(struct can_frame) ==
	iSocketCANReceiveISR(iSocket, &xPacket, &xReceiveAddress)) {
	if (pdPASS !=
	    xQueueSendFromISR((QueueHandle_t) pvContext, &xPacket,
			      &xHigherTaskWoken)) {
	    printf("CAN xQuere full!\n");
	}
    } else {
	printf("CAN Rx failed: %d\n", errno);
    }
    portEND_SWITCHING_ISR(xHigherTaskWoken);
}

/*-----------------------------------------------------------*/
