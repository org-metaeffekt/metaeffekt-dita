/**
 * Copyright 2009-2021 the original author or authors.
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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Checksum;
import org.apache.tools.ant.taskdefs.Expand;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * This class takes care of all tasks necessary to provide a consistent Dita
 * Toolkit installation for the usage within a Maven build. <br>
 * <b>Warning:</b> The calculation of the checksum differs between Ant versions.
 * This might cause problems later on.
 * <p>
 * The file structure used looks as follows:
 * <pre>
 *     ${installationFolder}
 *                  |- ${USER_1}_${ARCHIVE_CHECKSUM}
 *                  |       |- dita-toolkit-installation
 *                  |       |           |- ...
 *                  |       |- installation.success
 *                  |- ${USER_2}_${ARCHIVE_CHECKSUM}
 *                          |- dita-toolkit-installation
 *                          |           |- ...
 *                          |- installation.success
 * </pre>
 *
 * @author Siegfried E.
 * @author Karsten Klein
 */
public class DitaInstallationHelper {
    /**
     * Name of the property that specifies the location of the Dita Toolkit.
     */
    public static final String DITA_TOOLKIT_ROOT_PROPERTY = "ae.dita.toolkit.installroot";

    /**
     * The type of the Dita Toolkit dependency.
     */
    public static final String DITA_TOOLKIT_TYPE = "zip";

    /**
     * Static reference to the temporary directory.
     */
    public static final File JAVA_IO_TMPDIR = new File(System.getProperty("java.io.tmpdir"));

    /**
     * Name of the property, which will hold aggregated folder checksums
     * calculated by Ant.
     */
    public static final String ANT_AGGREGATED_CHECKSUM_PROPERTY = "ae.dita.aggregated.checksum";

    /**
     * Name of the file to store the aggregated checksum in.
     */
    public static final String AGGREGATED_CHECKSUM_FILE = "toolkit.MD5";
    public static final String UTF_8 = "UTF-8";
    private static final String SUCCESS_FILE = "installation.success";

    /**
     * Username to generate installation root with. Helps to distinguish installations
     * created by different users.
     * <p>
     * May be null
     */
    private final String username;

    /**
     * The root directory where the Dita Toolkit is supposed to be installed.
     */
    private File installationFolder;

    /**
     * This is the archive from where the Dita Toolkit can be installed, if
     * necessary.
     */
    private File installationArchive;

    /**
     * Ant codeextraction for calculating checksums.
     */
    private Checksum checksumTask;

    /**
     * Ant Expand task for unzipping used to install the Dita Toolkit.
     */
    private Expand unzipTask;

    /**
     * Constructor.
     *
     * @param installationFolder  The installation folder.
     * @param installationArchive The installation archive.
     * @param username            username to distinguish user installations.
     */
    public DitaInstallationHelper(File installationFolder, File installationArchive, String username) {
        Preconditions.checkArgument(installationArchive.isFile(), "Installation archive must point to an existing file.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(username), "Username must not be null or empty.");

        this.installationFolder = installationFolder;
        this.installationArchive = installationArchive;
        this.username = username;
    }

    /**
     * Get a pointer to the location of a ready to use Dita Toolkit, where the
     * content of the location corresponds to the content of the given {@link DitaInstallationHelper}.
     *
     * @return String representing the path to the Dita Toolkit installation.
     * @throws IOException IOException
     */
    public File getDitaToolkitRoot() throws IOException {
        final File installationRoot = getInstallationRoot();
        File[] subDirs = installationRoot.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);

        if (subDirs.length != 1) {
            throw new IOException("The Dita installation cache contains more than one directory for the current Toolkit version.");
        }
        return subDirs[0];
    }

    /**
     * Create a checksum for the installation archive and return it as String.
     *
     * @return String containing the MD5 sum of the installation archive.
     */
    public String getInstallationArchiveChecksum() {
        checksumTask = new Checksum();
        final Project project = new Project();
        checksumTask.setProject(project);
        checksumTask.setFile(installationArchive);
        final String checksumPropertyKey = "md5Checksum";
        checksumTask.setProperty(checksumPropertyKey);
        checksumTask.execute();
        return project.getProperty(checksumPropertyKey);
    }

    /**
     * Installs the Dita Toolkit from the installation archive that is provided. <br>
     * Create a checksum of the installation archive and unzip the content of
     * the installation archive to a sub-folder of the given installationFolder.
     * The sub-folder's name is the checksum of the installation archive.
     * <p>
     * After extracting the Dita Toolkit to the target folder, create an aggregated
     * checksum of the directory content and save the checksum to a text file
     * right along with the folder.
     *
     * @return True if the installation was successful, false otherwise.
     * @throws IOException IOException
     */
    public boolean install() throws IOException {
        final File installRoot = getInstallationRoot();
        final File checksumFile = new File(installRoot, AGGREGATED_CHECKSUM_FILE);

        // remove the installation directory if it already exists.
        if (installRoot.exists()) {
            FileUtils.forceDelete(installRoot);
        }

        unzipTask = new Expand();
        unzipTask.setDest(installRoot);
        unzipTask.setSrc(installationArchive);

        // extract the toolkit
        unzipTask.execute();

        // get the actual root folder of the toolkit
        File toolkitRoot = this.getDitaToolkitRoot();

        FileUtils.writeStringToFile(new File(getInstallationRoot(), SUCCESS_FILE), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString(), UTF_8);

        return true;
    }

    /**
     * Calculates the root directory where the toolkit is unzipped into. The directory path
     * is based onto the checksum of the zip file and the username passed at instantiation.
     *
     * @return directory path based on checksum and username
     */
    private File getInstallationRoot() {
        String checksum = this.getInstallationArchiveChecksum();
        return new File(installationFolder, "%s_%s".formatted(username, checksum));
    }

    /**
     * Checks if the Dita Toolkit is installed. <br>
     * This method tests if the installation folder contains a {@link DitaInstallationHelper#SUCCESS_FILE}.
     *
     * @return <i>true</i> if the {@link DitaInstallationHelper#SUCCESS_FILE} exists, <i>false</i>
     * otherwise.
     */
    public boolean isInstalled() {
        final File installationRoot = getInstallationRoot();
        return new File(installationRoot, SUCCESS_FILE).exists();
    }
}