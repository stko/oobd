<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <xsl:apply-templates/>

dofile("../lib_lua/oobd_lib/serial_dxm.lua")
dofile("../lib_uds/standard-uds.lua")
dofile("../lib_uds/secaccess.lua")
dofile("../lib_uds/module.lua")

</xsl:template>
<xsl:template match="oobdobx">
moduleName = '<xsl:value-of select="./Name"/>'
shortName = '<xsl:value-of select="./ShortName"/>'
ModuleID = "<xsl:value-of select="./PhysAdress"/>"
ResponseID = "<xsl:value-of select="./RespAdress"/>"
FunctionalID = "<xsl:value-of select="./FuncAdress"/>"
BusID = "<xsl:value-of select="./Bus"/>";

secCodes= {}
-- optinclude("<xsl:value-of select="./ShortName"/>.seccode")

--[[
data sets for the readBMPPid() calls
to save some memory, the variable names are kept short:
 by : ByteNumber
 bi : BitNumber
 t  : Title
 sb : SingleBits
 ht : high Text
 lt : low Text
--]]

local BMPGroups = { 
<xsl:for-each select="BMP">id0x<xsl:value-of select="HighPID"/><xsl:value-of select="LowPID"/> = { t = "<xsl:value-of select="Group"/>" , sb = {
<xsl:apply-templates select="SingleBit" />dummy=0}
},
<xsl:value-of select="usr_id"/>
</xsl:for-each>dummy=0
}

-- read data for each Single value - ASCII
local ASCIIData = { 
<xsl:for-each select="SVL"><xsl:if test="ASCII">id0x22<xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/> = <xsl:apply-templates select="ASCII" /></xsl:if>
</xsl:for-each>dummy=0
}

-- read data for each Single value - Number
local NumData = {
<xsl:for-each select="SVL"><xsl:if test="BYTE">id0x22<xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/> = <xsl:apply-templates select="BYTE" /></xsl:if><xsl:if test="UNSIGNED">id0x22<xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/> = <xsl:apply-templates select="UNSIGNED" /></xsl:if>
<xsl:if test="SIGNED">id0x22<xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/> = <xsl:apply-templates select="SIGNED" /></xsl:if>
<!--
 <xsl:apply-templates select="BCD" />
 <xsl:apply-templates select="FLOAT" />
 <xsl:apply-templates select="ENUM" />
--> 
 </xsl:for-each>dummy=0
}

-- read data for each Packeted value - Number
local PacketedData = {
<!--
Loop for each PCK with different numbers of databytes is missing
-->
<xsl:for-each select="PCK">id0x22<xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/> = {  ti = "<xsl:value-of select="Name"/>", pv = {
<xsl:if test="BYTE"><xsl:apply-templates select="BYTE">
<xsl:with-param name="paramPCK" select="1" />
</xsl:apply-templates>
</xsl:if>
<xsl:if test="SIGNED"><xsl:apply-templates select="SIGNED">
<xsl:with-param name="paramPCK" select="1" />
</xsl:apply-templates>
</xsl:if>
<xsl:if test="UNSIGNED"><xsl:apply-templates select="UNSIGNED">
<xsl:with-param name="paramPCK" select="1" />
</xsl:apply-templates>
</xsl:if>
<xsl:if test="ASCII"><xsl:apply-templates select="ASCII">
<xsl:with-param name="paramPCK" select="1" />
</xsl:apply-templates>
</xsl:if>
<xsl:if test="FLOAT"><xsl:apply-templates select="FLOAT">
<xsl:with-param name="paramPCK" select="1" />
</xsl:apply-templates>
</xsl:if>
<xsl:if test="BCD"><xsl:apply-templates select="BCD">
<xsl:with-param name="paramPCK" select="1" />
</xsl:apply-templates>
</xsl:if>
<xsl:if test="ENUM"><xsl:apply-templates select="ENUM">
<xsl:with-param name="paramPCK" select="1" />
</xsl:apply-templates>
</xsl:if>dummy=0	}
},
</xsl:for-each>dummy=0
}

-- DTC list
local DTCs = {
<xsl:apply-templates select="DTCS" />dummy =0
}

-- data for selfTests()
local selftest = {
<xsl:apply-templates select="ROUTINES" />dummy =0
}

</xsl:template>

<xsl:template match="SingleBit">
<xsl:variable name="corrByteNr" select="0+number(./BytePos)"/>id0x<xsl:value-of select="../HighPID"/><xsl:value-of select="../LowPID"/><xsl:value-of select="number($corrByteNr)"/><xsl:value-of select="./BitPos"/> = { by = <xsl:value-of select="number($corrByteNr)"/> , bi = <xsl:value-of select="./BitPos"/><xsl:if test="../OutputSession">, session = "<xsl:value-of select="../OutputSession"/>"</xsl:if><xsl:if test="../OutputService">, service = "<xsl:value-of select="../OutputService"/>"</xsl:if>, t= "<xsl:value-of select="./Name"/>" , lt = "<xsl:value-of select="./LowText"/>" ,  ht = "<xsl:value-of select="./HighText"/>"},
</xsl:template>

<xsl:template match="UNSIGNED"> <xsl:param name="paramPCK" /><xsl:if test="$paramPCK = 1">id0x<xsl:value-of select="./BytePos" /><xsl:value-of select="./BitPos" /> = </xsl:if> {  pos = <xsl:value-of select="./BytePos"/> , len = <xsl:value-of select="./ByteNr"/> , mult = <xsl:value-of select="./Resolution"/> , offset = <xsl:value-of select="./Offset"/> , unit = "<xsl:value-of select="./Units"/>" , dtype = "UNSIGNED" , title = "<xsl:value-of select="./Name"/>"} ,
</xsl:template>

<xsl:template match="SIGNED"> <xsl:param name="paramPCK" /><xsl:if test="$paramPCK = 1">id0x<xsl:value-of select="./BytePos" /><xsl:value-of select="./BitPos" /> = </xsl:if> {  pos = <xsl:value-of select="./BytePos"/> , len = <xsl:value-of select="./ByteNr"/> , mult = <xsl:value-of select="./Resolution"/> , offset = <xsl:value-of select="./Offset"/> , unit = "<xsl:value-of select="./Units"/>" , dtype = "SIGNED", title = "<xsl:value-of select="./Name"/>"} ,
</xsl:template>

<xsl:template match="FLOAT"> <xsl:param name="paramPCK" /><xsl:if test="$paramPCK = 1">id0x<xsl:value-of select="./BytePos" /><xsl:value-of select="./BitPos" /> = </xsl:if> {  pos = <xsl:value-of select="./BytePos"/> , len = <xsl:value-of select="./ByteNr"/> , mult = <xsl:value-of select="./Resolution"/> , offset = <xsl:value-of select="./Offset"/> , unit = "<xsl:value-of select="./Units"/>" , dtype = "FLOAT", title = "<xsl:value-of select="./Name"/>"} ,
</xsl:template>

<xsl:template match="BCD"> <xsl:param name="paramPCK" /><xsl:if test="$paramPCK = 1">id0x<xsl:value-of select="./BytePos" /><xsl:value-of select="./BitPos" /> = </xsl:if> {  pos = <xsl:value-of select="./BytePos"/> , len = <xsl:value-of select="./ByteNr"/> , mult = <xsl:value-of select="./Resolution"/> , offset = <xsl:value-of select="./Offset"/> , unit = "<xsl:value-of select="./Units"/>" , dtype = "BCD", title = "<xsl:value-of select="./Name"/>"} ,
</xsl:template>

<xsl:template match="ASCII"> <xsl:param name="paramPCK" /><xsl:if test="$paramPCK = 1">id0x<xsl:value-of select="./BytePos" /><xsl:value-of select="./BitPos" /> = </xsl:if> {  pos = <xsl:value-of select="./BytePos"/> , len = <xsl:value-of select="./ByteNr"/> , dtype = "ASCII", title = "<xsl:value-of select="./Name"/>" },
</xsl:template>

<xsl:template match="BYTE"> <xsl:param name="paramPCK" /><xsl:if test="$paramPCK = 1">id0x<xsl:value-of select="./BytePos" /><xsl:value-of select="./BitPos" /> = </xsl:if> {  pos = <xsl:value-of select="./BytePos"/> , len = <xsl:value-of select="./ByteNr"/> , mult = <xsl:value-of select="./Resolution"/> , offset = <xsl:value-of select="./Offset"/> , unit = "<xsl:value-of select="./Units"/>" , dtype = "BYTE", title = "<xsl:value-of select="./Name"/>"} ,
</xsl:template>

<xsl:template match="ENUM"> <xsl:param name="paramPCK" /><xsl:variable name="corrByteNr" select="0+number(./SingleBit/ByteNr)" />
<xsl:if test="$paramPCK = 1">id0x<xsl:value-of select="number($corrByteNr)" /><xsl:value-of select="./SingleBit/BitPos" /></xsl:if> = { by = <xsl:value-of select="number($corrByteNr)"/> , bi = <xsl:value-of select="./SingleBit/BitNr"/> , t= "<xsl:value-of select="./SingleBit/Name"/>" , lt = "<xsl:value-of select="./SingleBit/LowText"/>" ,  ht = "<xsl:value-of select="./SingleBit/HighText"/>" , dtype = "ENUM"} ,
</xsl:template>

<!--
<xsl:template match="ENUM">
<xsl:variable name="corrByteNr" select="0+number(./SingleBit/BytePos)"/>id0x<xsl:value-of select="number($corrByteNr)"/><xsl:value-of select="./SingleBit/BitPos"/> = { by = <xsl:value-of select="number($corrByteNr)"/> , bi = <xsl:value-of select="./SingleBit/BitNr"/> , t= "<xsl:value-of select="./SingleBit/Name"/>" , lt = "<xsl:value-of select="./SingleBit/LowText"/>" ,  ht = "<xsl:value-of select="./SingleBit/HighText"/>"},
</xsl:template>
-->
<xsl:template match="DTC">id0x<xsl:value-of select="./ID"/> =  "<xsl:value-of select="./DESCRIPTION"/>" ,
</xsl:template>

<xsl:template match="ROUTINE">id0x31<xsl:value-of select="./ID"/> =  "<xsl:value-of select="./DESCRIPTION"/>" ,
</xsl:template>

</xsl:stylesheet> 