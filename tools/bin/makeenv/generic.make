UDSLIB=../lib_protocol
LUALIB=$(OOBDROOT)/tools/lib_lua/
LUASVNFILE=$(OOBDROOT)/tools/lib_lua/luaSVNRevs.inc
SVNREVLUASCRIPT=$(shell $(OOBDROOT)/tools/lib_lua/echoLuaRev.sh SVNREVLUASCRIPT)
SVNREVLUALIB=$(shell (cd $(OOBDROOT)/tools/lib_lua/ ; ./echoLuaRev.sh SVNREVLUALIB) )

LUAS=$(shell $(OOBDROOT)/tools/bin/filelist.sh lua)

SPECS=$(shell $(OOBDROOT)/tools/bin/filelist.sh mdx)

SOURCES=$(shell $(OOBDROOT)/tools/bin/filelist.sh luasource)
LBCFILES=$(shell $(OOBDROOT)/tools/bin/filelist.sh lbc)
KCDFILES=$(shell $(OOBDROOT)/tools/bin/filelist.sh kcd)
OODBFILES=$(shell $(OOBDROOT)/tools/bin/filelist.sh dbcsv)

CFLAGS=
MDXTFLAGS=$(OOBDROOT)/tools/xmltools/mdx2opendiagx.xslt $(OOBDROOT)/tools/xmltools/opendiagx2oobd.xslt
MDXTHTMLFLAGS=$(OOBDROOT)/tools/xmltools/mdx2opendiagx.xslt $(OOBDROOT)/tools/xmltools/opendiagx2html.xslt

KCDFLAGS=tr --omit-decl  $(OOBDROOT)/tools/xmltools/kcd2rtd.xslt
KCDHTMLFLAGS=tr --omit-decl  $(OOBDROOT)/tools/xmltools/kcd2html.xslt

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
	$(OLP) $< > m4.tmp
	m4 $(OPTM4) -DDBGUSER=$(DBGUSER) -DDBGLEVEL=$(DBGLEVEL) -P m4.tmp > lua.tmp
	$(CC) $(CFLAGS) -o $@ lua.tmp

SPECSOURCES=$(SPECS:.mdx=.luasource) 
%.luasource: %.mdx
	echo mdx
	$(ODXT) $(MDXTFLAGS) $< $(@F) 
	$(ODXT) $(MDXTHTMLFLAGS) $<  $(*F).html

LUASOURCES=$(ALLSOURCES:.lua=.luasource) 
%.luasource: %.lua 
	cp -p $< $(@F) 

KCDSOURCES=$(KCDFILES:.kcd=.luasource) 
%.luasource: %.kcd 
	xmlstarlet $(KCDFLAGS) $< > $(@F) 
	xmlstarlet $(KCDHTMLFLAGS) $<  > $(*F).html

revision:
	echo "$(SVNREVLUALIB)" > $(LUASVNFILE)
	echo "$(SVNREVLUASCRIPT)" >> $(LUASVNFILE)

OODBSOURCES=$(OODBFILES:.dbcsv=.oodb) 
%.oodb: %.dbcsv 
	php $(OOBDROOT)/tools/oodbcreate/oodbCreateCLI.php  $< > $(@F) 




source: revision specs $(CUSTOMSOURCE) luas kcds oodbs
luas: $(LUASOURCES) 
specs: $(SPECSOURCES) $(MDXTFLAGS) $(CUSTOMSPECSOURCES)
kcds: $(KCDSOURCES) 
oodbs: $(OODBSOURCES)
	echo db $(OODBSOURCES)
	echo target
scripts: $(OBJECTS)

debug: setdebug scripts
setdebug:
	$(eval OPTM4=-DDEBUG)
 
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

clean: genericclean $(CUSTOMCLEAN)

genericclean:
	rm -f *.lbc *.luasource *.lbc.pgp lua.tmp m4.tmp gn.txt tmp
