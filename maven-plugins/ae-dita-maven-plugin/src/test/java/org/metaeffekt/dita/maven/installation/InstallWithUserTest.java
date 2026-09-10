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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test the correct behavior of the DitaInstallationHelper.
 *
 * @author Siegfried E.
 *
 */
public class InstallWithUserTest {

    /**
     * The MD5 checksum of Dita Toolkit Archive mock.
     */
    public static final String DITA_ARCHIVE_CHECKSUM = "b4ea57804bf44a2dff708a1449cfdc12";

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
    File ditaInstallationCache;

    /**
     * Archive containing some content that imitates a real life Dita Toolkit.
     */
    File mockArchive;
    private String username = "ANY_USER";

    @BeforeEach
    public void setUp() throws Exception {
        // create a temporary directory for test installations
        ditaInstallationCache = new File("target" + File.separator + "test-data" + File.separator
                + File.createTempFile("dita-", "-install-test").getName());
        ditaInstallationCache.mkdirs();

        // get the test installation archive
        mockArchive = new File(this.getClass().getClassLoader().getResource(
                "dita-installation-test/dita-toolkit-dummy.zip").toURI());

        helper = new DitaInstallationHelper(ditaInstallationCache, mockArchive, username);
    }

    @AfterEach
    public void tearDown() throws Exception {
        FileUtils.forceDelete(ditaInstallationCache);
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

    @Test
    public void installationDirContainingUsername() throws IOException {
        helper.install();

        final File installRoot = helper.getDitaToolkitRoot();

        Assertions.assertThat(installRoot).isNotEmptyDirectory();
        Assertions.assertThat(installRoot.getParent()).endsWith(username + "_" + DITA_ARCHIVE_CHECKSUM);
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
