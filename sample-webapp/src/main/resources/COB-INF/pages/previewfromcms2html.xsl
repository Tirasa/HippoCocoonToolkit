<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:str="http://xsltsl.org/string"
                version="1.0">

    <xsl:import href="../common/identity.xsl"/>
    <xsl:import href="../common/xsltsl-1.2.1/string.xsl"/>

    <xsl:output method="xml"/>

    <xsl:template match="/page">
        <html>
            <head>
                <link rel="stylesheet" href="css/style.css" type="text/css"/>
            </head>
            <body>
                <xsl:apply-templates/>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
