/**
  ******************************************************************************
  * @file     stm3210_eval.c
  * @author   MCD Application Team
  * @version  V3.0.0
  * @date     04/06/2009
  * @brief  This file provides all the EVAL-Boards (STM3210E-EVAL and STM3210B-EVAL) 
  *         firmware functions used to configure and manage Leds and different 
  *         push buttons (buttons and JoyStick).
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
  
/* Includes ------------------------------------------------------------------*/
#include "stm3210_eval.h"

/* Private typedef -----------------------------------------------------------*/
/* Private define ------------------------------------------------------------*/
/* Private macro -------------------------------------------------------------*/
/* Private variables ---------------------------------------------------------*/
GPIO_TypeDef* GPIO_PORT[4] = {GPIO_PORT_LED1, GPIO_PORT_LED2, GPIO_PORT_LED3, GPIO_PORT_LED4};
const uint16_t GPIO_PIN[4] = {GPIO_PIN_LED1, GPIO_PIN_LED2, GPIO_PIN_LED3, GPIO_PIN_LED4};
const uint32_t RCC_APB2Periph_GPIO_LED[4] = {RCC_APB2Periph_GPIO_LED1, RCC_APB2Periph_GPIO_LED2,
                                             RCC_APB2Periph_GPIO_LED3, RCC_APB2Periph_GPIO_LED4};

GPIO_TypeDef* BUTTON_PORT[8] = {WAKEUP_BUTTON_PORT, TAMPER_BUTTON_PORT, 
                                KEY_BUTTON_PORT, RIGHT_BUTTON_PORT, LEFT_BUTTON_PORT,
                                UP_BUTTON_PORT, DOWN_BUTTON_PORT, SEL_BUTTON_PORT}; 

const uint16_t BUTTON_PIN[8] = {WAKEUP_BUTTON_PIN, TAMPER_BUTTON_PIN, 
                                KEY_BUTTON_PIN, RIGHT_BUTTON_PIN, LEFT_BUTTON_PIN,
                                UP_BUTTON_PIN, DOWN_BUTTON_PIN, SEL_BUTTON_PIN}; 

const uint32_t RCC_APB2Periph_GPIO_BUTTON[8] = {RCC_APB2Periph_GPIO_BUTTON_WAKEUP, RCC_APB2Periph_GPIO_BUTTON_TAMPER,
                                                RCC_APB2Periph_GPIO_BUTTON_KEY, RCC_APB2Periph_GPIO_BUTTON_RIGHT,
                                                RCC_APB2Periph_GPIO_BUTTON_LEFT, RCC_APB2Periph_GPIO_BUTTON_UP,
                                                RCC_APB2Periph_GPIO_BUTTON_DOWN, RCC_APB2Periph_GPIO_BUTTON_SEL};

const uint16_t BUTTON_EXTI_LINE[8] = {WAKEUP_BUTTON_EXTI_LINE, TAMPER_BUTTON_EXTI_LINE, 
                                      KEY_BUTTON_EXTI_LINE, RIGHT_BUTTON_EXTI_LINE, LEFT_BUTTON_EXTI_LINE,
                                      UP_BUTTON_EXTI_LINE, DOWN_BUTTON_EXTI_LINE, SEL_BUTTON_EXTI_LINE};

const uint16_t BUTTON_PORT_SOURCE[8] = {WAKEUP_BUTTON_PORT_SOURCE, TAMPER_BUTTON_PORT_SOURCE, 
                                        KEY_BUTTON_PORT_SOURCE, RIGHT_BUTTON_PORT_SOURCE, LEFT_BUTTON_PORT_SOURCE,
                                        UP_BUTTON_PORT_SOURCE, DOWN_BUTTON_PORT_SOURCE, SEL_BUTTON_PORT_SOURCE};
								 
const uint16_t BUTTON_PIN_SOURCE[8] = {WAKEUP_BUTTON_PIN_SOURCE, TAMPER_BUTTON_PIN_SOURCE, 
                                       KEY_BUTTON_PIN_SOURCE, RIGHT_BUTTON_PIN_SOURCE, LEFT_BUTTON_PIN_SOURCE,
                                       UP_BUTTON_PIN_SOURCE, DOWN_BUTTON_PIN_SOURCE, SEL_BUTTON_PIN_SOURCE}; 

const uint16_t BUTTON_IRQn[8] = {WAKEUP_BUTTON_IRQn, TAMPER_BUTTON_IRQn, 
                                 KEY_BUTTON_IRQn, RIGHT_BUTTON_IRQn, LEFT_BUTTON_IRQn,
                                 UP_BUTTON_IRQn, DOWN_BUTTON_IRQn, SEL_BUTTON_IRQn};

/* Private function prototypes -----------------------------------------------*/
/* Private functions ---------------------------------------------------------*/

/**
  * @brief Configures the LEDs relative GPIO port IOs.
  * @param LED: Specifies the Led to be configured. 
  *   This parameter can be one of following parameters:
  * @arg LED1: Led 1
  * @arg LED2: Led 2
  * @arg LED3: Led 3
  * @arg LED4: Led 4  
  * @retval: None
  */
void STM32_EVAL_LEDInit(Led_TypeDef LED)
{
  GPIO_InitTypeDef  GPIO_InitStructure;
  
  /* Enable the GPIO_LED Clock */
  RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIO_LED[LED], ENABLE);

  /* Configure the GPIO_LED pin */
  GPIO_InitStructure.GPIO_Pin = GPIO_PIN[LED];
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;
  GPIO_Init(GPIO_PORT[LED], &GPIO_InitStructure);
}

/**
  * @brief Turn selected LEDs On.
  * @param LED: Specifies the Led to be set on. 
  *   This parameter can be one of following parameters:
  * @arg LED1: Led 1
  * @arg LED2: Led 2
  * @arg LED3: Led 3
  * @arg LED4: Led 4  
  * @retval: None
  */
void STM32_EVAL_LEDOn(Led_TypeDef LED)
{
  /* Turn on the relative LED */
  GPIO_SetBits(GPIO_PORT[LED], GPIO_PIN[LED]);  
  
}

/**
  * @brief Turn selected LEDs Off.
  * @param LED: Specifies the Led to be set off. 
  *   This parameter can be one of following parameters:
  * @arg LED1: Led 1
  * @arg LED2: Led 2
  * @arg LED3: Led 3
  * @arg LED4: Led 4  
  * @retval: None
  */
void STM32_EVAL_LEDOff(Led_TypeDef LED)
{
  /* Turn off the relative LED */
  GPIO_ResetBits(GPIO_PORT[LED], GPIO_PIN[LED]);
}

/**
  * @brief Toggle the selected LED.
  * @param LED: Specifies the Led to be toggled. 
  *   This parameter can be one of following parameters:
  * @arg LED1: Led 1
  * @arg LED2: Led 2
  * @arg LED3: Led 3
  * @arg LED4: Led 4  
  * @retval: None
  */
void STM32_EVAL_LEDToggle(Led_TypeDef LED)
{
  /* Toggle the relative LED */
  GPIO_WriteBit(GPIO_PORT[LED], GPIO_PIN[LED], (BitAction)(1 - GPIO_ReadOutputDataBit(GPIO_PORT[LED], GPIO_PIN[LED])));
}

/**
  * @brief Configures the Push Buttons relative GPIO port IOs.
  * @param Button: Specifies the push-button to be configured.
  *   This parameter can be one of following parameters:   
  * @arg   Button_WAKEUP: Wakeup Push Button
  * @arg   Button_TAMPER: Tamper Push Button  
  * @arg   Button_KEY: Key Push Button 
  * @arg   Button_RIGHT: Joystick Right Push Button 
  * @arg   Button_LEFT: Joystick Left Push Button 
  * @arg   Button_UP: Joystick Up Push Button 
  * @arg   Button_DOWN: Joystick Down Push Button
  * @arg   Button_SEL: Joystick Sel Push Button    
  * @retval : None
  */
void STM32_EVAL_PBInit(Button_TypeDef Button)
{
  GPIO_InitTypeDef  GPIO_InitStructure;

  /* Enable the BUTTON Clock */
  RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIO_BUTTON[Button] | RCC_APB2Periph_AFIO, ENABLE);
  
  /* Configure the BUTTON pin to input floating */
  GPIO_InitStructure.GPIO_Pin = BUTTON_PIN[Button];
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN_FLOATING;
  GPIO_Init(BUTTON_PORT[Button], &GPIO_InitStructure);

  GPIO_EXTILineConfig(BUTTON_PORT_SOURCE[Button], BUTTON_PIN_SOURCE[Button]);  
}

/**
  * @brief Configures the Push Buttons relative external interrupt line and NVIC 
  *        IRQ channel.
  * @param Button: Specifies the push-button to be configured as external interrupt.
  *   This parameter can be one of following parameters:  
  * @arg   Button_WAKEUP: Wakeup Push Button
  * @arg   Button_TAMPER: Tamper Push Button  
  * @arg   Button_KEY: Key Push Button 
  * @arg   Button_RIGHT: Joystick Right Push Button 
  * @arg   Button_LEFT: Joystick Left Push Button 
  * @arg   Button_UP: Joystick Up Push Button 
  * @arg   Button_DOWN: Joystick Down Push Button
  * @arg   Button_SEL: Joystick Sel Push Button    
  * @retval : None
  */
void STM32_EVAL_PBITInit(Button_TypeDef Button)
{
  EXTI_InitTypeDef EXTI_InitStructure;
  NVIC_InitTypeDef NVIC_InitStructure;
 
  EXTI_InitStructure.EXTI_Mode = EXTI_Mode_Interrupt;
  EXTI_InitStructure.EXTI_Line = BUTTON_EXTI_LINE[Button];
  if(Button != Button_WAKEUP)
  {
    EXTI_InitStructure.EXTI_Trigger = EXTI_Trigger_Falling;  
  }
  else
  {
    EXTI_InitStructure.EXTI_Trigger = EXTI_Trigger_Rising;  
  }
  EXTI_InitStructure.EXTI_LineCmd = ENABLE;
  EXTI_Init(&EXTI_InitStructure);
   
  /* Enable the BUTTON EXTI Interrupt */
  NVIC_InitStructure.NVIC_IRQChannel = BUTTON_IRQn[Button];
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0;
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0;
  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
  NVIC_Init(&NVIC_InitStructure);  
}

/**
  * @brief Returns the selected push button state.
  * @param Button: Specifies the push-button to be checked.
  *   This parameter can be one of following parameters:    
  * @arg   Button_WAKEUP: Wakeup Push Button
  * @arg   Button_TAMPER: Tamper Push Button  
  * @arg   Button_KEY: Key Push Button 
  * @arg   Button_RIGHT: Joystick Right Push Button 
  * @arg   Button_LEFT: Joystick Left Push Button 
  * @arg   Button_UP: Joystick Up Push Button 
  * @arg   Button_DOWN: Joystick Down Push Button
  * @arg   Button_SEL: Joystick Sel Push Button    
  * @retval : None
  */
uint32_t STM32_EVAL_PBGetState(Button_TypeDef Button)
{
  return GPIO_ReadInputDataBit(BUTTON_PORT[Button], BUTTON_PIN[Button]);
}

/******************* (C) COPYRIGHT 2009 STMicroelectronics *****END OF FILE****/
