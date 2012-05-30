<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version="1.0">

    <xsl:import href="../common/identity.xsl"/>

    <xsl:output method="xml"/>

    <xsl:param name="scheme"/>
    <xsl:param name="servername"/>
    <xsl:param name="port"/>
    
    <xsl:template match="/page">
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="fo:external-graphic">
        <fo:external-graphic src="{$scheme}://{$servername}:{$port}{@src}">
            <xsl:apply-templates select="@*[name() != 'src']|node()"/>
        </fo:external-graphic>
    </xsl:template>

    <xsl:template match="fo:basic-link">
        <fo:basic-link>
            <xsl:apply-templates select="@*[name() != 'external-destination']"/>
            
            <xsl:variable name="quote">'</xsl:variable>
            <xsl:variable name="url">
                <xsl:value-of select="substring-before(substring-after(@external-destination, $quote), $quote)"/>
            </xsl:variable>
            
            <xsl:choose>
                <xsl:when test="starts-with($url, 'http') or starts-with($url, 'mailto')">
                    <xsl:attribute name="external-destination">
                        <xsl:text>url('</xsl:text>
                        <xsl:value-of select="$url"/>
                        <xsl:text>')</xsl:text>
                    </xsl:attribute>        
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="external-destination">
                        <xsl:text>url('</xsl:text>
                        <xsl:value-of select="$scheme"/>
                        <xsl:text>://</xsl:text>
                        <xsl:value-of select="$servername"/>
                        <xsl:text>:</xsl:text>
                        <xsl:value-of select="$port"/>
                        <xsl:value-of select="$url"/>
                        <xsl:text>')</xsl:text>
                    </xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            
            <xsl:apply-templates select="node()"/>
        </fo:basic-link>
    </xsl:template>
    
</xsl:stylesheet>
