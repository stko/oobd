# this is the template for your local settings.
# please copy this template as lokal.make
# and adapt the following variables acording to your local settings

#  your lua- compiler (luac)

CC=luac

# your "delete" command
# for Windows
# DEL=del
# for Linux
DEL=rm


# the OOBD Lua preprocessor
# for Windows
#OLP="..\olp.exe"
# for Linux
OLP=mono ../../../tools/bin/olp.exe


# your "copy" command
# for Windows
# CP=copy
# for Linux
CP=cp


# your path to copy the compiled lua code file to

LBCPATH="../res"
