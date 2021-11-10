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
package org.metaeffekt.dita.maven.generation;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;

public class AttachedPdfArtifact extends DefaultArtifact {

    public AttachedPdfArtifact(Artifact artifact, ArtifactHandler handler, File file, String artifactId, String classifier, String artifactVersion) {
        super(
            artifact.getGroupId(), // groupId
            artifactId,  // artifactId
            VersionRange.createFromVersion(artifactVersion), // version
            artifact.getScope(), // scope
            "pdf", // type
                classifier, // classifier
                new Handler(null, null), true); // artifactHandler
        setFile(file);
    }

    private static class Handler implements ArtifactHandler  {
        
        private String classifier;
        private String language;
        
        public Handler(String classifier, String language) {
            this.classifier = classifier;
            this.language = language;
        }
        
        @Override
        public String getClassifier() {
            return classifier;
        }

        @Override
        public String getDirectory() {
            return "pdfs";
        }

        @Override
        public String getExtension() {
            return "pdf";
        }

        @Override
        public String getLanguage() {
            return language;
        }

        @Override
        public String getPackaging() {
            return "pdf";
        }

        @Override
        public boolean isAddedToClasspath() {
            return false;
        }

        @Override
        public boolean isIncludesDependencies() {
            return false;
        }
        
    }
    
}
