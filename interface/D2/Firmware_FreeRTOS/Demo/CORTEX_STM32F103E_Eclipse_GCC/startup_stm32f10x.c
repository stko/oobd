/**
 * @file      startup_stm32f10x.c
 * @author    Stefano Oliveri (software@stf12.net)
 * @version   V1.0
 * @date      22/06/2009
 * @brief     STM32F10x vector table for GCC toolchain.
 *            This module performs:
 *                - Set the initial SP
 *                - Set the initial PC == Reset_Handler,
 *                - Set the vector table entries with the exceptions ISR address,
 *                - Branches to main in the C library (which eventually
 *                  calls main()).
 *            After Reset the Cortex-M3 processor is in Thread mode,
 *            priority is Privileged, and the Stack is set to Main.
 * @attention modified the vector table and the Reset_Handler to use
 *            the demo with eclipse and GCC and to provide support for the
 *            newlib C runtime library.
 *            Modified by Stefano Oliveri (software@stf12.net)
 * @copy
 *
 * THE PRESENT FIRMWARE WHICH IS FOR GUIDANCE ONLY AIMS AT PROVIDING USERS
 * WITH CODING INFORMATION REGARDING THEIR PRODUCTS IN ORDER FOR THEM TO SAVE
 * TIME. AS A RESULT, STEFANO OLIVERI SHALL NOT BE HELD LIABLE FOR ANY
 * DIRECT, INDIRECT OR CONSEQUENTIAL DAMAGES WITH RESPECT TO ANY CLAIMS ARISING
 * FROM THE CONTENT OF SUCH FIRMWARE AND/OR THE USE MADE BY CUSTOMERS OF THE
 * CODING INFORMATION CONTAINED HEREIN IN CONNECTION WITH THEIR PRODUCTS.
 *
 * <h2><center>&copy; COPYRIGHT 2009 Stefano Oliveri</center></h2>
 */

#include "startup/gcc_FreeRTOS/startup_stm32f10x_hd.c"
//#include "startup/gcc_FreeRTOS/startup_stm32f10x_md.c"
