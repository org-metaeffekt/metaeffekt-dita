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

import org.metaeffekt.dita.maven.generation.*;
import org.metaeffekt.dita.maven.installation.DitaInstallationHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Compile and create - potentially multiple - outputs during compile phase.
 *
 * @author Bernd A.
 * @author Siegfried E.
 * @author Karsten Klein
 * @author Adrian R.
 */
@Mojo(
        name = "generate-documentation"
)
public class GenerateDocumentationMojo extends AbstractDitaMojo {

    /**
     * Manages the created artifacts.
     */
    @Component
    private ArtifactHandlerManager artifactHandlerManager;

    /**
     * Directory in which the dita documents are build.
     */
    @Parameter(
            defaultValue = "${project.build.directory}/dita",
            property = "maven.ae.documentation.build.dir"
    )
    private File ditaBuildDir;

    /**
     * Directory in which the dita documents are stored to.
     */
    @Parameter(
            defaultValue = "${project.build.directory}",
            property = "maven.ae.documentation.target.dir"
    )
    private File ditaTargetDir;

    /**
     * Directory where dita resources are located.
     */
    @Parameter(
            defaultValue = "${project.build.directory}/dita",
            property = "maven.ae.documentation.dita.dir"
    )
    private File ditaGenerationDir;

    @Parameter(
            defaultValue = "true"
    )
    private boolean ditaCleanTemp;

    /**
     * Is the current document a draft or not.
     */
    @Parameter(
            defaultValue = "false",
            property = "maven.ae.documentation.dita.draft"
    )
    private boolean ditaIsDraft;

    /**
     * Flag indicating whether to generate a TOC.
     *
     * ToDo: Remove, since it has no effect.
     */
    @Parameter(
            defaultValue = "true",
            property = "maven.ae.documentation.dita.generate_toc"
    )
    @Deprecated
    private boolean generateToc;

    /**
     * Document items to be included the dita.
     */
    @Parameter(
            required = true
    )
    private ArrayList<DocumentItem> documentItems;

    /**
     * Can be used to point the plugin to a local DITA OT installation.
     */
    @Parameter(
            property = "maven.ae.documentation.dita.toolkit"
    )
    private String ditaHome;

    /**
     * To enable and disable the DITA OT grammar cache.
     * <br />
     * Having the grammar cache enabled might lead to issues with DITA files containing XML entities.
     *
     */
    @Parameter(
            defaultValue = "false",
            property = "maven.ae.documentation.dita.enable.grammar.cache"
    )
    private boolean enableGrammarCache;

    @Parameter(
            property = "maven.ae.documentation.dita.customization.dir"
    )
    private File ditaCustomizationDir;

    /**
     * All TransTypes creating PDF format.
     */
    public static final List<TransType> PDF_FORMATS = Arrays.asList(TransType.PDF, TransType.AE_STANDARD_PDF);

    /**
     * All TransTypes generating more than one output files.
     */
    public static final List<TransType> MULTI_FILE_FORMATS = Arrays.asList(TransType.HTML5, TransType.HTMLHELP, TransType.XHTML);

    /**
     * Iterates of the {@link org.metaeffekt.dita.maven.generation.DocumentItem} objects defined
     * in the plugin configuration. For each of the items to the following:
     * <ul>
     *     <li>Validate the configuration of the item.</li>
     *     <li>Setup the DITA OT Ant-based launcher.</li>
     *     <li>Generate the documentation in the specified output format.</li>
     *     <li>Do postprocessing depending on the type of documentation created (e.g. create ZIP-archive).</li>
     *     <li>Attach the generated artifact to the current Maven module.</li>
     * </ul>
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();
        if (skipProject()) {
            if (isPomPackagingProject()) {
                getLog().info("Skipping execution project of packaging type POM.");
            }
            return;
        }

        // iterate over the document items defined in the POM in the plugin configuration
        for (DocumentItem documentItem: documentItems) {
            // validate the configuration given and locate the DITA Map
            documentItem.validate();
            File ditaMap = new File(ditaBuildDir, documentItem.getDitaMap());
            if (!ditaMap.exists()) {
                throw new MojoExecutionException("DITA Map '" + ditaMap.getAbsolutePath() + "' does not exist!");
            } else {
                getLog().info("Found DITA Map: " + ditaMap.getAbsolutePath());
            }
            String documentName = documentItem.getTargetFileName(getCurrentProject().getVersion());
            getLog().info("Document name: " + documentName);

            // Common Bootstrapping

            // setup the Ant task for launching the DITA OT
            ExternalAntDitaLauncher dita = new ExternalAntDitaLauncher();
            dita.setInputMap(ditaMap.getAbsolutePath());
            dita.setBaseDir(ditaGenerationDir.getAbsolutePath());
            dita.setDitaDir(getDitaHome());
            dita.setTranstype(documentItem.getDitaTranstype().toString());
            dita.setCleanDitaTemp(ditaCleanTemp);
            dita.setLog(getLog());
            dita.setGenerateToc(generateToc);
            dita.setDraft(ditaIsDraft);
            dita.setGrammarCacheEnabled(enableGrammarCache);
            if (ditaCustomizationDir != null) {
                dita.setCustomizationDir(ditaCustomizationDir.getAbsolutePath());
            }
            dita.setCustomCss(documentItem.getCustomCss());
            addDitavalFile(dita, ditaMap);
            dita.setTempDir(getTempDirectory());

            ArtifactHandler handler = artifactHandlerManager.getArtifactHandler(getTransTypeFileExtension(documentItem.getDitaTranstype()));
            // the handler is not directly used, so the jar handler is also fine, if there is
            // no handler available for PDF documents
            if (handler == null) {
                handler = artifactHandlerManager.getArtifactHandler("jar");
            }

            if (MULTI_FILE_FORMATS.contains(documentItem.getDitaTranstype())) {
                dita.setOutputDir(
                        ditaTargetDir.getAbsolutePath() + File.separator
                                + documentItem.getArtifactId()
                                + ((documentItem.getArtifactClassifier().equals("")) ? "" : "-" + documentItem.getArtifactClassifier()));

                // generate documentation
                dita.execute();

                // copy additional resources, if any
                if (documentItem.getAdditionalResources() != null && documentItem.getAdditionalResources().isDirectory()) {
                    try {
                        FileUtils.copyDirectory(documentItem.getAdditionalResources(), new File(dita.getOutputDir()));
                    } catch (IOException e) {
                        getLog().error("Could not copy additional resources.", e);
                    }
                }

                // create zip of generated HTML-type documentation
                getLog().info("Create ZIP of generated documentation.");
                Zip zip = new Zip();
                zip.setProject(new Project());
                zip.setBasedir(new File(dita.getOutputDir()));
                zip.setDestFile(new File(
                                ditaTargetDir,
                                documentItem.getArtifactId() + ((documentItem.getArtifactClassifier() == null || documentItem.getArtifactClassifier().equals("")) ? "" : "-" + documentItem.getArtifactClassifier()) + ".zip")
                );
                zip.execute();

                AttachedZipArtifact artifact = new AttachedZipArtifact(
                        getCurrentProject().getArtifact(), // artifact
                        handler, // handler
                        new File(
                                ditaTargetDir,
                                documentItem.getArtifactId() + ((documentItem.getArtifactClassifier() == null || documentItem.getArtifactClassifier().equals("")) ? "" : "-" + documentItem.getArtifactClassifier()) + ".zip"), // fileToAttach
                        documentItem.getArtifactId(), // artifactId
                        documentItem.getArtifactClassifier(), // classifier
                        getCurrentProject().getVersion() // version
                );
                getLog().info("Attaching artifact: " + artifact.toString());
                getCurrentProject().addAttachedArtifact(artifact);
            } else if (PDF_FORMATS.contains(documentItem.getDitaTranstype())) {
                dita.setOutputDir(ditaTargetDir.getAbsolutePath());

                // generate documentation
                dita.execute();

                getLog().info("Renaming generated PDF file.");
                File fromPdf = getGeneratedPdfFile(dita, documentItem);
                File toPdf = getMavenizedPdfFile(dita, documentItem);
                try {
                    if (!fromPdf.getCanonicalPath().equals(toPdf.getCanonicalPath())) {
                        FileUtils.moveFile(
                                getGeneratedPdfFile(dita, documentItem),
                                toPdf
                        );
                    } else {
                        getLog().warn("No need to rename the PDF file, since the generated PDF has already the expected name.");
                    }
                } catch (IOException e) {
                    getLog().error("From File: " + fromPdf.getAbsolutePath());
                    getLog().error("To File: " + toPdf.getAbsolutePath());
                    throw new MojoExecutionException("Could not rename PDF file.");
                }

                AttachedPdfArtifact artifact = new AttachedPdfArtifact(
                        getCurrentProject().getArtifact(), // artifact
                        handler, // handler
                        toPdf, // fileToAttach
                        documentItem.getArtifactId(), // artifactId
                        documentItem.getArtifactClassifier(), // classifier
                        getCurrentProject().getVersion() // version
                );
                getLog().info("Attaching artifact: " + artifact.toString());
                getCurrentProject().addAttachedArtifact(artifact);
            } else {
                throw new MojoFailureException("The given TransType is not yet implemented.");
            }
        }
    }

    private String getTempDirectory() throws MojoExecutionException {
        // ensure the tmp dir does not mix with the other dita content
        File tmpDir = new File(ditaBuildDir.getAbsoluteFile() + "-tmp");
        try {
            FileUtils.forceMkdir(tmpDir);
        } catch (IOException e) {
            throw new MojoExecutionException("Could not create temporary DITA directory.", e);
        }
        return tmpDir.getAbsolutePath();
    }

    private String getTransTypeFileExtension(TransType transType) throws MojoExecutionException {
        if (PDF_FORMATS.contains(transType)) {
            return "pdf";
        }
        if (MULTI_FILE_FORMATS.contains(transType)) {
            return "zip";
        }
        throw new MojoExecutionException("Could not determine file extension for given trans type.");
    }

    /**
     * Checks to see if a ditaval file exists in the same directory and has the same name
     * as the Dita map. If so, adds it to the Launch configuration.
     *
     * Ditaval files support runtime processing of tags in Dita files.
     * It is a good way of handling different target audiences in a Dita file.
     */
    private void addDitavalFile(AbstractDitaLauncher dita, File ditaMap) {

        File ditavalFile = new File(ditaMap.getParent(),
                ditaMap.getName().replace(".ditamap", ".ditaval"));

        if (ditavalFile.exists()) {
            dita.setDitavalFile(ditavalFile.getPath());
        }
    }

    /**
     * Convenience method creating a pointer to the generated PDF file.
     *
     * @param ditaLauncher The launcher that created the PDF file.
     * @param documentItem The according {@link org.metaeffekt.dita.maven.generation.DocumentItem}.
     * @return {@link java.io.File} object pointing to the generated file.
     */
    private File getGeneratedPdfFile(ExternalAntDitaLauncher ditaLauncher, DocumentItem documentItem) {
        return new File(
                ditaLauncher.getOutputDir() + File.separator
                        + FilenameUtils.getBaseName(documentItem.getDitaMap())
                        + ".pdf"
        );
    }

    /**
     * Convenience method creating a {@link java.io.File} object representing the
     * desired PDF file name for generated PDF documentation.
     *
     * @param ditaLauncher The launcher that created the original PDF file.
     * @param documentItem The according {@link org.metaeffekt.dita.maven.generation.DocumentItem}.
     * @return {@link java.io.File} object pointing to the desired file location.
     */
    private File getMavenizedPdfFile(ExternalAntDitaLauncher ditaLauncher, DocumentItem documentItem) {
        return new File(
                ditaLauncher.getOutputDir() + File.separator
                + documentItem.getArtifactId()
                + ((documentItem.getArtifactClassifier() == null || documentItem.getArtifactClassifier().equals("")) ? "" : "-" + documentItem.getArtifactClassifier()) // append the classifier, if defined
                + ".pdf"
        );
    }

    /**
     * Retrieve the home directory of the temporary DITA Open Toolkit installation.
     *
     * @return The home directory as a String.
     */
    public String getDitaHome() {
        if (ditaHome == null) {
            ditaHome = getMavenSession().getUserProperties().getProperty(DitaInstallationHelper.DITA_TOOLKIT_ROOT_PROPERTY);
        }
        getLog().info("Using DITA Home: " + ditaHome);
        return ditaHome;
    }

    public void setDitaHome(String ditaHome) {
        this.ditaHome = ditaHome;
    }
}