<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
	<html>
	<body>
	<h2><xsl:value-of select="catalog/title"/></h2>
	<a href="/theme/default/settings.html">Settings</a>
	<form action="#" method="post" id="form_2">
		<label id="h2" form="form_2">Your Settings</label><br/>
		<label for="theme">UI Theme</label>  
		<select name="theme" id="theme" size="1">
			<xsl:for-each select="catalog/theme">
				<xsl:choose>
					<xsl:when test="@selected">
						<option selected="selected"><xsl:value-of select="."/></option>
					</xsl:when>
					<xsl:otherwise>
						<option><xsl:value-of select="."/></option>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</select><br/>
		<label for="pgppw">PGP passphrase</label> 
		<input type="text" name="pgppw" id="pgppw" maxlength="30"/><br/>

		<label for="rcid">Remote Connect ID</label>  
		<input type="text" name="rcid" id="rcid" maxlength="40"/><br/>
		<label for="connectType">Connection Type</label>  
		<select name="connectType" id="connectType" size="1">
			<xsl:for-each select="catalog/connection">
				<xsl:choose>
					<xsl:when test="@selected">
						<option selected="selected"><xsl:value-of select="."/></option>
					</xsl:when>
					<xsl:otherwise>
						<option><xsl:value-of select="."/></option>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</select><br/>
		<button type="submit">Set</button>
	</form>
	<table>
	<xsl:for-each select="catalog/script">
		<tr>
			<td>filename</td>
			<td><a href="{fileid}"><xsl:value-of select="filename"/></a></td>
		</tr>
		<tr>
			<td>title</td>
			<td><xsl:value-of select="title"/></td>
		</tr>
		<tr>
			<td>name</td>
			<td><xsl:value-of select="name"/></td>
		</tr>
		<tr>
			<td>shortname</td>
			<td><xsl:value-of select="shortname"/></td>
		</tr>
		<tr>
			<td>description</td>
			<td><xsl:value-of select="description"/></td>
		</tr>
		<tr>
			<td>version</td>
			<td><xsl:value-of select="version"/></td>
		</tr>
		<tr>
			<td>copyright</td>
			<td><xsl:value-of select="copyright"/></td>
		</tr>
		<tr>
			<td>author</td>
			<td><xsl:value-of select="author"/></td>
		</tr>
		<tr>
			<td>security</td>
			<td><xsl:value-of select="security"/></td>
		</tr>
		<tr>
			<td>date</td>
			<td><xsl:value-of select="date"/></td>
		</tr>
		<tr>
			<td>icon</td>
			<td>
				<xsl:choose>
					<xsl:when test="icon !=''">
						<xsl:value-of select="icon"/><img src="{filename}/{icon}"/>
					</xsl:when>
					<xsl:otherwise>
						-
					</xsl:otherwise>
				</xsl:choose>
			</td>
		</tr>
		<tr>
			<td>screenshot</td>
			<td><xsl:value-of select="screenshot"/></td>
		</tr>
		<tr>
			<td>url</td>
			<td><xsl:value-of select="url"/></td>
		</tr>
		<tr>
			<td>email</td>
			<td><xsl:value-of select="email"/></td>
		</tr>
		<tr>
			<td>phone</td>
			<td><xsl:value-of select="phone"/></td>
		</tr>
	</xsl:for-each>
	</table>
	</body>
	</html>
</xsl:template>

</xsl:stylesheet>