package planisphere.draw.starchart;

import static planisphere.config.Constants.BASIC_CHART_FILE;
import static planisphere.util.LogUtil.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.itextpdf.text.DocumentException;

import planisphere.config.Config;
import planisphere.config.ConfigFromFile;

/** Build a basic star chart. */
public final class BuildBasicStarChart {
  
  /** 
   As a standalone program, generate a chart as a single PDF file.
   The files are saved to the file system, in an existing directory (see config.ini).
  */
  public static void main(String... args) throws DocumentException, IOException {
    log("Building a basic star chart.");
    
    log("Config:");
    Config config = new ConfigFromFile().init();
    log(config.toString());
    
    String pole = config.isNorthernHemisphere() ? "north" : "south";
    log("This chart is for the " + pole + " pole.");
    
    GenerateBasicStarChart basicChart = new GenerateBasicStarChart(config);
    basicChart.outputTo(streamFor(BASIC_CHART_FILE, config));

    log("File saved to " + fullFileName(BASIC_CHART_FILE, config));
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
