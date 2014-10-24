UDSLIB=../lib_protocol
LUALIB=../../tools/lib_lua/
LUAS=$(shell ../../tools/bin/filelist.sh lua)
ifdef ENABLEODX
	SPECS=$(shell ../../tools/bin/filelist.sh odx-d)
else
	SPECS=$(shell ../../tools/bin/filelist.sh mdx)
endif
SOURCES=$(shell ../../tools/bin/filelist.sh luasource)
LBCFILES=$(shell ../../tools/bin/filelist.sh lbc)
KCDFILES=$(shell ../../tools/bin/filelist.sh kcd)

CFLAGS=
ifdef ENABLEODX
	ifdef ENABLEKWP2000
		ODXTFLAGS=../../tools/xslt-files/input/odx-KWP.xslt ../../../tools/xslt-files/output/oobd-KWP.xslt
		ODXTHTMLFLAGS=../../tools/xslt-files/input/odx-KWP.xslt ../../../tools/xslt-files/output/html.xslt
	else
		ODXTFLAGS=../../tools/xslt-files/input/odx-UDS.xslt ../../tools/xslt-files/output/oobd-UDS.xslt
		ODXTHTMLFLAGS=../../tools/xslt-files/input/odx-UDS.xslt ../../tools/xslt-files/output/html.xslt
	endif
else
	ODXTFLAGS=../../tools/xmltools/mdx2opendiagx.xslt ../../tools/xmltools/opendiagx2oobd.xslt
	ODXTHTMLFLAGS=../../tools/xmltools/mdx2opendiagx.xslt ../../tools/xmltools/opendiagx2html.xslt
endif

KCDFLAGS=tr --omit-decl  ../../tools/xmltools/kcd2rtd.xslt
KCDHTMLFLAGS=tr --omit-decl  ../../tools/xmltools/kcd2html.xslt

# adding external references file to complile
# do we want to compile lua files from other directories into here? Then list them in "lua_reference" as LUA_REFS=
-include lua_reference

# do we have local files to copy? Then list them in "files.inc" as CPFILES=
-include files.inc

# do we want ODX/MDX files out of the ODX/MDX-pool to be compiled? Then list them in "mdx_pool_reference" or "odx_pool_reference" as MDX_POOL=
#-include mdx_pool_reference
-include odx_pool_reference

ALLSOURCES=$(LUA_REFS) $(LUAS)

OBJECTS=$(SOURCES:.luasource=.lbc)
%.lbc: %.luasource 
	$(OLP) $<  > lua.tmp
	$(CC) $(CFLAGS) -o $@ lua.tmp

ifdef ENABLEODX
SPECSOURCES=$(SPECS:.odx-d=.luasource) 
%.luasource: %.odx-d
	echo odx
	$(ODXT) $(ODXTFLAGS) $< $(@F) 
	$(ODXT) $(ODXTHTMLFLAGS) $<  $(*F).html
else
	SPECSOURCES=$(SPECS:.mdx=.luasource) 
%.luasource: %.mdx
	echo mdx
	$(ODXT) $(ODXTFLAGS) $< $(@F) 
	$(ODXT) $(ODXTHTMLFLAGS) $<  $(*F).html
endif

#LUASOURCES=$(ALLSOURCES:.lua=.luasource) 
LUASOURCES:=$(notdir  $(ALLSOURCES:.lua=.luasource))
%.luasource: %.lua 
	cp -p $< $(@F) 

KCDSOURCES=$(KCDFILES:.kcd=.luasource) 
%.luasource: %.kcd 
	xmlstarlet $(KCDFLAGS) $< > $(@F) 
	xmlstarlet $(KCDHTMLFLAGS) $<  > $(*F).html

source: specs $(CUSTOMSOURCE) luas kcds
luas: $(LUASOURCES) 
specs: $(SPECSOURCES) $(ODXTFLAGS) $(CUSTOMSPECSOURCES)
kcds: $(KCDSOURCES) 
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
