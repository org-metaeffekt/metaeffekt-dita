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
package org.metaeffekt.dita.maven.mojo;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;

import org.metaeffekt.dita.maven.codeextraction.DitaCodeBlockExtractorTask;

/**
 * Processes the project sources and extracts DITA fragments
 * into {@link AbstractDitaContentMojo#getDitaSourceGenDir()}.
 * 
 * @author Siegfried E.
 * @author Bernd A.
 * @author Karsten Klein
 */
@Mojo(
        name = "generate-content",
        requiresDependencyResolution = ResolutionScope.RUNTIME
)
public class GenerateDitaContentFromSourceMojo extends AbstractDitaContentMojo {

    /**
     * Pattern defining, which files are included in the content extraction.
     */
    @Parameter (
            defaultValue = "src/main/java/**/*.java, src/test/java/**/*.java"
    )
    private String extractorIncludePattern = "src/main/java/**/*.java, src/test/java/**/*.java";

    /**
     * Pattern defining, which files are excluded from the content extraction.
     */
    @Parameter
    private String extractorExcludePattern;
    
    /**
     * @throws MojoExecutionException
     *             if an unexpected problem occurs.
     * @throws MojoFailureException
     *             if an expected problem (such as a compilation failure)
     *             occurs.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();
        if (skipProject()) {
            return;
        }

        extractDitaFragmentsFromCode();
    }

    private void extractDitaFragmentsFromCode() {
        // TODO: check whether extraction is enabled; introduce flag to enable/disable
        Project project = new Project();
        project.setBaseDir(getCurrentProject().getBasedir());

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(getCurrentProject().getBasedir());
        
        if (extractorIncludePattern != null) {
            scanner.setIncludes(createPatternArray(extractorIncludePattern));
        }
        if (extractorExcludePattern != null) {
            scanner.setExcludes(createPatternArray(extractorExcludePattern));
        }
        scanner.scan();
        
        String[] files = scanner.getIncludedFiles();

        // TODO: make target structure customizable
        // NOTE-KKL: we generate into the source folder this folder is included into the doc artifact
        //  also the generated dita may include tokens that should be transported in the doc artifact
        //  and not replaced locally.
        File ditaTargetDir = getDitaSourceGenDir();
        
        for (String filename : files) {
            // ensure directory exists (required by later process)
            ditaTargetDir.mkdirs();
            DitaCodeBlockExtractorTask extractor = new DitaCodeBlockExtractorTask();
            extractor.setProject(project);
            extractor.setOutputPath(ditaTargetDir.getAbsolutePath());
            extractor.setSourceDocument(new File(getCurrentProject().getBasedir(), filename).getAbsolutePath());
            extractor.execute();
        }
    }

    private String[] createPatternArray(String pattern) {
        String[] values = pattern.split(",");
        for (int i = 0; i < values.length; i++) {
            values[i] = values[i].trim();
        }
        return values;
    }

}