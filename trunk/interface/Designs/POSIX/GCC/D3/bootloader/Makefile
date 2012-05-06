CC=gcc
CFLAGS=-c -Wall
LDFLAGS=
SOURCES=main.c ymodem.c
#SOURCES=main.c 
OBJECTS=$(SOURCES:.c=.o)
EXECUTABLE=bootloader-sim

all: $(SOURCES) $(EXECUTABLE)
	
$(EXECUTABLE): $(OBJECTS) 
	$(CC) $(LDFLAGS) $(OBJECTS) -o $@

.c.o:
	$(CC) $(CFLAGS) $< -o $@

clean:
	indent -kr *.c *.h
	rm *.o
