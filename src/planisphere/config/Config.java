package planisphere.config;

import static planisphere.astro.time.AstroUtil.DEGREES_PER_HOUR;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import planisphere.astro.time.AstroUtil;
import planisphere.draw.Bounds;
import planisphere.math.Maths;

/** 
 Configuration data for the generation of the planisphere.
 
 The configuration data can come from different sources. 
 When run as a standalone program, the data can come from a text file.
 In a servlet environment, it may come from user input in a web form.
 
 <P>All fields are immutable objects.
 <P>This object is 'pilgrim data'. It's passed around from the top-level object to many lower-level objects.
 This design avoids storing data in static fields. That design can be used both in a standalone program, and in 
 server environments. 
*/
public final class Config {
  
  public Config(
    Integer year, String location, Double latitude, Double longitude, Integer hoursOffsetFromUT, 
    Integer minutesOffsetFromUT, Double declinationGap, Float width, Float height, 
    String outputDir, String fontDir, Integer greyConstellationLines, Integer greyAltAzLines, Integer smallestTimeDivision, 
    String radiants, String monthNames, String lunarTransitsTitle, String planetaryTransitsTitle, String planetNames
  ){
    this.year = year;
    this.location = location;
    this.latitude = latitude;
    this.longitude = longitude;
    this.hoursOffsetFromUT = hoursOffsetFromUT;
    this.minutesOffsetFromUT = minutesOffsetFromUT;
    this.declinationGap = declinationGap;
    this.width = width;
    this.height = height;
    this.outputDir = outputDir;
    this.fontDir = fontDir;
    this.greyConstellationLines = new Color(greyConstellationLines, greyConstellationLines, greyConstellationLines);
    this.greyAltAzLines = new Color(greyAltAzLines, greyAltAzLines, greyAltAzLines);
    this.smallestTimeDivision = smallestTimeDivision;
    this.radiants = radiants;
    this.monthNames = monthNames;
    this.lunarTransitsTitle = lunarTransitsTitle;
    this.planetaryTransitsTitle = planetaryTransitsTitle;
    this.planetNames = planetNames;
  }

  /** 
   The year for which the planisphere will be generated.
   Having a specific year helps greatly in increasing the accuracy of results.
   It also means you should regenerate the planisphere once a year. 
  */
  public Integer year() { return year; }
  
  /** Simple description of the observer's location. */
  public String location() {return location;  }
  
  /** The observer's geographical latitude. Radians. */
  public Double latitude() { return latitude; }
  
  /** The observer's geographical longitude. Radians. */
  public Double longitude() { return longitude; }
  
  /** How many hours between the observer's time zone and the prime meridian. */
  public Integer hoursOffsetFromUT() { return hoursOffsetFromUT;  }
  
  /** 
   How many minutes (0..59) to be added to {@link #hoursOffsetFromUT()}.
   For most jurisdictions, this number is 0, since most time zones are offset by a whole
   number of hours from UT. 
  */
  public Integer minutesOffsetFromUT() {  return minutesOffsetFromUT;  }
  
  /**
   In the northern (southern) hemisphere, this number of degrees of declination will be abandoned in 
   in the south (north). With the stereographic projection, in most cases it's usually not desirable 
   to go all the way to the horizon in the south (north).
   In very high latitudes, this may be set to 0, or even to negative amounts, in order to see 
   the Sun graze the horizon.
   A decimal number between -30 and 30 degrees.
  */
  public Double declinationGap() { return declinationGap; }
  
  /** 
   Page width in points (72 points per inch).
   Not the width of the chart, but the width of the page that contains the chart.
   Same for the height. 
  */
  public Float width() {return width; }
  
  /** Page height in points (72 points per inch). */
  public Float height() { return height; }
  
  /**
   The directory where the PDF files for the planisphere are generated. The directory must already exist.
   This setting may not be needed in a servlet environment, where the files are served as a byte stream to the browser. 
  */
  public String outputDir() { return outputDir; }
  
  /** 
   The directory on the computer that contains the Times New Roman font.
   This tool is hard-coded to that specific font. Times New Roman is pre-installed
   on almost all computers. 
  */
  public String fontDir() { return fontDir; }
  
  /** The shade of grey used to render the constellation lines (and other items). */
  public Color greyConstellationLines() { return greyConstellationLines; }
  
  /** The shade of grey used to render the altitude-azimuth lines on the transparency. */
  public Color greyAltAzLines() { return greyAltAzLines; }
  
  /** 
   The smallest division of time on the time-scale, in minutes.
   You have only two choices for this item: 1 or 2.
   You may prefer 2 minutes when the planisphere is small.
  */
  public Integer smallestTimeDivision() { return smallestTimeDivision; }
  
  /** 
   A structured string containing data for the major meteor showers.
   Reference: <a href='https://www.imo.net/files/data/vmdb/vmdbrad.txt'>IMO</a>.
  */
  public String meteorShowerRadiants() { return radiants; }
  
  /**
   The names of the months.
   These need to be abbreviations, in order to fit nicely where they are currently placed 
   on the date-scale, between the first and the fifth day. Separate the months with a comma.
   You can use numbers if you wish (01..12).
  */
  public String monthNames() { return monthNames; }
  
  /** Title of a table on the back of the star chart. */
  public String lunarTransitsTitle() { return lunarTransitsTitle; }
  
  /** Title of a table on the back of the star chart. */
  public String planetaryTransitsTitle() { return planetaryTransitsTitle; };
  
  /** The names of planets on the back of the star chart. */
  public String planetNames() { return planetNames; }
  
  /** The extreme declination to be shown on the planisphere, in degrees. Calculated field. */
  public Double declinationLimit() {
    double lat = Maths.radsToDegs(latitude);
    double sign = isNorthernHemisphere() ? +1 : -1;
    return lat - sign*90.0  + sign*declinationGap;
  }
  
  /** Radians. Calculated field. */
  public Double radsWestOfCentralMeridian() {
    double hours = hoursOffsetFromUT() + minutesOffsetFromUT()/60.0; //avoid integer division!
    double degrees = hours * DEGREES_PER_HOUR;
    double timeZoneCentralLongitude = Maths.degToRads(degrees); 
    return timeZoneCentralLongitude - longitude; // -60 - (-63) = 3 deg
  }
  
  /** Calculated field. */
  public boolean isNorthernHemisphere() { return latitude >= 0; }
  
  /**
   Upper and lower limits on declination and right ascension for the star chart, in degrees and hours.
   Calculated field. 
  */
  public Bounds starChartBounds() {
    double POLE = 90.0; //degrees
    double lowerDec = isNorthernHemisphere() ? declinationLimit() : -POLE;
    double upperDec = isNorthernHemisphere() ? POLE : declinationLimit();
    return new Bounds(lowerDec, upperDec, 0.0, 24.0);
  }
  
  /** Month names. Calculated field. */
  public List<String> monthNamesList(){
    List<String> result = new ArrayList<>();
    String[] parts = monthNames.split(Pattern.quote(","));
    for(String part : parts) {
      result.add(part.trim());
    }
    if (result.size() != 12) {
      throw new RuntimeException("You don't have 12 month names specified in the config file.");
    }
    return result;
  }

  /** For debugging. All config settings. */
  @Override public String toString() {
    StringBuilder result = new StringBuilder();
    toStringLine(Setting.output_directory, outputDir(), result);
    toStringLine(Setting.width, width(), result);
    toStringLine(Setting.height, height(), result);
    toStringLine(Setting.location, location(), result);
    toStringLine(Setting.latitude, AstroUtil.radsToDegreeString(latitude()), result);
    toStringLine(Setting.longitude, AstroUtil.radsToDegreeString(longitude()), result);
    toStringLine(Setting.hours_offset_from_ut, hoursOffsetFromUT(), result);
    toStringLine(Setting.minutes_offset_from_ut, minutesOffsetFromUT(), result);
    toStringLine(Setting.degrees_west_of_central_meridian, AstroUtil.radsToDegreeString(radsWestOfCentralMeridian()), result);
    toStringLine(Setting.declination_gap, declinationGap(), result);
    toStringLine(Setting.year, year(), result);
    toStringLine(Setting.font_directory, fontDir(), result);
    toStringLine(Setting.grey_constellation_lines, greyConstellationLines(), result);
    toStringLine(Setting.grey_alt_az_lines, greyAltAzLines(), result);
    toStringLine(Setting.smallest_time_division, smallestTimeDivision(), result);
    toStringLine(Setting.radiants, meteorShowerRadiants(), result);
    toStringLine(Setting.month_names, monthNames(), result);
    toStringLine(Setting.lunar_transits_title, lunarTransitsTitle(), result);
    toStringLine(Setting.planetary_transits_title, planetaryTransitsTitle(), result);
    toStringLine(Setting.planet_names, planetNames(), result);
    return result.toString().trim();
  }
  
  private Integer year;
  private String location = "";
  private Double latitude;
  private Double longitude;
  private Integer hoursOffsetFromUT;
  private Integer minutesOffsetFromUT;
  private Double declinationGap;
  
  private Float width;
  private Float height;
  private String outputDir = "";
  
  private String fontDir = "";
  private Color greyConstellationLines;
  private Color greyAltAzLines;
  private Integer smallestTimeDivision;
  private String radiants = "";
  private String monthNames = "";
  
  private String lunarTransitsTitle = "";
  private String planetaryTransitsTitle = "";
  private String planetNames = "";

  private void toStringLine(Setting setting, Object value, StringBuilder result) {
    result.append("  " + setting.toString() + " = " + value.toString() + Constants.NL); 
  }
}
