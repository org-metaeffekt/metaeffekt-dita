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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Checksum;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.types.FileSet;

/**
 * This class takes care of all tasks necessary to provide a consistent Dita
 * Toolkit installation for the usage within a Maven build. <br>
 * <b>Warning:</b> The calculation of the checksum differs between Ant versions.
 * This might cause problems later on.
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
     * Ant codeextraction for unzipping used to install the Dita Toolkit.
     */
    private Expand unzipTask;

    /**
     * Constructor.
     * 
     * @param installationFolder The installation folder.
     * @param installationArchive The installation archive.
     */
    public DitaInstallationHelper(File installationFolder, File installationArchive) {
        this.installationFolder = installationFolder;
        this.installationArchive = installationArchive;
    }

    /**
     * Get a pointer to the location of a ready to use Dita Toolkit, where to
     * content of the location corresponds to the content of the given {@link DitaInstallationHelper}.
     * 
     * @return String representing the path to the Dita Toolkit installation.
     * @throws IOException IOException
     */
    public File getDitaToolkitRoot() throws IOException {
        String checksum = this.getInstallationArchiveChecksum();
        File installationRoot = new File(installationFolder, checksum);
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
     * @throws IOException IOException
     */
    public String getInstallationArchiveChecksum() throws IOException {
        if (installationArchive == null) {
            throw new NullPointerException("No installation archive!");
        }

        checksumTask = new Checksum();
        final Project project = new Project();
        checksumTask.setProject(project);
        checksumTask.setFile(installationArchive);
        final String md5ChecksumProperty = "md5Checksum";
        checksumTask.setProperty(md5ChecksumProperty);
        checksumTask.execute();
        return project.getProperty(md5ChecksumProperty);
    }

    /**
     * Check if the Dita Toolkit is installed. <br>
     * This method tests if the installation directory of the Dita Toolkit
     * exists. It does not check the installation for consistency.
     * 
     * @return <i>true</i> if the installation folder exists, <i>false</i>
     *         otherwise.
     * @throws IOException IOException
     */
    public boolean isInstalled() throws IOException {
        String checksum = this.getInstallationArchiveChecksum();
        File installationDirectory = new File(getInstallationFolder(), checksum);
        return installationDirectory.isDirectory();
    }

    /**
     * Checks the current Dita Toolkit installation for consistency. <br>
     * This method is best to be executed after isInstalled(). It
     * creates an aggregated checksum on the Dita Toolkit installation
     * directory. The checksum is then compared to the checksum that was created
     * upon installation. This way the consistency of the installation can be
     * ensured.
     * 
     * @return <i>true</i> if the checksums match, <i>false</i> if the checksums
     *         do not match, or the checksum file cannot be found.
     */
    public boolean isConsistent() {
        File checksumFile = null;
        String archivedChecksum;
        String calculatedChecksum;

        try {
            checksumFile = new File(this.getDitaToolkitRoot().getParent(), AGGREGATED_CHECKSUM_FILE);
        } catch (IOException e) {
            return false;
        }
        try {
            archivedChecksum = FileUtils.readFileToString(checksumFile);
            calculatedChecksum = getAggregatedChecksum(getDitaToolkitRoot());

            if (archivedChecksum.equals(calculatedChecksum)) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    /**
     * Install the Dita Toolkit from the installation archive that is provided. <br>
     * Create a checksum of the installation archive and unzip the content of
     * the installation archive to a sub-folder of the given installationFolder.
     * The sub-folder's name is the checksum. After
     * extracting the Dita Toolkit to the target folder, create an aggregated
     * checksum on the directory content and save the checksum to a text file
     * right along with the folder. The name of the text file is
     * <i>&lt;INSTALL_ARCHIVE_CHECKSUM&gt;.txt</i>.
     * 
     * @return True if the installation was successful, false otherwise.
     * @throws IOException IOException
     */
    public boolean install() throws IOException {
        String checksum = this.getInstallationArchiveChecksum();
        File installRoot = new File(installationFolder, checksum);
        File checksumFile = new File(installRoot, AGGREGATED_CHECKSUM_FILE);
        File toolkitRoot;
        String aggregatedChecksum = "";

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
        toolkitRoot = this.getDitaToolkitRoot();

        // get the checksum of the fresh installation an remember it
        aggregatedChecksum = getAggregatedChecksum(toolkitRoot);
        FileUtils.writeStringToFile(checksumFile, aggregatedChecksum, UTF_8);

        return true;
    }

    /**
     * This method creates an aggregated checksum for a given directory. <br>
     * The checksum is calculated using the Ant codeextraction <i>Checksum</i> with the
     * according file inclusions.
     * 
     * @param target
     *            Directory to calculate the checksum for.
     *
     * @return The aggregated checksum
     */
    public String getAggregatedChecksum(File target) {
        Project checksumProject = new Project();
        FileSet fileSet = new FileSet();
        String checksum;

        // define FileSet to include all files recursively
        fileSet.setProject(checksumProject);
        fileSet.setIncludes("*/**");
        fileSet.setDir(target);

        // calculate the actual checksum
        checksumTask = new Checksum();
        checksumTask.setProject(checksumProject);
        checksumTask.setTodir(new File(JAVA_IO_TMPDIR, "dita-checksums"));
        checksumTask.setTotalproperty(ANT_AGGREGATED_CHECKSUM_PROPERTY);
        checksumTask.addFileset(fileSet);

        try {
           checksumTask.execute();
           checksum = checksumTask.getProject().getProperty(ANT_AGGREGATED_CHECKSUM_PROPERTY);
        } catch (BuildException e) {
           checksum = "";
        }

        return checksum;
    }

    /**
     * @return the installationFolder
     */
    public File getInstallationFolder() {
        return installationFolder;
    }

    /**
     * @param installationFolder
     *            the installationFolder to set
     */
    public void setInstallationFolder(File installationFolder) {
        this.installationFolder = installationFolder;
    }

    /**
     * @return the installationArchive
     */
    public File getInstallationArchive() {
        return installationArchive;
    }

    /**
     * @param installationArchive
     *            the installationArchive to set
     */
    public void setInstallationArchive(File installationArchive) {
        this.installationArchive = installationArchive;
    }
}