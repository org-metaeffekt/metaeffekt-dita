<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format" version="1.0">

    <!--
        Customizations:
        - Gray table header row.
        - Borders for the cells.
        - Different text align as the rest of the document for the body entries.
        - Smaller font size in tables.
    -->

    <!--
        - Background color of the header row.
        - Border of the header row entries.
    -->
    <xsl:attribute-set name="thead.row.entry" use-attribute-sets="common.border">
        <xsl:attribute name="background-color">lightgray</xsl:attribute>
    </xsl:attribute-set>

    <!--
        Border of the body and footer rows.
    -->
    <xsl:attribute-set name="tbody.row.entry" use-attribute-sets="common.border" />
    <xsl:attribute-set name="tfoot.row.entry" use-attribute-sets="common.border" />

    <!--
        - Bold font for table header entries.
        - Align all header entries in the center.
    -->
    <xsl:attribute-set name="common.table.head.entry" use-attribute-sets="__align__center">
        <xsl:attribute name="font-weight">bold</xsl:attribute>
    </xsl:attribute-set>

    <!--
        Define the text align for table body entries.
    -->
    <xsl:attribute-set name="common.table.body.entry">
        <xsl:attribute name="space-before">3pt</xsl:attribute>
        <xsl:attribute name="space-before.conditionality">retain</xsl:attribute>
        <xsl:attribute name="space-after">3pt</xsl:attribute>
        <xsl:attribute name="space-after.conditionality">retain</xsl:attribute>
        <xsl:attribute name="start-indent">3pt</xsl:attribute>
        <xsl:attribute name="end-indent">3pt</xsl:attribute>
        <!-- Explicit text align for table body entries. -->
        <xsl:attribute name="text-align">start</xsl:attribute>
        <xsl:attribute name="font-size">9pt</xsl:attribute>
    </xsl:attribute-set>

</xsl:stylesheet>