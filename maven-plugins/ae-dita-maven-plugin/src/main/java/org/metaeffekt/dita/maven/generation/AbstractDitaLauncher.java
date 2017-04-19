/**
 * Copyright 2009-2017 the original author or authors.
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
package org.metaeffekt.dita.maven.generation;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * The Class AbstractDitaLauncher.
 */
public abstract class AbstractDitaLauncher {

    /** The input map. */
    private String inputMap;

    /** The base dir. */
    private String baseDir;

    /** The ouptut dir. */
    private String outputDir;

    /** The temp dir. */
    private String tempDir;

    /** The transtype. */
    private String transtype;

    /** The draft. */
    private boolean draft;

    /** The dita dir. */
    private String ditaDir;

    private String customCss;

    /** The log. */
    private Log log;

    /** The dita Val file. */
    private String ditaValFile;

    private String customizationDir;
    
    /** The dita generator toc parameter. */
    private boolean generateToc = true;

    private boolean cleanDitaTemp = true;

    private boolean grammarCacheEnabled = true;

    public boolean isGrammarCacheEnabled() {
        return grammarCacheEnabled;
    }

    public void setGrammarCacheEnabled(boolean grammarCacheEnabled) {
        this.grammarCacheEnabled = grammarCacheEnabled;
    }

    public String getDitaDir() {
        return ditaDir;
    }

    public String getDitavalFile() {
        return ditaValFile;
    }

    public void setDitavalFile(String valFile) {
        this.ditaValFile = valFile;
    }

    public void setDitaDir(String ditaDir) {
        this.ditaDir = ditaDir;
    }

    public String getInputMap() {
        return inputMap;
    }

    public void setInputMap(String inputMap) {
        this.inputMap = inputMap;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public String getTranstype() {
        return transtype;
    }

    public void setTranstype(String transtype) {
        this.transtype = transtype;
    }

    public boolean isDraft() {
        return draft;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public Log getLog() {
        return log;
    }
    
    public boolean isGenerateToc() {
        return generateToc;
    }

    public void setGenerateToc(boolean generateToc) {
        this.generateToc = generateToc;
    }

    public boolean isCleanDitaTemp() {
        return cleanDitaTemp;
    }

    public void setCleanDitaTemp(boolean cleanDitaTemp) {
        this.cleanDitaTemp = cleanDitaTemp;
    }

    public String getCustomizationDir() {
        return customizationDir;
    }

    public void setCustomizationDir(String customizationDir) {
        this.customizationDir = customizationDir;
    }

    public String getCustomCss() {
        return customCss;
    }

    public void setCustomCss(String customCss) {
        this.customCss = customCss;
    }

    /**
     * Launches DITA.
     * 
     * @throws MojoExecutionException
     *             the mojo execution exception
     */
    public abstract void execute() throws MojoExecutionException;

}
