package planisphere.astro.time;

import static planisphere.astro.time.AstroUtil.J2000;
import static planisphere.astro.time.AstroUtil.julianCenturiesSinceJ2000;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import planisphere.astro.precession.LongTermPrecession;
import planisphere.astro.precession.Nutation;
import planisphere.config.Config;
import planisphere.config.ConfigFromFile;
import planisphere.math.Maths;
import planisphere.util.LogUtil;

/** 
 The apparent sidereal time at 20h for every day of the year, in the configured time zone.
 This is used to make a date-scale that precisely matches the given year.
 
 <P>On the date-scale, most years will have a noticeable discontinuity at the end of the year.
 This is expected. It expresses the distinction between the length of a sidereal day and a solar day. 
*/
public final class SiderealTime {
  
  public SiderealTime(Config config) {
    this.config = config;
  }
  
  /** 
   The apparent sidereal time at 20h standard time (in the configured time zone) for every day of the year, ordered by day.
   If the year is a leap year, then Feb 29 is included in the result, just like any other day. 
  */
  public List<DailySiderealTime> everyDayOfTheYear(int year){
    List<DailySiderealTime> result = new ArrayList<>();
    LocalDateTime day = LocalDateTime.of(year, Month.JANUARY.getValue(), 1, CLOCK_TIME, 0);
    double ε = obliquity(year);
    while(day.getYear() == year) {
      double lst = siderealTime(
        day.getYear(), day.getMonthValue(), day.getDayOfMonth(), day.getHour(), 0, 0, 0, 
        config.hoursOffsetFromUT(), config.minutesOffsetFromUT(), config.longitude()
      );
      double jd = GregorianCal.jd(day.getYear(), day.getMonthValue(), day.getDayOfMonth() + day.getHour()/24.0);
      lst = lst + nutation(ε, jd);
      DailySiderealTime localSiderealTimeAt20h = new DailySiderealTime(lst, day.getMonthValue(), day.getDayOfMonth());
      LogUtil.log("  Apparent sidereal time for "  + day + ", in the configured time zone: " + AstroUtil.radsToTimeString(localSiderealTimeAt20h.getRa()));
      result.add(localSiderealTimeAt20h);
      day = day.plusDays(1);
    }
    //Util.log("Sidereal time on Jan 01, at 20h (standard time in the configured time zone): " + AstroUtil.radsToTimeString(result.get(0).getRa()));
    return result;
  }

  /**
   The time is a local time, with a certain offset from UT. 
   The offsets and longitude are negative for western longitudes, positive for eastern longitudes.
   The longitude is in radians.
   The return value is in radians, 0..2pi. 
  */
  public Double siderealTime(int y, int m, int d, int h, int min, int s, int nanos, int offsetHours, int offsetMins, double longitude) {
    double jd = GregorianCal.jdForLocal(y, m, d, h, min, s, nanos, offsetHours, offsetMins);
    double result = greenwichSideralTimeFromJd(jd) + longitude;
    return Maths.in2pi(result);
  }

  /** 
   The Standard Time in the configured time zone, 
   for which the daily sidereal time is found for the configured position - {@value}. 
  */
  public static final int CLOCK_TIME = 20;

  //PRIVATE
  
  private Config config;

  /** Return rads. */
  private double obliquity(int year) {
    double jd = GregorianCal.jd(year, 7 , 1.0);
    LongTermPrecession precession = new LongTermPrecession();
    return precession.obliquity(jd);
  }
  
  /** Returns radians, 0..2pi. */
  private double greenwichSideralTimeFromJd(double jd) {
    //Meeus page 84
    double T = julianCenturiesSinceJ2000(jd);
    double result = 280.46061837 + 360.98564736629*(jd - J2000) + 0.000387933*(T*T) - (T*T*T)/38710000; // degrees
    result = Maths.in2pi(Maths.degToRads((result)));
    return result;
  }
  
  /** Tiny correction for nutation, on the order of 1 second of time. */
  private double nutation(double obliquity, double jd) {
    Nutation nutation = new Nutation(jd);
    double Δψ = nutation.Δψ(); //rads
    double correction = Δψ * Math.cos(obliquity); //rads
    return correction;
  }
  
  /** Informal test. */
  public static void main(String... args) {
    Config config = new ConfigFromFile().init();
    SiderealTime sidTime = new SiderealTime(config);
    double rads = sidTime.siderealTime(1987, 4, 10, 0, 0, 0, 0, 0, 0, 0);
    LogUtil.log("Apparent LST " + AstroUtil.radsToTimeString(rads));
    
    rads = sidTime.siderealTime(2022, 1, 2, 0, 0, 0, 0, 0, 0, 0);
    LogUtil.log("Apparent LST at Greenwich 2022-01-02 0h: " + AstroUtil.radsToTimeString(rads));
  }
}
