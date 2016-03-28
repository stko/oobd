# Makefile im Top-Level-Verzeichnis

include local.make



all:
	$(foreach dir,$(DIRS),make --directory=$(dir) ;)

scripts:
	$(foreach dir,$(DIRS),make --directory=$(dir) scripts;)

source:
	$(foreach dir,$(DIRS),make --directory=$(dir) source;)

clean:
	$(foreach dir,$(DIRS),make --directory=$(dir) clean;)
	
pack:
	$(foreach dir,$(DIRS),make --directory=$(dir) pack;)

epa:
	$(foreach dir,$(DIRS),make --directory=$(dir) epa;)

debug:
	$(foreach dir,$(DIRS),make --directory=$(dir) debug;)
