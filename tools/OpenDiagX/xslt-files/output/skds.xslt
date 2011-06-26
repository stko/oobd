<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <xsl:apply-templates/>

</xsl:template>

 
<xsl:template match="oobdobx">
global

	Modulename: String = '<xsl:value-of select="./Name"/>';
	ModuleID : Byte =$<xsl:value-of select="./PhysAdressShort"/>;

endvar;

(*#include <xsl:text disable-output-escaping="yes"><![CDATA[<standard-uds.pas>]]></xsl:text> *)


procedure autorun;

endproc;



{----------------------------------------------------------------------}


<xsl:apply-templates select="BMP" />

{----------------------------------------------------------------------}


<xsl:apply-templates select="ASCII" />

{----------------------------------------------------------------------}


<xsl:apply-templates select="NUM" />

</xsl:template>

<xsl:template match="BMP">
	<xsl:variable name="corrByteNr" select="1+number(./ByteNr)"/>
procedure PIDBMP<xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/>_<xsl:value-of select="number($corrByteNr)"/>_<xsl:value-of select="./BitNr"/>
(*#menuitem "*" "U" "<xsl:value-of select="./Name"/>" "PIDBMP<xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/>_<xsl:value-of select="number($corrByteNr)"/>_<xsl:value-of select="./BitNr"/>" "PIDBMP" "Input"*)
    getPIDBMP( $<xsl:value-of select="./HighPID"/> , $<xsl:value-of select="./LowPID"/> , <xsl:value-of select="number($corrByteNr)"/> , <xsl:value-of select="./BitNr"/> , "<xsl:value-of select="./LowText"/>" , "<xsl:value-of select="./HighText"/>")
endproc

</xsl:template>

<xsl:template match="ASCII">

procedure PIDASC<xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/>
(*#menuitem "*" "U" "<xsl:value-of select="./Name"/>" "PIDASC<xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/>" "PIDASC" "Input"*)
    getPIDASC( $<xsl:value-of select="./HighPID"/> , $<xsl:value-of select="./LowPID"/> , <xsl:value-of select="./Len"/> )
endproc

</xsl:template>


<xsl:template match="NUM">

procedure PIDNUM<xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/>
(*#menuitem "*" "U" "<xsl:value-of select="./Name"/>" "PIDNUM<xsl:value-of select="./HighPID"/><xsl:value-of select="./LowPID"/>" "PIDNUM" "Input"*)
    getPIDNUM( $<xsl:value-of select="./HighPID"/> , $<xsl:value-of select="./LowPID"/> , <xsl:value-of select="./Resolution"/> , <xsl:value-of select="./Offset"/> , "<xsl:value-of select="./Units"/>" )
endproc

</xsl:template>


</xsl:stylesheet> 
