# Installation

The implementation of a working LUA compiler inside a emscripten driven webpage is not trivial. This document describes how to set up the compilation process


## Prepare the Lua sources

At first download the Lua sources from [https://www.lua.org/ftp/](https://www.lua.org/ftp/). Make sure to use Lua 5.1, not 5.2 or above, as the OOBD Lua runtime only understands 5.1 objects

unpack the sources into the lua_compile directory


After unpacking, the Makefiles need to be tuned to allow compilation also with the emscripten (emcc) compiler. Here are the before/after (diff) changes:

`../lua-5.1.5/Makefile`
````
41c41
< PLATS= aix ansi bsd freebsd generic linux macosx mingw posix solaris
---
> PLATS= aix ansi bsd freebsd generic linux macosx mingw posix solaris emcc
````

`../lua-5.1.5/src/Makefile`

````
10c10
< CC= gcc
---
> CC= emcc
15c15
< LIBS= -lm $(MYLIBS)
---
> #LIBS= -lm $(MYLIBS)
41a42,44
> ALL_C= $(LUA_A) $(LUAC_T)
> comp:   $(ALL_C)
> 
116a120,123
> 
> 
> emcc:
> 	$(MAKE) comp MYCFLAGS=
````


## Getting Emscripten
Emscripten is available as Ubuntu package, while I've used the portable version directly from the Emscripten webpage because of historic reasons. If the Ubuntu package will also work is not tested yet


## Pitfalls

After strange errors after some small updates after some years of usage, it finally came out that the bytecode format generated my the emscripten lua compiler [became incompatible](https://stackoverflow.com/a/19588524) from the library bytecode which was generated through the quickscript preparation process. As workaround the compiled library was replaced by the raw source code, but the problem itself is not solved yet.