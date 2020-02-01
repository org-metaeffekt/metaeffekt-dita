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
package org.metaeffekt.dita.maven.mojo;

import java.io.File;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Explicit Mojo to make the local generated content read only.
 * 
 * @author Karsten Klein
 */
@Mojo(
        name = "clean-aggregated-content"
)
public class CleanAggregatedDitaContentMojo extends AbstractAggregateDitaContentMojo {

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
        }
    }

}