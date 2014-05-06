<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:my="http://kayak.2codeornot2code.org/1.0">

	<xsl:template match="*">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="/my:NetworkDefinition/my:Bus">scriptTitle ="Real Time Data for <xsl:value-of select="@name"/>"

---- add here everything you would like to have a carline specific presets

presets = {
		<xsl:apply-templates select="my:Message" />
}</xsl:template>
	<xsl:template match="my:Message">{ t="<xsl:value-of select="@name"/>", elements={ <xsl:apply-templates select="my:Signal" />
	}
	},
</xsl:template> 
	<xsl:template match="my:Signal"> 
		<xsl:variable name="canid" select="concat(substring('000000000',string-length(../@id)), substring(../@id,3))"/>"rtdArray_NOP_<xsl:value-of select="$canid"/>_sd_<xsl:value-of select="format-number(position(),'00')"/>", </xsl:template>

	<xsl:template match="text()|@*">
	</xsl:template>
</xsl:stylesheet> 