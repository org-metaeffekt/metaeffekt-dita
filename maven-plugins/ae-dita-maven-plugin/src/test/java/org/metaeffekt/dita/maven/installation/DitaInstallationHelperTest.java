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
package org.metaeffekt.dita.maven.installation;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the correct behavior of the DitaInstallationHelper.
 *
 * @author Siegfried E.
 *
 */
public class DitaInstallationHelperTest {

    /**
     * The MD5 checksum of Dita Toolkit Archive mock.
     */
    public static final String DITA_ARCHIVE_CHECKSUM = "28da645fd00eb362f797c3d0cccea096";

    /**
     * The aggregated MD5 checksum of the expanded Dita Toolkit archive.
     */
    public static final String DITA_EXPANDED_CHECKSUM = "445b0053ae67796c8da928cc4f15c6a9";

    /**
     * Object of interest.
     */
    DitaInstallationHelper helper;

    /**
     * This folder is root to all Dita Toolkit installations.
     */
    File testDitaInstallationCache;

    /**
     * Archive containing some content that imitates a real life Dita Toolkit.
     */
    File installationArchiveMock;

    @BeforeEach
    public void setUp() throws Exception {
        // create a temporary directory for test installations
        testDitaInstallationCache = new File("target" + File.separator + "test-data" + File.separator
                + File.createTempFile("dita-", "-install-test").getName());
        testDitaInstallationCache.mkdirs();

        // get the test installation archive
        installationArchiveMock = new File(this.getClass().getClassLoader().getResource(
                "dita-installation-test/dita-toolkit-dummy.zip").toURI());

        helper = new DitaInstallationHelper(testDitaInstallationCache, installationArchiveMock);
    }

    @AfterEach
    public void tearDown() throws Exception {
        FileUtils.forceDelete(testDitaInstallationCache);
    }

    /**
     * Test the correct calculation of the checksum for the installation
     * archive.
     */
    @Test
    public void test_getInstallationArchiveChecksum() throws Exception {
        assertEquals(DITA_ARCHIVE_CHECKSUM, helper
                .getInstallationArchiveChecksum(), "The checksum was not as expected.");
    }

    /**
     * Test the correct behavior when bogus values are given.
     *
     * @throws Exception
     */
    @Test
    public void testBoundaries_getInstallationArchiveChecksum() throws Exception {
        helper.setInstallationFolder(null);
        assertEquals(DITA_ARCHIVE_CHECKSUM, helper
                .getInstallationArchiveChecksum(), "The checksum was not as expected.");

        helper.setInstallationArchive(null);
        assertThrows(NullPointerException.class, () ->
                        helper.getInstallationArchiveChecksum(),
                "NPE should have been thrown.");
    }

    /**
     * Check if the {@link DitaInstallationHelper#isInstalled()} and
     * {@link DitaInstallationHelper#install()} work as expected under normal
     * conditions.
     */
    @Test
    public void test_isInstalled_and_install() throws Exception {
        assertFalse(helper.isInstalled(), "Result should have been false.");

        // install the Dita Toolkit
        assertTrue(helper.install(), "Result should have been true, Dita installation may have failed.");

        // check if installation was successful
        assertTrue(helper.isInstalled(), "Result should have been true.");

        // this assertion is very instable. Needs in-depth analysis

        // // check if the checksum file has been created with the correct
        // checksum
        // assertEquals(
        // "The stored checksum differs from the expected one.",
        // DITA_EXPANDED_CHECKSUM,
        // FileUtils.readFileToString(new
        // File(installation.getDitaToolkitRoot().getParentFile(),
        // DitaInstallationHelper.AGGREGATED_CHECKSUM_FILE))
        // );
    }

    /**
     * Test if {@link DitaInstallationHelper#isConsistent()} does work as
     * expected.
     *
     * @throws Exception
     */
    @Test
    public void test_isConsistent_and_install() throws Exception {
        // install the Dita Toolkit
        helper.install();

        // check if the built-in consistency check does work
        assertTrue(helper.isConsistent(), "Consistency check failed, but shouldn't have.");

        // now changing the content of the installed dita and see if the
        // consistency check picks it up
        File toolkitRoot = helper.getDitaToolkitRoot();
        FileUtils.forceDelete(new File(toolkitRoot, "build.xml"));

        // now check again for consistency
        assertFalse(helper.isConsistent(), "Consistency check succeded, but shouldn't have.");

        // now do a reinstall since the installation is corrupt
        assertTrue(helper.install(), "Subsequent installation should have succeeded but didn't.");
    }

    /**
     * Test the boundaries of {@link DitaInstallationHelper#isConsistent()}.
     *
     * @throws Exception
     */
    @Test
    public void testBoundaries_isConsistent() throws Exception {
        // install the Dita Toolkit
        helper.install();

        // delete the checksum file to provoke internal exception
        File checksumFile = new File(helper.getDitaToolkitRoot().getParentFile(),
                DitaInstallationHelper.AGGREGATED_CHECKSUM_FILE);
        FileUtils.forceDelete(checksumFile);

        // check that the change is correctly picked up
        assertFalse(helper.isConsistent(), "Consistency check succeded, but shouldn't have.");
    }

    /**
     * Test the behavior, when an empty directory is given for checksum calculation.
     *
     */
    @Test
    public void testIsConsistent_Boundaries() {
        File emptyTestDirectory = new File("target" + File.separator + "test-data" + File.separator + "empty-test-dir");
        emptyTestDirectory.mkdirs();
        String checksum = helper.getAggregatedChecksum(emptyTestDirectory);
        assertNotNull(checksum, "Checksum was null, but was not expected to be.");
    }

    /**
     * Test the boundaries of
     * {@link DitaInstallationHelper#getDitaToolkitRoot()}.
     *
     * @throws Exception
     */
    @Test
    public void testBoundaries_getDitaToolkitRoot() throws Exception {
        // install the Dita Toolkit
        helper.install();

        // create an additional directory to provoke an exception
        File additionalDirectory = new File(helper.getDitaToolkitRoot().getParent(), "additionalDirectory");
        additionalDirectory.mkdir();

        // now check if the correct exception is thrown
        assertThrows(IOException.class, () ->
                helper.getDitaToolkitRoot(),
            "IOException should have been thrown.");
    }
}