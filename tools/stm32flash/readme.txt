Darstellung der Aufrufparameter von STM32flash.
Die Binaries basieren auf dem Open source flash program, have a look on 
http://code.google.com/p/stm32flash/

--- LINUX ---
Get device indormation:
./stm32flash /dev/ttyS0

Write with verify and then start execution:
./stm32flash -w <filename> -v -g 0x0 /dev/ttyS0

Read flash to file:
./stm32flash -r <filename> /dev/ttyS0

Start execution:
./stm32flash -g 0x0 /dev/ttyS0

Example to write OOBD Flashloader:
./stm32flash -w OOBD_Flashloader_SVN267.hex -v -g 0x0 /dev/ttyS0


--- WINDOWS ---
Get device indormation:
stm32flash.exe /dev/ttyS0

Write with verify and then start execution:
stm32flash.exe -w <filename> -v -g 0x0 /dev/ttyS0

Read flash to file:
stm32flash.exe -r <filename> /dev/ttyS0

Start execution:
stm32flash.exe -g 0x0 /dev/ttyS0

Example to write OOBD Flashloader:
stm32flash.exe -w OOBD_Flashloader_SVN267.hex -v -g 0x0 /dev/ttyS0
