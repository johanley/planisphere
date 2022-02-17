package planisphere.astro.star;

import planisphere.draw.CompassPoint;

/** Data-carrier for stars. */
public final class Star {
  
  /** Index from the underlying star catalog. */
  public Integer INDEX;

  /**
   Examples: β And, β1 And, 45 And. Not the proper name (such as 'Aldebaran'). 
   First take Bayer; it not present, then take Flamsteed number. 
   Possibly empty, never null; 
  */
  public String NAME;
  
  /** Where the Bayer designation should be drawn on the chart, with respect to the star. */
  public CompassPoint BAYER_COMPASS_POINT;
  
  /** 
   Greek letter only. May have a number after the letter.
   Return an empty string if it's a Flamsteed number. 
  */
  public String getBayer() {
     String result = "";
     if (!NAME.isEmpty() && NAME.trim().length() > 0) {
       String first = NAME.trim().substring(0, 1);
       boolean isNumeric = first.chars().allMatch(Character::isDigit);
       if (!isNumeric) {
         String[] parts = NAME.trim().split(" ");
         result = parts[0];
       }
     }
     return result;
  }
  
  /** For example, 'Peg' for Pegasus. */
  public String getConstellationAbbr() {
    String result = "";
    if (NAME.trim().length()>0) {
      int space = NAME.indexOf(" ");
      if (space != -1) {
        result = NAME.substring(space).trim();
      }
    }
    return result;
  }
  
  /** Right ascension in radians. */
  public Double RA; 
  
  /** Added in order to easily define a Comparator. */
  public Double getRightAscension() {
    return RA;
  }
  
  /** Added in order to easily define a Comparator. */
  public Double getMagnitude() {
    return MAG;
  }
  
  /** Declination in radians. */
  public Double DEC; 
  
  /** Visual magnitude. */
  public Double MAG;
  
  public Position position() {
    return new Position(RA, DEC);
  }
  
  /** 
   'Vega', for instance. Possibly empty, never null.
   CAREFUL: the name varies with language! 
  */
  public String PROPER_NAME = "";
  
  /** Proper motion in right ascension. Arcsececonds per year (not radians, to retain precision). */
  public Double PROPER_MOTION_RA;
  
  /** Proper motion in declination. Arcseconds per year (not radians, to retain precision). */
  public Double PROPER_MOTION_DEC;
  
  /** 
   Parallax in arcseconds. 
   WARNING: most catalogs report some parallax values as negative. 
   Negative values are an artifact due to experimental errors.
   They usually happen with faint sources in crowded fields and/or with large proper motion.
  */
  public Double PARALLAX;

  /** Heliocentric radial velocity in km/s. */
  public Double RADIAL_VELOCITY;
  
  /** Identifier in the Durchmusterung catalog. */
  public String DM_DESIGNATION;
  /** Identifier in the Henry Draper catalog. */
  public String HD_DESIGNATION;
  /** Identifier in the SAO catalog. */
  public String SAO_DESIGNATION;
  /** Identifier in the FK5 catalog. */
  public String FK5_DESIGNATION;
  
  /** How this object is formatted into a single line in text file (utf-8). */  
  @Override public String toString(){
    String sep = ",";
    return INDEX+sep+getConstellationAbbr()+sep+RA+sep+DEC+sep+MAG+sep+NAME+sep+PROPER_NAME+sep+PROPER_MOTION_RA+sep+PROPER_MOTION_DEC+sep+PARALLAX+sep+RADIAL_VELOCITY+sep+HD_DESIGNATION;
  }
}