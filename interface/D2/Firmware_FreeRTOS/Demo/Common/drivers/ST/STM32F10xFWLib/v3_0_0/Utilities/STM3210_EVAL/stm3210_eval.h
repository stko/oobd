/**
  ******************************************************************************
  * @file     stm3210_eval.h
  * @author   MCD Application Team
  * @version  V3.0.0
  * @date     04/06/2009
  * @brief    Header file for stm3210_eval.c module.
  ******************************************************************************
  * @copy
  *
  * THE PRESENT FIRMWARE WHICH IS FOR GUIDANCE ONLY AIMS AT PROVIDING CUSTOMERS
  * WITH CODING INFORMATION REGARDING THEIR PRODUCTS IN ORDER FOR THEM TO SAVE
  * TIME. AS A RESULT, STMICROELECTRONICS SHALL NOT BE HELD LIABLE FOR ANY
  * DIRECT, INDIRECT OR CONSEQUENTIAL DAMAGES WITH RESPECT TO ANY CLAIMS ARISING
  * FROM THE CONTENT OF SUCH FIRMWARE AND/OR THE USE MADE BY CUSTOMERS OF THE
  * CODING INFORMATION CONTAINED HEREIN IN CONNECTION WITH THEIR PRODUCTS.
  *
  * <h2><center>&copy; COPYRIGHT 2009 STMicroelectronics</center></h2>
  */ 
  
/* Define to prevent recursive inclusion -------------------------------------*/
#ifndef __STM3210_EVAL_H
#define __STM3210_EVAL_H

/* Includes ------------------------------------------------------------------*/
#include "stm32f10x.h"

/* Exported types ------------------------------------------------------------*/
/* Board Leds */
typedef enum 
{
  LED1 = 0,
  LED2 = 1,
  LED3 = 2,
  LED4 = 3
} Led_TypeDef;

/* Board Push buttons and JoyStick buttons*/
typedef enum 
{  
  Button_WAKEUP = 0,
  Button_TAMPER = 1,
  Button_KEY = 2,
  Button_RIGHT = 3,
  Button_LEFT = 4,
  Button_UP = 5,
  Button_DOWN = 6,
  Button_SEL = 7
} Button_TypeDef;

/* Exported constants --------------------------------------------------------*/

/* Uncomment the line corresponding to the STMicroelectronics evaluation board
   used to run the example */
#if !defined (USE_STM3210B_EVAL) &&  !defined (USE_STM3210E_EVAL)
 //#define USE_STM3210B_EVAL
 #define USE_STM3210E_EVAL
#endif

/* Define the STM32F10x hardware depending on the used evaluation board */
#ifdef USE_STM3210E_EVAL
  /* Leds Configuration */
  #define GPIO_PORT_LED1                     (GPIO_TypeDef*) GPIOF_BASE
  #define RCC_APB2Periph_GPIO_LED1           RCC_APB2Periph_GPIOF
  #define GPIO_PIN_LED1                      GPIO_Pin_6

  #define GPIO_PORT_LED2                     (GPIO_TypeDef*) GPIOF_BASE
  #define RCC_APB2Periph_GPIO_LED2           RCC_APB2Periph_GPIOF
  #define GPIO_PIN_LED2                      GPIO_Pin_7

  #define GPIO_PORT_LED3                     (GPIO_TypeDef*) GPIOF_BASE
  #define RCC_APB2Periph_GPIO_LED3           RCC_APB2Periph_GPIOF  
  #define GPIO_PIN_LED3                      GPIO_Pin_8

  #define GPIO_PORT_LED4                     (GPIO_TypeDef*) GPIOF_BASE
  #define RCC_APB2Periph_GPIO_LED4           RCC_APB2Periph_GPIOF  
  #define GPIO_PIN_LED4                      GPIO_Pin_9

  /* Key Push Button Configuration */
  #define KEY_BUTTON_PORT                    (GPIO_TypeDef*)GPIOG
  #define RCC_APB2Periph_GPIO_BUTTON_KEY     RCC_APB2Periph_GPIOG
  #define KEY_BUTTON_PIN                     GPIO_Pin_8
  #define KEY_BUTTON_EXTI_LINE               EXTI_Line8
  #define KEY_BUTTON_PORT_SOURCE             GPIO_PortSourceGPIOG
  #define KEY_BUTTON_PIN_SOURCE              GPIO_PinSource8
  #define KEY_BUTTON_IRQn                    EXTI9_5_IRQn

  /* Joystick Buttons Configuration */
  #define RIGHT_BUTTON_PORT                  (GPIO_TypeDef*)GPIOG
  #define RCC_APB2Periph_GPIO_BUTTON_RIGHT   RCC_APB2Periph_GPIOG
  #define RIGHT_BUTTON_PIN                   GPIO_Pin_13
  #define RIGHT_BUTTON_EXTI_LINE             EXTI_Line13
  #define RIGHT_BUTTON_PORT_SOURCE           GPIO_PortSourceGPIOG
  #define RIGHT_BUTTON_PIN_SOURCE            GPIO_PinSource13
  #define RIGHT_BUTTON_IRQn                  EXTI15_10_IRQn
    
  #define LEFT_BUTTON_PORT                   (GPIO_TypeDef*)GPIOG
  #define RCC_APB2Periph_GPIO_BUTTON_LEFT    RCC_APB2Periph_GPIOG
  #define LEFT_BUTTON_PIN                    GPIO_Pin_14
  #define LEFT_BUTTON_EXTI_LINE              EXTI_Line14
  #define LEFT_BUTTON_PORT_SOURCE            GPIO_PortSourceGPIOG
  #define LEFT_BUTTON_PIN_SOURCE             GPIO_PinSource14
  #define LEFT_BUTTON_IRQn                   EXTI15_10_IRQn  

  #define UP_BUTTON_PORT                     (GPIO_TypeDef*)GPIOG
  #define RCC_APB2Periph_GPIO_BUTTON_UP      RCC_APB2Periph_GPIOG
  #define UP_BUTTON_PIN                      GPIO_Pin_15
  #define UP_BUTTON_EXTI_LINE                EXTI_Line15
  #define UP_BUTTON_PORT_SOURCE              GPIO_PortSourceGPIOG
  #define UP_BUTTON_PIN_SOURCE               GPIO_PinSource15
  #define UP_BUTTON_IRQn                     EXTI15_10_IRQn  
  
  #define DOWN_BUTTON_PORT                   (GPIO_TypeDef*)GPIOD
  #define RCC_APB2Periph_GPIO_BUTTON_DOWN    RCC_APB2Periph_GPIOD
  #define DOWN_BUTTON_PIN                    GPIO_Pin_3
  #define DOWN_BUTTON_EXTI_LINE              EXTI_Line3
  #define DOWN_BUTTON_PORT_SOURCE            GPIO_PortSourceGPIOD
  #define DOWN_BUTTON_PIN_SOURCE             GPIO_PinSource3
  #define DOWN_BUTTON_IRQn                   EXTI3_IRQn  
  
  #define SEL_BUTTON_PORT                    (GPIO_TypeDef*)GPIOG
  #define RCC_APB2Periph_GPIO_BUTTON_SEL     RCC_APB2Periph_GPIOG
  #define SEL_BUTTON_PIN                     GPIO_Pin_7
  #define SEL_BUTTON_EXTI_LINE               EXTI_Line7
  #define SEL_BUTTON_PORT_SOURCE             GPIO_PortSourceGPIOG
  #define SEL_BUTTON_PIN_SOURCE              GPIO_PinSource7
  #define SEL_BUTTON_IRQn                    EXTI9_5_IRQn 
      
#elif defined USE_STM3210B_EVAL

  /* Leds Configuration */
  #define GPIO_PORT_LED1                     (GPIO_TypeDef*) GPIOC_BASE
  #define RCC_APB2Periph_GPIO_LED1           RCC_APB2Periph_GPIOC  
  #define GPIO_PIN_LED1                      GPIO_Pin_6
  
  #define GPIO_PORT_LED2                     (GPIO_TypeDef*) GPIOC_BASE
  #define RCC_APB2Periph_GPIO_LED2           RCC_APB2Periph_GPIOC  
  #define GPIO_PIN_LED2                      GPIO_Pin_7
  
  #define GPIO_PORT_LED3                     (GPIO_TypeDef*) GPIOC_BASE
  #define RCC_APB2Periph_GPIO_LED3           RCC_APB2Periph_GPIOC  
  #define GPIO_PIN_LED3                      GPIO_Pin_8
  
  #define GPIO_PORT_LED4                     (GPIO_TypeDef*) GPIOC_BASE
  #define RCC_APB2Periph_GPIO_LED4           RCC_APB2Periph_GPIOC  
  #define GPIO_PIN_LED4                      GPIO_Pin_9

  /* Key Push Button Configuration */
  #define KEY_BUTTON_PORT                    (GPIO_TypeDef*)GPIOB
  #define RCC_APB2Periph_GPIO_BUTTON_KEY     RCC_APB2Periph_GPIOB
  #define KEY_BUTTON_PIN                     GPIO_Pin_9
  #define KEY_BUTTON_EXTI_LINE               EXTI_Line9
  #define KEY_BUTTON_PORT_SOURCE             GPIO_PortSourceGPIOB
  #define KEY_BUTTON_PIN_SOURCE              GPIO_PinSource9
  #define KEY_BUTTON_IRQn                    EXTI9_5_IRQn

  /* Joystick Buttons Configuration */
  #define RIGHT_BUTTON_PORT                  (GPIO_TypeDef*)GPIOE
  #define RCC_APB2Periph_GPIO_BUTTON_RIGHT   RCC_APB2Periph_GPIOE
  #define RIGHT_BUTTON_PIN                   GPIO_Pin_0
  #define RIGHT_BUTTON_EXTI_LINE             EXTI_Line0
  #define RIGHT_BUTTON_PORT_SOURCE           GPIO_PortSourceGPIOE
  #define RIGHT_BUTTON_PIN_SOURCE            GPIO_PinSource0
  #define RIGHT_BUTTON_IRQn                  EXTI0_IRQn
    
  #define LEFT_BUTTON_PORT                   (GPIO_TypeDef*)GPIOE
  #define RCC_APB2Periph_GPIO_BUTTON_LEFT    RCC_APB2Periph_GPIOE
  #define LEFT_BUTTON_PIN                    GPIO_Pin_1
  #define LEFT_BUTTON_EXTI_LINE              EXTI_Line1
  #define LEFT_BUTTON_PORT_SOURCE            GPIO_PortSourceGPIOE
  #define LEFT_BUTTON_PIN_SOURCE             GPIO_PinSource1
  #define LEFT_BUTTON_IRQn                   EXTI1_IRQn  

  #define UP_BUTTON_PORT                     (GPIO_TypeDef*)GPIOD
  #define RCC_APB2Periph_GPIO_BUTTON_UP      RCC_APB2Periph_GPIOD
  #define UP_BUTTON_PIN                      GPIO_Pin_8
  #define UP_BUTTON_EXTI_LINE                EXTI_Line8
  #define UP_BUTTON_PORT_SOURCE              GPIO_PortSourceGPIOD
  #define UP_BUTTON_PIN_SOURCE               GPIO_PinSource8
  #define UP_BUTTON_IRQn                     EXTI9_5_IRQn  
  
  #define DOWN_BUTTON_PORT                   (GPIO_TypeDef*)GPIOD
  #define RCC_APB2Periph_GPIO_BUTTON_DOWN    RCC_APB2Periph_GPIOD
  #define DOWN_BUTTON_PIN                    GPIO_Pin_14
  #define DOWN_BUTTON_EXTI_LINE              EXTI_Line14
  #define DOWN_BUTTON_PORT_SOURCE            GPIO_PortSourceGPIOD
  #define DOWN_BUTTON_PIN_SOURCE             GPIO_PinSource14
  #define DOWN_BUTTON_IRQn                   EXTI15_10_IRQn  
  
  #define SEL_BUTTON_PORT                    (GPIO_TypeDef*)GPIOD
  #define RCC_APB2Periph_GPIO_BUTTON_SEL     RCC_APB2Periph_GPIOD
  #define SEL_BUTTON_PIN                     GPIO_Pin_12
  #define SEL_BUTTON_EXTI_LINE               EXTI_Line12
  #define SEL_BUTTON_PORT_SOURCE             GPIO_PortSourceGPIOD
  #define SEL_BUTTON_PIN_SOURCE              GPIO_PinSource12
  #define SEL_BUTTON_IRQn                    EXTI15_10_IRQn 
           
#endif /* USE_STM3210B_EVAL */

/* Wakeup Push Button */
#define WAKEUP_BUTTON_PORT                   (GPIO_TypeDef*)GPIOA
#define RCC_APB2Periph_GPIO_BUTTON_WAKEUP    RCC_APB2Periph_GPIOA
#define WAKEUP_BUTTON_PIN                    GPIO_Pin_0
#define WAKEUP_BUTTON_EXTI_LINE              EXTI_Line0
#define WAKEUP_BUTTON_PORT_SOURCE            GPIO_PortSourceGPIOA
#define WAKEUP_BUTTON_PIN_SOURCE             GPIO_PinSource0
#define WAKEUP_BUTTON_IRQn                   EXTI0_IRQn 

/* Tamper Push Button */
#define TAMPER_BUTTON_PORT                   (GPIO_TypeDef*)GPIOC
#define RCC_APB2Periph_GPIO_BUTTON_TAMPER    RCC_APB2Periph_GPIOC
#define TAMPER_BUTTON_PIN                    GPIO_Pin_13
#define TAMPER_BUTTON_EXTI_LINE              EXTI_Line13
#define TAMPER_BUTTON_PORT_SOURCE            GPIO_PortSourceGPIOC
#define TAMPER_BUTTON_PIN_SOURCE             GPIO_PinSource13
#define TAMPER_BUTTON_IRQn                   EXTI15_10_IRQn 
  
/* Exported macro ------------------------------------------------------------*/
/* Exported functions ------------------------------------------------------- */

/* LED Functions */
void STM32_EVAL_LEDInit(Led_TypeDef LED);
void STM32_EVAL_LEDOn(Led_TypeDef LED);
void STM32_EVAL_LEDOff(Led_TypeDef LED);
void STM32_EVAL_LEDToggle(Led_TypeDef LED);

/* Push Buttons and JoyStick Functions */
void STM32_EVAL_PBInit(Button_TypeDef Button);
void STM32_EVAL_PBITInit(Button_TypeDef Button);
uint32_t STM32_EVAL_PBGetState(Button_TypeDef Button);

#endif /* __STM3210_EVAL_H */

/******************* (C) COPYRIGHT 2009 STMicroelectronics *****END OF FILE****/
