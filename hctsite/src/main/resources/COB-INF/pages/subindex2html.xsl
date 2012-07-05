<?xml version="1.0" encoding="UTF-8"?>
<!--
  Copyright (C) 2012 Tirasa

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:i18n="http://apache.org/cocoon/i18n/3.0"
                version="1.0">

  <xsl:import href="../common/identity.xsl"/>

  <xsl:param name="locale"/>
  <xsl:param name="requestURI"/>

  <xsl:output method="xml"/>

  <xsl:template match="/page">
    <html>
      <head>
        <link rel="stylesheet" href="css/style.css" type="text/css"/>
      </head>
      <body>
        <div id="container">
          <div style="float:left;">
            <h3>
              <i18n:text i18n:key="welcome"/>
            </h3>
          </div>
          <div style="text-align:right;">
            <xsl:choose>
              <xsl:when test="$locale = 'en'">
                <img src="images/en.png"/>
              </xsl:when>
              <xsl:otherwise>
                <a href="{substring-before($requestURI, 'sample_')}sample_en{substring-after($requestURI, concat('sample_', $locale))}">
                  <img src="images/en.png"/>
                </a>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:choose>
              <xsl:when test="$locale = 'it'">
                <img src="images/it.png"/>
              </xsl:when>
              <xsl:otherwise>
                <a href="{substring-before($requestURI, 'sample_')}sample_it{substring-after($requestURI, concat('sample_', $locale))}">
                  <img src="images/it.png"/>
                </a>
              </xsl:otherwise>
            </xsl:choose>
          </div>
          <xsl:apply-templates/>
        </div>
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
