default:


download:
	-rm -r new_blockly blockly_lua_repository blockly_original_repository
	svn export https://github.com/google/blockly.git/trunk blockly_original_repository
	svn export https://github.com/espertus/blockly-lua.git/trunk blockly_lua_repository
	svn export https://github.com/google/closure-library.git/trunk closure-library

build:
	-rm -r new_blockly 
	mkdir new_blockly
	cp -r blockly_original_repository/* new_blockly
#	-rm -r new_blockly/generators/dart* new_blockly/generators/javascript* new_blockly/generators/python*
	cp -r oobd_build_includes/* new_blockly
	cp -r blockly_lua_repository/generators/lua* new_blockly/generators
#	cp -r blockly_lua_repository/tests/generators/unittest_lua.js web/new_blockly/tests/generators/
	(cd new_blockly ; python build.py)
	./wrapCodeintoFunction.sh new_blockly/oobdluac.wrap.js lua_compile/lua-5.1.5/src/luac.js
