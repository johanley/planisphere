package planisphere.astro.time;

import static planisphere.astro.time.AstroUtil.SECONDS_PER_DAY;
import static planisphere.astro.time.AstroUtil.SECONDS_PER_HOUR;
import static planisphere.astro.time.AstroUtil.SECONDS_PER_MINUTE;

import java.time.LocalDateTime;

import planisphere.util.LogUtil;

/** 
 Convert a date from the Gregorian proleptic calendar into a Julian Date (JD).
 There are no restrictions on the input date.
 
 <P>For dates in the Julian calendar, see {@link JulianCal}.
 
 <P>See {@JulianCalendar} for important information, which is not repeated here. 
 The implementation of that class is simpler, and is easier to understand, since it's 
 based on a simpler cycle of 4 years.
 
 <P>Ref: Explanatory Supplement to the Ephemeris, 1961.
 <P>Ref: https://legacy-www.math.harvard.edu/computing/javascript/Calendar/index.html
*/
public final class GregorianCal {
  
  /** Return the Julian Date (JD) corresponding to the given moment in the Gregorian calendar (UT), at Greenwich. */
  public static double jd(int year, int month, double day) {
    double result = 0.0;
    int sign = year < 0 ? -1 : 1;
    /* 
      Chop into blocks of years, from largest to smallest, and count the days in each block.
      There is asymmetry between + and - years; they aren't handled in the same way.
      
      There are 4 parts to consider:
        1. big cycles (complete) - N * 400 years 
        2. small cycles (complete) - M * 4 years 
        3. remainder years - complete (0..3)
        4. remainder days - in the last year 1..366
     */
    if (sign > 0) {
      result = nonNegativeYears(year, month, day);
    }
    else {
      result = negativeYears(year, month, day);
    }
    return result;
  }
  
  /** Return the Julian Date for the given Gregorian calendar date, according to a local time zone - with indicated offset. */
  public static double jdForLocal(int y, int m, int d, int h, int min, int s, int nanos, int offsetHours, int offsetMins) {
    //use Java to add the offset hours to the date-time, to get a new date-time; handles roll-over issues
    LocalDateTime local = LocalDateTime.of(y, m, d, h, min, s, nanos);
    LocalDateTime ut = local.plusHours(-offsetHours).plusMinutes(-offsetMins);
    double ONE_BILLION = 1_000_000_000.0;
    double seconds = ut.getSecond() + ut.getNano()/ONE_BILLION; //avoid integer div
    return jd(ut.getYear(), ut.getMonthValue(), ut.getDayOfMonth(), ut.getHour(), ut.getMinute(), seconds);
  }
  
  /** Return the Julian Date (JD) corresponding to the given moment in the Gregorian calendar (UT), at Greenwich. */
  public static Double jd(int year, int month, int day, int hour, int min, double fractionalSeconds) {
    double fractionalDay = fractionalDay(day, hour, min, fractionalSeconds);
    return jd(year, month, fractionalDay);
  }

  public static int numDaysIn(int year) {
    return isLeap(year) ? LEAP_YEAR : NORMAL_YEAR;
  }
  
  /**
   The Julian Date as of Jan 0.0 TT (midnight) in the year 0, according to the Gregorian calendar (not the Julian).
   At this time, the two calendars differed by two days, with the Gregorian two days behind the Julian.
   Treat this as the basis. Calculate the number of days +/- from this date. Value - {@value}.
  */
  public static final double JAN_0_0_YEAR_0000 = JulianCal.JAN_0_0_YEAR_0000 + 2.0;

  private static final int LEAP_YEAR = JulianCal.LEAP_YEAR; //366
  private static final int NORMAL_YEAR = JulianCal.NORMAL_YEAR; //365
  private static final int SMALL_CYCLE_DAYS = JulianCal.CYCLE_DAYS; //1461
  private static final int SMALL_CYCLE_YRS = JulianCal.CYCLE_YRS; //4
  
  private static final int LONG_CENTURY = 25*SMALL_CYCLE_DAYS; // 36525
  private static final int SHORT_CENTURY = 24*SMALL_CYCLE_DAYS + 4*NORMAL_YEAR; // 36524 
  private static final int BIG_CYCLE_DAYS = 3*SHORT_CENTURY + 1*LONG_CENTURY; // 146097
  private static final int BIG_CYCLE_YRS = 400;

  /** The calculation for non-negative years. */
  private static double nonNegativeYears(int year, int month, double day) {
    double result = 0.0;
    
    //1. big cycles - no need to track the exact years because their length is fixed 
    int numBigCycles = year / BIG_CYCLE_YRS;  //integer division!
    int bigCycles = numBigCycles * BIG_CYCLE_DAYS;
    
    //2. small cycles - track the exact years (variable numbers of leap years in the block)
    int bigRemainder = year % BIG_CYCLE_YRS; //0..399
    int numSmallCycles = bigRemainder / SMALL_CYCLE_YRS; //0..99, integer division
    int startYr = numBigCycles * BIG_CYCLE_YRS;
    int endYr = startYr + numSmallCycles * SMALL_CYCLE_YRS;
    int smallCycles = daysInCompleteYears(startYr, endYr); //excluded end
    
    //3. remainder years - whole years left after the small cycles
    int remainderYears = daysInCompleteYears(endYr, year); //excluded end
    
    //4. remainder days in the final year
    double remainderDays = JulianCal.remainderDaysFromJan0(month, day, isLeap(year));
    
    result = JAN_0_0_YEAR_0000 + bigCycles + smallCycles + remainderYears + remainderDays; 
    return result;
  }
  
  /** The calculation for negative years. */
  private static double negativeYears(int year, int month, double day) {
    double result = 0.0;
    //The zero point is for Dec 31 in the year -1. 
    //In the negative years, it's convenient to use year+1 as the base from which to track cycles.
    int y_biased = year + 1;

    //1. big cycles - no need to track the exact years because their length is fixed 
    int numBigCycles = Math.abs((y_biased) / BIG_CYCLE_YRS);  //integer division!
    int bigCycles = numBigCycles * BIG_CYCLE_DAYS;
    
    //2. small cycles - track the exact years (variable numbers of leap years in this block)
    int bigRemainder = Math.abs(y_biased) % BIG_CYCLE_YRS; //0..399
    int numSmallCycles = bigRemainder / SMALL_CYCLE_YRS; //0..99, integer division
    int endYr = -numBigCycles * BIG_CYCLE_YRS;
    int startYr = endYr - numSmallCycles * SMALL_CYCLE_YRS;
    int smallCycles = daysInCompleteYears(startYr, endYr); //excluded end
    
    //3. remainder years - 0..3 whole years left after the small cycles
    int start = y_biased;
    int end = startYr; //from above, the start of the small cycles
    int remainderYears = daysInCompleteYears(start, end); //excluded endpoint
    
    //4. remainder days in the first year
    double remainderDays = JulianCal.remainderDaysUntilDec32(month, day, isLeap(year));
    
    int OVERHANG = 1; // Jan 0.0 is already impinging onto the negative years, by 1 day
    result = JAN_0_0_YEAR_0000 + OVERHANG - (bigCycles + smallCycles + remainderYears + remainderDays);
    return result;
  }
  
  /** Number of days in a full set of complete years, including the start but excluding the end. */
  private static int daysInCompleteYears(int smallCycleYearsStart, int smallCycleYearsEnd) {
    int result = 0;
    for(int y = smallCycleYearsStart; y < smallCycleYearsEnd; ++y) {
      int increment = isLeap(y) ? LEAP_YEAR : NORMAL_YEAR;
      result = result + increment;
    }
    return result;
  }
  
  /** Not the same rule as in the Julian Calendar. */
  private static boolean isLeap(int year) {
    boolean result = (year % SMALL_CYCLE_YRS == 0);
    //override: century years are leap years only if evenly divisible by 400
    if (year % 100 == 0) {
      result = (year % BIG_CYCLE_YRS == 0);
    }
    return result;
  }
  
  private static double fractionalDay(int day, int hour, int min, double fractionalSeconds) {
    double totalSeconds = fractionalSeconds + (min * SECONDS_PER_MINUTE) + (SECONDS_PER_HOUR * hour);
    return day + (totalSeconds / SECONDS_PER_DAY);
  }
  
  /** Informal test harness. */
  public static void main(String... args) {
    test(0,1,1.0, JAN_0_0_YEAR_0000 + 1.0);
    test(0,1,31.0, JAN_0_0_YEAR_0000 + 31.0);
    test(0,2,1.0, JAN_0_0_YEAR_0000 + 31.0 + 1.0);
    test(0,3,1.0, JAN_0_0_YEAR_0000 + 31.0 + 29.0 + 1.0); // year 0 is a leap year
    test(1,1,1.0, JAN_0_0_YEAR_0000 + 1.0 + LEAP_YEAR);
    test(2,1,1.0, JAN_0_0_YEAR_0000 + 1.0 + LEAP_YEAR + 1*NORMAL_YEAR);
    test(3,1,1.0, JAN_0_0_YEAR_0000 + 1.0 + LEAP_YEAR + 2*NORMAL_YEAR);
    test(4,1,1.0, JAN_0_0_YEAR_0000 + 1.0 + LEAP_YEAR + 3*NORMAL_YEAR);
    test(5,1,1.0, JAN_0_0_YEAR_0000 + 1.0 + LEAP_YEAR + 3*NORMAL_YEAR + 1*LEAP_YEAR);
    
    //https://legacy-www.math.harvard.edu/computing/javascript/Calendar/index.html
    test(-8, 1, 1.5, 1718138.0);
    test(-101, 1, 1.5, 1684171.0);
    test(-799, 1, 1.5, 1429232.0);
    test(-800, 1, 1.5, 1428866.0);
    test(-801, 1, 1.5, 1428501.0);
    test(99, 12,31.5, 1757584.0);
    test(100,1,1.5, 1757584.0 + 1.0);
    test(100,1,31.5, 1757584.0 + 31.0);
    test(100,2,1.5, 1757584.0 + 31.0 + 1.0);
    test(100,2,28.5, 1757584.0 + 31.0 + 28.0); //100 is not a leap year
    test(100,3,1.5, 1757584.0 + 31.0 + 28.0 + 1.0);
    test(3000, 1, 1.5, 2816788);
    test(30000, 1, 1.5, 12678335);
        
    test(100,1,1.5, 1757585.0);
    test(101,1,1.5, 1757950.0); 
    test(200,1,1.5, 1794109.0); 
    test(300,1,1.5, 1830633.0); 
    test(400,1,1.5, 1867157); 
    test(700,1,1.5, 1976730);  
    test(800,1,1.5, 2013254);
    
    //Explanatory Supplement
    test(1500, 1, 1.5, 2268923.0 + 1.0);  
    test(1600, 1, 1.5, 2305447.0 + 1.0); 
    test(1700, 1, 1.5, 2341972.0 + 1.0); 
    test(1800, 1, 1.5, 2378496.0 + 1.0);  
    test(1900, 1, 1.5, 2415020.0 + 1.0);  
    test(1901, 1, 1.5, 2415020.0 + 1.0 + 365.0); 
    
    //Meeus
    test(1957, 10, 4.81, 2436116.31);
    
    //From Vondrak, Wallace, Capitaine 2011
    // -1374 May 3, at 13:52:19.2 TT 
    test(-1374, 5, 3.578, 1219339.078); 
    
    LogUtil.log("Done testing.");
  }
  
  private static void test(int y, int m, double d, double expected) {
    double jd = GregorianCal.jd(y, m, d);
    if (jd != expected) {
      throw new RuntimeException("Expected:" + expected + " calc:" + jd + " for " + y+"-"+m+"-"+d);
    }
  }
}
