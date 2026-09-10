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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

public class SuccessFileTest {


    private static File mockArchive;

    @BeforeAll
    public static void setUp(@TempDir File archivePath) throws IOException, URISyntaxException {
        mockArchive = new File(SuccessFileTest.class.getClassLoader().getResource(
                "dita-installation-test/dita-toolkit-dummy.zip").toURI());

    }

    @Test
    public void unsuccessfulInstallation_notSuccessful(@TempDir File installationFolder) throws Exception {

        final DitaInstallationHelper helper = new DitaInstallationHelper(installationFolder, mockArchive, "ANY_USER");

        installUnsuccessfully(helper);

        assertThat(helper.isInstalled()).isFalse();
    }

    @Test
    public void installWithSuccess_successful(@TempDir File installationFolder) throws Exception {

        final DitaInstallationHelper helper = new DitaInstallationHelper(installationFolder, mockArchive, "ANY_USER");

        installSuccessfully(helper);

        assertThat(helper.isInstalled()).isTrue();
    }

    @Test
    public void installForUser1WithSuccess_user2Without_noInterference(@TempDir File installationFolder) throws IOException {

        final DitaInstallationHelper helperUser1 = new DitaInstallationHelper(installationFolder, mockArchive, "USER_1");
        final DitaInstallationHelper helperUser2 = new DitaInstallationHelper(installationFolder, mockArchive, "USER_2");

        installSuccessfully(helperUser1);
        installUnsuccessfully(helperUser2);

        assertThat(helperUser1.isInstalled()).isTrue();
        assertThat(helperUser2.isInstalled()).isFalse();
    }

    protected void installSuccessfully(DitaInstallationHelper helper) throws IOException {
        helper.install();
    }

    private void installUnsuccessfully(DitaInstallationHelper helper) {
        // do nothing to simulate a failed installation
    }
}
