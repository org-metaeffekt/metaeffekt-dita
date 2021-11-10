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
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * An implementation of the dita launcher using ant (launched as an external
 * process, to avoid class loader problems), which seems to be the easiest way
 * right now.
 * 
 * Needs to be adjusted though.
 */
public class ExternalAntDitaLauncher extends AbstractDitaLauncher {
    
    /**
     * Launches dita.
     * 
     * @throws MojoExecutionException
     *             the mojo execution exception
     */
    public void execute() throws MojoExecutionException {

        String antHome = this.getDitaDir();
        String antBin = antHome + File.separator + "bin" + File.separator;

        String osName = System.getProperty("os.name");
        if (osName != null && osName.toLowerCase().indexOf("windows") != -1) {
            antBin += "dita.bat";
        } else {
            antBin += "dita";
        }

        Commandline cli = new Commandline(antBin);
        cli.addArguments(new String[] { "--args.input=" + this.getInputMap() });
        cli.addArguments(new String[] { "--output.dir=" + this.getOutputDir() });
        cli.addArguments(new String[] { "--dita.temp.dir=" + this.getTempDir() });
        cli.addArguments(new String[] { "--clean.temp=" + (this.isCleanDitaTemp() ? "yes" : "no") });
        cli.addArguments(new String[] { "--transtype=" + this.getTranstype() });
        cli.addArguments(new String[] { "--nav-toc=partial" });
        cli.addArguments(new String[] { "--args.draft=" + (this.isDraft() ? "yes" : "no") });
        cli.addArguments(new String[] { "--args.grammar.cache=" + (this.isGrammarCacheEnabled() ? "yes" : "no") });
        cli.addArguments(new String[] { "--args.indexshow=yes" });
        if (this.getCustomCss() != null)
            cli.addArguments(new String[] { "--args.css="+this.getCustomCss() });
        cli.addArguments(new String[] { "--args.csspath=css" });
        cli.addArguments(new String[] { "--dita.dir=" + this.getDitaDir() });
        if (this.getDitavalFile() != null) {
            cli.addArguments(new String[] { "--dita.input.valfile=" + this.getDitavalFile() });
        }
        if (this.getCustomizationDir() != null) {
            cli.addArguments(new String[] { "--customization.dir=" + this.getCustomizationDir() });
        }
        cli.addArguments(new String[] { "--outer.control=quiet" });

        for (String param : cli.getArguments()) {
            getLog().info(param);
        }

        cli.setWorkingDirectory(getBaseDir());

        final List<String> errorLines = new ArrayList<String>();
        StreamConsumer consumer = new StreamConsumer() {
            
            public void consumeLine(String line) {
                boolean log = true;

                // escalate error messages
                if (
                    line.contains("[FATAL]") ||
                    line.contains("[ERROR]") ||
                    line.contains("[SCHWERWIEGEND]") ||
                    line.contains("[FATAL]")) {
                        errorLines.add(line);
                }

                // filter specific messages
                if (line.contains("BUILD SUCCESSFUL")) {
                    log = false;
                }

                // suppress specific messages (that cannot be suppressed by logger configurations)
                if (line.contains(" [main] DEBUG ") ||
                    line.contains(" [main] INFO ") ||
                    line.contains(" [main] WARN ") ||
                    line.contains("] Loading stylesheet ") ||
                    line.contains("] Processing ")) {
                    log = false;
                }

                if (log) {
                    getLog().info(line);
                }
            }
        };

        try {
            getLog().info(" +-------------------------------------------------------+ ");
            getLog().info(" | DITA                                                  | ");
            getLog().info(" +-------------------------------------------------------+ ");
            
            int result = CommandLineUtils.executeCommandLine(cli, consumer, consumer);
            
            if (result != 0) {
                for (String argument : cli.getArguments()) {
                    getLog().info("Parameter: [" + argument + "]");
                }
                throw new MojoExecutionException("Exception while executing DITA. Process return value :" + result);
            }
            
            if (!errorLines.isEmpty()) {
                getLog().error("Errors detected:");
                for (String errorLine : errorLines) {
                    getLog().error(errorLine);
                }
                throw new MojoExecutionException("Exception while executing DITA.");
            }
        } catch (CommandLineException cle) {
            throw new MojoExecutionException("Exception while executing DITA", cle);
        }
    }
}
