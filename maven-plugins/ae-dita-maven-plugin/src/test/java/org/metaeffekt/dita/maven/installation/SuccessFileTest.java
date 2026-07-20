/*
 * Copyright 2021-2026 the original author or authors.
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

package org.metaeffekt.dita.maven.installation;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class SuccessFileTest {


    private static File mockArchive;

    @BeforeAll
    public static void setUp(@TempDir File archivePath) throws IOException {
        mockArchive = new File(archivePath," mock_archive.zip");
        Files.touch(mockArchive);
    }

    @Test
    public void noSuccessFile_notSuccessful(@TempDir File installationFolder) throws Exception {

        final DitaInstallationHelper helper = new DitaInstallationHelper(installationFolder, mockArchive, "ANY_USER");

        assertThat(helper.wasSuccessful()).isFalse();
    }

    @Test
    public void createSuccesFile_successful(@TempDir File installationFolder) throws Exception {

        final DitaInstallationHelper helper = new DitaInstallationHelper(installationFolder, mockArchive, "ANY_USER");

        createSuccessFile(helper, "installation.success");

        assertThat(helper.wasSuccessful()).isTrue();
    }

    @Test
    public void user1WithSuccessFile_user2Without_noInterference(@TempDir File installationFolder) throws IOException {

        final DitaInstallationHelper helperUser1 = new DitaInstallationHelper(installationFolder, mockArchive, "USER_1");
        final DitaInstallationHelper helperUser2 = new DitaInstallationHelper(installationFolder, mockArchive, "USER_2");

        createSuccessFile(helperUser1, "installation.success");

        assertThat(helperUser1.wasSuccessful()).isTrue();
        assertThat(helperUser2.wasSuccessful()).isFalse();
    }

    protected void createSuccessFile(DitaInstallationHelper helperUser1, String successFileName) throws IOException {
        final File successFileUser1 = new File(helperUser1.getInstallationRoot(), successFileName);
        FileUtils.createParentDirectories(successFileUser1);
        successFileUser1.createNewFile();
    }

}
