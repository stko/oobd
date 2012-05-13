<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <xsl:apply-templates/>

dofile("../lib_lua/oobd_lib/serial_dxm.lua")
dofile("../lib_uds/standard-uds.lua")
dofile("../lib_uds/module.lua")

</xsl:template>
<xsl:template match="oobdobx">
local moduleName = '<xsl:value-of select="./Name"/>'
local shortName = '<xsl:value-of select="./ShortName"/>'
local ModuleID = "<xsl:value-of select="./PhysAdress"/>";
local ResponseID = "<xsl:value-of select="./RespAdress"/>";
local FunctionalID = "<xsl:value-of select="./FuncAdress"/>";
local BusID = "<xsl:value-of select="./Bus"/>";

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
<xsl:for-each select="BMP">
   id0x<xsl:value-of select="HighPID"/><xsl:value-of select="LowPID"/> = { t = "<xsl:value-of select="Group"/>" , sb = {
<xsl:apply-templates select="SingleBit" />dummy=0}
},
<xsl:value-of select="usr_id"/>
</xsl:for-each>dummy=0
}

-- data for readASCPid()
local ACSIIData = {
<xsl:apply-templates select="ASCII" />dummy =0
}

-- data for readNumPid()
local NumData = {
<xsl:apply-templates select="NUM" />dummy =0
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
<xsl:variable name="corrByteNr" select="0+number(./ByteNr)"/>id0x<xsl:value-of select="../HighPID"/><xsl:value-of select="../LowPID"/><xsl:value-of select="number($corrByteNr)"/><xsl:value-of select="./BitNr"/> = { by = <xsl:value-of select="number($corrByteNr)"/> , bi = <xsl:value-of select="./BitNr"/> , t= "<xsl:value-of select="./Name"/>" , lt = "<xsl:value-of select="./LowText"/>" ,  ht = "<xsl:value-of select="./HighText"/>"},
</xsl:template>

<xsl:template match="ASCII">id0x22<xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/> = {  title = "<xsl:value-of select="./Name"/>" },
</xsl:template>


<xsl:template match="NUM">id0x22<xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/> = {  mult = <xsl:value-of select="./Resolution"/> , offset = <xsl:value-of select="./Offset"/> , len = <xsl:value-of select="./Len"/> , unit = "<xsl:value-of select="./Units"/>" , title = "<xsl:value-of select="./Name"/>"} ,
</xsl:template>

<xsl:template match="DTC">id0x<xsl:value-of select="./ID"/> =  "<xsl:value-of select="./DESCRIPTION"/>" ,
</xsl:template>

<xsl:template match="ROUTINE">id0x31<xsl:value-of select="./ID"/> =  "<xsl:value-of select="./DESCRIPTION"/>" ,
</xsl:template>


</xsl:stylesheet> 
