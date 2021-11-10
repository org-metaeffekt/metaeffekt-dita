/**
 * Copyright 2009-2021 the original author or authors.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Map.Entry;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


public class DitaCodeBlockExtractorTask extends Task {
    
    private String sourceDocument;
    
    private String outputPath;
    
    private String documentHeader =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<!DOCTYPE topic\n" +
        "PUBLIC \"-//OASIS//DTD DITA Topic//EN\" \"http://docs.oasis-open.org/dita/v1.1/OS/dtd/topic.dtd\">\n" +
        "<topic id=\"%s\">\n" +
        "    <title/>\n" +
        "    <body>\n";
    private String documentFooter =
        "    </body>\n" +
        "</topic>\n";
    private String fragmentHeader = "<codeblock id =\"%s\" otherprops=\"language(%s)\"><![CDATA[";
    private String fragmentFooter = "]]></codeblock>\n";
    
    private String documentId;
    
    private String sourceType;
    
    @Override
    public void execute() throws BuildException {
        if (sourceDocument == null || outputPath == null)
            throw new BuildException("Missing sourceDocument or outputPath!");
        
        File sourceFile = new File(sourceDocument);
        String sourceFileName = sourceFile.getName();
        
        if (documentId == null || sourceType == null) {
            int dotIndex = sourceFileName.lastIndexOf('.');
            if (sourceType == null) {
                if (dotIndex == -1)
                    throw new BuildException("Cannot deduce file type from file name, set it explicitly with sourceType.");
                sourceType = sourceFileName.substring(dotIndex + 1, sourceFileName.length());
            }
            
            if (documentId == null) {
                if (dotIndex == -1)
                    dotIndex = sourceFileName.length();
                documentId = sourceFileName.substring(0, dotIndex);
            }
        }
        
        CodeBlockExtractor extractor = new CodeBlockExtractor();
        String line;
        
        HashMap<String,String> fragments;
        
        try (
            BufferedReader br = new BufferedReader(new FileReader(sourceFile))) {
            while((line = br.readLine()) != null) {
                extractor.processLine(line);
            }
        
            fragments = extractor.collectFragments();
            String id = extractor.getDocumentId();
            if (id != null) {
                documentId = id;
            }

            if (!fragments.isEmpty()) {
                File outFile = new File(outputPath);
                outFile = new File(outputPath, documentId + ".dita");

                outFile.getParentFile().mkdirs();
                
                FileWriter fw = new FileWriter(outFile);
                
                fw.write(String.format(documentHeader, documentId.replaceAll("/", "_")));
                
                for (Entry<String,String> fragment: fragments.entrySet()) {
                    String block = String.format(fragmentHeader, fragment.getKey(), sourceType) +
                        fragment.getValue() + fragmentFooter;
                    fw.write(block);
                }
                
                fw.write(documentFooter);
                
                fw.flush();
                fw.close();
            }
        } catch (IllegalFormatException ife) {
            throw new BuildException("Illegal number of placeholder '%s' in header!");
        } catch (Exception e) {throw new BuildException(e);}
        
    }
    
    public void setSourceDocument(String sourceDocument) {
        this.sourceDocument = sourceDocument;
    }
    
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
    
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
    
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
    
    public void addConfiguredCodeblockHeader(CodeblockHeader header) {
        fragmentHeader = getProject().replaceProperties(header.text);
    }
    
    public void addConfiguredCodeblockFooter(CodeblockFooter footer) {
        fragmentFooter = getProject().replaceProperties(footer.text);
    }
    
    public void addConfiguredTopicHeader(TopicHeader header) {
        documentHeader = getProject().replaceProperties(header.text);
    }
    
    public void addConfiguredTopicFooter(TopicFooter footer) {
        documentFooter = getProject().replaceProperties(footer.text);
    }
    
    static class CommonBlock {
        public CommonBlock() {}
        
        String text;
        
        public void addText(String text) {
            this.text = text;
        }

    }
    
    static public class CodeblockHeader extends CommonBlock { }

    static public class CodeblockFooter extends CommonBlock { }

    static public class TopicHeader extends CommonBlock { }

    static public class TopicFooter extends CommonBlock { }

}
