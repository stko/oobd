# this is the template for your local settings.
# please copy this template as lokal.make
# and adapt the following variables acording to your local settings

#  your lua- compiler (luac)
# for Windows
#CC="<drive>:<directory_to_Lua>/luac"
# for Linux
CC=luac

# the OOBD Lua preprocessor
# for Cygwin
#OLP=../../tools/bin/olp.exe
# for Linux
OLP=mono ../../tools/bin/olp.exe

# Temporary output for Windows
# PACKDIR="C:\\temp\\diagoutput\\"
# Temporary output for linux
PACKDIR="/media/ram/diagoutput/"



# for Cygwin
#ODXT="../../tools/bin/OpenDiagXCL.exe" 
# for Linux
ODXT=mono ../../tools/bin/OpenDiagXCL.exe 

# relative path to the MDX pool
MDXPOOL=../../mdx_pool/


#enable PGP encryption by uncomment ENABLEPGP 
#ENABLEPGP= isEnabled

# your personal nickname, which should be shown as author of your lua- scripts

#AUTHOR="yournickname"


# the directories you want to compile, seperated by commas
# as list of the available directories depents on the individual user access rights, this
# directory list is part of the local user configuration

DIRS=b232_my12 b232_my12_mdx b299 c520

# Do NEVER remove this warning!!!
warning:
	echo Call this make without a defined target can create undefined results!


-include ../custom.make
