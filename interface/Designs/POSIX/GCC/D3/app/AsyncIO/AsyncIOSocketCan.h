/*
 * AsyncIOSocketCan.h
 *
 * based on
 *  AsyncIOSocket.h
 *  Created on: 23 Sep 2009
 *      Author: William Davy
 *
 *  Created on: 01.04.2013
 *      Author: Steffen Köhler
 */

#ifndef ASYNCIOSOCKETCAN_H_
#define ASYNCIOSOCKETCAN_H_


#include <linux/can.h>
#include <linux/can/raw.h>


/* CAN Packet size to send/receive. */
#define CAN_INTERFACE			"can0"


/* At time of writing, these constants are not defined in the headers */
#ifndef PF_CAN
#define PF_CAN 29
#endif

#ifndef AF_CAN
#define AF_CAN PF_CAN
#endif

/*
typedef struct CAN_PACKET {
    //   unsigned portCHAR ucPacket[CAN_PACKET_SIZE];
    struct can_frame frame;
    unsigned portCHAR ucNull;
} xCANPacket;

*/


/**
 * Opens a socket and registers the signal handler for received messages.
 * @param vSocketCallback A function pointer of a function that will be called back
 * when the socket has just received a packet. This call back is called from within
 * the signal handler so must use ISR safe routines.
 * @param pvContext The socket and caller supplied pointer to a Context are passed
 * to the call back function.
 * @param pxBindAddress A pointer to a struct sockaddr_can which describes what addresses
 * to accept packets from.
 * @return The newly opened socket.
 */
int iSocketOpenCAN(void (*vSocketCallback) (int, void *), void *pvContext,
		   struct sockaddr_can *pxBindAddress);

/**
 * Closes the socket and removes the call back function.
 * @param iSocket The socket to close.
 */
void vSocketClose(int iSocket);

/**
 * Send a packet to the given address.
 * @param iSocket A handle to a valid socket.
 * @param pxPacket A pointer to the CAN Packet to send.
 * @param pxSendAddress A pointer to a sockaddr_can structure that already contains the address and port for sending the data.
 * @return The number of bytes transmitted.
 */
int iSocketCANSendTo(int iSocket, struct can_frame *pxPacket,
		     struct sockaddr_can *pxSendAddress);

/**
 * ISR only CAN Receive packet call.
 * @param iSocket The Handle to receive the data from.
 * @param pxPacket A packet to receive the data into.
 * @param pxReceiveAddress A pointer to a structure describing valid addresses to receive from.
 * @return The number of bytes received.
 */
int iSocketCANReceiveISR(int iSocket, struct can_frame *pxPacket,
			 struct sockaddr_can *pxReceiveAddress);

/**
 * Non-ISR CAN Receive packet call.
 * @param iSocket The Handle to receive the data from.
 * @param pxPacket A packet to receive the data into.
 * @param pxReceiveAddress A pointer to a structure describing valid addresses to receive from.
 * @return The number of bytes received.
 */
int iSocketCANReceiveFrom(int iSocket, struct can_frame *pxPacket,
			  struct sockaddr_can *pxReceiveAddress);

/**
 * Typical implementation of a call back function which simply delivers the received packet to a Queue which it is passed.
 * @param iSocket A socket desicriptor to receive the packet from.
 * @param pvQueueHandle An xQueueHandle which is waiting to receive the just received packet.
 */
void vCANReceiveAndDeliverCallback(int iSocket, void *pvQueueHandle);

#endif				/* ASYNCIOSOCKETCAN_H_ */
