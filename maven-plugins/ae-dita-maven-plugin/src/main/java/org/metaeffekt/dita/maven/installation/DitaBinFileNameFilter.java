/**
 * Copyright 2009-2016 the original author or authors.
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
import java.io.FilenameFilter;

/**
 * FileNameFilter which accepts all executable files from Ant.
 * 
 * @author Bernd A.
 */
public class DitaBinFileNameFilter implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {

        boolean accepted = false;

        if (name.startsWith("ant") || name.startsWith("dita"))
            accepted = true;

        return accepted;
    }

}
