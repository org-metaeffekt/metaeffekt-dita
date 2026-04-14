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
package org.metaeffekt.dita.maven.mojo;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.dom4j.DocumentException;

import org.metaeffekt.dita.maven.glossary.GlossaryMapCreator;
import org.xml.sax.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

@Mojo(name = "generate-glossary")
public class GenerateGlossaryMapMojo extends AbstractDitaContentMojo {

    @Parameter(defaultValue = "en")
    private String language; 

    @Parameter(defaultValue = "glossary/${language}/gmap_glossary.ditamap")
    private String targetGlossaryMap; 

    @Parameter(defaultValue = "${project.artifactId}/${document.ditamap}")
    private String ditaMap; 

    @Parameter(defaultValue = "-nothing-")
    private String excludePattern; 

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // only run if we can do something
        if(isPomPackagingProject()){
            return;
        }

        GlossaryMapCreator glossaryMapCreator = new GlossaryMapCreator();
        glossaryMapCreator.setBaseDir(getDitaSourceDir());
        glossaryMapCreator.setTargetGlossaryMap(new File(getDitaSourceGenDir(), replace(targetGlossaryMap)));
        glossaryMapCreator.setLanguage(language);
        glossaryMapCreator.setDitaMap(new File(replace(ditaMap)));

        validateDitaWellFormed();

        try {
            glossaryMapCreator.create();
        } catch (DocumentException | IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void validateDitaWellFormed() throws MojoExecutionException {
        getLog().info("Validating DITA content in " + getDitaSourceGenDir());

        if (!getDitaSourceDir().exists()) return;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        try {
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            SAXParser parser = factory.newSAXParser();
            XMLReader xmlReader = parser.getXMLReader();
            xmlReader.setErrorHandler(new ValidationErrorHandler());

            xmlReader.setEntityResolver(new EmptyEntityResolver());

            List<File> ditaFiles = getDitaFiles();

            for (File file : ditaFiles) {
                try {
                    getLog().debug("Checking well-formedness: " + file.getName());
                    xmlReader.parse(new InputSource(Files.newInputStream(file.toPath())));
                } catch (SAXException | IOException e) {
                    throw new MojoExecutionException(
                            "File is not well-formed: " + file.getAbsolutePath(), e);
                }
            }

            getLog().info("All DITA files successfully validated in " + getDitaSourceGenDir());
        } catch (ParserConfigurationException | SAXException e) {
            throw new MojoExecutionException("Error during validation setup", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class EmptyEntityResolver implements EntityResolver {
        @Override
        public InputSource resolveEntity(String publicId, String systemId) {
            return new InputSource(new StringReader(""));
        }
    }

    private List<File> getDitaFiles() throws IOException {
        List<File> ditaFiles = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(getDitaSourceGenDir().toPath())) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.toString().toLowerCase();
                        return name.endsWith(".dita") || name.endsWith(".ditamap");
                    })
                    .forEach(path -> ditaFiles.add(path.toFile()));
        }
        return ditaFiles;
    }

    private String replace(String string) {
        return string.replaceAll("\\$\\{language\\}", language);
    }

    private class ValidationErrorHandler implements ErrorHandler {
        @Override
        public void warning(SAXParseException exception) throws SAXException {
            getLog().warn("Validation warning: " + exception.getMessage());
        }

        @Override
        public void error(SAXParseException exception) throws SAXException {
            throw new SAXException("Validation error: " + exception.getMessage(), exception);
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            throw new SAXException("Fatal validation error: " + exception.getMessage(), exception);
        }
    }
}
