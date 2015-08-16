.. default-role:: code

=====================================
  Robot Framework Quick Start Guide
=====================================

Copyright © Nokia Solutions and Networks. Licensed under the
`Creative Commons Attribution 3.0 Unported`__ license.

__ http://creativecommons.org/licenses/by/3.0/

.. contents:: Table of contents:
   :local:
   :depth: 2



Test cases
==========

Workflow tests
--------------

Robot Framework test cases are created using a simple tabular syntax.

.. code:: robotframework

    *** Test Cases ***
    Dongle reports version
        when send dongle command  p 0 0 0 
        then answer should match    .*(OBD).*

.. code:: robotframework

    *** Settings ***
    Library           OperatingSystem
    Library           lib/DongleCmdLine.py



.. code:: robotframework

    *** Keywords ***

    Create valid user
        [Arguments]    ${username}    ${password}
        Create user    ${username}    ${password}
        Status should be    SUCCESS

 
    # Keywords below used by higher level tests. Notice how given/when/then/and
    # prefixes can be dropped. And this is a commend.

    A user has a valid account
        Create valid user    ${USERNAME}    ${PASSWORD}


.. code:: robotframework

    *** Variables ***
    ${port}               /tmp/DXM

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

Organizing test cases
=====================

Test suites
-----------

Collections of test cases are called test suites in Robot Framework. Every
input file which contains test cases forms a test suite. When `executing this
guide`, you see test suite `QuickStart` in the console output. This name is
got from the file name and it is also visible in reports and logs.

It is possible to organize test cases hierarchically by placing test case
files into directories and these directories into other directories. All
these directories automatically create higher level test suites that get their
names from directory names. Since test suites are just files and directories,
they are trivially placed into any version control system.

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
    test Setup       Open Port  ${port}
    test Teardown    close port

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

