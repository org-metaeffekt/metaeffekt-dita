<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format" version="1.0">

    <!--
        Complementary attribute definitions for the header customization.
    -->

    <xsl:attribute-set name="__header__image">
        <xsl:attribute name="top">20pt</xsl:attribute>
        <xsl:attribute name="position">absolute</xsl:attribute>
        <xsl:attribute name="left">
            <xsl:value-of select="$page-margins"/>
        </xsl:attribute>
        <xsl:attribute name="text-align">start</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__headerTitleText">
        <xsl:attribute name="padding-top">0.3in</xsl:attribute>
        <xsl:attribute name="text-align">center</xsl:attribute>
    </xsl:attribute-set>


    <!-- FIXME: move to dedicated file -->
    <xsl:attribute-set name="__glossary__def">
        <xsl:attribute name="margin-left"><xsl:value-of select="$side-col-width"/></xsl:attribute>
        <xsl:attribute name="space-after">40pt</xsl:attribute>
    </xsl:attribute-set>

</xsl:stylesheet>
