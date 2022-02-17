package planisphere.config;

import static planisphere.util.LogUtil.log;

import java.util.List;
import java.util.regex.Pattern;

import planisphere.math.Maths;
import planisphere.util.DataFileReader;

/** Use a text file as the source of configuration data. */
public final class ConfigFromFile {

  /** 
   Read the config.ini file and return a corresponding object.
   When running as a standalone program (not in a servlet context), THIS METHOD MUST BE CALLED IMMEDIATELY UPON STARTUP.
   The returned object will be passed around to many other classes.
  
   By default, the file is located in the same directory as this class.
   To use a different file as the config ile, use the command line setting: 
   
   {@code -DplanisphereConfigFile=C:\mydirectory\myfile}
   
   Use the full file name, including the directory.
  */
  public Config init() {
    DataFileReader reader = new DataFileReader();
    String fileLocationOverride = System.getProperty("planisphereConfigFile");
    List<String> lines = null;
    if (fileLocationOverride == null) {
      log("Reading config file: " + CONFIG_INI);
      lines = reader.readFile(this.getClass(), CONFIG_INI);
    }
    else {
      log("Reading config file: " + fileLocationOverride);
      lines = reader.readFile(fileLocationOverride);
    }

    for(String line : lines) {
      processEach(line.trim());
    }
    double geometricalDeclinationLimit = -(90 - Maths.radsToDegs(latitude)); // degs
    if (declinationLimit < geometricalDeclinationLimit) {
      String msg = "WARNING: your declination limit is below the real horizon.";
      log(msg);
    }
    return buildConfigObjectFromSettings();
  }

  // PRIVATE 

  private static final String CONFIG_INI = "config.ini";
  private static final String SEPARATOR = "=";
  private static final int NAME = 0;
  private static final int VALUE = 1;

  private Integer year;
  private String location = "";
  private Double latitude;
  private Double longitude;
  private Integer hoursOffsetFromUT;
  private Integer minutesOffsetFromUT;
  private Double declinationLimit;
  
  private Float width;
  private Float height;
  private String outputDir = "";
  
  private String fontDir = "";
  private Integer greyConstellationLines;
  private Integer smallestTimeDivision;
  private String radiants = "";
  private String monthNames = "";
  private String lunarTransitsTitle = "";
  private String planetaryTransitsTitle = "";
  private String planetNames = "";

  private void processEach(String line) {
    if (line.startsWith(DataFileReader.COMMENT) || line.length() == 0) {
      //ignore it!
    }
    else {
      String[] parts = line.split(Pattern.quote(SEPARATOR));
      if (matches(Setting.output_directory, parts)) {
        outputDir = asString(parts);
      }
      else if (matches(Setting.width, parts)) {
        width = asPoints(parts);
      }
      else if (matches(Setting.height, parts)) {
        height = asPoints(parts);
      }
      else if (matches(Setting.location, parts)) {
        location = asString(parts);
      }
      else if (matches(Setting.latitude, parts)) {
        latitude = asRads(parts);
      }
      else if (matches(Setting.longitude, parts)) {
        longitude = asRads(parts);
      }
      else if (matches(Setting.hours_offset_from_ut, parts)) {
        hoursOffsetFromUT = asInteger(parts);
      }
      else if (matches(Setting.minutes_offset_from_ut, parts)) {
        minutesOffsetFromUT = asInteger(parts);
      }
      else if (matches(Setting.declination_limit, parts)) {
        declinationLimit = asDouble(parts);
      }
      else if (matches(Setting.year, parts)) {
        year = asInteger(parts);
      }
      else if (matches(Setting.font_directory, parts)) {
        fontDir = asString(parts);
      }
      else if (matches(Setting.grey_constellation_lines, parts)) {
        greyConstellationLines = asInteger(parts);
      }
      else if (matches(Setting.smallest_time_division, parts)) {
        smallestTimeDivision = asInteger(parts);
        if (smallestTimeDivision > 2 || smallestTimeDivision < 1) {
          String msg = "Config problem! smallest_time_division can only be 1 or 2. Your value is " + smallestTimeDivision;
          throw new RuntimeException(msg);
        }
      }
      else if (matches(Setting.radiants, parts)) {
        radiants = asString(parts);
      }
      else if (matches(Setting.month_names, parts)) {
        monthNames = asString(parts);
      }
      else if (matches(Setting.lunar_transits_title, parts)) {
        lunarTransitsTitle = asString(parts);
      }
      else if (matches(Setting.planetary_transits_title, parts)) {
        planetaryTransitsTitle = asString(parts);
      }
      else if (matches(Setting.planet_names, parts)) {
        planetNames = asString(parts);
      }
    }
  }
  
  private boolean matches(Setting setting, String[] parts) {
    return parts[NAME].trim().equalsIgnoreCase(setting.toString());
  }

  private String asString(String[] parts) {
    return parts[VALUE].trim();
  }
  
  private Double asDouble(String[] parts) {
    return Double.valueOf(asString(parts));
  }
  
  private Double asRads(String[] parts) {
    return Maths.degToRads(asDouble(parts));
  }
  
  private Integer asInteger(String[] parts) {
    return Integer.valueOf(asString(parts));
  }
  
  private static final int POINTS_PER_INCH = 72; //the itext default is 72

  private Float asPoints(String[] parts) {
    return Float.valueOf(parts[VALUE]) * POINTS_PER_INCH;
  }

  private Config buildConfigObjectFromSettings() {
    return new Config(year, location, latitude, longitude, hoursOffsetFromUT, minutesOffsetFromUT, declinationLimit, 
        width, height, outputDir, fontDir, greyConstellationLines, smallestTimeDivision, radiants, monthNames, lunarTransitsTitle, 
        planetaryTransitsTitle, planetNames);
  }
}
