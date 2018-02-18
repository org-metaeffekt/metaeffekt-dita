/**
 * Copyright 2009-2018 the original author or authors.
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
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.util.StringUtils;

import java.io.File;

/**
 * A document item can be used in the configuration section of the DITA Maven Plugin.
 * It contains all relevant information on which transtype to use and on the
 * final name of the document.
 * 
 * Several properties of this class have been deprecated to provide
 * an alternative naming approach for documents. The convention implemented
 * here is too specific. Consumers should be able to determine their convention
 * while being harnessed by the Maven requirements. The new approach is to specify
 * only the artifact id (maven terminology) of the resulting documentation artifact
 * all other items are either derived from the pom or set to a vital default (classifier,
 * artifact type). The transtype to control the DITA OT can be specified explicitly and
 * independently from the artifact type.
 * 
 * The imposed naming convention is as follows:
 * ${artifactId}-${version}[-${artifactClassifier}].${artifactType}. ArtifactId and
 * artifact classifier can be chosen freely according to company guidelines. The remaining
 * structure prevent undesired version mixtures. The groupId as well as version are derived
 * from the POM.
 * 
 * @author Karsten Klein
 */
public class DocumentItem {
    
    /**
     * The ditamap of the document
     */
    @Parameter(
            required = true
    )
    private String ditaMap;
    
    /**
     * The artifactType for the document
     */
    @Parameter(
        defaultValue = "AE_STANDARD_PDF"
    )
    private TransType ditaTranstype = TransType.AE_STANDARD_PDF;
    
    /**
     * The artifactId for the document
     */
    @Parameter
    private String artifactId;
    
    /**
     * The artifactClassifier for the document
     */
    @Parameter(
            defaultValue = ""
    )
    private String artifactClassifier;

    /**
     * An optional folder location with additional resources.
     */
    @Parameter
    private File additionalResources;

    @Parameter
    private String customCss;
    
    public String getArtifactId() {
        return artifactId;
    }
    
    public void setArtifactId(String documentationFileName) {
        this.artifactId = documentationFileName;
    }
    
    public String getArtifactClassifier() {
        return artifactClassifier;
    }

    public void setArtifactClassifier(String classifier) {
        this.artifactClassifier = classifier;
    }

    public String getTargetFileName(String version) {
        StringBuilder sb = new StringBuilder(getArtifactId());
        sb.append("-");
        sb.append(version);
        if (StringUtils.hasText(getArtifactClassifier())) {
            sb.append("-");
            sb.append(getArtifactClassifier());
        }
        if (getDitaTranstype() == TransType.PDF) {
            sb.append(".").append("pdf");
        }
        return sb.toString();
    }
    
    public void validate() throws MojoExecutionException {
        String ditaMap = getDitaMap();
        if (ditaMap == null || ditaMap.length() == 0) {
            throw new MojoExecutionException("Please ensure documentItems contain ditaMap settings!");
        }
    }

    public TransType getDitaTranstype() {
        return ditaTranstype;
    }

    public void setDitaTranstype(TransType ditaTranstype) {
        this.ditaTranstype = ditaTranstype;
    }

    public String getDitaMap() {
        return ditaMap;
    }

    public void setDitaMap(String ditaMap) {
        this.ditaMap = ditaMap;
    }

    public File getAdditionalResources() {
        return additionalResources;
    }

    public void setAdditionalResources(File additionalResources) {
        this.additionalResources = additionalResources;
    }

    public String getCustomCss() {
        return customCss;
    }

    public void setCustomCss(String customCss) {
        this.customCss = customCss;
    }
}