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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * An abstract class summarizing all common parameters for the Mojos in this
 * Maven Plugin.
 *
 * @author Siegfried E.
 * @author Bernd A.
 */
public abstract class AbstractDitaMojo extends AbstractMojo {

    /**
     * To get access to the Maven session.
     */
    @Parameter(
            property = "session"
    )
    private MavenSession mavenSession;

    /**
     * To get access to the properties of the current Maven project.
     */
    @Parameter(
            property = "project",
            required = true,
            readonly = true
    )
    private MavenProject currentProject;

    /**
     * Returns the current MavenSession.
     * 
     * @return the current MavenSession.
     */
    protected MavenSession getMavenSession() {
        return this.mavenSession;
    }

    /**
     * Returns the current MavenProject.
     * 
     * @return the current MavenProject.
     */
    protected MavenProject getCurrentProject() {
        return this.currentProject;
    }

    /**
     * Method was introduced to provide a way for 'injecting' a MavenProject for
     * testing purposes.
     * <br />
     * See as an example in the integration test <i>GenerateDocumentationMojoTest</i>.
     * 
     * @param project A {@link MavenProject} object.
     */
    public void setMockedProject(MavenProject project) {
        currentProject = project;
        getLog().warn("***********************************************************************************************");
        getLog().warn("A new MavenProject object was injected  in the current Mojo for testing purposes!");
        getLog().warn("If you see this message outside of an unit or integration test, something might be wrong!");
        getLog().warn("***********************************************************************************************");
    }

    /**
     * Method was introduced to provide a way for 'injecting' a MavenSession for
     * testing purposes.
     * <br />
     * See as an example in the integration test <i>GenerateDocumentationMojoTest</i>.
     * 
     * @param session A {@link MavenSession} object.
     */
    public void setMockedSession(MavenSession session) {
        mavenSession = session;
        getLog().warn("***********************************************************************************************");
        getLog().warn("A new MavenSession object was injected  in the current Mojo for testing purposes!");
        getLog().warn("If you see this message outside of an unit or integration test, something might be wrong!");
        getLog().warn("***********************************************************************************************");
    }

    protected boolean isPomPackagingProject() {
        validateProject(getCurrentProject());
        return "pom".equalsIgnoreCase(getCurrentProject().getPackaging());
    }

    protected boolean isJarPackagingProject() {
        validateProject(getCurrentProject());
        return "jar".equalsIgnoreCase(getCurrentProject().getPackaging());
    }

    private static void validateProject(MavenProject project) {
        if (project == null) {
            throw new IllegalStateException("Unexpected reference to a null maven project.");
        }
    }

    protected boolean skipProject() {
        return isPomPackagingProject() || "true".equalsIgnoreCase(System.getProperty("maven.dita.skip"));
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // only run if we can do something
        if(isPomPackagingProject()){
            return;
        }
    }
}
