./compile.sh
touch in.jar
rm -f in.jar
cd bin
zip -r ../in.jar *
cd ..
java -jar proguard.jar @applications.pro
mkdir -p obf
rm -rf obf/*
cd obf
unzip ../out.jar
cd ..
ls -la *.jar
