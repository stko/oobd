UDSLIB=../lib_protocol
LUALIB=../../tools/lib_lua/
LUAS=$(shell ../../tools/bin/filelist.sh lua)

SPECS=$(shell ../../tools/bin/filelist.sh mdx)

SOURCES=$(shell ../../tools/bin/filelist.sh luasource)
LBCFILES=$(shell ../../tools/bin/filelist.sh lbc)
CFLAGS=
MDXTFLAGS=../../tools/xmltools/mdx2opendiagx.xslt ../../tools/xmltools/opendiagx2oobd.xslt
MDXTHTMLFLAGS=../../tools/xmltools/mdx2opendiagx.xslt ../../tools/xmltools/opendiagx2html.xslt

# adding external references file to complile
# do we want to compile lua files from other directories into here? Then list them in "lua_reference" as LUA_REFS=
-include lua_reference

# do we have local files to copy? Then list them in "files.inc" as CPFILES=
-include files.inc

# do we want MDX files out of the MDX-pool to be compiled? Then list them in "mdx_pool_reference" as MDX_POOL=
-include mdx_pool_reference

ALLSOURCES=$(LUA_REFS) $(LUAS)
SPECS+=$(MDX_POOL)


OBJECTS=$(SOURCES:.luasource=.lbc)
%.lbc: %.luasource 
	$(OLP) $<  > lua.tmp
	$(CC) $(CFLAGS) -o $@ lua.tmp


SPECSOURCES=$(SPECS:.mdx=.luasource) 
%.luasource: %.mdx
	echo mdx
	$(ODXT) $(MDXTFLAGS) $< $(@F) 
	$(ODXT) $(MDXTHTMLFLAGS) $<  $(*F).html


LUASOURCES=$(ALLSOURCES:.lua=.luasource) 
%.luasource: %.lua 
	cp -p $< $(@F) 

source: specs $(CUSTOMSOURCE) luas
luas: $(LUASOURCES) 
specs: $(SPECSOURCES) $(MDXTFLAGS) $(CUSTOMSPECSOURCES)

	echo target
scripts: $(OBJECTS) 
pack: 
ifdef ENABLEPGP
	export GROUPNAME=`pwd >gn.txt; sed -e 's/.*\///g' gn.txt` ; \
	export TARGETDIR=$(PACKDIR)/oobd/scripts/$$GROUPNAME ; \
	echo $$TARGETDIR ; \
	mkdir -p $$TARGETDIR ; \
	rm -f $$TARGETDIR/* ; \
	for file in $(LBCFILES) ; do \
		export basefile=$$(basename $$file .lbc) ;\
		echo $$basefile ; \
		gpg --trust-model always --yes --no-default-keyring  --keyring ../../oobd_groups.pub -r $$GROUPNAME --output $$basefile.lbc.pgp --encrypt $$basefile.lbc ; \
		cp $$basefile.lbc.pgp  $$TARGETDIR ; \
	done  ; \
	for cpfile in $(CPFILES) ; do \
		export basefile=$$(basename $$cpfile) ;\
		echo $$basefile ; \
		gpg --trust-model always --yes --no-default-keyring  --keyring ../../oobd_groups.pub -r $$GROUPNAME --output $$TARGETDIR/$$basefile.pgp --encrypt $$cpfile ; \
	done ; \
	(cd $$TARGETDIR ; md5sum * > md5sum.txt) ;\
	find $(PACKDIR)/oobd -name .svn -exec rm -rf {} \; 
else
	export GROUPNAME=`pwd >gn.txt; sed -e 's/.*\///g' gn.txt` ; \
	export TARGETDIR=$(PACKDIR)/oobd/scripts/$(GROUPNAME) ; \
	mkdir -p $$TARGETDIR ; \
	for file in $(LBCFILES) ; do \
		export basefile=$$(basename $$file .lbc) ;\
		cp $$basefile.lbc  $$TARGETDIR ; \
	done  ; \
	for cpfile in $(CPFILES) ; do \
		cp -r $$cpfile $$TARGETDIR ; \
	done ; \
	find $(PACKDIR)/oobd -name .svn -exec rm -rf {} \; 
endif



clean:
	rm -f *.lbc *.luasource *.html *.lbc.pgp lua.tmp
