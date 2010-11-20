include local.make



LIB=lib
#CFLAGS=-u $(AUTHOR) -I $(LIB) -x $(RSAID)
CFLAGS=

SOURCES= OOBD.lua

OBJECTS=$(SOURCES:.lua=.lbc)

%.lbc: %.lua   $(LIB)/serial_dxm.lua
	$(CC) $(CFLAGS)-o $@ $(LIB)/serial_dxm.lua $<
	$(CP) $@ $(LBCPATH)/


all: $(OBJECTS)


clean:
	$(DEL) *.lbc
