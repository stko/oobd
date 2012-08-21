Darstellung der Aufrufparameter von STM32flash.
Die Binaries basieren auf dem Open source flash program, have a look on 
http://code.google.com/p/stm32flash/

--- LINUX ---
Get device indormation:
./stm32flash /dev/ttyS0

Read unprotect device:
./stm32flash -z /dev/ttyS0

Write unprotect device:
./stm32flash -u /dev/ttyS0

Write bootloader, erase whole memory verify and then start execution:
./stm32flash -w <filename> -v -x -g 0x0 /dev/ttyS0

Write firmware on offset 0x2F3C (0x08002F3C), verify and then start execution:
./stm32flash -w <filename> -v -o 0x2f3C-g 0x0 /dev/ttyS0

Write with verify and then start execution:
./stm32flash -w <filename> -v -g 0x0 /dev/ttyS0

General write with verify and then start execution:
./stm32flash -w <filename> -v -g 0x0 /dev/ttyS0

Read flash to file:
./stm32flash -r <filename> /dev/ttyS0

Start execution:
./stm32flash -g 0x0 /dev/ttyS0

Example to write OOBD Flashloader:
./stm32flash -w OOBD_Flashloader_SVN392.hex -v -g 0x0 /dev/ttyS0


--- WINDOWS ---
Get device indormation:
stm32flash.exe COM1

Read unprotect device:
./stm32flash -z COM1

Write unprotect device:
./stm32flash -u COM1

Write bootloader, erase whole memory verify and then start execution:
./stm32flash -w <filename> -v -x -g 0x0 COM1

Write firmware on offset 0x2F3C (0x08002F3C), verify and then start execution:
./stm32flash -w <filename> -v -o 0x2f3C-g 0x0 /dev/COM1

General write with verify and then start execution:
stm32flash.exe -w <filename> -v -g 0x0 COM1

Read flash to file:
stm32flash.exe -r <filename> COM1

Start execution:
stm32flash.exe -g 0x0 COM1

Example to write OOBD Flashloader:
stm32flash.exe -w OOBD_Flashloader_SVN392.hex -v -g 0x0 COM1
