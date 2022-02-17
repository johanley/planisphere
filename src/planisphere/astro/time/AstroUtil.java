package planisphere.astro.time;

import planisphere.astro.star.Position;
import planisphere.math.Maths;

/** Constants and simple functions. */
public final class AstroUtil {

  public static final double J2000 = 2451545.0;
  public static final double DAYS_PER_JULIAN_CENTURY = 36525.0;
  public static final double DAYS_PER_JULIAN_MILLENIUM = 365250.0;
  public static final double DAYS_PER_JULIAN_YEAR = 365.25;
  public static final double DEGREES_PER_HOUR = 15.0;
  
  public static final int SECONDS_PER_MINUTE = 60;
  public static final int MINUTES_PER_HOUR = 60;
  public static final int SECONDS_PER_HOUR = 60 * SECONDS_PER_MINUTE;
  public static final int SECONDS_PER_DAY = 24 * SECONDS_PER_HOUR;
  
  public static final int ARCMINUTES_PER_DEGREE = 60;
  public static final int SECONDS_PER_ARCMIN = 60;
  
  /** IAU 1976. */
  public static double KM_PER_AU = 1.49597870E8;   
  
  public static double julianCenturiesSinceJ2000(double jd) {
    return (jd - J2000) / DAYS_PER_JULIAN_CENTURY; 
  }
  
  public static double julianMilleniaSinceJ2000(double jd) {
    return (jd - J2000) / DAYS_PER_JULIAN_MILLENIUM; 
  }
  
  public static double julianYearsSinceJ2000(double jd) {
    return (jd - J2000) / DAYS_PER_JULIAN_YEAR; 
  }
  
  /** Format as +2° 16' 22.2'', for example ;*/
  public static String radsToDegreeString(double rads) {
    double val = Math.abs(rads);
    String sign =  rads > 0 ? "+" : "-";
    
    double degs = Maths.radsToDegs(val); //25.123
    double degrees = Math.floor(degs); //25
    double arcmins = (degs - degrees) * ARCMINUTES_PER_DEGREE; // 0.123 * 60 = 7.38
    double arcminutes = Math.floor(arcmins); //7
    double arcsecs = (arcmins - arcminutes) * SECONDS_PER_ARCMIN; // 0.38 * 60 =  22.8
    
    return sign + (int)degrees + "°" + (int)arcminutes + "'" + threeDecimals(arcsecs) + "''";
  }
  
  /** Format as +2h 16m 22.2s, for example ;*/
  public static String radsToTimeString(double rads) {
    double val = Math.abs(rads);
    String sign =  rads > 0 ? "+" : "-";
    
    double hrs = Maths.radsToHours(val); //6.123
    double hours = Math.floor(hrs); //6
    double mins = (hrs - hours) * MINUTES_PER_HOUR; // 0.123 * 60 = 7.38
    double minutes = Math.floor(mins); //7
    double secs = (mins - minutes) * SECONDS_PER_MINUTE; // 0.38 * 60 =  22.8
    
    return sign + (int)hours + "h" + (int)minutes + "m" + threeDecimals(secs) + "s";
  }
  
  /** Return rads, 0..pi. */
  public static double angularSeparation(Position a, Position b) {
    //Meeus page 111
    double Δα = b.α - a.α;
    double Δδ = b.δ - a.δ;
    double havd = hav(Δδ) + Math.cos(a.δ) * Math.cos(b.δ) * hav(Δα);
    double cosd = 1 - 2 * havd;
    return Math.acos(cosd); //0..pi
  }

  /** Haversine of an angle. Useful for small angles. */
  private static double hav(double θ) {
    double val = Math.sin(θ/2.0);
    return Maths.sqr(val);
  }
  
  private static double threeDecimals(double val) {
    return Math.round(val * 1000) / 1000.0; //avoid int div
  }
  
}
