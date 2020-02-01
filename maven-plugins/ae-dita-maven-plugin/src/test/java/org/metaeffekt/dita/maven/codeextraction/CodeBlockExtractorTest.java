/**
 * Copyright 2009-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.metaeffekt.dita.maven.codeextraction;

import static org.metaeffekt.dita.maven.codeextraction.CodeBlockExtractor.dedent;
import static org.metaeffekt.dita.maven.codeextraction.CodeBlockExtractor.getIndent;
import static org.metaeffekt.dita.maven.codeextraction.CodeBlockExtractor.spaces;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.MULTILINE;
import static java.util.regex.Pattern.compile;

import java.util.HashMap;

import junit.framework.TestCase;

public class CodeBlockExtractorTest extends TestCase {
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
    
    public CodeBlockExtractorTest(){
        super();
    }
    
    public void setUp(){
        extractor = new CodeBlockExtractor();
    }
    
    public void testOptionalDocumentId() throws Exception {
        extractor.processLine("        // @dita-document (id=\"My-Generated-Document\")");
        assertEquals("My-Generated-Document", extractor.getDocumentId());

    }
    
    public void testDuplicateDocumentId() throws Exception {
        extractor.processLine("        // @dita-document (id=\"My-Generated-Document-1\")");
        try {
            extractor.processLine("        // @dita-document (id=\"My-Generated-Document-2\")");
            fail("Should throw exception when two dita-document annotations are found");
        } catch (Exception expected) {
            assertEquals("Duplicate document ID 'My-Generated-Document-2' found. There is only one @dita-document annotation allowed in one file.", expected.getMessage());
        }
    }
    
    public void testCorrectFragments() throws Exception {
        for (String line: fragment)
            extractor.processLine(line);
        extractor.processLine("        // @dita-end (id=\"code-fragment-2\")");
        HashMap<String,String> fragments = extractor.collectFragments();
        assertEquals(2, fragments.size());
        for (String fragment : fragments.values()) {
            assertTrue(leadingNonWhitespace.matcher(fragment).matches());
        }
        assertNull(extractor.getDocumentId());

    }
    
    public void testMissingEnd() throws Exception {
        for (String line: fragment)
            extractor.processLine(line);
        try {
            extractor.collectFragments();
            fail("Should throw exception when end delimiter is missing");
        } catch (Exception e) {}
    }
    
    public void testMissingBegin() throws Exception {
        for (String line: fragment)
            extractor.processLine(line);
        try {
            extractor.processLine("        // @dita-end (id=\"code-fragment-3\")");
            fail("Should throw exception when begin delimiter is missing");
        } catch (Exception e) {}
    }
    
    public void testMissingId() throws Exception {
        for (String line: fragment)
            extractor.processLine(line);
        try {
            extractor.processLine("        // @dita-end");
            fail("Should throw exception when id is missing");
        } catch (Exception e) {}
    }

    public void testSingleCharId() throws Exception {
        for (String line: fragment)
            extractor.processLine(line);
        extractor.processLine("        // @dita-end (id=\"code-fragment-2\")");
        extractor.processLine("        // @dita-begin (id=\"c\")");
        extractor.processLine("        // @dita-end (id=\"c\")");
    }

    public void testEmptyId() throws Exception {
        for (String line: fragment)
            extractor.processLine(line);
        try {
            extractor.processLine("        // @dita-end (id=\"\")");
            fail("Should throw exception when id is empty");
        } catch (Exception e) {}
    }

    public void testUnclosedId() throws Exception {
        for (String line: fragment)
            extractor.processLine(line);
        try {
            extractor.processLine("        // @dita-end (id=\")");
            fail("Should throw exception when id is empty");
        } catch (Exception e) {}
    }

    public void testUnspecifiedId() throws Exception {
        for (String line: fragment)
            extractor.processLine(line);
        try {
            extractor.processLine("        // @dita-end (id=)");
            fail("Should throw exception when id is unspecified");
        } catch (Exception e) {}
    }

    public void testDummyId() throws Exception {
        for (String line: fragment)
            extractor.processLine(line);
        try {
            extractor.processLine("        // @dita-end (id)");
            fail("Should throw exception when id is unspecified");
        } catch (Exception e) {}
    }

    public void testWhitespace() throws Exception {
        for (String line: fragment)
            extractor.processLine(line);
        extractor.processLine("        //      @dita-end  (id    = \"code-fragment-2\")");
        assertEquals(2, extractor.collectFragments().size());
    }
    
    public void testSpaces() {
        assertEquals(4, spaces(4).length());
    }

    public void testDedent() {
        assertEquals("asdf\n aoeu", dedent("  asdf\n   aoeu", 2));
    }

    public void testGetIndent() {
        assertEquals(4, getIndent("    aoeu"));
    }

}
