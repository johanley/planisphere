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

  --------------
  
 To-do:

  Order the two-piece rivets, to be ready when I need them.
 What kind of rivet? Removable. Match the thickness!
 Some rivets allow for variable thickness.
 https://www.mcmaster.com/two-piece-rivets/
 
  
  Servlet? 
  https://kb.itextsupport.com/home/it7kb/faq/how-can-i-serve-a-pdf-to-a-browser-without-storing-a-file-on-the-server-side
     no file, but byte arrays in memory, served to the client
       zip together? or two separate operations, for the two files?
     will need this project as a jar file; use it as a library. File-Export-java-jar
  plain server, one form, two buttons for download 2 files separately, gets only, serve bytestream, error handling re-shows the form
    year, lat, long, hours/mins, name, dec limit, format (big or small)
    /planisphere/index.html - form; two post-targets, for two files 
    /planisphere/build - servlet; might recycle to the form; build a config object, generate 1 file at a time

  Animations: 
    pole precession (with proper motion); proper motion in general; motion of lunar nodes
    

---------------------------------------------------------------------------------------------

 Cloudy nights post: see https://www.cloudynights.com/topic/799573-the-planisphere/page-2, where a lady
  is asking for a custom version.

 This incorrectly implies the ecliptic pole is stationary: 
 https://en.wikipedia.org/wiki/Orbital_pole#Ecliptic_pole

 https://www.cloudynights.com/topic/690886-planisphere-hack/
 Post to https://www.quasarastronomy.com.au/ ?
 https://www.shadowspro.com/en/index.html
 
 Link from https://en.wikipedia.org/wiki/Planisphere
 
 PROBS FOUND 
   ephem.js: precession has extra line that shouldn't be there ?? 
       var temp = Math.acos(Math.sqrt(A*A + B*B)); //0..pi YES 
        ephem.δ = Math.sin(ephem.δ) * temp;  ???? NO 
   ephem.js: the Julian Date function is wrong for negative years; Math.floor should be Maths.truncate.
*/
public final class Build {
  
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
