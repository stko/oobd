<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <xsl:apply-templates/>

</xsl:template>
<xsl:template match="oobdobx">
local Modulename = '<xsl:value-of select="./Name"/>'
local ModuleID : Byte ="7<xsl:value-of select="./PhysAdressShort"/>";

<!-- (*#include <xsl:text disable-output-escaping="yes"><![CDATA[<standard-uds.pas>]]></xsl:text> *) -->
local Menu2Data = {
<xsl:apply-templates select="BMP" />
<xsl:apply-templates select="ASCII" />
<xsl:apply-templates select="NUM" />dummy =""
}
</xsl:template>

<xsl:template match="BMP"><xsl:variable name="corrByteNr" select="0+number(./ByteNr)"/>id0x22<xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/> = {byteNr = <xsl:value-of select="number($corrByteNr)"/> , bitNr = <xsl:value-of select="./BitNr"/> , title = "<xsl:value-of select="./Name"/>" , highText = "<xsl:value-of select="./LowText"/>" ,  lowText = "<xsl:value-of select="./HighText"/>"), call = "readBMPPid" },
</xsl:template>

<xsl:template match="ASCII">id0x22<xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/> = {title = "<xsl:value-of select="./Name"/>" , call = "readASCPid" },
</xsl:template>


<xsl:template match="NUM">id0x22<xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/> = { mult = <xsl:value-of select="./Resolution"/> , offset = <xsl:value-of select="./Offset"/> , len = <xsl:value-of select="./Len"/> , unit = "<xsl:value-of select="./Units"/>" , title = "<xsl:value-of select="./Name"/>" , call = "readNumPid"} ,
</xsl:template>


</xsl:stylesheet> 
