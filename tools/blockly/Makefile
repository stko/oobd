#  your lua- compiler (luac)
# for Windows
#CC="<drive>:<directory_to_Lua>/luac"
# for Linux
CC=luac
CFLAGS= -s
# the OOBD Lua preprocessor
# for Cygwin
#OLP=../../tools/bin/olp.exe
# for Linux
OLP=mono ../../tools/bin/olp.exe

LUASVNFILE=../../tools/lib_lua/luaSVNRevs.inc
SVNREVLUASCRIPT=$(shell ../../tools/lib_lua/echoLuaRev.sh SVNREVLUASCRIPT)
SVNREVLUALIB=$(shell (cd ../../tools/lib_lua/ ; ./echoLuaRev.sh SVNREVLUALIB) )



default:
	@echo make download : svn exports all the needed external libraries
	@echo make build : builds the webdirectory and compress the javascript files
	@echo make lualib : compiles the lua library


clean:
	-rm -r new_blockly blockly_lua_repository blockly_original_repository closure-library
	(cd lua_compile/lua-5.1.5/ ; make clean )

download:
	-rm -r new_blockly blockly_lua_repository blockly_original_repository closure-library
	# this was the original as the correct libraries where online
	# svn export https://github.com/google/blockly.git/trunk blockly_original_repository
	# svn export https://github.com/espertus/blockly-lua.git/trunk blockly_lua_repository
	# svn export https://github.com/google/closure-library.git/trunk closure-library
	# but as the originals are gone, we'll use the saved backup instead
	unzip blockly_frozen_libraries.zip


build:
	-rm -r new_blockly 
	mkdir new_blockly
	cp -r blockly_original_repository/blocks new_blockly
	cp -r blockly_original_repository/core new_blockly
	cp -r blockly_original_repository/i18n new_blockly
	cp -r blockly_original_repository/media new_blockly
	cp -r blockly_original_repository/msg new_blockly
	mkdir new_blockly/generators
	cp -r blockly_original_repository/generators/javascript* new_blockly/generators
	cp -r blockly_original_repository/generators/python* new_blockly/generators
	cp -r blockly_original_repository/generators/dart* new_blockly/generators
#	-rm -r new_blockly/generators/dart* new_blockly/generators/javascript* new_blockly/generators/python*
	cp -r oobd_build_includes/* new_blockly
	cp -r blockly_lua_repository/generators/lua* new_blockly/generators
#	cp -r blockly_lua_repository/tests/generators/unittest_lua.js web/new_blockly/tests/generators/
	(cd new_blockly ; python ../blockly_original_repository/build.py)
	./wrapCodeintoFunction.sh new_blockly/oobdluac.wrap.js lua_compile/lua-5.1.5/src/luac.js

lualib:
	echo Do not forget to run the "source  emsdk_set_env.sh" command to set the path to emcc
	(cd lua_compile/lua-5.1.5/ ; make emcc )
	echo "$(SVNREVLUALIB)" > $(LUASVNFILE)
	echo "$(SVNREVLUASCRIPT)" >> $(LUASVNFILE)
	# Do not forget to let m4 remove all the DEBUG directives...!
	$(OLP) ../lib_lua/blockly.lua  > m4.tmp
	m4 -P m4.tmp > lua.tmp
	$(OLP) ../lib_lua/blockly.lua  > lua_compile/lua-5.1.5/src/lualib.lua
	cp lua.tmp lua_compile/lua-5.1.5/src/lualib.lua
	(cd lua_compile/lua-5.1.5/src/ ; emcc -o luac.html  luac.o print.o liblua.a --embed-file ./lualib.lua@lualib.lua )

all: download lualib build
