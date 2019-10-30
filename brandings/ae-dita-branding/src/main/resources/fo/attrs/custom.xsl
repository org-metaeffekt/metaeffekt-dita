<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format" version="1.0">

    <!-- Entry point for the DITA OT Plugin, see included files for actual customizations. -->

    <xsl:variable name="toc.toc-indent" select="'12pt'"/>

    <xsl:include href="customizations/ca_chapterBorders.xsl" />
    <xsl:include href="customizations/ca_cover.xsl" />
    <xsl:include href="customizations/ca_footer.xsl" />
    <xsl:include href="customizations/ca_header.xsl" />
    <xsl:include href="customizations/ca_lists.xsl" />
    <xsl:include href="customizations/ca_lot-lof.xsl" />
    <xsl:include href="customizations/ca_note.xsl" />
    <xsl:include href="customizations/ca_overall.xsl" />
    <xsl:include href="customizations/ca_screen.xsl" />
    <xsl:include href="customizations/ca_table.xsl" />
    <xsl:include href="customizations/ca_toc.xsl" />

    <!-- introduce spacing between glossary terms -->
    <!-- FIXME: move to dedicated file; clarify responsibilities between plugin and branding  -->
    <xsl:attribute-set name="__glossary__def">
        <xsl:attribute name="margin-left"><xsl:value-of select="$side-col-width"/></xsl:attribute>
        <xsl:attribute name="space-after">10pt</xsl:attribute>
    </xsl:attribute-set>

    <!-- use latin numbers for List of Tables (LOT) and List of Figures (LOF) -->
    <xsl:attribute-set name="page-sequence.lot" use-attribute-sets="page-sequence.toc">
        <xsl:attribute name="format">1</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="page-sequence.lof" use-attribute-sets="page-sequence.toc">
        <xsl:attribute name="format">1</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="page-sequence.frontmatter">
      <xsl:attribute name="format">1</xsl:attribute>
    </xsl:attribute-set>

    <!-- TOC related attribute tuning -->
    <xsl:attribute-set name="__toc__header" use-attribute-sets="common.title">
        <xsl:attribute name="space-before">0pt</xsl:attribute>
        <xsl:attribute name="space-after">16.8pt</xsl:attribute>
        <xsl:attribute name="font-size">18pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="padding-top">16.8pt</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__toc__indent__lot" use-attribute-sets="__toc__indent__booklist">
        <xsl:attribute name="font-size">12pt</xsl:attribute>
        <xsl:attribute name="padding-top">10mm</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="lines" use-attribute-sets="base-font">
        <xsl:attribute name="space-before">0.0em</xsl:attribute>
        <xsl:attribute name="space-after">0.0em</xsl:attribute>
        <xsl:attribute name="white-space-collapse">true</xsl:attribute>
        <xsl:attribute name="linefeed-treatment">preserve</xsl:attribute>
        <xsl:attribute name="wrap-option">wrap</xsl:attribute>
        <xsl:attribute name="font-size">100%</xsl:attribute>
    </xsl:attribute-set>

    <xsl:variable name="page-margin-bottom">25mm</xsl:variable>

    <xsl:attribute-set name="region-after">
        <xsl:attribute name="extent">25mm</xsl:attribute>
        <xsl:attribute name="display-align">after</xsl:attribute>
        <xsl:attribute name="padding-bottom">4mm</xsl:attribute>

        <!-- Uncomment to visualize footer region
        <xsl:attribute name="background-color">#f0f0f0</xsl:attribute>
        -->
    </xsl:attribute-set>

</xsl:stylesheet>
