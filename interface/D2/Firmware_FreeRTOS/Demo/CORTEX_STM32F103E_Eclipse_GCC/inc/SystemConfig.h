

/* Define to prevent recursive inclusion -------------------------------------*/
#ifndef __SystemConfig_H
#define __SystemConfig_H

/* -------- Create Global variables ---------------------------------------------------*/
void 		System_Configuration(void);
void        RCC_Configuration(void);
void        GPIO_Configuration(void);
void        NVIC_Configuration(void);
void        SysTick_Configuration(void);
/* void        ADC_Configuration(void);  */
/* void        SPI_Configuration(void);  */

#define USART1_BAUDRATE_DEFAULT USART1_BAUDRATE_115200
#define USART1_BAUDRATE_4800    4800
#define USART1_BAUDRATE_9600    9600
#define USART1_BAUDRATE_19200   19200
#define USART1_BAUDRATE_38400   38400
#define USART1_BAUDRATE_57600   57600
#define USART1_BAUDRATE_115200  115200
#define USART1_BAUDRATE_230400  230400
#define USART1_BAUDRATE_460800  460800

#endif  /* __SystemConfig_H */
