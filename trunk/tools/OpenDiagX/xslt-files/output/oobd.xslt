<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <xsl:apply-templates/>

dofile("../lib_lua/oobd_lib/serial_dxm.lua")
dofile("../lib_lua/luabit/bit.lua")
dofile("../lib_protocol/standard-uds.lua")
dofile("../lib_protocol/secaccess.lua")
dofile("../lib_protocol/module-uds.lua")

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
<xsl:for-each select="BMP">id0x<xsl:value-of select="HighPID"/><xsl:value-of select="LowPID"/> = { t = "<xsl:value-of select="Group"/>"<xsl:if test="AccessParams/AccessParamRead/ServiceID">, sev_r = "<xsl:value-of select="AccessParams/AccessParamRead/ServiceID"/>"</xsl:if><xsl:if test="AccessParams/AccessParamRead/SessionIDs">, ses_r ="<xsl:for-each select="AccessParams/AccessParamRead/SessionIDs/SessionID"><xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if><xsl:if test="AccessParams/AccessParamWrite/ServiceID">, sev_w = "<xsl:value-of select="AccessParams/AccessParamWrite/ServiceID"/>"</xsl:if><xsl:if test="AccessParams/AccessParamWrite/SessionIDs">, ses_w ="<xsl:for-each select="AccessParams/AccessParamWrite/SessionIDs/SessionID"><xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if><xsl:if test="AccessParams/AccessParamIOControl/ServiceID">, sev_ioc = "<xsl:value-of select="AccessParams/AccessParamIOControl/ServiceID"/>"</xsl:if><xsl:if test="AccessParams/AccessParamIOControl/SessionIDs">, ses_ioc ="<xsl:for-each select="AccessParams/AccessParamIOControl/SessionIDs/SessionID"><xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if><xsl:if test="AccessParams/AccessParamIOControl/IOControlParams">, iocp ="<xsl:for-each select="AccessParams/AccessParamIOControl/IOControlParams/IOControlParam">0<xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if>, sb = {
<xsl:apply-templates select="SingleBit" />dummy=0}
},
<xsl:value-of select="usr_id"/>
</xsl:for-each>dummy=0
}

-- read data for each Single value - ASCII
local ASCIIData = { 
<xsl:for-each select="SVL"><xsl:if test="ASCII">id0x<xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/> = { t = "<xsl:value-of select="Name"/>"<xsl:if test="AccessParams/AccessParamRead/ServiceID">, sev_r = "<xsl:value-of select="AccessParams/AccessParamRead/ServiceID"/>"</xsl:if><xsl:if test="AccessParams/AccessParamRead/SessionIDs">, ses_r ="<xsl:for-each select="AccessParams/AccessParamRead/SessionIDs/SessionID"><xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if><xsl:if test="AccessParams/AccessParamWrite/ServiceID">, sev_w = "<xsl:value-of select="AccessParams/AccessParamWrite/ServiceID"/>"</xsl:if><xsl:if test="AccessParams/AccessParamWrite/SessionIDs">, ses_w ="<xsl:for-each select="AccessParams/AccessParamWrite/SessionIDs/SessionID"><xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if><xsl:if test="AccessParams/AccessParamIOControl/ServiceID">, sev_ioc = "<xsl:value-of select="AccessParams/AccessParamIOControl/ServiceID"/>"</xsl:if><xsl:if test="AccessParams/AccessParamIOControl/SessionIDs">, ses_ioc ="<xsl:for-each select="AccessParams/AccessParamIOControl/SessionIDs/SessionID"><xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if><xsl:if test="AccessParams/AccessParamIOControl/IOControlParams">, iocp ="<xsl:for-each select="AccessParams/AccessParamIOControl/IOControlParams/IOControlParam">0<xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if>, av = {
<xsl:apply-templates select="ASCII" />dummy=0}
},
</xsl:if>
</xsl:for-each>dummy=0
}

-- read data for each Single value - Number
local NumData = {
<xsl:for-each select="SVL">
<xsl:if test="BYTE">id0x<xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/> = { t = "<xsl:value-of select="Name"/>"<xsl:if test="AccessParams/AccessParamRead/ServiceID">, sev_r = "<xsl:value-of select="AccessParams/AccessParamRead/ServiceID"/>"</xsl:if><xsl:if test="AccessParams/AccessParamRead/SessionIDs">, ses_r ="<xsl:for-each select="AccessParams/AccessParamRead/SessionIDs/SessionID"><xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if><xsl:if test="AccessParams/AccessParamWrite/ServiceID">, sev_w = "<xsl:value-of select="AccessParams/AccessParamWrite/ServiceID"/>"</xsl:if><xsl:if test="AccessParams/AccessParamWrite/SessionIDs">, ses_w ="<xsl:for-each select="AccessParams/AccessParamWrite/SessionIDs/SessionID"><xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if><xsl:if test="AccessParams/AccessParamIOControl/ServiceID">, sev_ioc = "<xsl:value-of select="AccessParams/AccessParamIOControl/ServiceID"/>"</xsl:if><xsl:if test="AccessParams/AccessParamIOControl/SessionIDs">, ses_ioc ="<xsl:for-each select="AccessParams/AccessParamIOControl/SessionIDs/SessionID"><xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if><xsl:if test="AccessParams/AccessParamIOControl/IOControlParams">, iocp ="<xsl:for-each select="AccessParams/AccessParamIOControl/IOControlParams/IOControlParam">0<xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if>, sv = {
<xsl:apply-templates select="BYTE" />dummy=0}
},
</xsl:if>
<xsl:if test="UNSIGNED">id0x<xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/> = { t = "<xsl:value-of select="Name"/>"<xsl:if test="AccessParams/AccessParamRead/ServiceID">, sev_r = "<xsl:value-of select="AccessParams/AccessParamRead/ServiceID"/>"</xsl:if><xsl:if test="AccessParams/AccessParamRead/SessionIDs">, ses_r ="<xsl:for-each select="AccessParams/AccessParamRead/SessionIDs/SessionID"><xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if><xsl:if test="AccessParams/AccessParamWrite/ServiceID">, sev_w = "<xsl:value-of select="AccessParams/AccessParamWrite/ServiceID"/>"</xsl:if><xsl:if test="AccessParams/AccessParamWrite/SessionIDs">, ses_w ="<xsl:for-each select="AccessParams/AccessParamWrite/SessionIDs/SessionID"><xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if><xsl:if test="AccessParams/AccessParamIOControl/ServiceID">, sev_ioc = "<xsl:value-of select="AccessParams/AccessParamIOControl/ServiceID"/>"</xsl:if><xsl:if test="AccessParams/AccessParamIOControl/SessionIDs">, ses_ioc ="<xsl:for-each select="AccessParams/AccessParamIOControl/SessionIDs/SessionID"><xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if><xsl:if test="AccessParams/AccessParamIOControl/IOControlParams">, iocp ="<xsl:for-each select="AccessParams/AccessParamIOControl/IOControlParams/IOControlParam">0<xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if>, sv = {
<xsl:apply-templates select="UNSIGNED" />dummy=0}
},
</xsl:if>
<xsl:if test="SIGNED">id0x<xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/> = { t = "<xsl:value-of select="Name"/>"<xsl:if test="AccessParams/AccessParamRead/ServiceID">, sev_r = "<xsl:value-of select="AccessParams/AccessParamRead/ServiceID"/>"</xsl:if><xsl:if test="AccessParams/AccessParamRead/SessionIDs">, ses_r ="<xsl:for-each select="AccessParams/AccessParamRead/SessionIDs/SessionID"><xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if><xsl:if test="AccessParams/AccessParamWrite/ServiceID">, sev_w = "<xsl:value-of select="AccessParams/AccessParamWrite/ServiceID"/>"</xsl:if><xsl:if test="AccessParams/AccessParamWrite/SessionIDs">, ses_w ="<xsl:for-each select="AccessParams/AccessParamWrite/SessionIDs/SessionID"><xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if><xsl:if test="AccessParams/AccessParamIOControl/ServiceID">, sev_ioc = "<xsl:value-of select="AccessParams/AccessParamIOControl/ServiceID"/>"</xsl:if><xsl:if test="AccessParams/AccessParamIOControl/SessionIDs">, ses_ioc ="<xsl:for-each select="AccessParams/AccessParamIOControl/SessionIDs/SessionID"><xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if><xsl:if test="AccessParams/AccessParamIOControl/IOControlParams">, iocp ="<xsl:for-each select="AccessParams/AccessParamIOControl/IOControlParams/IOControlParam">0<xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if>, sv = {
<xsl:apply-templates select="SIGNED" />dummy=0}
},
</xsl:if>
<xsl:if test="ENUM">id0x<xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/> = { t = "<xsl:value-of select="Name"/>"<xsl:if test="AccessParams/AccessParamRead/ServiceID">, sev_r = "<xsl:value-of select="AccessParams/AccessParamRead/ServiceID"/>"</xsl:if><xsl:if test="AccessParams/AccessParamRead/SessionIDs">, ses_r ="<xsl:for-each select="AccessParams/AccessParamRead/SessionIDs/SessionID"><xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if><xsl:if test="AccessParams/AccessParamWrite/ServiceID">, sev_w = "<xsl:value-of select="AccessParams/AccessParamWrite/ServiceID"/>"</xsl:if><xsl:if test="AccessParams/AccessParamWrite/SessionIDs">, ses_w ="<xsl:for-each select="AccessParams/AccessParamWrite/SessionIDs/SessionID"><xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if><xsl:if test="AccessParams/AccessParamIOControl/ServiceID">, sev_ioc = "<xsl:value-of select="AccessParams/AccessParamIOControl/ServiceID"/>"</xsl:if><xsl:if test="AccessParams/AccessParamIOControl/SessionIDs">, ses_ioc ="<xsl:for-each select="AccessParams/AccessParamIOControl/SessionIDs/SessionID"><xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if><xsl:if test="AccessParams/AccessParamIOControl/IOControlParams">, iocp ="<xsl:for-each select="AccessParams/AccessParamIOControl/IOControlParams/IOControlParam">0<xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if>, sv = {
<xsl:apply-templates select="ENUM" />dummy=0}
},
</xsl:if>
</xsl:for-each>dummy=0
}

-- read data for each Packeted value - Number
local PacketedData = {
<!--
Loop for each PCK with different numbers of databytes is missing
-->
<xsl:for-each select="PCK">id0x<xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/> = { t = "<xsl:value-of select="Name"/>"<xsl:if test="AccessParams/AccessParamRead/ServiceID">, sev_r = "<xsl:value-of select="AccessParams/AccessParamRead/ServiceID"/>"</xsl:if><xsl:if test="AccessParams/AccessParamRead/SessionIDs">, ses_r ="<xsl:for-each select="AccessParams/AccessParamRead/SessionIDs/SessionID"><xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if><xsl:if test="AccessParams/AccessParamWrite/ServiceID">, sev_w = "<xsl:value-of select="AccessParams/AccessParamWrite/ServiceID"/>"</xsl:if><xsl:if test="AccessParams/AccessParamWrite/SessionIDs">, ses_w ="<xsl:for-each select="AccessParams/AccessParamWrite/SessionIDs/SessionID"><xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if><xsl:if test="AccessParams/AccessParamIOControl/ServiceID">, sev_ioc = "<xsl:value-of select="AccessParams/AccessParamIOControl/ServiceID"/>"</xsl:if><xsl:if test="AccessParams/AccessParamIOControl/SessionIDs">, ses_ioc ="<xsl:for-each select="AccessParams/AccessParamIOControl/SessionIDs/SessionID"><xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if><xsl:if test="AccessParams/AccessParamIOControl/IOControlParams">, iocp ="<xsl:for-each select="AccessParams/AccessParamIOControl/IOControlParams/IOControlParam">0<xsl:value-of select="current()"/>;</xsl:for-each>"</xsl:if>, pv = {
<xsl:if test="BYTE"><xsl:apply-templates select="BYTE" /></xsl:if>
<xsl:if test="SIGNED"><xsl:apply-templates select="SIGNED" /></xsl:if>
<xsl:if test="UNSIGNED"><xsl:apply-templates select="UNSIGNED" /></xsl:if>
<xsl:if test="ASCII"><xsl:apply-templates select="ASCII" /></xsl:if>
<xsl:if test="FLOAT"><xsl:apply-templates select="FLOAT" /></xsl:if>
<xsl:if test="BCD"><xsl:apply-templates select="BCD" /></xsl:if>
<xsl:if test="ENUM"><xsl:apply-templates select="ENUM" /></xsl:if>dummy=0}
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

<xsl:template match="UNSIGNED">
<xsl:variable name="corrByteNr" select="0+number(./BytePos)"/>id0x<xsl:value-of select="../HighPID"/><xsl:value-of select="../LowPID"/><xsl:value-of select="number($corrByteNr)"/><xsl:value-of select="./BitPos"/> = {  bpos = <xsl:value-of select="./BytePos"/> , blen = <xsl:value-of select="./ByteNr"/> , Bpos = <xsl:value-of select="./BitPos"/> , Blen = <xsl:value-of select="./BitNr"/> , mult = <xsl:value-of select="./Resolution"/> , offset = <xsl:value-of select="./Offset"/> , unit = "<xsl:value-of select="./Units"/>" , dtype = "UNSIGNED" , t = "<xsl:value-of select="./Name"/>"} ,
</xsl:template>

<xsl:template match="SIGNED">
<xsl:variable name="corrByteNr" select="0+number(./BytePos)"/>id0x<xsl:value-of select="../HighPID"/><xsl:value-of select="../LowPID"/><xsl:value-of select="number($corrByteNr)"/><xsl:value-of select="./BitPos"/> = {  bpos = <xsl:value-of select="./BytePos"/> , blen = <xsl:value-of select="./ByteNr"/> , Bpos = <xsl:value-of select="./BitPos"/> , Blen = <xsl:value-of select="./BitNr"/> , mult = <xsl:value-of select="./Resolution"/> , offset = <xsl:value-of select="./Offset"/> , unit = "<xsl:value-of select="./Units"/>" , dtype = "SIGNED", t = "<xsl:value-of select="./Name"/>"} ,
</xsl:template>

<xsl:template match="FLOAT">
<xsl:variable name="corrByteNr" select="0+number(./BytePos)"/>id0x<xsl:value-of select="../HighPID"/><xsl:value-of select="../LowPID"/><xsl:value-of select="number($corrByteNr)"/><xsl:value-of select="./BitPos"/> = {  bpos = <xsl:value-of select="./BytePos"/> , blen = <xsl:value-of select="./ByteNr"/> , Bpos = <xsl:value-of select="./BitPos"/> , Blen = <xsl:value-of select="./BitNr"/> , mult = <xsl:value-of select="./Resolution"/> , offset = <xsl:value-of select="./Offset"/> , unit = "<xsl:value-of select="./Units"/>" , dtype = "FLOAT", t = "<xsl:value-of select="./Name"/>"} ,
</xsl:template>

<xsl:template match="BCD">
<xsl:variable name="corrByteNr" select="0+number(./BytePos)"/>id0x<xsl:value-of select="../HighPID"/><xsl:value-of select="../LowPID"/><xsl:value-of select="number($corrByteNr)"/><xsl:value-of select="./BitPos"/> = {  bpos = <xsl:value-of select="./BytePos"/> , blen = <xsl:value-of select="./ByteNr"/> , Bpos = <xsl:value-of select="./BitPos"/> , Blen = <xsl:value-of select="./BitNr"/> , mult = <xsl:value-of select="./Resolution"/> , offset = <xsl:value-of select="./Offset"/> , unit = "<xsl:value-of select="./Units"/>" , dtype = "BCD", t = "<xsl:value-of select="./Name"/>"} ,
</xsl:template>

<xsl:template match="ASCII">
<xsl:variable name="corrByteNr" select="0+number(./BytePos)"/>id0x<xsl:value-of select="../HighPID"/><xsl:value-of select="../LowPID"/><xsl:value-of select="number($corrByteNr)"/><xsl:value-of select="./BitPos"/> = {  bpos = <xsl:value-of select="./BytePos"/> , blen = <xsl:value-of select="./ByteNr"/> , Bpos = <xsl:value-of select="./BitPos"/> , Blen = <xsl:value-of select="./BitNr"/> , dtype = "ASCII", t = "<xsl:value-of select="./Name"/>" },
</xsl:template>

<xsl:template match="BYTE">
<xsl:variable name="corrByteNr" select="0+number(./BytePos)"/>id0x<xsl:value-of select="../HighPID"/><xsl:value-of select="../LowPID"/><xsl:value-of select="number($corrByteNr)"/><xsl:value-of select="./BitPos"/> = {  bpos = <xsl:value-of select="./BytePos"/> , blen = <xsl:value-of select="./ByteNr"/> , Bpos = <xsl:value-of select="./BitPos"/> , Blen = <xsl:value-of select="./BitNr"/> , mult = <xsl:value-of select="./Resolution"/> , offset = <xsl:value-of select="./Offset"/> , unit = "<xsl:value-of select="./Units"/>" , dtype = "BYTE", t = "<xsl:value-of select="./Name"/>"} ,
</xsl:template>

<xsl:template match="ENUM">
<xsl:variable name="corrByteNr" select="0+number(./BytePos)"/>id0x<xsl:value-of select="../HighPID"/><xsl:value-of select="../LowPID"/><xsl:value-of select="number($corrByteNr)"/><xsl:value-of select="./BitPos"/> = { bpos = <xsl:value-of select="number($corrByteNr)"/>, blen = <xsl:value-of select="./ByteNr"/>, Bpos = <xsl:value-of select="./BitPos"/> , Blen = <xsl:value-of select="./BitNr"/>, dtype = "ENUM", t = "<xsl:value-of select="./Name"/>", ev = { 
<xsl:for-each select="EnumMember">id<xsl:value-of select="./EnumValue"/> = { bv = <xsl:value-of select="EnumValue"/> , t = "<xsl:value-of select="EnumDescription"/>"},
</xsl:for-each>dummy=0}},
</xsl:template>

<xsl:template match="DTC">id0x<xsl:value-of select="./ID"/> =  "<xsl:value-of select="./DESCRIPTION"/>" ,
</xsl:template>

<xsl:template match="ROUTINE">id0x31<xsl:value-of select="./ID"/> =  "<xsl:value-of select="./DESCRIPTION"/>" ,
</xsl:template>

</xsl:stylesheet> 