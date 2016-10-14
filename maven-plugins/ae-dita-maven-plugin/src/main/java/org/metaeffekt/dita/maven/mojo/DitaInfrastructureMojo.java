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
import java.io.IOException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.metaeffekt.dita.maven.installation.DitaBinFileNameFilter;
import org.metaeffekt.dita.maven.installation.DitaInstallationHelper;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * This goal installs the DITA toolkit. The implementation supports to cache the toolkit with
 * a checksum so that the same toolkit is not installed twice.
 *
 * @author Siegfried E.
 */
@Mojo(
        name = "ensure-dita-toolkit",
        requiresProject = true
)
public class DitaInfrastructureMojo extends AbstractDitaMojo {
    
    /**
     * Artifact resolver, needed to download stuff.
     */
    @Component
    private org.apache.maven.artifact.resolver.ArtifactResolver artifactResolver;

    /**
     * Local Maven repository where artifacts are cached during the build
     * process.
     */
    @Parameter(
            defaultValue = "${localRepository}",
            required = true,
            readonly = true
    )
    private ArtifactRepository localRepository;

    @SuppressWarnings("rawtypes")
    @Parameter(
            defaultValue = "${project.remoteArtifactRepositories}"
    )
    private java.util.List remoteRepositories;
    
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
     * Group id of the DITA Open Toolkit to use.
     */
    @Parameter(
            defaultValue = "ae-dita-toolkit"
    )
    private String ditaToolkitArtifactId;
    
    /**
     * Group id of the DITA Open Toolkit to use.
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

        // create new installation installation with the according parameters
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
     * @throws MojoExecutionException
     */
    public Artifact getDitaToolkitDependency() throws MojoExecutionException {
        Artifact toolkitArtifact;

        toolkitArtifact = new DefaultArtifact(ditaToolkitGroupId,
                ditaToolkitArtifactId, VersionRange.createFromVersion(ditaToolkitVersion),
                Artifact.SCOPE_COMPILE, DitaInstallationHelper.DITA_TOOLKIT_TYPE,
                ditaToolkitClassifier, new DefaultArtifactHandler(
                        DitaInstallationHelper.DITA_TOOLKIT_TYPE));

        try {
            artifactResolver.resolve(toolkitArtifact, remoteRepositories, localRepository);
        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException("Dita Toolkit artifact cannot be resolved.", e);
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("Exception while resolving the Dita Toolkit.", e);
        }

        return toolkitArtifact;
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