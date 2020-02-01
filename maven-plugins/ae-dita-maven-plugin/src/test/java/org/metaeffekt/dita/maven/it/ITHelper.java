/**
 * Copyright 2009-2020 the original author or authors.
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
package org.metaeffekt.dita.maven.it;

import java.io.File;

/**
 * This class provides some common methods needed throughout the integration
 * tests.
 * 
 * @author Siegfried E.
 * 
 */
public class ITHelper {
    /**
     * Tests if the given file exists and is a file.
     * 
     * @param file
     *            File to test.
     * @return <i>true</i> if the file exists and is a file, <i>false</i>
     *         otherwise.
     */
    public static boolean testFileExistence(File file) {
        if (file.exists() && file.isFile()) {
            System.out.println(file.getAbsolutePath() + " exists.");
            return true;
        }
        System.out.println(file.getAbsolutePath() + " does not exist.");
        return false;
    }

    /**
     * Tests if the given directory exists and is a directory.
     * 
     * @param dir
     *            Directory to test.
     * @return <i>true</i> if the directory exists and is a directory,
     *         <i>false</i> otherwise.
     */
    public static boolean testDirectoryExistence(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            System.out.println(dir.getAbsolutePath() + " exists.");
            return true;
        }
        System.out.println(dir.getAbsolutePath() + " does not exist.");
        return false;
    }
}