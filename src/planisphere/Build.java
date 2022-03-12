package planisphere;

import static planisphere.config.Constants.STAR_CHART_FILE;
import static planisphere.config.Constants.TRANSPARENCY_FILE;
import static planisphere.util.LogUtil.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.itextpdf.text.DocumentException;

import planisphere.config.Config;
import planisphere.config.ConfigFromFile;
import planisphere.draw.starchart.GenerateStarChart;
import planisphere.draw.transparency.GenerateTransparency;

/** 
 Build the two files for the planisphere, as a standalone program from the command line.
 
 One file is the star chart (back), and the other is a transparency (front).
*/
public final class Build {
  
  /*
 PROBS FOUND 
   ephem.js: precession has extra line that shouldn't be there ?? 
       var temp = Math.acos(Math.sqrt(A*A + B*B)); //0..pi YES 
        ephem.δ = Math.sin(ephem.δ) * temp;  ???? NO 
   ephem.js: the Julian Date function is wrong for negative years; Math.floor should be Maths.truncate.
 */
  
  /** 
   As a standalone program, generate the planisphere as a pair of PDF files.
   The files are saved to the file system, in an existing directory (see config.ini). 
  */
  public static void main(String... args) throws DocumentException, IOException {
    log("Building a planisphere from a config file...");
    
    log("Config:");
    Config config = new ConfigFromFile().init();
    log(config.toString());
    
    log("Generating star chart PDF.");
    GenerateStarChart starChart = new GenerateStarChart(config);
    starChart.outputTo(streamFor(STAR_CHART_FILE, config));

    log("Generating transparency PDF.");
    GenerateTransparency transparency = new GenerateTransparency(config);
    transparency.outputTo(streamFor(TRANSPARENCY_FILE, config));
    
    log("File saved to " + fullFileName(STAR_CHART_FILE, config));
    log("File saved to " + fullFileName(TRANSPARENCY_FILE, config));
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
