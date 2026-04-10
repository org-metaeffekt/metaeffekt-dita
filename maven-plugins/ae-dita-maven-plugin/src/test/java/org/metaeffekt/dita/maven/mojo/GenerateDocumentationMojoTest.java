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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metaeffekt.dita.maven.installation.DitaInstallationHelper;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link GenerateDocumentationMojo}.
 */
public class GenerateDocumentationMojoTest {

    private static final String DITA_INSTALLATION = "DITA-OT";
    private static GenerateDocumentationMojo docMojo = null;
    private static File ditaInstallation = null;
    private static String ditaHome = null;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        docMojo = new GenerateDocumentationMojo();
        provideMocks();

        ditaInstallation = FileUtils.toFile(GenerateDocumentationMojoTest.class.getClassLoader().getResource(
                DITA_INSTALLATION));
        ditaHome = ditaInstallation.getAbsolutePath();
    }

    /**
     * Provide the Mocks necessary for emulating a Maven environment.
     */
    private static void provideMocks() {

        MavenProject mavenProject = null;
        MavenSession mavenSession = null;

        mavenSession = new MavenSessionMock();

        mavenProject = new MavenProject();
        // FIXME: Figure out if this line is actually needed or not!
        // mavenProject.setBasedir(new File("."));

        docMojo.setMockedSession(mavenSession);
        docMojo.setMockedProject(mavenProject);
    }

    @BeforeEach
    public void initDitaHome() {
        docMojo.getMavenSession().getExecutionProperties().setProperty(
                DitaInstallationHelper.DITA_TOOLKIT_ROOT_PROPERTY, ditaHome);
    }

    @Test
    public void testBogus() {
        assertTrue(true);
    }

}
