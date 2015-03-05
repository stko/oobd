<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:my="http://kayak.2codeornot2code.org/1.0">

	<xsl:template match="*">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="/my:NetworkDefinition/my:Bus">-- this file is been automatically generated out of the CanBabel translated DBC file
	
rtdArray = {
		<xsl:apply-templates select="my:Message" />
}

dofile("presets.rtd")
dofile("../../tools/lib_lua/serial_dxm.lua")
dofile("../../tools/lib_lua/lua_utils.lua")
dofile("../../tools/lib_lua/luabit/bit.lua")
dofile("../lib_protocol/standard-uds.lua")
dofile("../lib_protocol/realtimedata.lua")
</xsl:template>
	<xsl:template match="my:Message">

	<xsl:variable name="canid" select="concat(substring('000000000',string-length(@id)), substring(@id,3))"/>
rtdArray_NOP_<xsl:value-of select="$canid"/> = { rtdinit="27<xsl:value-of select="$canid"/>00000<xsl:value-of select="@length"/>", t="<xsl:value-of select="@name"/>", bus="HS-CAN", cid=<xsl:value-of select="@id"/>, sd = { 
	<xsl:apply-templates select="my:Signal" />}
	},</xsl:template>
	
	<xsl:template match="my:Signal">sd_<xsl:value-of select="format-number(position(),'00')"/> = { 
		<xsl:variable name="bytesize" select="round(../@length div 8)"/>
		<xsl:variable name="bitsize" select="@length"/>
		<xsl:variable name="bitpos" select="@offset"/>t="<xsl:value-of select="@name"/>", <xsl:choose>
			<xsl:when test="@offset">Bpos=<xsl:value-of select="$bitpos"/></xsl:when><xsl:otherwise>, Bpos=255</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="@length">, Blen=<xsl:value-of select="$bitsize"/></xsl:when><xsl:otherwise>, Blen=1</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="@endianess='big'">, endianess="Motorola"</xsl:when><xsl:otherwise>, endianess="Intel"</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="my:Value/@slope">, mult=<xsl:value-of select="my:Value/@slope"/></xsl:when><xsl:otherwise>, mult=1</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="my:Value/@intercept">, offset=<xsl:value-of select="my:Value/@intercept"/></xsl:when><xsl:otherwise>, offset=0</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="my:Value/@unit">, unit="<xsl:value-of select="my:Value/@unit"/>"</xsl:when><xsl:otherwise>, unit=""</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="my:Value/@min">, min_val=<xsl:value-of select="my:Value/@min"/></xsl:when><xsl:otherwise>, min_val=0</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="my:Value/@max">, max_val=<xsl:value-of select="my:Value/@max"/></xsl:when><xsl:otherwise>, max_val=0</xsl:otherwise>
		</xsl:choose>

		<xsl:choose><xsl:when test="my:LabelSet">, dtype="ENUM"</xsl:when><xsl:otherwise><xsl:choose><xsl:when test="my:Value/@type='signed'">, dtype="SIGNED"</xsl:when><xsl:otherwise>, dtype="UNSIGNED"</xsl:otherwise></xsl:choose></xsl:otherwise></xsl:choose>

	<xsl:if test="my:LabelSet">
			<xsl:apply-templates select="my:LabelSet" />
	</xsl:if>},
	</xsl:template>
	
	<xsl:template match="my:LabelSet">, ev = { <xsl:apply-templates select="my:Label" />
		}
	</xsl:template>
	 
	<xsl:template match="my:Label">
		ev_0x<xsl:value-of select="format-number(position(),'00')"/><xsl:apply-templates select="my:Label" /> = { bv=0x<xsl:value-of select="format-number(@value,'00')"/>, t="<xsl:value-of select="@name"/>"}, </xsl:template>

	<xsl:template match="text()|@*">
	</xsl:template>
</xsl:stylesheet> 