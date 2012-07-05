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
                xmlns:hct="http://www.tirasa.net/hct/1.0"
                xmlns:str="http://xsltsl.org/string"
                version="1.0">

  <xsl:import href="../common/identity.xsl"/>
  <xsl:import href="../common/xsltsl-1.2.1/string.xsl"/>

  <xsl:output method="xml"/>

  <xsl:param name="contextPath"/>
  <xsl:param name="availability"/>

  <xsl:template match="hct:queryResult">
    <div id="summary">
      <ul>
        <xsl:for-each select="hct:document">
          <li>
            <xsl:variable name="href">
              <xsl:choose>
                <xsl:when test="starts-with(@path, '/content/taxonomies/')">
                  <xsl:text>taxonomy/</xsl:text>
                  <xsl:value-of select="substring-after(substring-after(substring-after(substring-after(substring-after(
                                        substring-before(@path, '/hippotaxonomy:documents/hippo:resultset/'), 
                                        '/'), '/'), '/'), '/'), '/')"/>
                  <xsl:text>/</xsl:text>
                  <xsl:value-of select="substring-after(@path, '/hippotaxonomy:documents/hippo:resultset/')"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:call-template name="str:substring-before-last">
                    <xsl:with-param name="text"
                                    select="substring-after(substring-after(substring-after(@path, '/'), '/'), '/')"/>
                    <xsl:with-param name="chars">/</xsl:with-param>
                  </xsl:call-template>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <div id="{@name}">
              <a href="{$contextPath}/{$availability}/{$href}.html">
                <xsl:value-of select="hct:field[@name = 'sample:title']/hct:value"/>
              </a>
              <br/>
              <i><xsl:value-of select="hct:field[@name = 'sample:summary']/hct:value"/></i>
<!--              <xsl:apply-templates select="hct:field"/>-->
              <xsl:apply-templates select="hct:tags"/>
              <xsl:apply-templates select="hct:taxonomies"/>
              <xsl:apply-templates select="hct:images"/>
              <xsl:apply-templates select="hct:relatedDocs"/>
            </div>
          </li>
        </xsl:for-each>
      </ul>
    </div>
  </xsl:template>
    
  <xsl:template match="hct:translations">
    <xsl:text>&#160;[</xsl:text>
    <xsl:for-each select="hct:translation">
      <a>
        <xsl:attribute name="href">
          <xsl:call-template name="str:substring-before-last">
            <xsl:with-param name="text"
                            select="concat($contextPath, '/', $availability, '/',
                                                substring-after(substring-after(substring-after(@path, '/'), '/'), '/'))"/>
            <xsl:with-param name="chars">/</xsl:with-param>
          </xsl:call-template>
          <xsl:text>.html?lang=</xsl:text>
          <xsl:value-of select="@locale"/>
        </xsl:attribute>
        <xsl:value-of select="@localizedName"/>
        <xsl:text>&#160;(</xsl:text>
        <xsl:value-of select="@locale"/>
        <xsl:text>)</xsl:text>
      </a>
      <xsl:text>&#160;</xsl:text>
    </xsl:for-each>
    <xsl:text>]</xsl:text>        
  </xsl:template>
    
  <xsl:template match="hct:field">
    <div id="{@name}">
      <xsl:choose>
        <xsl:when test="not(html)">
          <xsl:for-each select="hct:value">
            <span>
              <xsl:value-of select="text()"/>
            </span>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="html/body/*"/>
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>
    
  <xsl:template match="hct:tags">
    <div id="tags">
      <ul>
        <xsl:for-each select="hct:tag">
          <li>
            <xsl:value-of select="text()"/>
          </li>
        </xsl:for-each>
      </ul>
    </div>
  </xsl:template>
  
  <xsl:template match="hct:taxonomies">
    <div id="taxonomies">
      <ul>
        <xsl:for-each select="hct:taxonomy">
          <li>
            <a href="{concat($contextPath, '/', $availability, '/', 'taxonomy', '/',
                      substring-after(substring-after(substring-after(substring-after(substring-after(@path, '/'), '/'), '/'), '/'), '/'), '/')}">
              <xsl:value-of select="@localizedName"/>
            </a>
          </li>
        </xsl:for-each>
      </ul>
    </div>
  </xsl:template>
  
  <xsl:template match="hct:images">
    <div id="images">
      <xsl:for-each select="hct:image">
        <div>
          <xsl:variable name="imgURL">
            <xsl:call-template name="str:substring-before-last">
              <xsl:with-param name="text"
                              select="substring-after(substring-after(@path, '/'), '/')"/>
              <xsl:with-param name="chars">/</xsl:with-param>
            </xsl:call-template>
          </xsl:variable>
          <img src="{$contextPath}/{$imgURL}:thumbnail" alt="{@name}"/>
        </div>
      </xsl:for-each>
    </div>
  </xsl:template>
    
  <xsl:template match="hct:relatedDocs">
    <div id="related">
      <xsl:for-each select="hct:document">
        <div>
          <a href="{$contextPath}/{substring-after(substring-after(substring-after(@path, '/'), '/'), '/')}.html">
            <xsl:value-of select="@localizedName"/>
          </a>
        </div>
      </xsl:for-each>
    </div>
  </xsl:template>
    
</xsl:stylesheet>
