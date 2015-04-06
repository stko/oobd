#include <jni.h>

/* Header for class se_spaced_shared_lua_LuaCompiler */

#ifndef _Included_se_krka_kahluax_compiler_LuaCompiler
#define _Included_se_krka_kahluax_compiler_LuaCompiler
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     se_spaced_shared_lua_LuaCompiler
 * Method:    compile
 * Signature: (Ljava/lang/String;)[B
 */
JNIEXPORT jbyteArray JNICALL Java_se_krka_kahluax_compiler_LuaCompiler_nativeCompile
  (JNIEnv *, jobject, jstring, jstring);



#ifdef __cplusplus
}


#endif
#endif
