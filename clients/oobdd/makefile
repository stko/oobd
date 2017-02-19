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

BASE=../Base/dist/Base.jar
APP=dist/oobdd.jar
STORE=store/oobdd.jar
PROGUARD=store/oobdd_branded.jar
WINSTALL=oobdd_Setup.exe
ZIPNAME=oobdd.zip
HTMLLIBPARENTDIR=/home/steffen/Desktop/workcopies/oobd/tools/

clean:
	@echo "clean it all..."
	-rm $(APP)
	-rm $(STORE)
	-rm $(PROGUARD)
	ant -f ../Base -Dnb.internal.action.name=clean clean
	ant -Dnb.internal.action.name=clean clean

aesbrand:
	$(eval MYBRAND:=$(BRAND))	
	@echo "branded brand" $(MYBRAND)
	(cd ../Base/src/org/oobd/crypt/AES/ ; cp PassPhraseProvider.java.$(BRAND) PassPhraseProvider.java)


aespublic:
	$(eval MYBRAND:=public)	
	@echo "public brand" $(MYBRAND)
	(cd ../Base/src/org/oobd/crypt/AES/ ; cp PassPhraseProvider.java.public PassPhraseProvider.java)

base: $(BASE)
$(BASE):
	ant -f ../Base -Dnb.internal.action.name=build jar 

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
	makensis oobdd_Windows_Setup.nsi && mv $(WINSTALL) OOBDesk_$(REV).exe

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
 
# this mysterious ONESHELL is given as solution to mahre heredocs (<<) work in a makefile. I've no clue what is does, but now it works.. http://stackoverflow.com/a/35517304
 .ONESHELL:
 
zip: 
	-rm $(ZIPNAME)
	cat <<- 'EOT' > localsettings.json
	{
	  "Bluetooth": {
	    "ServerProxyPort": 0,
	    "SerialPort": "/tmp/DXM",
	    "ConnectServerURL": "",
	    "ServerProxyHost": "",
	    "SerialPort_lock": false
	  },
	  "UIHandler": "WsUIHandler",
	  "UIHandler_lock": true,
	  "Password_lock": true,
	  "ConnectType": "Bluetooth",
	  "LibraryDir": "lib_html",
	  "LibraryDir_lock": true,
	  "ScriptDir": "scripts",
	  "Kadaver": {
	    "ServerProxyPort": 0,
	    "SerialPort": "",
	    "ConnectServerURL": "wss://oobd.luxen.de/websockssl/",
	    "ServerProxyPort_lock": false,
	    "ServerProxyHost": "",
	    "ServerProxyHost_lock": false,
	    "SerialPort_lock": false,
	    "ConnectServerURL_lock": false
	  },
	  "Telnet": {
	    "SerialPort": "localhost:1234",
	    "ConnectServerURL": "wss://oobd.luxen.de/websockssl/",
	    "SerialPort_lock": false,
	    "ConnectServerURL_lock": false
	  },
	  "PGPEnabled_lock": false,
	  "ScriptDir_lock": false,
	  "PGPEnabled": true,
	  "ConnectType_lock": true,
	  "Password": "bill"
	}
	EOT
	echo "java -jar ./oobdd.jar --settings localsettings.json" >oobdd.sh
	echo "java -jar oobdd.jar --settings localsettings.json" >oobdd.bat
	chmod a+x oobdd.sh oobdd.bat
	zip $(ZIPNAME) localsettings.json oobdd.sh  oobdd.bat
	## Not writing the whole commands in one single line means wasting f##cking hours in fault finding ##*§$!
	FULLZIPPATH=$$(readlink -f $(ZIPNAME)) ; \
	(cd store ;  zip $$FULLZIPPATH oobdd.jar  ) ; \
	echo 1 ; pwd ; echo 2 ; echo $(HTMLLIBPARENTDIR) ; echo 3 ; cd $(HTMLLIBPARENTDIR) ; pwd ;  echo 4 ; echo $$FULLZIPPATH ; echo 5 ; zip -r $$FULLZIPPATH  lib_html
	rm oobdd.sh oobdd.bat localsettings.json

