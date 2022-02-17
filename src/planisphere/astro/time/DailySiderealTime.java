package planisphere.astro.time;

import planisphere.math.Maths;

/** The Sidereal Time at 20h UT, for every day of the year. */
public final class DailySiderealTime implements Comparable<DailySiderealTime>{
  
  public DailySiderealTime(Double ra, Integer month, Integer day) {
    this.ra = ra;
    this.month = month;
    this.day = day;
  }

  /** Right ascension in radians, for the given day. */
  public Double getRa() {
    return ra;
  }
  
  /** The day in the month, 1..31 */
  public Integer getDay() {
    return day;
  }
  
  /** The month, 1..12 */
  public Integer getMonth() {
    return month;
  }

  /** Sorts by right ascension. */
  @Override public int compareTo(DailySiderealTime that) {
    return this.ra.compareTo(that.ra);
  }

  @Override public String toString() {
    return "month:" + month + " day:" + day + " sidereal time at 20h :" + Maths.radsToDegs(ra);
  }
  
  private Double ra;
  private Integer day; //1..31
  private Integer month; //1..12; use a number to allow for translation
  
}
