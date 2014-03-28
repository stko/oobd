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
PROJECT_NAME=OOBD_POSIX

# set compiler switches
OOBD_PLATFORM=OOBD_PLATFORM_POSIX
OOBD_DESIGN='(const char *)("POSIX")'

#version settings
OS_VERSION=FreeRTOSV8.0.0
OOBD_VERSION=v1


# Directory definition.
RTOS_SOURCE_DIR=../../../../../OS/$(OS_VERSION)/Source
RTOS_PORT_DIR=$(RTOS_SOURCE_DIR)/portable/GCC/Posix
OOBD_COMMON_DIR=../../../../../OOBD/$(OOBD_VERSION)
DEMO_INCLUDE_DIR=../../../../../OS/$(OS_VERSION)/Demo/Common/include
PORT_INCLUDE_DIR=./AsyncIO

# Directory for output files (lst, obj, dep, elf, sym, map, hex, bin etc.).
OUTDIR = Debug

# Toolchain definition.
CC=gcc

#LDSCRIPT=stm32_link_script.ld
RM=rm



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
CFLAGS=-I . \
		-I $(RTOS_SOURCE_DIR)/include \
		-I $(RTOS_PORT_DIR) \
		-I $(OOBD_COMMON_DIR) \
		-I $(DEMO_INCLUDE_DIR) \
		-I $(PORT_INCLUDE_DIR) \
		-D__GCC_POSIX__=1 \
		-DDEBUG_BUILD=1 \
		-DSVNREV=$(SVNVERSION) \
		-DBUILDDATE=$(BUILDDATE) \
		-DOOBDDESIGN=$(OOBD_DESIGN) \
		-D$(OOBD_PLATFORM) \
		-DUSE_STDIO=1 \
		-O0 \
		-g \
		-rdynamic \
		-Wall \
		-c \
		-fmessage-length=0 \
		-pthread \
		-lrt \
		-Wno-pointer-sign \
		-MMD \
		-MP \
		-MF"$(@:%.o=%.d)" \
		-MT"$(@:%.o=%.d)" 



# Source files
SOURCE=	mc_can.c \
		mc_serial.c \
		mc_sys.c



# OOBD common  source files.
include $(OOBD_COMMON_DIR)/common_oobd.mk

# FreeRTOS source files.
FREERTOS_SOURCE= $(RTOS_SOURCE_DIR)/list.c \
		$(RTOS_SOURCE_DIR)/queue.c \
		$(RTOS_SOURCE_DIR)/tasks.c \
		$(RTOS_PORT_DIR)/port.c \
		$(RTOS_SOURCE_DIR)/portable/MemMang/heap_3.c

PORT_INCLUDE_SOURCE= $(PORT_INCLUDE_DIR)/AsyncIO.c \
		$(PORT_INCLUDE_DIR)/AsyncIOSerial.c \
		$(PORT_INCLUDE_DIR)/AsyncIOSocketCan.c \
		$(PORT_INCLUDE_DIR)/PosixMessageQueueIPC.c \
		libsocketcan.c


SOURCE+=$(OOBD_COMMON_SOURCE)
SOURCE+=$(FREERTOS_SOURCE)
SOURCE+=$(PORT_INCLUDE_SOURCE)



# List of all source files without directory and file-extension.
ALLSRCBASE = $(notdir $(basename $(SOURCE)))


LIBS=

# List of all objects files.
OBJS = $(addprefix $(OUTDIR)/, $(addsuffix .o, $(ALLSRCBASE)))

# should use --gc-sections but the debugger does not seem to be able to cope with the option.
LINKER_FLAGS= -pthread -lrt -o"$(PROJECT_NAME).bin" 

# Define Messages.
# English
MSG_BEGIN = -------- begin --------
MSG_END = --------  end  --------


# Rules definition. ***********************************************************

all: begin gccversion $(OUTDIR)/$(PROJECT_NAME).bin end

$(OUTDIR)/$(PROJECT_NAME).bin : $(OBJS)  Makefile
	$(CC) $(OBJS)  $(LIBS) $(LINKER_FLAGS)



# Compile: create object files from C source files.
define COMPILE_C_TEMPLATE
$(OUTDIR)/$(notdir $(basename $(1))).o : $(1)
##	@echo
	@echo $$< "->" $$@
	$(CC) -c  $$(CFLAGS) $$< -o $$@
endef
$(foreach src, $(SOURCE), $(eval $(call COMPILE_C_TEMPLATE, $(src))))


clean :  format
	-$(RM) $(OBJS)


# generate doxygen docs
docs:
	cd ../../../../.. ; doxygen
#	$(shell cd .. ; doxygen)


# generate doxygen docs
publish:
	cd ../../../../.. ; sitecopy -u oobd-firmware
#	$(shell cd .. ; doxygen)


# text formating
format:

	cd .. ; for src in `find   \( -name "*.c" -o -name "*.h" \)   -type f  -print0  | grep -FzZ --invert-match "Common/" - | xargs -0 ` ; do \
	echo $$src ; \
	fromdos $$src ; \
	indent $$src -kr ; \
	done 
	cd ../../../../../OOBD ; for src in `find   \( -name "*.c" -o -name "*.h" \)   -type f  -print0  | grep -FzZ --invert-match "Common/" - | xargs -0 ` ; do \
	echo $$src ; \
	fromdos $$src ; \
	indent $$src -kr ; \
	done 
#define TEXTFORMAT
#@echo $(1)
#endef
#$(foreach src, $(OOBD_COMMON_SOURCE), $(eval $(call TEXTFORMAT, $(src))))
#
#	for number in $(SOURCE) ; do \
#	echo $$number ; \
#	done


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