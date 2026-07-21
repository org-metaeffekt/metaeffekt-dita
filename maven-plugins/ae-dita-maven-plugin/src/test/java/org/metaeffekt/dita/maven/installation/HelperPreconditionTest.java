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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

/**
 * Check preconditions at helper instantiation.
 */
class HelperPreconditionTest {

    private File mockArchive;

    @BeforeEach
    public void setup() throws Exception {
        mockArchive = new File(this.getClass().getClassLoader().getResource(
                "dita-installation-test/dita-toolkit-dummy.zip").toURI());
    }

    @Test
    public void doesNotAllowNullUsername() throws IOException {

        final Throwable throwable = Assertions.catchThrowable(() -> new DitaInstallationHelper(null, mockArchive, null));

        Assertions.assertThat(throwable).isInstanceOf(IllegalArgumentException.class).hasMessageContainingAll("not", "null");
    }

    @Test
    public void doesNotAllowEmptyUsername() {

        final Throwable throwable = Assertions.catchThrowable(() -> new DitaInstallationHelper(null, mockArchive, ""));

        Assertions.assertThat(throwable).isInstanceOf(IllegalArgumentException.class).hasMessageContainingAll("not", "empty");
    }

    @Test
    public void installationArchiveMustBeValidFile(@TempDir File tempDir) throws Exception {

        final File nonExistentArchive = new File(tempDir, "non-existent-archive.zip");
        final Throwable throwable = Assertions.catchThrowable(() -> new DitaInstallationHelper(null, nonExistentArchive, "ANY_USER"));

        Assertions.assertThat(throwable).isInstanceOf(IllegalArgumentException.class).hasMessageContainingAll("must", "point", "existing", "file");
    }
}
