/**
 * Copyright 2009-2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.metaeffekt.dita.maven.codeextraction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static java.util.regex.Pattern.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.metaeffekt.dita.maven.codeextraction.CodeBlockExtractor.*;

public class CodeBlockExtractorTest {
    private static final java.util.regex.Pattern leadingNonWhitespace = compile(".*^[^\\s].*", MULTILINE + DOTALL);
    private static final String[] fragment = {
            "        // @dita-begin (id=\"code-fragment-2\")",
            "        DocumentContentContextDto documentContent =",
            "            createDocumentContentContext(\"Child Document\",",
            "                \"Test Provider\");",
            "",
            "        // @dita-begin (id=\"code-fragment-1\")",
            "        documentModuleService.uploadDocumentPart(documentContent,",
            "                parentQualifier);",
            "        // @dita-end (id=\"code-fragment-1\")"
    };

    private CodeBlockExtractor extractor;

    @BeforeEach
    public void setUp() {
        extractor = new CodeBlockExtractor();
    }

    @Test
    public void testOptionalDocumentId() throws Exception {
        extractor.processLine("        // @dita-document (id=\"My-Generated-Document\")");
        assertEquals("My-Generated-Document", extractor.getDocumentId());

    }

    @Test
    public void testDuplicateDocumentId() throws Exception {
        extractor.processLine("        // @dita-document (id=\"My-Generated-Document-1\")");
        final Exception exception = assertThrows(Exception.class, () ->
                        extractor.processLine("        // @dita-document (id=\"My-Generated-Document-2\")"),
                "Should throw exception when two dita-document annotations are found");
        assertEquals("Duplicate document ID 'My-Generated-Document-2' found. There is only one @dita-document annotation allowed in one file.", exception.getMessage());
    }

    @Test
    public void testCorrectFragments() throws Exception {
        for (String line : fragment) {
            extractor.processLine(line);
        }
        extractor.processLine("        // @dita-end (id=\"code-fragment-2\")");
        HashMap<String, String> fragments = extractor.collectFragments();
        assertEquals(2, fragments.size());
        for (String fragment : fragments.values()) {
            assertTrue(leadingNonWhitespace.matcher(fragment).matches());
        }
        assertNull(extractor.getDocumentId());

    }

    @Test
    public void testMissingEnd() throws Exception {
        for (String line : fragment)
            extractor.processLine(line);
        assertThrows(Exception.class, () ->
                        extractor.collectFragments(),
                "Should throw exception when end delimiter is missing"
        );
    }

    @Test
    public void testMissingBegin() throws Exception {
        for (String line : fragment)
            extractor.processLine(line);
        assertThrows(Exception.class, () ->
                        extractor.processLine("        // @dita-end (id=\"code-fragment-3\")"),
                "Should throw exception when begin delimiter is missing"
        );
    }

    @Test
    public void testMissingId() throws Exception {
        for (String line : fragment)
            extractor.processLine(line);
        assertThrows(Exception.class, () ->
                        extractor.processLine("        // @dita-end"),
                "Should throw exception when id is missing");
    }

    @Test
    public void testSingleCharId() throws Exception {
        for (String line : fragment)
            extractor.processLine(line);
        extractor.processLine("        // @dita-end (id=\"code-fragment-2\")");
        extractor.processLine("        // @dita-begin (id=\"c\")");
        extractor.processLine("        // @dita-end (id=\"c\")");
    }

    @Test
    public void testEmptyId() throws Exception {
        for (String line : fragment)
            extractor.processLine(line);
        assertThrows(Exception.class, () ->
                        extractor.processLine("        // @dita-end (id=\"\")"),
                "Should throw exception when id is empty");
    }

    @Test
    public void testUnclosedId() throws Exception {
        for (String line : fragment)
            extractor.processLine(line);
        assertThrows(Exception.class, () ->
                        extractor.processLine("        // @dita-end (id=\")"),
                "Should throw exception when id is empty");
    }

    @Test
    public void testUnspecifiedId() throws Exception {
        for (String line : fragment)
            extractor.processLine(line);
        assertThrows(Exception.class, () ->
                        extractor.processLine("        // @dita-end (id=)"),
                "Should throw exception when id is unspecified");
    }

    @Test
    public void testDummyId() throws Exception {
        for (String line : fragment)
            extractor.processLine(line);
        assertThrows(Exception.class, () ->
            extractor.processLine("        // @dita-end (id)"),
            "Should throw exception when id is unspecified");
    }

    @Test
    public void testWhitespace() throws Exception {
        for (String line : fragment)
            extractor.processLine(line);
        extractor.processLine("        //      @dita-end  (id    = \"code-fragment-2\")");
        assertEquals(2, extractor.collectFragments().size());
    }

    @Test
    public void testSpaces() {
        assertEquals(4, spaces(4).length());
    }

    @Test
    public void testDedent() {
        assertEquals("asdf\n aoeu", dedent("  asdf\n   aoeu", 2));
    }

    @Test
    public void testGetIndent() {
        assertEquals(4, getIndent("    aoeu"));
    }

}
