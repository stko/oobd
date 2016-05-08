include ../../local.make

SVNVERSION=$(warning Look for SVN revision)$(shell $(OOBD_ROOT_DIR)/interface/D2/Firmware_FreeRTOS/Demo/Common_OOBD/getrev.sh)
BUILDDATE=$(warning get actual date)'(const char *)("$(shell date --rfc-822)")'


# Compiler flags definition.
CFLAGS=
#		-define:BUILDDATE=$(BUILDDATE) \
#		-define:SVNREV=$(SVNVERSION) 


all: olp.exe

olp.exe: olp/olp.cs
	mcs $(CFLAGS) olp/olp.cs

clean:
	rm *.exe
