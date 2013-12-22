all: OOBDFlash.class

OobdBtWrapper.class: OobdBtWrapper.java 
	javac -cp "./jars/*" OobdBtWrapper.java

OOBDFlashHandler.class: OOBDFlashHandler.java 
	javac -cp "./jars/*" OobdBtWrapper.java OOBDFlashHandler.java

OOBDFileHandler.class: OOBDFileHandler.java 
	javac -cp "./jars/*"  OOBDFileHandler.java

OOBDFlash.class: OOBDFlash.java OobdBtWrapper.class OOBDFlashHandler.class OOBDFileHandler.class
	javac -cp "./jars/*" OOBDFileHandler.java OOBDFlashHandler.java OobdBtWrapper.java OOBDFlash.java 




run:
	java -Djava.library.path=jars -cp .:jars/bluecove-2.1.0.jar:jars/bluecove-gpl-2.1.0.jar OOBDFlash


clean:
	rm *.class *.java~ 
