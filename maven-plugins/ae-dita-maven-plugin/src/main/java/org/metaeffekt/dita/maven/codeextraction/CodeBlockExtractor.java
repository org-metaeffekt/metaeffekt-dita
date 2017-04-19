/**
 * Copyright 2009-2016 the original author or authors.
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
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CodeBlockExtractor {
    HashMap<String,StringBuilder> fragmentAccumulators = new HashMap<>();
    HashMap<String,String> completeFragments = new HashMap<>();
    private static final Pattern idPattern = Pattern.compile(".*id *= *\"([^\"]+)\".*");
    private static final Pattern indentPattern = Pattern.compile("^ *");
    private static final Pattern spacePattern = Pattern.compile("\\s*");
    private static final String NL = System.getProperty("line.separator");
    private static final String TAB_TO_SPACES = spaces(4);
    private HashMap<String,Integer> minIndent = new HashMap<>();
    private String documentId;
    
    public void processLine(String line) throws Exception{
            
        boolean begin = false;
        boolean end = false;

        if ((begin = line.contains("@dita-begin")) || (end = line.contains("@dita-end"))) {
            Matcher matcher = idPattern.matcher(line);
            if (!matcher.matches()) throw new Exception("No ID for delimiter!");
            String id = matcher.group(1);
            if (begin) {
                fragmentAccumulators.put(id, new StringBuilder());
            } else if (end) {
                if (!fragmentAccumulators.containsKey(id)) throw new Exception("Code block end for id " + id + " without begin");
                String fragment = fragmentAccumulators.get(id).toString();
                Integer indent = minIndent.get(id);
                if (indent == null) indent = 0;
                completeFragments.put(id, dedent(fragment, indent));
                fragmentAccumulators.remove(id);
            }
        } else if (line.contains("@dita-document")) {
            Matcher matcher = idPattern.matcher(line);
            if (!matcher.matches()) throw new Exception("No id for delimiter!");
            String id = matcher.group(1);
            if (documentId != null)
                throw new Exception("Duplicate document ID '" + id + "' found. " +
                    "There is only one @dita-document annotation allowed in one file.");
            documentId = id;
        } else {
            String replacedLine = line.replace("\t", TAB_TO_SPACES);
            boolean isWhiteSpace = spacePattern.matcher(line).matches();
            int indent = Integer.MAX_VALUE;
                if (!isWhiteSpace) indent = getIndent(replacedLine);
            
            for (Entry<String,StringBuilder> accumulator: fragmentAccumulators.entrySet()) {
                accumulator.getValue().append(replacedLine + NL);
                
                if (!isWhiteSpace) {
                    String id = accumulator.getKey();
                    Integer currentIndent = minIndent.get(id);
                    if (currentIndent == null)
                        minIndent.put(id, indent);
                    else
                        minIndent.put(id, Math.min(indent, currentIndent));
                }
            }
        }
        
    }
    
    static int getIndent(String line) {
        Matcher matcher = indentPattern.matcher(line);
        int indent = 0;
        if (matcher.lookingAt())
            indent = matcher.end();
        return indent;
    }
    
    static String dedent(String text, int size) {
        return text.replaceAll("(?m:^)" + spaces(size), "");
    }
    
    static String spaces(int num) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < num; i++)
            sb.append(' ');
        return sb.toString();
    }
    
    public HashMap<String,String> collectFragments() throws Exception{
        if (fragmentAccumulators.size() != 0) throw new Exception("Unclosed code block fragments!");
        return completeFragments;
    }
    
    public static void main(String... args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        CodeBlockExtractor extractor = new CodeBlockExtractor();
        String line;
        try {
            while((line = br.readLine()) != null) {
                extractor.processLine(line);
            }
        
            System.out.println(extractor.collectFragments());
        } catch (Exception e) {e.printStackTrace();}
    }

    public String getDocumentId() {
        return documentId;
    }
}
