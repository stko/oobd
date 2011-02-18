

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

#endif  /* __SystemConfig_H */
