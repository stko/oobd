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
        send dongle command  p 6 5 ${moduleID}
        answer should match   .*(\\.\\+cr\\+>)
        send dongle command  p 8 10 1 $700 
        answer should match    .*(\\.\\+cr\\+>)
        send dongle command  p 8 11 1 $700 
        answer should match    .*(\\.\\+cr\\+>)
    activate the bus
	send dongle command  p 8 2 3 
        answer should match    .*(\\.\\+cr\\+>)
    test normal response
       send dongle command  19018D
       answer should match    .*(\\.\\+cr\\+>)
    increase time out and request timeout msg
       send dongle command  p 6 1 1000 0
       answer should match    .*(\\.\\+cr\\+>)
       send dongle command  22F222
       answer should match    .*(\\.\\+cr\\+>)
    restore time out and request timeout msg
       send dongle command  p 6 1 10
       answer should match    .*(\\.\\+cr\\+>)
       send dongle command  22F222
       answer should match    .*(:Error: \\d+ \\d+ \\d+ Answer time exeeded\\+cr\\+>)
    increase time out and request timeout msg again
       send dongle command  p 6 1 1000
       answer should match    .*(\\.\\+cr\\+>)
       send dongle command  22F222
       answer should match    .*(\\.\\+cr\\+>)
    deactivate the bus
	send dongle command  p 8 2 0 
        answer should match    .*(\\.\\+cr\\+>)
    restore timeout to standard and request msg with inactive bus
      send dongle command  p 6 1 10
       answer should match    .*(\\.\\+cr\\+>)
       send dongle command  22F222
       answer should match    .*(:Error: \\d+ \\d+ \\d+ Answer time exeeded\\+cr\\+>)

.. code:: robotframework

    *** Settings ***
    Library           OperatingSystem
    Library           ../lib/DongleTelnetCmdLine.py
    Variables         ../local_settings.py
    suite Setup       Open Port  ${FirmwareHost}  ${FirmwarePort}
    suite Teardown    close port

