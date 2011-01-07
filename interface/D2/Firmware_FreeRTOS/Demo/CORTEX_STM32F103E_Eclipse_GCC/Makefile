#******************************************************************************
# @file      Makefile
# @author    Stefano Oliveri (software@stf12.net)
# @version   V2.0
# @date      22/06/2009
# @copy
#
# THE PRESENT FIRMWARE WHICH IS FOR GUIDANCE ONLY AIMS AT PROVIDING USERS
# WITH CODING INFORMATION REGARDING THEIR PRODUCTS IN ORDER FOR THEM TO SAVE
# TIME. AS A RESULT, STEFANO OLIVERI SHALL NOT BE HELD LIABLE FOR ANY
# DIRECT, INDIRECT OR CONSEQUENTIAL DAMAGES WITH RESPECT TO ANY CLAIMS ARISING
# FROM THE CONTENT OF SUCH FIRMWARE AND/OR THE USE MADE BY CUSTOMERS OF THE
# CODING INFORMATION CONTAINED HEREIN IN CONNECTION WITH THEIR PRODUCTS.
#
# <h2><center>&copy; COPYRIGHT 2009 Stefano Oliveri</center></h2>
#******************************************************************************

# Project name
PROJECT_NAME=OOBD_DXM


# Directory definition.
RTOS_SOURCE_DIR=../../Source
DEMO_COMMON_DIR=../Common/Minimal
DEMO_INCLUDE_DIR=../Common/include
ST_LIB_DIR=../Common/drivers/ST/STM32F10xFWLib/v3_0_0/Libraries/STM32F10x_StdPeriph_Driver
ARM_CMSIS_DIR=../Common/drivers/ST/STM32F10xFWLib/v3_0_0/Libraries/CMSIS

# Directory for output files (lst, obj, dep, elf, sym, map, hex, bin etc.).
OUTDIR = Debug

# Toolchain definition.
CC=arm-none-eabi-gcc
OBJCOPY=arm-none-eabi-objcopy
OBJDUMP=arm-none-eabi-size
NM = arm-none-eabi-nm
LDSCRIPT=stm32_flash.ld
#LDSCRIPT=stm32_link_script.ld
RM=rm

# should use --gc-sections but the debugger does not seem to be able to cope with the option.
LINKER_FLAGS=-nostartfiles -Xlinker -o$(PROJECT_NAME).axf -Xlinker -M -Xlinker -Map=$(PROJECT_NAME).map -Xlinker --no-gc-sections


# Debugging format.
#DEBUG = stabs
#DEBUG = dwarf-2
DEBUG= gdb

# Optimization level, can be [0, 1, 2, 3, s].
# 0 = turn off optimization. s = optimize for size.
# (Note: 3 is not always the best optimization level. See avr-libc FAQ.)
OPT = s
#OPT = 2
#OPT = 3
#OPT = 0


# Compiler flag to set the C Standard level.
# c89   - "ANSI" C
# gnu89 - c89 plus GCC extensions
# c99   - ISO C99 standard (not yet fully implemented)
# gnu99 - c99 plus GCC extensions
CSTANDARD = gnu89


# Compiler flags definition.
CFLAGS=-g$(DEBUG) \
		-O$(OPT) \
		-std=$(CSTANDARD) \
		-T$(LDSCRIPT) \
		-I . \
		-I ./STCode \
		-I $(RTOS_SOURCE_DIR)/include \
		-I $(RTOS_SOURCE_DIR)/portable/GCC/ARM_CM3 \
		-I $(DEMO_INCLUDE_DIR) \
		-I $(ST_LIB_DIR)/inc \
		-I $(ARM_CMSIS_DIR)/Core/CM3 \
		-D STM32F10X_HD \
		-D USE_STDPERIPH_DRIVER \
		-D VECT_TAB_FLASH \
		-D GCC_ARMCM3 \
		-D inline= \
		-D PACK_STRUCT_END=__attribute\(\(packed\)\) \
		-D ALIGN_STRUCT_END=__attribute\(\(aligned\(4\)\)\) \
		-mthumb \
		-mcpu=cortex-m3 \
		-ffunction-sections \
		-fdata-sections


# Source files
SOURCE=	main.c \
		stm32f10x_it.c \
		stf_syscalls_minimal.c \
		can.c \
		can_debug.c \
		SerialComm.c \
		SystemConfig.c


# ST Library source files.
ST_LIB_SOURCE= $(ARM_CMSIS_DIR)/Core/CM3/core_cm3.c \
		$(ARM_CMSIS_DIR)/Core/CM3/system_stm32f10x.c \
		$(ST_LIB_DIR)/src/misc.c \
		$(ST_LIB_DIR)/src/stm32f10x_rcc.c \
		$(ST_LIB_DIR)/src/stm32f10x_gpio.c \
		$(ST_LIB_DIR)/src/stm32f10x_spi.c \
		$(ST_LIB_DIR)/src/stm32f10x_tim.c \
		$(ST_LIB_DIR)/src/stm32f10x_usart.c \
		$(ST_LIB_DIR)/src/stm32f10x_can.c \
		$(ST_LIB_DIR)/src/stm32f10x_fsmc.c \

# FreeRTOS common demo source files.
FREERTOS_COMMON_DEMO_SOURCE = $(DEMO_COMMON_DIR)/flash.c \
		$(DEMO_COMMON_DIR)/blocktim.c \
		$(DEMO_COMMON_DIR)/death.c \
		$(DEMO_COMMON_DIR)/integer.c \
		$(DEMO_COMMON_DIR)/PollQ.c \
		$(DEMO_COMMON_DIR)/semtest.c \

# FreeRTOS source files.
FREERTOS_SOURCE= $(RTOS_SOURCE_DIR)/list.c \
		$(RTOS_SOURCE_DIR)/queue.c \
		$(RTOS_SOURCE_DIR)/tasks.c \
		$(RTOS_SOURCE_DIR)/portable/GCC/ARM_CM3/port.c \
		$(RTOS_SOURCE_DIR)/portable/MemMang/heap_2.c


SOURCE+=$(ST_LIB_SOURCE)
#SOURCE+=$(FREERTOS_COMMON_DEMO_SOURCE)
SOURCE+=$(FREERTOS_SOURCE)


# List of all source files without directory and file-extension.
ALLSRCBASE = $(notdir $(basename $(SOURCE)))


LIBS=

# List of all objects files.
OBJS = $(addprefix $(OUTDIR)/, $(addsuffix .o, $(ALLSRCBASE)))


# Define Messages.
# English
MSG_BEGIN = -------- begin --------
MSG_END = --------  end  --------


# Rules definition. ***********************************************************

all: begin gccversion $(OUTDIR)/$(PROJECT_NAME).bin end

$(OUTDIR)/$(PROJECT_NAME).bin : $(PROJECT_NAME).axf
	$(OBJCOPY) $(PROJECT_NAME).axf -O binary $(PROJECT_NAME).bin

$(PROJECT_NAME).axf : $(OBJS) $(OUTDIR)/startup_stm32f10x.o Makefile
	$(CC) $(CFLAGS) $(OBJS) $(OUTDIR)/startup_stm32f10x.o $(LIBS) $(LINKER_FLAGS)


# Compile: create object files from C source files.
define COMPILE_C_TEMPLATE
$(OUTDIR)/$(notdir $(basename $(1))).o : $(1)
##	@echo
	@echo $$< "->" $$@
	$(CC) -c  $$(CFLAGS) $$< -o $$@
endef
$(foreach src, $(SOURCE), $(eval $(call COMPILE_C_TEMPLATE, $(src))))

$(OUTDIR)/startup_stm32f10x.o : startup_stm32f10x.c Makefile
	$(CC) -c $(CFLAGS) -O1 startup_stm32f10x.c -o $(OUTDIR)/startup_stm32f10x.o

clean :
	-$(RM) $(OBJS)
	-$(RM) $(OUTDIR)/startup_stm32f10x.o
	-$(RM) $(PROJECT_NAME).axf
	-$(RM) $(PROJECT_NAME)_SymbolTable.txt
	-$(RM) $(PROJECT_NAME)_MemoryListingSummary.txt
	-$(RM) $(PROJECT_NAME)_MemoryListingDetails.txt

log : $(PROJECT_NAME).axf
	$(NM) -n $(PROJECT_NAME).axf > $(PROJECT_NAME)_SymbolTable.txt
	$(OBJDUMP) --format=SysV $(PROJECT_NAME).axf > $(PROJECT_NAME)_MemoryListingSummary.txt
	$(OBJDUMP) $(OBJS) > $(PROJECT_NAME)_MemoryListingDetails.txt

# Eye candy.
begin:
##	@echo
	@echo $(MSG_BEGIN)

end:
	@echo $(MSG_END)
##	@echo

# Display compiler version information.
gccversion :
	@$(CC) --version

$(shell mkdir $(OUTDIR) )