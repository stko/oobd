# this is the template for your local settings.
# please copy this template as lokal.make
# and adapt the following variables acording to your local settings

## to allow a make process from different directories,
## point to the OOBD root directory here, where OOBD is located

OOBDROOT=~/Desktop/workcopies/skdsscripts/oobd2/OEMs/Ford/trunk

#  your lua- compiler (luac)
# for Windows
#CC="<drive>:<directory_to_Lua>/luac"
# for Linux
CC=luac

# the OOBD Lua preprocessor
# for Cygwin
#OLP=$(OOBDROOT)/tools/bin/olp.exe
# for Linux
OLP=mono $(OOBDROOT)/tools/bin/olp.exe

# Temporary output for Windows
# PACKDIR="C:\\temp\\diagoutput\\"
# Temporary output for linux
PACKDIR="/media/ram/diagoutput/"



# for Cygwin
#ODXT="$(OOBDROOT)/tools/bin/OpenDiagXCL.exe" 
# for Linux
ODXT=mono $(OOBDROOT)/tools/bin/OpenDiagXCL.exe 

# relative path to the MDX pool
MDXPOOL=../../mdx_pool/


#enable PGP encryption by uncomment ENABLEPGP 
#ENABLEPGP= isEnabled

# your personal nickname, which should be shown as author of your lua- scripts

#AUTHOR="yournickname"


# the directories you want to compile, seperated by commas
# as list of the available directories depents on the individual user access rights, this
# directory list is part of the local user configuration

DIRS=examples \
obdII-standard

# Do NEVER remove this warning!!!
warning:
	echo Call this make without a defined target can create undefined results!


-include ../custom.make
