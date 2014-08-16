<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:my="http://kayak.2codeornot2code.org/1.0">

	<xsl:template match="*">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="/my:NetworkDefinition/my:Bus">
	
	<p>This is confidential Data - all regulations regarding the handling of confidential data have to be applied</p>
<html>
<body>
<table>
<tr><th>CAN- Identifer</th><th>Name</th><th>Unit</th><th>Bus (or Table Id)</th></tr>
<xsl:apply-templates select="my:Message" />
</table>
</body>
</html>
</xsl:template>
	<xsl:template match="my:Message">
<tr><td><strong><xsl:value-of select="@id"/></strong></td><td><strong><xsl:value-of select="@name"/></strong></td><td></td><td><strong>HS-CAN</strong></td></tr>

<xsl:apply-templates select="my:Signal" />
</xsl:template>
	
<xsl:template match="my:Signal">
<tr><td></td><td><xsl:value-of select="@name"/></td><td><xsl:apply-templates select="my:Value" /></td><td><xsl:variable name="canid" select="concat(substring('000000000',string-length(../@id)), substring(../@id,3))"/>rtdArray_NOP_<xsl:value-of select="$canid"/>_sd_<xsl:value-of select="format-number(position(),'00')"/></td></tr>
</xsl:template>
	 
<xsl:template match="my:Value">
<xsl:choose>
<xsl:when test="@unit">(<xsl:value-of select="@unit"/>)</xsl:when>
</xsl:choose>
</xsl:template>
	 
	 
<xsl:template match="text()|@*">
</xsl:template>
</xsl:stylesheet> 
