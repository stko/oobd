<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
    <table>
      <xsl:for-each select="dtcs/dtc">
      <tr>
        <td style="background: #f5d67a;"><xsl:value-of select="dtccode"/></td>
       <td><xsl:value-of select="type"/></td>
         <td><xsl:value-of select="dtchex"/></td>
      </tr>
      <tr>
        <td   style="background: #f5d67a;" colspan="3"><xsl:value-of select="dtctext"/></td>
      </tr>
      <tr>
        <td  colspan="2"><xsl:value-of select="subcodetext"/></td>
        <td><xsl:value-of select="subcodehex"/></td>
      </tr>
      <tr colspan="2">
        <td  colspan="3"><xsl:value-of select="repair"/></td>
      </tr style="border-bottom: 1px solid #d6d6d6;">
      </xsl:for-each>
    </table>

</xsl:template>
</xsl:stylesheet>