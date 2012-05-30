<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:hct="http://forge.onehippo.org/gf/project/hct/1.0"
                xmlns:str="http://xsltsl.org/string"
                version="1.0">

  <xsl:import href="../common/identity.xsl"/>
  <xsl:import href="../common/xsltsl-1.2.1/string.xsl"/>

  <xsl:output method="xml"/>

  <xsl:param name="contextPath"/>
  <xsl:param name="availability"/>

  <xsl:template match="hct:queryResult">
    <div id="collection">
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
        <xsl:text>&#160;[</xsl:text>
        <xsl:for-each select="hct:translations/hct:translation">
          <a href="{concat($contextPath, '/', $availability, '/', 
                                                      substring-after(substring-after(substring-after(@path, '/'), '/'), '/'))}/index.html?lang={@locale}">
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
