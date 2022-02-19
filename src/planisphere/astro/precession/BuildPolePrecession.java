package planisphere.astro.precession;

import static planisphere.config.Constants.PRECESSION_DEMO_FILE;
import static planisphere.util.LogUtil.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.itextpdf.text.DocumentException;

import planisphere.config.Config;
import planisphere.config.ConfigFromFile;

/** 
 Build a chart showing the long-term precession of the poles.
 Either the north poles or the south poles are shown, according to the latitude in config.ini.
*/
public final class BuildPolePrecession {
  
  /** 
   As a standalone program, generate a chart as a single PDF file.
   The files are saved to the file system, in an existing directory (see config.ini).
  */
  public static void main(String... args) throws DocumentException, IOException {
    log("Building a chart for long-term precession.");
    
    log("Config:");
    Config config = new ConfigFromFile().init();
    log(config.toString());
    
    String pole = config.isNorthernHemisphere() ? "north" : "south";
    log("This chart is for the " + pole + " pole.");
    
    GeneratePolePrecession poles = new GeneratePolePrecession(config);
    poles.outputTo(streamFor(PRECESSION_DEMO_FILE, config));

    log("File saved to " + fullFileName(PRECESSION_DEMO_FILE, config));
    log("Done.");
  }

  //PRIVATE 
  
  private static OutputStream streamFor(String fileName, Config config) throws FileNotFoundException {
    return new FileOutputStream(fullFileName(fileName, config));
  }
  
  private static String fullFileName(String name, Config config) {
    String result = config.outputDir() + File.separator + name;
    return result;
  }
}
