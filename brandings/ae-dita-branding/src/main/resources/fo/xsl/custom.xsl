<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet exclude-result-prefixes="metaeffekt" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:metaeffekt="org.metaeffekt"
                version="2.0">

    <!--
        Entry point for the DITA OT Plugin, see included files for actual customizations.
    -->

    <xsl:include href="customizations/cover.xsl" />
    <xsl:include href="customizations/fix-for-layout-masters.xsl" />
    <xsl:include href="customizations/footer.xsl" />
    <xsl:include href="customizations/header.xsl" />
    <xsl:include href="customizations/note.xsl" />
    <xsl:include href="customizations/table.xsl" />
    <xsl:include href="customizations/toc-and-numbering.xsl" />

</xsl:stylesheet>
