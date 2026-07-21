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

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metaeffekt.dita.maven.mojo.DitaInfrastructureMojo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

/**
 * Test in the context of mojo.
 */
class MojoIntegrationTest {

    File ditaToolkitRoot;

    DitaInfrastructureMojo mojo;
    File ditaInstallationCache;

    @BeforeEach
    public void setUp() throws Exception {
        // create a temporary directory for test installations
        File tempFile = File.createTempFile("dita-", "-install-test");
        this.ditaInstallationCache = new File("target" + File.separator + "test-data" + File.separator + tempFile.getName());
        this.ditaInstallationCache.mkdirs();

        File mockArchive =
                new File(this.getClass().getClassLoader().getResource("dita-installation-test/dita-toolkit-dummy.zip").toURI());

        DitaInstallationHelper helper = new DitaInstallationHelper(this.ditaInstallationCache, mockArchive, "ANY_USER");
        helper.install();

        ditaToolkitRoot = helper.getDitaToolkitRoot();
        mojo = new MavenAgnosticMojo(helper);
    }

    @AfterEach
    public void tearDown() throws Exception {
        FileUtils.forceDelete(ditaInstallationCache);
    }

    @Test
    public void alreadyInstalled_accessRightsChanged_mojoShouldNotFailReinstall() throws IOException, MojoExecutionException, MojoFailureException {

        setPermissions(this.ditaToolkitRoot, "r-xr-xr-x");

        mojo.execute();
    }

    @Test
    public void alreadyInstalled_minimalPermissions_mojoShouldNotFailReinstall() throws IOException, MojoExecutionException, MojoFailureException {

        setPermissions(ditaToolkitRoot, "r-x------");


        mojo.execute();
    }


    private Path setPermissions(File onPath, String withPermissions) throws IOException {
        return Files.setPosixFilePermissions(onPath.toPath(), PosixFilePermissions.fromString(withPermissions));
    }

    /**
     * Avoid calls to maven infrastructure.
     */
    class MavenAgnosticMojo extends DitaInfrastructureMojo {

        private final DitaInstallationHelper helper;

        MavenAgnosticMojo(DitaInstallationHelper helper) {
            this.helper = helper;
        }

        /**
         * Bypass maven infrastructure by overriding {@link DitaInfrastructureMojo#execute()}.
         */
        @Override
        public void execute() throws MojoExecutionException, MojoFailureException {
            executeInstallation(helper);
        }
    }
}
