
default:
	@echo "Make requires the OOBD base project to be compiled first in the parallel directory ../Base"
	@echo "Make targets:"
	@echo "clean    : guess what.."
	@echo "base     : compile the core in Base.jar to Base.dll"
	@echo "exe      : compiles .Net console demo"
	@echo "run      : runs the .NET exr "

BASE=Base.dll
EXE=oobd.Net.exe


clean:
	@echo "clean it all..."
	-rm $(BASE)
	-rm $(EXE)


base: $(BASE)
$(BASE):
	ikvmc -target:library ../Base/store/Base.jar

exe: $(EXE)
$(EXE): $(BASE)
	mcs /reference:Base.dll /reference:/usr/lib/ikvm/IKVM.OpenJDK.Core.dll oobd.Net.cs

run: $(EXE)
	mono oobd.Net.exe ../oobdd/settings.json


revision:
	$(eval  REV=$(MYBRAND)-$(shell git describe --dirty --always)_$(shell date -I))
	@echo "rev:" $(REV)


