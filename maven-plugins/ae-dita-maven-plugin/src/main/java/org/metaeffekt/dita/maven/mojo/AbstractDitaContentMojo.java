/**
 * Copyright 2009-2017 the original author or authors.
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

import java.io.File;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class AbstractDitaContentMojo extends AbstractDitaMojo {

    /**
     * The input directory (the output of the dependency extraction).
     */
    @Parameter(
            property = "ae.dita.source.dir",
            defaultValue = "${basedir}/src/main/dita",
            required = true
    )
    private File ditaSourceDir;
    
    /**
     * The input directory (the output of the dependency extraction).
     */
    @Parameter(
            property = "ae.dita.source.gen.dir",
            defaultValue = "${basedir}/src/main/dita/gen"
    )
    private File ditaSourceGenDir; 
    
    public File getDitaSourceDir() {
        return ditaSourceDir;
    }
    
    public void setDitaSourceDir(File ditaSourceDir) {
        this.ditaSourceDir = ditaSourceDir;
    }

    public File getDitaSourceGenDir() {
        return ditaSourceGenDir;
    }

    public void setDitaSourceGenDir(File ditaSourceGenDir) {
        this.ditaSourceGenDir = ditaSourceGenDir;
    }

    public void markContent(File basedir, boolean readOnly) {
        if (basedir.exists() && basedir.isDirectory()) {
            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(basedir);
            scanner.scan();
            String[] files = scanner.getIncludedFiles();
            
            for (String relPath : files) {
                File file = new File(basedir, relPath);
                
                if (readOnly) {
                    file.setReadOnly();
                } else {
                    file.setWritable(true);
                }
            }
        }
    }
    
    public void cleanDirectory(File basedir) {
        Delete delete = new Delete();
        delete.setProject(new Project());
        delete.setDir(basedir);
        delete.setIncludeEmptyDirs(true);
        delete.setIncludes("**/*");
        delete.setFailOnError(false);
        delete.setFollowSymlinks(false);
        delete.execute();
    }

}
