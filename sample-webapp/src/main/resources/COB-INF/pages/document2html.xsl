<?xml version="1.0" encoding="UTF-8"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

    <xsl:import href="../common/identity.xsl"/>

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
    
    <xsl:template match="top">
        <div id="top">
            <xsl:apply-templates select="html/body/*"/>
        </div>
    </xsl:template>

    <xsl:template match="left">
        <div id="left">
            <xsl:apply-templates select="html/body/*"/>
        </div>
    </xsl:template>

    <xsl:template match="center">
        <div id="center">
            <xsl:apply-templates/>
        </div>
    </xsl:template>
    
    <xsl:template match="right">
        <div id="right">
            <xsl:apply-templates select="html/body/*"/>
        </div>
    </xsl:template>
    
    <xsl:template match="bottom">
        <div id="bottom">
            <xsl:apply-templates select="html/body/*"/>
        </div>
    </xsl:template>

</xsl:stylesheet>
