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

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.metaeffekt.dita.maven.AbstractDitaTest;
import org.metaeffekt.dita.maven.installation.DitaInstallationHelper;

/**
 * Tests for {@link GenerateDocumentationMojo}.
 */
public class GenerateDocumentationMojoTest extends AbstractDitaTest {

    private static GenerateDocumentationMojo docMojo = null;
    private static final String DITA_INSTALLATION = "DITA-OT";
    private static File ditaInstallation = null;
    private static String ditaHome = null;

    @BeforeClass
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

    @Before
    public void initDitaHome() {
        docMojo.getMavenSession().getExecutionProperties().setProperty(
                DitaInstallationHelper.DITA_TOOLKIT_ROOT_PROPERTY, ditaHome);
    }

    @Test
    public void testBogus() {
        assertTrue(true);
    }

    // @Test
    // public void testMakeDitaExecutable() {
    // try {
    //
    // docMojo.setDitaHome(ditaHome);
    // docMojo.makeDitaExecutable();
    // assertDitaExecutable();
    //
    // } catch (Exception e) {
    // fail("Setting execution bit failed!\n" + e);
    // }
    // }
    //
    // private void assertDitaExecutable() throws Exception {
    //
    // String antBin = null;
    // File[] antExecs = null;
    // File antDir = null;
    // File ditaExec = null;
    //
    // ditaExec = new File(ditaHome, "startcmd.bat");
    // assertTrue("startcmd.bat is not executable!", ditaExec.canExecute());
    // ditaExec = new File(ditaHome, "startcmd.sh");
    // assertTrue("startcmd.sh is not executable!", ditaExec.canExecute());
    //
    // antBin = ditaHome + "/tools/ant/bin";
    // antDir = new File(antBin);
    // antExecs = antDir.listFiles(new AntFileNameFilter());
    //
    // for (File currentFile : antExecs) {
    //
    // assertTrue(currentFile.getName() + "is not executable!",
    // currentFile.canExecute());
    // }
    // }

    // @Test
    // public void testProvideAbsolutePaths() {
    //
    // try {
    // docMojo.setDitavalFile(SOME_RELATIVE_DIR);
    // docMojo.setInputDirectory(SOME_RELATIVE_DIR);
    // docMojo.setOutputDirectory(SOME_RELATIVE_DIR);
    // docMojo.setTopicMapPath(SOME_RELATIVE_DIR);
    //
    // docMojo.provideAbsolutePaths();
    //
    // assertTrue(new File(docMojo.getDitavalFile()).isAbsolute());
    // assertTrue(new File(docMojo.getInputDirectory()).isAbsolute());
    // assertTrue(new File(docMojo.getOutputDirectory()).isAbsolute());
    // assertTrue(new File(docMojo.getTopicMapPath()).isAbsolute());
    // } catch(Exception e) {
    // fail("Failed testing provideAbsolutePaths()!\n" + e);
    // }
    // }
    //
    // @Test
    // public void testProvideAbsolutePathsNoNPE() {
    //
    // try {
    //
    // docMojo.setDitaHome(null);
    // docMojo.setDitavalFile(null);
    // docMojo.setInputDirectory(null);
    // docMojo.setOutputDirectory(null);
    // docMojo.setTopicMapPath(null);
    //
    // docMojo.provideAbsolutePaths();
    //
    // } catch(Exception e) {
    // fail("provideAbsolutePaths() must not fail even when null is given" + e);
    // }
    // }
    //
    // @Test
    // public void testProvideAbsolutePathsWithAbsArgs() {
    //
    // docMojo.setDitaHome(SOME_ABSOLUTE_PATH);
    // docMojo.setDitavalFile(SOME_ABSOLUTE_PATH);
    // docMojo.setInputDirectory(SOME_ABSOLUTE_PATH);
    // docMojo.setOutputDirectory(SOME_ABSOLUTE_PATH);
    // docMojo.setTopicMapPath(SOME_ABSOLUTE_PATH);
    //
    // docMojo.provideAbsolutePaths();
    //
    // assertTrue(new File(docMojo.getDitaHome()).isAbsolute());
    // assertTrue(new File(docMojo.getDitavalFile()).isAbsolute());
    // assertTrue(new File(docMojo.getInputDirectory()).isAbsolute());
    // assertTrue(new File(docMojo.getOutputDirectory()).isAbsolute());
    // assertTrue(new File(docMojo.getTopicMapPath()).isAbsolute());
    // }
    //
    // @Test
    // public void testIncludesAndExcludesForDitaMaps() throws
    // MojoExecutionException, MojoFailureException {
    // docMojo.setDitaHome(SOME_ABSOLUTE_PATH);
    // docMojo.setDitavalFile(SOME_ABSOLUTE_PATH);
    // docMojo.setInputDirectory(SOME_ABSOLUTE_PATH);
    // docMojo.setOutputDirectory(SOME_ABSOLUTE_PATH);
    // docMojo.setTopicMapPath(SOME_ABSOLUTE_PATH);
    //
    // // Search all ditamaps
    // File targetDir =
    // FileUtils.toFile(getClass().getResource("/generateDocumentationMojoTest/ditamaps"));
    // String[] expected = docMojo.findTopicMaps(targetDir);
    // assertTrue(expected.length == 2);
    //
    // //Use includes to select example1
    // docMojo.setIncludes(new String[]{"**/example1.ditamap"});
    // docMojo.setExcludes(null);
    // expected = docMojo.findTopicMaps(targetDir);
    // assertTrue(expected.length == 1);
    // assertEquals(expected[0], "example1.ditamap");
    //
    // //Use excludes to unselect example1
    // docMojo.setIncludes(null);
    // docMojo.setExcludes(new String[]{"**/example1.ditamap"});
    // expected = docMojo.findTopicMaps(targetDir);
    // assertTrue(expected.length == 1);
    // assertEquals(expected[0], "example2.ditamap");
    //
    // //Unselect all
    // try {
    // docMojo.setIncludes(null);
    // docMojo.setExcludes(new String[]{"**/**.ditamap"});
    // expected = docMojo.findTopicMaps(targetDir);
    // } catch (MojoExecutionException ex) {
    // assertTrue(true);
    // }
    //
    // //No include or exclude set
    // docMojo.setIncludes(null);
    // docMojo.setExcludes(null);
    // expected = docMojo.findTopicMaps(targetDir);
    // assertTrue(expected.length == 2);
    // assertEquals(expected[0], "example1.ditamap");
    // assertEquals(expected[1], "example2.ditamap");
    //
    // }
}
