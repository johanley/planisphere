package planisphere.astro.time;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import planisphere.math.Maths;

/**
 Calculate GST (Greenwich Sidereal Time, with mean equinox) at 20h UT, for various days of the year.
 
 <P>This calculation is necessarily a bit vague. 
 Each year, on a given day, the exact GST at 20h UT will be different.
 As well, it very gradually changes because of precession.
 However, in the given context, there's no real need to be exact. 
*/
public final class GreenwichSiderealTime {

  /** Ordered by day. */
  public List<DailySiderealTime> everyDayOfTheYear(){
    List<DailySiderealTime> result = new ArrayList<>();
    for(Month month : EnumSet.range(Month.JANUARY, Month.DECEMBER)){
      for (int day = 1; day <= month.minLength() /*Feb 28*/; ++day) {
        double gst = gstAt20h00For(month.getValue(), day);
        result.add(new DailySiderealTime(gst, month.getValue(), day));
      }
    }
    return result;
  }
  
  // PRIVATE 
  
  /**
   For the start of the year, Jan 1, at 20h UT
   https://dc.zah.uni-heidelberg.de/apfs/times/q/form
   2000 : 2h 43m 09.4s
   2019 : 2h 44m 43.7s (this agrees with the Observer's Handbook)
   2020 : 2h 43m 46.4s
   2021 : 2h 46m 45.6s
   2022 : 2h 45m 48.3s
   2023 : 2h 44m 51.0s
   Let's simply take a representative value in the middle of the above range.
  */
  private static double GST_AT_20H_START_DATE =  Maths.rightAscensionToRads(2, 44, 57.0); //41.2375 degrees
  
  /**
   We approximate this as simply 365 exactly. 
   This will yield a date scale that "matches up" at year end, without a discontinuity.   
  */
  //private static double TROPICAL_YEAR = 365.242189;
  private static double TROPICAL_YEAR = 365.0;
  
  /** The amount by which the GST grows each day, in radians. */
  private static double DAILY_CHANGE = Maths.degToRads(360) / TROPICAL_YEAR;
  
  /** A nominal year, for running through all of its days. All years are treated the same. Avoid leap years. */
  private static LocalDate START_DATE = LocalDate.parse("2019-01-01");
  
  /** 
   Return a value in radians, in the range 0..2pi
   Example values (in degrees):
    Jan 1 degs: 40.78916666666667
    Jan 2 degs: 41.7748140284505
  */
  private double gstAt20h00For(int month, int dayOfTheMonth) {
    LocalDate theDate = LocalDate.of(START_DATE.getYear(), month, dayOfTheMonth);
    Long numDays = ChronoUnit.DAYS.between(START_DATE, theDate);
    double result = GST_AT_20H_START_DATE + numDays * DAILY_CHANGE;
    double TWO_PI = 2*Math.PI;
    if (result > TWO_PI) {
      result = result - TWO_PI;
    }
    return result;
  }
  
}