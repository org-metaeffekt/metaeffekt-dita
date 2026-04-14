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
package org.metaeffekt.dita.maven.mojo;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.metaeffekt.dita.maven.installation.DitaBinFileNameFilter;
import org.metaeffekt.dita.maven.installation.DitaInstallationHelper;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This goal installs the DITA toolkit. The implementation supports to cache the toolkit with
 * a checksum so that the same toolkit is not installed twice.
 *
 * @author Siegfried E.
 */
@Mojo(name = "ensure-dita-toolkit", requiresProject = true)
public class DitaInfrastructureMojo extends AbstractDitaMojo {

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repositorySystemSession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    private List<RemoteRepository> remoteProjectRepositories;

    @Inject
    private RepositorySystem repositorySystem;

    /**
     * This parameter points to the location where the plugin will unzip the
     * needed DITA Open Toolkit to.
     */
    @Parameter(
            defaultValue = "${java.io.tmpdir}/dita",
            property = "ae.dita.toolkit.cache.dir"
    )
    private File ditaToolkitCacheDir;

    /**
     * The version of the DITA Open Toolkit to use.
     * <br />
     * The sensible default is to use the version of the plugin
     * as version of the DITA Toolkit to use.
     */
    @Parameter(
            required = true,
            defaultValue = "${plugin.version}"
    )
    private String ditaToolkitVersion;

    /**
     * Group id of the DITA Open Toolkit to use.
     */
    @Parameter(
            defaultValue = "org.metaeffekt.dita"
    )
    private String ditaToolkitGroupId;

    /**
     * Artifact id of the DITA Open Toolkit to use.
     */
    @Parameter(
            defaultValue = "ae-dita-toolkit"
    )
    private String ditaToolkitArtifactId;

    /**
     * Classifier of the DITA Open Toolkit to use.
     */
    @Parameter(
            defaultValue = "bin"
    )
    private String ditaToolkitClassifier;

    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();
        if (skipProject()) {
            return;
        }

        File installArchive = getDitaToolkitDependency().getFile();
        getLog().info("DITA Open Toolkit install archive: " + installArchive.getAbsolutePath());
        String toolkitPath = "";

        // create new installation with the according parameters
        DitaInstallationHelper installHelper =
                new DitaInstallationHelper(ditaToolkitCacheDir, installArchive);

        try {
            if (!installHelper.isInstalled() || !installHelper.isConsistent()) {
                getLog().info("No consistent DITA Open Toolkit installation found. Installing ...");
                installHelper.install();
            }
            toolkitPath = installHelper.getDitaToolkitRoot().getAbsolutePath();

            getLog().info("Using DITA Open Toolkit at: " + toolkitPath);

            // ensure that the binaries of the dita toolkit are executable
            makeDitaExecutable(installHelper.getDitaToolkitRoot());

            getMavenSession().getUserProperties().setProperty(
                    DitaInstallationHelper.DITA_TOOLKIT_ROOT_PROPERTY, toolkitPath);
        } catch (IOException e) {
            throw new MojoExecutionException("Error while checking or installing the DITA Open Toolkit.", e);
        }
    }

    /**
     * Resolve the DITA Open Toolkit and throw an exception when the dependency
     * is not found.
     *
     * @return Pointer to the DITA Open Toolkit.
     * @throws MojoExecutionException MojoExecutionException
     */
    public Artifact getDitaToolkitDependency() throws MojoExecutionException {

        DefaultArtifact toolkitArtifact = new DefaultArtifact(
                ditaToolkitGroupId,
                ditaToolkitArtifactId,
                ditaToolkitClassifier,
                DitaInstallationHelper.DITA_TOOLKIT_TYPE,
                ditaToolkitVersion
        );

        try {
            final ArtifactResult artifactResult = repositorySystem.resolveArtifact(repositorySystemSession, new ArtifactRequest(toolkitArtifact, remoteProjectRepositories, null));
            if (!artifactResult.isResolved()) {
                throw new MojoExecutionException("Exception while resolving the Dita Toolkit. Reasons: %s".formatted(StringUtils.join(artifactResult.getExceptions(), '\n')));
            }
            if (artifactResult.isMissing()) {
                throw new MojoExecutionException("Dita Toolkit artifact [%s] cannot be resolved. Reasons: %s".formatted(artifactResult.getRequest().getArtifact(), StringUtils.join(artifactResult.getExceptions(), '\n')));
            }
            return artifactResult.getArtifact();
        } catch (org.eclipse.aether.resolution.ArtifactResolutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * If a new DITA Open Toolkit is installed, this method makes the necessary files
     * executable and the correct line breaks in place.
     *
     * @param ditaHome Root directory of the DITA Open Toolkit.
     */
    private void makeDitaExecutable(File ditaHome) throws MojoExecutionException {
        String ditaBin = null;
        File[] ditaExecs = null;
        File binDir = null;
        File ditaExec = null;

        try {
            if (ditaHome == null) {
                throw new MojoExecutionException("The root directory of the DITA Open Toolkit could not be found.");
            }

            ditaExec = new File(ditaHome, "startcmd.bat");
            ditaExec.setExecutable(true, false);
            ditaExec = new File(ditaHome, "startcmd.sh");
            ditaExec.setExecutable(true, false);

            ditaBin = ditaHome + "/bin";
            binDir = new File(ditaBin);
            ditaExecs = binDir.listFiles(new DitaBinFileNameFilter());

            for (File currentFile : ditaExecs) {
                currentFile.setExecutable(true, false);
            }

        } catch (Exception e) {
            getLog().error("Failed while trying to make DITA files executable!", e);
            throw new MojoExecutionException("Failed while trying to make DITA Open Toolkit binaries executable!", e);
        }
    }
}