<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:my="http://kayak.2codeornot2code.org/1.0">

<xsl:template match="*">
  <xsl:apply-templates/>
 </xsl:template>


<xsl:template match="/my:NetworkDefinition/my:Bus">
<xsl:apply-templates select="my:Message" />
</xsl:template>


<xsl:template match="my:Message">
message-----
<xsl:apply-templates select="my:Signal" />
</xsl:template>

<xsl:template match="my:Signal">
Signal----
msgname=<xsl:value-of select="../@name"/>, msgid=<xsl:value-of select="../@id"/>, msglen=<xsl:value-of select="../@length"/>, signame=<xsl:value-of select="@name"/>
<xsl:choose>
 <xsl:when test="@endianess='big'">, intel=false</xsl:when>
 <xsl:otherwise>, intel=true</xsl:otherwise>
</xsl:choose>
<xsl:if test="my:Value">
<xsl:apply-templates select="my:Value" />
</xsl:if>
<xsl:if test="not(my:Value)">
<xsl:choose>
 <xsl:when test="@length">, siglen=<xsl:value-of select="@length"/></xsl:when>
 <xsl:otherwise>, siglen=1</xsl:otherwise>
</xsl:choose>
<xsl:choose>
 <xsl:when test="@offset">, offset=<xsl:value-of select="@offset"/></xsl:when>
 <xsl:otherwise>, offset=0</xsl:otherwise>
</xsl:choose>, min=0.0, max=0.0, unit="", slope=1.0
</xsl:if>

</xsl:template>

<xsl:template match="my:Value">
<xsl:choose>
 <xsl:when test="@min">, min=<xsl:value-of select="@min"/></xsl:when>
 <xsl:otherwise>, min=0.0</xsl:otherwise>
</xsl:choose>
<xsl:choose>
 <xsl:when test="@max">, max=<xsl:value-of select="@max"/></xsl:when>
 <xsl:otherwise>, max=0.0</xsl:otherwise>
</xsl:choose>
<xsl:choose>
 <xsl:when test="@unit">, unit="<xsl:value-of select="@unit"/>"</xsl:when>
 <xsl:otherwise>, unit=""</xsl:otherwise>
</xsl:choose>
<xsl:choose>
 <xsl:when test="@slope">, slope=<xsl:value-of select="@slope"/></xsl:when>
 <xsl:otherwise>, slope=1.0</xsl:otherwise>
</xsl:choose>


</xsl:template>





<xsl:template match="text()|@*">
</xsl:template>

</xsl:stylesheet> 
