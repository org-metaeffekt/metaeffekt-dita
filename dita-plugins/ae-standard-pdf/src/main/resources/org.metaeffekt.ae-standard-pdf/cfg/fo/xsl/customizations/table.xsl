<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet exclude-result-prefixes="metaeffekt" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:metaeffekt="org.metaeffekt"
                version="2.0">

    <!--
        Alternating colors for table rows.
    -->

    <xsl:template match="*[contains(@class, ' topic/tbody ')]/*[contains(@class, ' topic/row ')]">
        <fo:table-row xsl:use-attribute-sets="tbody.row">
            <xsl:choose>
                <xsl:when test="(count(preceding-sibling::*[contains(@class, ' topic/row ')]) mod 2) = 0">
                    <!-- even row, white -->
                    <xsl:attribute name="background-color">white</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <!-- odd row, gray -->
                    <xsl:attribute name="background-color">rgb(240, 240, 240)</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:table-row>
    </xsl:template>

</xsl:stylesheet>