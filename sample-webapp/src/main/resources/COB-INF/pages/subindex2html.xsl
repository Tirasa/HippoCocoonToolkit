<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:i18n="http://apache.org/cocoon/i18n/3.0"
                version="1.0">

    <xsl:import href="../common/identity.xsl"/>

    <xsl:output method="xml"/>

    <xsl:template match="/page">
        <html>
            <head>
                <link rel="stylesheet" href="css/style.css" type="text/css"/>
            </head>
            <body>
                <h3>
                    <i18n:text i18n:key="welcome"/>
                </h3>
                <xsl:apply-templates/>
            </body>
        </html>
    </xsl:template>
    
    <xsl:template match="top">
        <div id="top">
            <xsl:apply-templates select="html/body/*"/>
        </div>
    </xsl:template>

    <xsl:template match="left">
        <div id="left">
            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <xsl:template match="center">
        <div id="center">
            <xsl:apply-templates/>
        </div>
    </xsl:template>
    
    <xsl:template match="right">
        <div id="right">
            <xsl:apply-templates/>
        </div>
    </xsl:template>
    
    <xsl:template match="bottom">
        <div id="bottom">
            <xsl:apply-templates select="html/body/*"/>
        </div>
    </xsl:template>

</xsl:stylesheet>
