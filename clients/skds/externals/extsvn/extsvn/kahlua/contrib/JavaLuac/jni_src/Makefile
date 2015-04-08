all:
	echo Choose platform - ex: make linux64 or make linux32 or make osx

linux64:
	g++ -shared -fPIC \
	-I/usr/lib/jvm/java-6-sun/include \
	-I/usr/lib/jvm/java-6-sun/include/linux \
	-I../lua/include \
	-o ../bin/libJavaLuac_linux64.so JavaLuac.cpp ../lua/liblua5.1_x86_64.a

linux32:
	g++ -shared -fPIC \
	-I/usr/lib/jvm/java-6-sun/include \
	-I/usr/lib/jvm/java-6-sun/include/linux \
	-I../lua/include \
	-o ../bin/libJavaLuac_linux32.so JavaLuac.cpp ../lua/liblua5.1_x86_32.a

osx:
	g++ -shared -fPIC \
	-I/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Home/include \
	-I../lua/include \
	-o ../bin/libJavaLuac_osx64.jnilib JavaLuac.cpp



