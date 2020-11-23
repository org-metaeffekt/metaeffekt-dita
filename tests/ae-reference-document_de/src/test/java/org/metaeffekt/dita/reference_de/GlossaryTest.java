package org.metaeffekt.dita.reference_de;

import org.dom4j.DocumentException;
import org.junit.Ignore;
import org.junit.Test;
import org.metaeffekt.dita.maven.glossary.GlossaryMapCreator;

import java.io.File;
import java.io.IOException;

public class GlossaryTest {

   @Ignore
   @Test
   public void testGlossary() throws IOException, DocumentException {

      final GlossaryMapCreator glossaryMapCreator = new GlossaryMapCreator();
      glossaryMapCreator.setBaseDir(new File("src/main/dita"));
      glossaryMapCreator.setDitaMap(new File("ae-reference-document.ditamap"));
      glossaryMapCreator.setLanguage("de");
      glossaryMapCreator.setTargetGlossaryMap(new File("test_glossary.map"));

      glossaryMapCreator.create();


   }

}
