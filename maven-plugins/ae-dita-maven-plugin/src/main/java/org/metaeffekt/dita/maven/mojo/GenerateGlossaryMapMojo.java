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
package org.metaeffekt.dita.maven.mojo;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.dom4j.DocumentException;

import org.metaeffekt.dita.maven.glossary.GlossaryMapCreator;

@Mojo(name = "generate-glossary")
public class GenerateGlossaryMapMojo extends AbstractDitaContentMojo {

    @Parameter(defaultValue = "en")
    private String language; 

    @Parameter(defaultValue = "glossary/${language}/gmap_glossary.ditamap")
    private String targetGlossaryMap; 

    @Parameter(defaultValue = "${project.artifactId}/${document.ditamap}")
    private String ditaMap; 

    @Parameter(defaultValue = "-nothing-")
    private String excludePattern; 

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // only run if we can do something
        if(isPomPackagingProject()){
            return;
        }
        
        GlossaryMapCreator glossaryMapCreator = new GlossaryMapCreator();
        glossaryMapCreator.setBaseDir(getDitaSourceDir());
        glossaryMapCreator.setTargetGlossaryMap(new File(getDitaSourceGenDir(), replace(targetGlossaryMap)));
        glossaryMapCreator.setLanguage(language);
        glossaryMapCreator.setDitaMap(new File(replace(ditaMap)));
        
        try {
            glossaryMapCreator.create();
        } catch (DocumentException | IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private String replace(String string) {
        return string.replaceAll("\\$\\{language\\}", language);
    }
}
