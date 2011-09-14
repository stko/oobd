<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="oobdobx">

<html>
<head><title><xsl:value-of select="./ShortName"/> - <xsl:value-of select="./Name"/></title></head>
<body>
<a href="http://oobd.org">OOBD</a>-Report of the PartII Spec (MDX) of 
<h2><xsl:value-of select="./ShortName"/> - <xsl:value-of select="./Name"/></h2>

<table>
<tr><td>Name:</td><td><xsl:value-of select="./Name"/></td></tr>
<tr><td>Short Name:</td><td><xsl:value-of select="./ShortName"/></td></tr>
<tr><td>Module-Address:</td><td>0x7<xsl:value-of select="./PhysAdressShort"/></td></tr>
<tr><td>Bus:</td><td><xsl:value-of select="./Bus"/></td></tr>
</table>

<h3>Bitmapped DiDs</h3>

<table border="1" width="90%">
<tr><th>DiD (Byte/Bit)</th><th>Description</th><th>---Result---</th></tr>
<xsl:apply-templates select="BMP" />
</table>


<h3>ASCII DiDs</h3>
<table border="1" width="90%">
<tr><th>DiD</th><th>Description</th><th>---Result---</th></tr>
<xsl:apply-templates select="ASCII" />
</table>

<h3>Numeric DiDs</h3>
<table border="1" width="90%">
<tr><th>DiD</th><th>Description</th><th>---Result---</th></tr>
<xsl:apply-templates select="NUM" />
</table>

<h3>DTC List</h3>
<table>
<tr><th>Value</th><th>Description</th></tr>
<xsl:apply-templates select="DTCS" />
</table>


<h3>Self Tests</h3>
<table>
<tr><th>DiD</th><th>Description</th></tr>
<xsl:apply-templates select="ROUTINES" />
</table>
</body></html>
</xsl:template>


<xsl:template match="BMP"><xsl:apply-templates select="SingleBit" /></xsl:template>

<xsl:template match="SingleBit">
<xsl:variable name="corrByteNr" select="1+number(ByteNr)"/>
<tr><td><xsl:value-of select="../HighPID"/><xsl:value-of select="../LowPID"/> (<xsl:value-of select="number($corrByteNr)"/> - <xsl:value-of select="./BitNr"/>)</td><td><xsl:value-of select="../Group"/> - <xsl:value-of select="./Name"/></td><td></td></tr>
</xsl:template>


<xsl:template match="ASCII">
<tr><td><xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/></td><td><xsl:value-of select="./Name"/></td><td></td></tr>
</xsl:template>


<xsl:template match="NUM">
<tr><td><xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/></td><td><xsl:value-of select="./Name"/></td><td></td></tr>
</xsl:template>

<xsl:template match="DTC">
<tr><td><xsl:value-of select="./ID"/></td><td><xsl:value-of select="./DESCRIPTION"/></td></tr>
</xsl:template>

<xsl:template match="ROUTINE">
<tr><td><xsl:value-of select="./ID"/></td><td><xsl:value-of select="./DESCRIPTION"/></td></tr>
</xsl:template>


</xsl:stylesheet> 
