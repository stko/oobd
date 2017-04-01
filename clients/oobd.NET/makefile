
default:
	@echo "Make requires the OOBD base project to be compiled first in the parallel directory ../Base"
	@echo "Make targets:"
	@echo "clean    : guess what.."
	@echo "base     : compile the core in Base.jar to Base.dll"
	@echo "exe      : compiles .Net console demo"
	@echo "java      : compiles java console demo(for debug comparision only)"
	@echo "run      : runs the .NET exe "
	@echo "runjava      : runs the java exe "

BASE=Base.dll
EXE=oobd.Net.exe
JAVA=OobdNet.class

clean:
	@echo "clean it all..."
	-rm $(BASE)
	-rm $(EXE)
	-rm $(JAVA)


base: $(BASE)
$(BASE):
	ant -f ../Base -Dnb.internal.action.name=package-for-store jar 
	ikvmc -target:library ../Base/store/Base.jar

exe: $(EXE)
$(EXE): $(BASE)
	mcs /reference:Base.dll /reference:/usr/lib/ikvm/IKVM.OpenJDK.Core.dll oobd.Net.cs

run: $(EXE)
	mono oobd.Net.exe ../oobdd/settings.json

java: $(JAVA)
$(JAVA): 
	javac -classpath .:../Base/store/Base.jar OobdNet.java

runjava: $(JAVA)
	java -classpath .:../Base/store/Base.jar OobdNet ../oobdd/settings.json


revision:
	$(eval  REV=$(MYBRAND)-$(shell git describe --dirty --always)_$(shell date -I))
	@echo "rev:" $(REV)


