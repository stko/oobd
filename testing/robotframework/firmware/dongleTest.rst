.. code:: robotframework

    *** Test Cases ***
    Initial Delay
        Sleep                0.4
     Dongle reports version
         ${out} =  Execute Command  p 0 0 0 
        Should Match Regexp	${out}    (?im).*(OOBD).*
    Dongle reports version old
        write               p 0 0 0 
        ${out} =	Read Until Regexp  > 
        Should Match Regexp	${out}    (?im).*(OBD).*
    set 125K 
       write               p 8 3 1 
        ${out} =	read until prompt
        Should Match Regexp	${out}    (?im).*(\\.\\+cr\\+>)
    set response id to 740
       write               p 6 5 $740 
        ${out} =	read until prompt
        Should Match Regexp	${out}    (?im).*(\\.\\+cr\\+>)
    set mask to 700
       write               p 8 11 1 $700 
        ${out} =	read until prompt
        Should Match Regexp	${out}    (?im).*(\\.\\+cr\\+>)
    set ID to 0
       write               p 8 10 1 $700 
        ${out} =	read until prompt
        Should Match Regexp	${out}    (?im).*(\\.\\+cr\\+>)
    activate bus 
       write               p 8 2 3 
        ${out} =	read until prompt
        Should Match Regexp	${out}    (?im).*(\\.\\+cr\\+>)
    test normal response
       write               19018D 
        ${out} =	read until prompt
        Should Match Regexp	${out}    (?im).*(\\.\\+cr\\+>)
    increase time out and request timeout msg
       write               p 6 1 1000 0 
        ${out} =	read until prompt
        Should Match Regexp	${out}    (?im).*(\\.\\+cr\\+>)
       write               22F222 
        ${out} =	read until prompt
        Should Match Regexp	${out}    (?im).*(\\.\\+cr\\+>)
    restore time out and request timeout msg
       write               p 6 1 10 
        ${out} =	read until prompt
        Should Match Regexp	${out}    (?im).*(\\.\\+cr\\+>)
       write               22F222 
        ${out} =	read until prompt
        Should Match Regexp	${out}    (?im).*(:Error: \\d+ \\d+ \\d+ Answer time exeeded\\+cr\\+>)
    increase time out and request timeout msg again
       write               p 6 1 1000 
        ${out} =	read until prompt
        Should Match Regexp	${out}    (?im).*(\\.\\+cr\\+>)
       write               22F222 
        ${out} =	read until prompt
        Should Match Regexp	${out}    (?im).*(\\.\\+cr\\+>)

    *** Settings ***
    Library           OperatingSystem
    Library           Telnet 
    Variables         ../local_settings.py
    suite Setup       Open Connection  localhost  port=3001  prompt=\\r\\.\\r|\\r:  prompt_is_regexp=yes  newline=CR  terminal_emulation=True  terminal_type=vt100  window_size=400x100
    suite Teardown    Close Connection

important: Do NOT forget the telnet patch ! as descripted in: https://github.com/robotframework/robotframework/issues/2351
