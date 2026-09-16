<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet exclude-result-prefixes="metaeffekt" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:metaeffekt="org.metaeffekt"
                version="2.0">

    <!-- Overrides to fix DITA-OT 3.2.1 bug where keyref targets resolve to multiple elements -->
    <xsl:template match="*[contains(@class,' topic/term ')]" name="topic.term">
        <xsl:param name="keys" select="@keyref" as="attribute()?"/>
        <xsl:param name="contents" as="node()*">
            <xsl:variable name="target" select="if (exists(root()) and @href) then (key('id', substring(@href, 2))[contains(@class, ' topic/topic ')])[1] else ()" as="element()?"/>
            <xsl:choose>
                <xsl:when test="not(normalize-space(.)) and $keys and $target/self::*[contains(@class,' topic/topic ')]">
                    <xsl:apply-templates select="$target/*[contains(@class, ' topic/title ')]/node()"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:param>
        <xsl:variable name="topicref" select="key('map-id', substring(@href, 2))"/>
        <xsl:choose>
            <xsl:when test="$keys and @href">
                <fo:basic-link xsl:use-attribute-sets="xref">
                    <xsl:call-template name="buildBasicLinkDestination">
                        <xsl:with-param name="scope" select="@scope"/>
                        <xsl:with-param name="format" select="@format"/>
                        <xsl:with-param name="href" select="@href"/>
                    </xsl:call-template>
                    <xsl:sequence select="$contents"/>
                </fo:basic-link>
            </xsl:when>
            <xsl:otherwise>
                <fo:inline xsl:use-attribute-sets="term">
                    <xsl:call-template name="commonattributes"/>
                    <xsl:sequence select="$contents"/>
                </fo:inline>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


</xsl:stylesheet>
