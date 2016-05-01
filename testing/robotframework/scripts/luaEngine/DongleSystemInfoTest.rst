.. default-role:: code

=====================================
  OOBD Core Test ing
=====================================

Robot Framework is Copyright © Nokia Solutions and Networks. Licensed under the
`Creative Commons Attribution 3.0 Unported`__ license.

__ http://creativecommons.org/licenses/by/3.0/

.. contents:: Table of contents:
   :local:
   :depth: 2



Test cases
==========

Workflow tests
--------------

This text is a so called "reStructuredText" as defined in docUtils (https://pypi.python.org/pypi/docutils), which e.g. Github interprets as Markdown syntax, while Robotframework (RF) reads the embedded testcases in it. This is a little bit scary, but why the other ones should not have scary ideas too?

So the test cases can contain their own documentation 


Syntax Info
-----------

In RF parameters can contain blanks. To seperate commands and parameters from each other, you have to use at least *two* blanks!

Answer match algorythm: To be a valid answer, the answer must contain all the keys and values of the match pattern. Also the type of the values must be equal.

To comparing strings, the algorythm support two leading modifiers, % and #. Both are optional, but the sequence first % and then # must be kept when using both.

When the pattern begins with %, the remaining pattern is handled as a regular expression for the comparision. If the % modifier is not given, both strings are compared 1:1

When the pattern starts with #, the string in the answer is seen as base64 coded and base64- encoded first before compared with the remaining pattern





.. code:: robotframework

    *** Test Cases ***
    Starting the script testsuite.lbc via HTTP
	Create Http Context   ${httpOobd}
	Get  /dGVzdHN1aXRlLmxiYw==
	open webUI  ${wsOobdURL}  ${wsSocketTimeout}
    Test for initial connect message
	answer should match    {"type":"WSCONNECT"}
	answer should match    {"type":"WRITESTRING" ,"data":"%#.*(OBD).*"}
 	answer should match    {"type":"PAGE" , "name":"OOBD Testsuite"}
	answer should match    {"type":"VISUALIZE" ,"name":"createCMD01Menu:"}
	answer should match    {"type":"VISUALIZE" ,"name":"greet:"}
	answer should match    {"type":"VISUALIZE" ,"name":"clearOutput:"}
	answer should match    {"type":"VISUALIZE" ,"name":"saveOutputAs:"}
	answer should match    {"type":"VISUALIZE" ,"name":"saveOutput:"}
	answer should match    {"type":"VISUALIZE" ,"name":"saveBuffer1:"}
	answer should match    {"type":"VISUALIZE" ,"name":"userAlert:"}
	answer should match    {"type":"VISUALIZE" ,"name":"userConfirm:"}
	answer should match    {"type":"VISUALIZE" ,"name":"userPrompt:"}
	answer should match    {"type":"VISUALIZE" ,"name":"bufferSequence:"}
	answer should match    {"type":"PAGEDONE" ,"name":"Canvastest_1"}
    Test for Dongle Version with Dongle connected
        send webUI command  {"name":"interface_version:","optid":"","actValue":"","updType":3}
	answer should match    {"type":"VALUE" ,"value":"%#OOBD"}
    Test for Dongle Serial Nr with Dongle connected
        send webUI command  {"name":"interface_serial:","optid":"","actValue":"","updType":3}
	answer should match    {"type":"VALUE" ,"value":"#000"}
    Test for Dongle Voltage with Dongle connected
        send webUI command  {"name":"interface_voltage:","optid":"","actValue":"","updType":3}
	answer should match    {"type":"VALUE" ,"value":"%#[0-9.]+ Volt"}
    Test for Dongle Bus with Dongle connected
        send webUI command  {"name":"interface_bus:","optid":"","actValue":"","updType":3}
	answer should match    {"type":"VALUE" ,"value":"#CAN Bus"}
    Test for Dongle Device ID with Dongle connected
        send webUI command  {"name":"interface_deviceID:","optid":"","actValue":"","updType":3}
	answer should match    {"type":"VALUE" ,"value":"%#OOBD-"}
    Interupt Dongle communication
	Pause Execution      Disable dongle communication to generate fault condition
    Test for Dongle Version without Dongle connected
        send webUI command  {"name":"interface_version:","optid":"","actValue":"","updType":3}
	answer should match    {"type":"VALUE" ,"value":"#Lost Connection"}
    Test for Dongle Serial Nr without Dongle connected
        send webUI command  {"name":"interface_serial:","optid":"","actValue":"","updType":3}
	answer should match    {"type":"VALUE" ,"value":"#Lost Connection"}
    Test for Dongle Voltage without Dongle connected
        send webUI command  {"name":"interface_voltage:","optid":"","actValue":"","updType":3}
	answer should match    {"type":"VALUE" ,"value":"#Lost Connection"}
    Test for Dongle Bus without Dongle connected
        send webUI command  {"name":"interface_bus:","optid":"","actValue":"","updType":3}
	answer should match    {"type":"VALUE" ,"value":"#Lost Connection"}
    Test for Dongle Device ID without Dongle connected
        send webUI command  {"name":"interface_deviceID:","optid":"","actValue":"","updType":3}
	answer should match    {"type":"VALUE" ,"value":"#Lost Connection"}
    Re-Establish Dongle communication
	Pause Execution      Enable dongle communication again for futher testing
	close webUI


.. code:: robotframework

    *** Settings ***
    Library           OperatingSystem
    Library           Dialogs
    Library           ../../lib/webUIClient.py
    Variables         ../../local_settings.py
    Library           HttpLibrary.HTTP

for HTTP testing we choose the testing library from https://github.com/peritus/robotframework-httplibrary/



.. code:: robotframework

    *** Variables ***
    

Variables can also be given from the command line which is useful if
the tests need to be executed in different environments. For example
this demo can be executed like::

   pybot --variable USERNAME:johndoe --variable PASSWORD:J0hnD0e QuickStart.rst

In addition to user defined variables, there are some built-in variables that
are always available. These variables include `${TEMPDIR}` and `${/}` which
are used in the above example.

Using variables
---------------

Variables can be used in most places in the test data. They are most commonly
used as arguments to keywords like the following test case demonstrates.
Return values from keywords can also be assigned to variables and used later.
For example, the following `Database Should Contain` `user keyword` sets
database content to `${database}` variable and then verifies the content
using BuiltIn keyword `Should Contain`. Both library and user keywords can
return values.



Setups and teardowns
--------------------

If you want certain keywords to be executed before or after each test,
use the `Test Setup` and `Test Teardown` settings in the setting table.
Similarly you can use the `Suite Setup` and `Suite Teardown` settings to
specify keywords to be executed before or after an entire test suite.

Individual tests can also have a custom setup or teardown by using `[Setup]`
and `[Teardown]` in the test case table. This works the same way as
`[Template]` was used earlier with `data-driven tests`.

In this demo we want to make sure the database is cleared before execution
starts and that every test also clears it afterwards:

.. code:: robotframework

   *** Settings ***
    #suite Setup       open webUI  ${wsOobdURL}  ${wsSocketTimeout}
    #suite Teardown    close webUI

Using tags
----------

Robot Framework allows setting tags for test cases to give them free metadata.
Tags can be set for all test cases in a file with `Force Tags` and `Default
Tags` settings like in the table below. It is also possible to define tags
for a single test case using `[Tags]` settings like in earlier__ `User
status is stored in database` test.

__ `Using variables`_

.. code:: robotframework

    *** Settings ***
    Force Tags        quickstart
    Default Tags      example    smoke

When you look at a report after test execution, you can see that tests have
specified tags associated with them and there are also statistics generated
based on tags. Tags can also be used for many other purposes, one of the most
important being the possibility to select what tests to execute. You can try,
for example, following commands::

    pybot --include smoke QuickStart.rst
    pybot --exclude database QuickStart.rst

