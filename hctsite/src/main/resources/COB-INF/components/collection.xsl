<?xml version="1.0" encoding="UTF-8"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
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
  <xsl:param name="requestURI"/>

  <xsl:template match="hct:queryResult">
    <div id="collection">
      <xsl:choose>
	<xsl:when test="contains($requestURI, '/index.html')">
	  <a href="../index.html">Go up one level</a>
	</xsl:when>
	<xsl:otherwise>
	  <a href="index.html">Back to folder</a>
	</xsl:otherwise>
      </xsl:choose>
      <ul>
        <xsl:apply-templates select="hct:folder|hct:taxonomy"/>
      </ul>
    </div>
  </xsl:template>
  
  <xsl:template match="hct:folder">
    <li>
      <xsl:variable name="href" select="substring-after(substring-after(substring-after(@path, '/'), '/'), '/')"/>
      <div id="{@name}">
        <a href="{$contextPath}/{$availability}/{$href}/index.html">
          <xsl:value-of select="@localizedName"/>
        </a>        
      </div>
    </li>
  </xsl:template>
    
  <xsl:template match="hct:taxonomy">
    <li>
      <xsl:variable name="href" 
                    select="substring-after(substring-after(substring-after(substring-after(substring-after(@path, '/'), '/'), '/'), '/'), '/')"/>
      <div id="{@name}">
        <a href="{$contextPath}/{$availability}/taxonomy/{$href}/index.html">
          <xsl:value-of select="@localizedName"/>
        </a>
        <xsl:text>&#160;[</xsl:text>
        <xsl:for-each select="hct:translations/hct:translation">
          <a href="{$contextPath}/{$availability}/taxonomy/{$href}/index.html?lang={@locale}">
            <xsl:value-of select="@localizedName"/>
            <xsl:text>&#160;(</xsl:text>
            <xsl:value-of select="@locale"/>
            <xsl:text>)</xsl:text>
          </a>
          <xsl:text>&#160;</xsl:text>
        </xsl:for-each>
        <xsl:text>]</xsl:text>        
      </div>
    </li>
  </xsl:template>
  
</xsl:stylesheet>
