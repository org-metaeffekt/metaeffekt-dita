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
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

/**
 * Aggregates all documentation artifacts from the depencencies.
 * Dependencies qualify for the aggregation by having the classifier '
 * {@link AbstractAggregateDitaContentMojo#DITA_CLASSIFIER}'.
 * 
 * @author Siegfried E.
 * @author Bernd A.
 * @author Karsten Klein
 */
@Mojo(
        name = "aggregate-content",
        requiresDependencyResolution = ResolutionScope.RUNTIME
)
public class AggregateDitaContentMojo extends AbstractAggregateDitaContentMojo {

    /**
     * To look up Archiver/UnArchiver implementations.
     */
    @Component
    private org.codehaus.plexus.archiver.manager.ArchiverManager archiverManager;

    /**
     * Executes this Mojo.
     * 
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

        Set<Artifact> artifacts = getDependencySets();
        for (Artifact artifact : artifacts) {
            final File location = new File(getDitaSourceDir(), artifact.getArtifactId());
            cleanDirectory(location);
            unpack(artifact.getFile(), location);
            markContent(location, true);
        }
    }

    /**
     * Unpacks the archive file.
     * 
     * @param file
     *            File to be unpacked.
     * @param location
     *            Target folder.
     * @throws MojoExecutionException
     *             if an unexpected problem occurs.
     */
    protected void unpack(File file, File location) throws MojoExecutionException {
        try {
            getLog().info("Unpacking " + file.getPath() + " to " + location.getPath());

            location.mkdirs();

            if (!location.exists() && !location.isDirectory()) {
                throw new MojoExecutionException("Target path is not a directory: " + location.getPath());
            }

            UnArchiver unArchiver;
            unArchiver = getArchiverManager().getUnArchiver(
                    org.codehaus.plexus.util.FileUtils.getExtension(file.getName()));
            unArchiver.setSourceFile(file);

            // FIXME: here we need to be able to provide additional folders
            unArchiver.setDestDirectory(location);
            unArchiver.setOverwrite(true);
            unArchiver.extract();

        } catch (NoSuchArchiverException e) {
            throw new MojoExecutionException("Unknown archiver type", e);
        } catch (ArchiverException e) {
            throw new MojoExecutionException("Error unpacking file: " + file + " to: " + location + "\r\n"
                    + e.toString(), e);
        }
    }

    /**
     * @return the archiverManager
     */
    public org.codehaus.plexus.archiver.manager.ArchiverManager getArchiverManager() {
        return archiverManager;
    }

    /**
     * @param archiverManager
     *            the archiverManager to set
     */
    public void setArchiverManager(org.codehaus.plexus.archiver.manager.ArchiverManager archiverManager) {
        this.archiverManager = archiverManager;
    }
    
}