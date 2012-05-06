include local.make



SOURCES= OOBD.lua




LUALIB=lib

#CFLAGS=-u $(AUTHOR) -I $(LIB) -x $(RSAID)
CFLAGS=


OBJECTS=$(SOURCES:.lua=.lbc)
%.lbc: %.lua 
	$(OLP) $<  | $(CC) $(CFLAGS) -o $@ -
	$(CP) $@ $(LBCPATH)/



all:  scripts 

scripts: $(OBJECTS)  $(LUALIB)/serial_dxm.lua 


clean:
	$(DEL) *.lbc
