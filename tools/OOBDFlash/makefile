all: OOBDFlash.class

jar: OOBDFlash.jar

OOBDFlash.jar: OOBDFlash.class MANIFEST.MF 
	jar -cvmf MANIFEST.MF OOBDFlash.jar *.class 
#	jar -cvmf MANIFEST.MF OOBDFlash.jar *.class `find jars -not -path "*/.svn/*" -not -type d`




OobdBtWrapper.class: OobdBtWrapper.java 
	javac -cp "./jars/*" OobdBtWrapper.java

OOBDFlashHandler.class: OOBDFlashHandler.java 
	javac -cp "./jars/*" OobdBtWrapper.java OOBDFlashHandler.java

OOBDFileHandler.class: OOBDFileHandler.java 
	javac -cp "./jars/*"  OOBDFileHandler.java

YModem1K.class: YModem1K.java 
	javac -cp "./jars/*"  YModem1K.java

OOBDFlash.class: OOBDFlash.java OobdBtWrapper.class OOBDFlashHandler.class OOBDFileHandler.class YModem1K.class
	javac -cp "./jars/*" OOBDFileHandler.java OOBDFlashHandler.java OobdBtWrapper.java YModem1K.java OOBDFlash.java 




run:
	java -Djava.library.path=jars -cp .:jars/bluecove-2.1.0.jar:jars/bluecove-gpl-2.1.0.jar OOBDFlash

runjar:
	java -jar OOBDFlash.jar

zip: jar
	-rm OOBDFlash.zip
	zip OOBDFlash.zip OOBDFlash.jar jars/* OOBDFlash.props runOOBDFlash.bat runOOBDFlash.sh

clean:
	-rm *.class *.java~ 
