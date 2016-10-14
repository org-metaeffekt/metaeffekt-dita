import org.apache.commons.io.*;

System.out.println("### BEGIN IT Validate ###");

File buildLog = new File(basedir, "build.log");
String log = FileUtils.readFileToString(buildLog);
boolean flag = true;

// testing default value of the mojo
if (!log.contains("ditaToolkitCacheDir = /tmp/dita") &&  !log.contains("\\dita")) {
   println("Toolkit cache dir not found in build.log.");
   flag = false;
}

// TODO: add a more meaningful test
if (!log.contains("DITA Open Toolkit install archive")) {
   println("No message about Toolkit archive found in the build.log.");
   flag = false;
}

System.out.println("### END IT Validate ###");

return flag;