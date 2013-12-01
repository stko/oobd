<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:my="http://kayak.2codeornot2code.org/1.0">

<xsl:template match="*">
  <xsl:apply-templates/>
 </xsl:template>


<xsl:template match="/my:NetworkDefinition/my:Bus">Name	ID	Request	Bitsize	Startbit	Priority	Intel	Resolution	Offset	Units	Type_ID	Min_value	Max_value	Datatype<xsl:apply-templates select="my:Message" />
</xsl:template>


<xsl:template match="my:Message">
<xsl:apply-templates select="my:Signal" />
</xsl:template>

<xsl:template match="my:Signal">
<xsl:if test="my:Value">
<xsl:apply-templates select="my:Value" />
</xsl:if>


</xsl:template>

<xsl:template match="my:Value">
<xsl:variable name="bytesize" select="../@length"/>
<xsl:variable name="bitsize" select="$bytesize * 8"/>
Name=<xsl:value-of select="../@name"/>,	ID=<xsl:value-of select="../../@id"/>,	Request=0000<xsl:choose>
<xsl:when test="../@length">,	Bitsize=<xsl:value-of select="$bitsize"/></xsl:when>
<xsl:otherwise>,	Bitsize=1</xsl:otherwise>
</xsl:choose>,	Startbit=<xsl:value-of select="../@offset"/>,	Priority=1<xsl:choose>
<xsl:when test="../@endianess='big'">,	Intel=false</xsl:when>
<xsl:otherwise>,	Intel=true</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="@slope">,	Resolution=<xsl:value-of select="@slope"/></xsl:when>
<xsl:otherwise>,	Resolution=1.0</xsl:otherwise>
</xsl:choose>
<xsl:choose>
 <xsl:when test="@intercept">,	Offset=<xsl:value-of select="@intercept"/></xsl:when>
 <xsl:otherwise>,	Offset=0.0</xsl:otherwise>
</xsl:choose>
<xsl:choose>
 <xsl:when test="@unit">,	Units="<xsl:value-of select="@unit"/>"</xsl:when>
 <xsl:otherwise>,	Units=""</xsl:otherwise>
</xsl:choose>,	Type_ID=RTD<xsl:choose>
 <xsl:when test="@min">,	Min_value=<xsl:value-of select="@min"/></xsl:when>
 <xsl:otherwise>,	Min_value=0.0</xsl:otherwise>
</xsl:choose>
<xsl:choose>
 <xsl:when test="@max">,	Max_value=<xsl:value-of select="@max"/></xsl:when>
 <xsl:otherwise>,	Max_value=0.0</xsl:otherwise>
</xsl:choose>
<xsl:choose>
 <xsl:when test="@signed">,	Datatype=<xsl:value-of select="@max"/></xsl:when>
 <xsl:otherwise>,	Datatype=unsigned</xsl:otherwise>
</xsl:choose>


</xsl:template>





<xsl:template match="text()|@*">
</xsl:template>

</xsl:stylesheet> 
