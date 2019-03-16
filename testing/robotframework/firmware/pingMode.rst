.. code:: robotframework

    *** Test Cases ***
    Dongle reports version
	Sleep                0.4
        send dongle command  p 0 0 0 
        answer should match    .*(OBD).*
    Dongle reset protocol to UDS
        send dongle command  p 1 1 1 0
        answer should match    ..*(\\.\\+cr\\+>)
    restore timeout to standard and request msg with closed filters
       send dongle command  p 6 1 10
       answer should match    .*(\\.\\+cr\\+>)
       send dongle command  22F222
       answer should match    .*(:Error: \\d+ \\d+ \\d+ Answer time exeeded\\+cr\\+>)
    open the can filters
        send dongle command  p 8 3 1 
        answer should match    .*(\\.\\+cr\\+>)
        send dongle command  p 6 5 $7DF
        answer should match   .*(\\.\\+cr\\+>)
        send dongle command  p 8 10 1 $700 
        answer should match    .*(\\.\\+cr\\+>)
        send dongle command  p 8 11 1 $700 
        answer should match    .*(\\.\\+cr\\+>)
    activate the bus
	send dongle command  p 8 2 3 
        answer should match    .*(\\.\\+cr\\+>)
    activate Ping Mode
       Sleep                1.0
       send dongle command  p 6 11 1 0
       answer should match    .*(\\.\\+cr\\+>)
    do Ping request
       send dongle command  190102
       answer should be equal  190102+cr+000007ec065901ff00+cr+0000000000072b06+cr+5901ca0000000000+cr+00072e065901ca00+cr+000000000007cf06+cr+5901ca0000000000+cr+0007ee065901ff00+cr+000000000007d806+cr+5901cb0000000000+cr+00074e065901fb00+cr+000000000007e806+cr+5901ff0000000000+cr+00073e065901fb00+cr+000000+cr+.+cr+>





.. code:: robotframework

    *** Settings ***
    Library           OperatingSystem
    Library           ../lib/DongleTelnetCmdLine.py
    Variables         ../local_settings.py
    suite Setup       Open Port  ${FirmwareHost}  ${FirmwarePort}
    suite Teardown    close port

