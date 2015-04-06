rem The paths in this file needs to be adapted to the current system
call "C:\Program Files\Microsoft Visual Studio 9.0\Common7\Tools\vsvars32.bat"
set luaInclude="..\lua\include"
set luaLib="..\lua\lua5.1.lib"
set jdkInclude="C:\Program Files\Java\jdk1.6.0_03\include"
set jdkIncludeWin32="C:\Program Files\Java\jdk1.6.0_03\include\win32"

cl /O2 /I %luaInclude% /I %jdkIncludeWin32% /I %jdkInclude% JavaLuac.cpp /LD /link %luaLib%
