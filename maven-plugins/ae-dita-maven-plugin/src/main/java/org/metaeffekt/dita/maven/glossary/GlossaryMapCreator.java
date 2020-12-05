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
package org.metaeffekt.dita.maven.glossary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Generates the glossary for a given ditamap of folder.
 */
public class GlossaryMapCreator {

    /**
     * DITA project level basedir. Usually src/main/dita/&lt;artifact-id&gt;.
     */
    private File baseDir;

    /**
     * Relative path to the target glossary map. This file is produced.
     */
    private File targetGlossaryMap = new File("glossary/en/gmap_glossary.ditamap");

    /**
     * The dita map to generate the glossary for.
     */
    private File ditaMap;

    /**
     * Include list for the files to be scanned for key references. The includes
     * should cover the relevant language, only (otherwise it is currently not deterministic).
     */
    private String[] includes = new String[] { "**/*.dita" };

    /**
     * Exclude list for the files to be scanned for key references.
     */
    private String[] excludes = new String[] { "-nothing-" };;

    /**
     * The language of the glossary.
     */
    private String language = "en";

    public GlossaryMapCreator() {
    }

    public static class GlossaryRef {
        String path;
        String term;
        String value; // the translated value; used to support lang-specific ordering

        @Override
        public boolean equals(Object other) {
            return this.term.equals(((GlossaryRef) other).term);
        }

        @Override
        public int hashCode() {
            return term.hashCode();
        }

        // getter required by velocity template
        public String getPath() {
            return path;
        }

        // getter required by velocity template
        public String getTerm() {
            return term;
        }
    }

    public void create() throws DocumentException, IOException {
        if (baseDir.exists()) {

            if (ditaMap != null) {
                // repeat scan until no further files containing glossary keys are added
                final Set<String> processed = new HashSet<>();
                do {
                    // the initial run will clear the current map
                    updateGlossaryMap(processed.toArray(new String[processed.size()]));

                    // rescan after update using separate set
                    Set<String> processedInternal = new HashSet<>();
                    scan(new File(baseDir, ditaMap.getPath()), processedInternal);

                    // stop the loop when no more files are detected
                    if (processedInternal.size() == processed.size()) {
                        break;
                    }

                    // extend processed list with internal set
                    processed.addAll(processedInternal);
                } while (true);
            } else {
                // use directory scan to identify files
                DirectoryScanner scanner = new DirectoryScanner();
                scanner.setBasedir(baseDir);
                scanner.setIncludes(includes);
                scanner.setExcludes(excludes);
                scanner.scan();
                updateGlossaryMap(scanner.getIncludedFiles());
            }
        }
    }

    private void updateGlossaryMap(String[] files) throws DocumentException, IOException {
        final File f = makeAbsolute(targetGlossaryMap);
        final Set<GlossaryRef> glossaryRefs = extractRequiredGlossaryTerms(files, f);
        renderGlossaryMap(f, glossaryRefs);
    }

    /**
     * Combines the file - if relative - with the basedir.
     *
     * @param file
     * @return
     */
    protected File makeAbsolute(File file) {
        if (!file.isAbsolute()) {
            return new File(baseDir, file.getPath());
        }
        return file;
    }

    private void scan(File file, Set<String> processed) throws IOException {
        if (!processed.contains(file.getCanonicalPath())) {
            final Document document = readDocument(file);
            if (document != null) {
                processed.add(file.getCanonicalPath());
                @SuppressWarnings("unchecked")
                final List<Attribute> elements = document.selectNodes("//@href|//@conref");
                for (Attribute element : elements) {
                    String href = element.getValue();
                    final int hashIndex = href.indexOf("#");
                    if (hashIndex != -1) {
                        href = href.substring(0, hashIndex);
                    }
                    final File child = new File(file.getParent(), href);
                    if (child.exists() && !processed.contains(child.getCanonicalPath())) {
                        scan(child, processed);
                    }
                }
            }
        }
    }

    protected Set<GlossaryRef> extractRequiredGlossaryTerms(final String[] files, File targetFile) throws DocumentException, IOException {
        final Set<GlossaryRef> glossaryRefs = new HashSet<>();

        final Set<String> coveredKeys = new HashSet<>();
        final Set<String> glossaryRefKeys = new HashSet<>();

        // collect already covered key; omit the target file
        for (int i = 0; i < files.length; i++) {
            final File f = new File(files[i]);
            // do not included the target file when evaluating the covered keys
            if (!f.getCanonicalPath().equals(targetFile.getCanonicalPath())) {
                extractCoveredKeys(f, coveredKeys);
            }
        }

        // collect required glossary keys
        for (int i = 0; i < files.length; i++) {
            extractRequiredGlossaryTerms(makeAbsolute(new File(files[i])), glossaryRefKeys);
        }

        // remove already covered keys to prevent conflicts
        glossaryRefKeys.removeAll(coveredKeys);

        // generate glossary map based on the remaining glossaryRefKeys
        createGlossaryRefsForCoveredKeys(glossaryRefs, glossaryRefKeys);

        return glossaryRefs;
    }

    private void extractCoveredKeys(File file, Set<String> coveredKeys) {
        Document document = readDocument(file);
        if (document != null) {
            @SuppressWarnings("unchecked")
            List<Attribute> elements = document.selectNodes("//@keys");
            for (Attribute element : elements) {
                String keys = element.getValue();
                String[] keyArray = keys.split(" ");
                for (String key : keyArray) {
                    coveredKeys.add(key);
                }
            }
        }
    }

    private String extractTermValue(File glossaryTermFile, String defaultValue) {
        final Document document = readDocument(glossaryTermFile);
        if (document != null) {
            @SuppressWarnings("unchecked")
            final List<Element> elements = document.selectNodes("//glossterm");
            for (Element element : elements) {
                return element.getTextTrim();
            }
        }
        return defaultValue;
    }

    private void extractRequiredGlossaryTerms(File file, Set<String> keyRefs) throws DocumentException {
        final Document document = readDocument(file);
        @SuppressWarnings("unchecked")
        final List<Element> keyRefElements = document.selectNodes("//abbreviated-form");
        for (Element element : keyRefElements) {
            String keyRef = element.attributeValue("keyref");
            if (keyRef != null && !keyRefs.contains(keyRef)) {
                keyRefs.add(keyRef);
            }
        }
    }

    private void createGlossaryRefsForCoveredKeys(Collection<GlossaryRef> glossaryRefs, Set<String> keyRefs) {
        for (String key : keyRefs) {
            final GlossaryRef ref = new GlossaryRef();
            ref.term = key;
            glossaryRefs.add(ref);
        }
    }

    protected Document readDocument(File file) {
        SAXReader reader = new SAXReader();
        reader.setValidation(false);
        reader.setIncludeInternalDTDDeclarations(false);
        reader.setIncludeExternalDTDDeclarations(false);
        reader.setIgnoreComments(true);
        reader.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String arg0, String arg1) throws SAXException, IOException {
                return new InputSource(new InputStream() {
                    @Override
                    public int read() throws IOException {
                        return -1;
                    }
                });
            }
        });
        try {
            return reader.read(file);
        } catch (DocumentException e) {
            return null;
        }
    }

    private String computeRelativePath(File targetFile, File file) {
        Set<File> set = new LinkedHashSet<>();
        File commonBaseDir = file;
        set.add(commonBaseDir);
        while (commonBaseDir.getParentFile() != null) {
            set.add(commonBaseDir.getParentFile());
            commonBaseDir = commonBaseDir.getParentFile();
        }

        commonBaseDir = targetFile;
        String relativePath = "";
        String path = file.getName();
        while (commonBaseDir.getParentFile() != null && !set.contains(commonBaseDir.getParentFile())) {
            commonBaseDir = commonBaseDir.getParentFile();
            path = commonBaseDir.getName() + "/" + path;
            relativePath = "../" + relativePath;
        }

        return relativePath;
    }

    private void renderGlossaryMap(File targetFolder, Set<GlossaryRef> glossaryRefs) {
        // ISSUE: sorting operated on key not on (translated) value
        // TODO: scan for files; parse tranlated title; order by title;

        // find and determine relative path to glossary files
        DirectoryScanner scan = new DirectoryScanner();
        scan.setBasedir(baseDir);
        String pathToBaseDir = computeRelativePath(targetFolder, baseDir);

        for (GlossaryRef glossaryRef : glossaryRefs) {
            scan.setIncludes(new String[] { "**/" + language + "/g_" + glossaryRef.term.toLowerCase() + ".dita" });
            scan.scan();
            if (scan.getIncludedFilesCount() == 0) {
                throw new IllegalStateException("No glossary file found for key '" + glossaryRef.term + "'. "
                        + "At least one is required. Please make sure all glossary teams are available.");
            } else {
                // in case multiple matches are found we take the first
                final String includedFile = scan.getIncludedFiles()[0];
                glossaryRef.path = (pathToBaseDir + includedFile).replace("\\", "/");
                glossaryRef.value = extractTermValue(new File(baseDir, includedFile), glossaryRef.term);
            }

            if (!glossaryRef.term.equals(glossaryRef.term.toLowerCase())) {
                System.out.println("It is recommended to use all lower case ids for glossary references for key '"
                        + glossaryRef.term + "'. Otherwise the term may be included multiple times in the glossary.");
            }
        }

        final List<GlossaryRef> orderedTerms = new ArrayList<>();
        orderedTerms.addAll(glossaryRefs);
        Collections.sort(orderedTerms, Comparator.comparing(arg0 -> arg0.value));

        VelocityContext context = new VelocityContext();
        context.put("glossaryRefs", orderedTerms);

        VelocityEngine velocityEngine = initializeVelocityEngine();
        Template template = velocityEngine.getTemplate("/META-INF/templates/dita/template.glossary.ditamap.vt",
                "UTF-8");
        applyVelocity(targetFolder, template, context);
    }

    public VelocityEngine initializeVelocityEngine() {
        final String ENCODING_UTF_8 = "UTF-8";
        Properties velocityProperties = new Properties();
        velocityProperties.put(Velocity.RESOURCE_LOADER, "class, file");
        velocityProperties.put("class.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityProperties.put(Velocity.FILE_RESOURCE_LOADER_CACHE, Boolean.FALSE);
        velocityProperties.put(Velocity.INPUT_ENCODING, ENCODING_UTF_8);
        velocityProperties.put(Velocity.OUTPUT_ENCODING, ENCODING_UTF_8);
        velocityProperties.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS,
                "org.apache.velocity.runtime.log.NullLogChute");
        VelocityEngine velocityEngine = new VelocityEngine(velocityProperties);
        return velocityEngine;
    }

    public static void applyVelocity(File targetFile, Template template, VelocityContext context) {
        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        try {
            FileUtils.write(targetFile, sw.toString());
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    public File getTargetGlossaryMap() {
        return targetGlossaryMap;
    }

    public void setTargetGlossaryMap(File targetGlossaryMap) {
        this.targetGlossaryMap = targetGlossaryMap;
    }

    public String[] getIncludes() {
        return includes;
    }

    public void setIncludes(String[] includes) {
        trimStringArray(includes);
        this.includes = includes;
    }

    protected void trimStringArray(String[] includes) {
        if (includes != null) {
            for (int i = 0; i < includes.length; i++) {
                includes[i] = includes[i].trim();
            }
        }
    }

    public String[] getExcludes() {
        return excludes;
    }

    public void setExcludes(String[] excludes) {
        trimStringArray(excludes);
        this.excludes = excludes;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public File getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public File getDitaMap() {
        return ditaMap;
    }

    public void setDitaMap(File ditaMap) {
        this.ditaMap = ditaMap;
    }

}
