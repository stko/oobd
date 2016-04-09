BRAND=public

default:
	@echo "Make targets:"
	@echo "clean    : guess what.."
	@echo "base     : compile the core"
	@echo "public   : compiles the public versiom"
	@echo "brand [-BRAND=foo] : compiles a branded version with different PGP application key"
	@echo "winstall : generates windows installer"
	@echo "run      : runs the obfusicated version"
	@echo "runapp   : runs the unmodified version"

BASE=../skds/NBprojects/Base/dist/Base.jar
APP=dist/OOBDesk.jar
STORE=store/OOBDesk.jar
PROGUARD=store/OOBDesk_Rxxx_Branded.jar
WINSTALL=OOBDesk_Setup.exe

clean:
	@echo "clean it all..."
	-rm $(APP)
	-rm $(STORE)
	-rm $(PROGUARD)
	ant -f ../skds/NBprojects/Base -Dnb.internal.action.name=clean clean
	ant -Dnb.internal.action.name=clean clean

aesbrand:
	$(eval MYBRAND:=$(BRAND))	
	@echo "branded brand" $(MYBRAND)
	(cd ../skds/base_src/org/oobd/crypt/AES/ ; cp PassPhraseProvider.java.$(BRAND) PassPhraseProvider.java)


aespublic:
	$(eval MYBRAND:=public)	
	@echo "public brand" $(MYBRAND)
	(cd ../skds/base_src/org/oobd/crypt/AES/ ; cp PassPhraseProvider.java.sample PassPhraseProvider.java)

base: $(BASE)
$(BASE):
	ant -f ../skds/NBprojects/Base -Dnb.internal.action.name=build jar 

app: $(APP)
$(APP): $(BASE)
	ant -Dnb.internal.action.name=build jar

store: $(STORE)
$(STORE): $(APP)
	ant package-for-store

proguard: $(PROGUARD)
$(PROGUARD): $(STORE)
	./doProGuard.sh


winstall: revision $(WINSTALL) 
$(WINSTALL): $(PROGUARD)
	makensis OOBD_Windows_Setup.nsi && mv $(WINSTALL) OOBDesk_$(REV).exe

runapp: $(APP)
	java -jar $(APP)

run: $(PROGUARD)
	java -jar $(PROGUARD)


revision:
	$(eval  REV=$(MYBRAND)-$(shell git describe --dirty --always)_$(shell date -I))
	@echo "rev:" $(REV)

		

public: clean aespublic revision $(WINSTALL)

brand:  clean aesbrand  revision $(WINSTALL)

all:
	$(MAKE) public
	$(MAKE) brand BRAND=$(BRAND)
