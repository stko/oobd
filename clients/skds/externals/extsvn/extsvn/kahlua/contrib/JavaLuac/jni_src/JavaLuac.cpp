#include <jni.h>
#include <vector>
#include <iostream>

#include "JavaLuac.h"


extern "C" {
	#include "lua.h"
	#include "lualib.h"
	#include "lauxlib.h"

}

struct ByteBuff {
	std::vector<char> b;
} bytebuff;


int LuaWriter(lua_State *L, const void* data, size_t size, void* userData)
{
	ByteBuff *buffert = (ByteBuff*)userData;
	char *d = (char*)data;
	for (int i = 0; i< size; i++)
	{
		buffert->b.push_back(d[i]);
	}
	return 0;
	
}

static int LuaFileWriter(lua_State* L, const void* p, size_t size, void* u)
{
  return (fwrite(p,size,1,(FILE*)u)!=1) && (size!=0);
}



JNIEXPORT jbyteArray JNICALL Java_se_krka_kahluax_compiler_LuaCompiler_nativeCompile(JNIEnv *env, jobject obj, jstring srcStr, jstring srcName) 
{
	//convert the utf string to local char*
	const char *src = env->GetStringUTFChars(srcStr, 0);
	const char *name = env->GetStringUTFChars(srcName, 0);
	lua_State* L = lua_open();
	
	//luaL_loadstring(L, src);
	lua_pushfstring(L, "@%s", name);
	int status = luaL_loadbuffer(L, src, strlen(src), lua_tostring(L, -1));

	size_t size = 0;
	char* buff;
	if (status == 0) {
		ByteBuff *bytebuff = new ByteBuff();
		lua_dump(L, LuaWriter, bytebuff);

		size = bytebuff->b.size();
		buff = new char[size];
		for (int i = 0; i< size; i++)
		{
			buff[i] = bytebuff->b.at(i);

		}
		delete bytebuff;
	} else {
		const char* s = lua_tolstring(L, -1, &size);
		buff = new char[size];
		strncpy(buff, s, size); 
	}
	
	
	lua_close(L);
	
	env->ReleaseStringUTFChars(srcStr, src);
	env->ReleaseStringUTFChars(srcName, name);
	
	//create java byte array from the buffert
	jbyteArray jba = env->NewByteArray(size);
	env->SetByteArrayRegion(jba, 0, size, (jbyte *)buff);
	
	delete[] buff;
	

	return jba;
}
