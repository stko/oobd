<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <html>
  <body>
  <h2>My OOBD Script Collection</h2>
  <table>
    <xsl:for-each select="catalog/script">
    <tr>
      <td><a href="{fileid}"><xsl:value-of select="title"/></a></td>
    </tr>
    </xsl:for-each>
  </table>
  </body>
  </html>
</xsl:template>

</xsl:stylesheet>