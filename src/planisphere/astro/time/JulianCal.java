package planisphere.astro.time;

import java.time.Month;

import planisphere.util.LogUtil;

/** 
 Convert any date from the Julian proleptic calendar into a Julian Date.
 There are no restrictions on the input date.
 
 <P>Many algorithms for this task cannot handle dates before JD=0.0 (or even worse, for negative years).
 Example: <a href='https://aa.usno.navy.mil/calculated/juliandate?ID=AA&date=01%2F01%2F10000&era=BC&time=00%3A00%3A00.000&submit=Get+Date'>the US Naval Observatory</a>.
 (Did the Universe somehow pop into existence in 4713 BC, at the command of an Old Testament God?)
 
 <P>Many algorithms for this task are inexcusably cryptic.
 
 <P>java.time.temporal.JulianFields isn't a Julian date; it uses only whole days, is ambiguous about the starting day, 
 and has the Julian day starting at midnight. 

 <P>The astronomer's convention is used here: 1AD is preceded by the year 0, not 1BC. 
 The year 2BC is the year -1 in this convention, and so on.

 <P> JD = 0.0 is for Jan 1 12hTT (Jan 1.5), -4712 (4713 BC) in the Julian calendar. 
 The Julian day starts at noon. 
 During one calendar day, the Julian day will have two values, one before noon and one after noon.
 
 <P>The following years are leap years: 
 <pre>... -12, -8, -4, 0, 4, 8, 12, ...</pre>
 
 <P>Ref: Explanatory Supplement to the Ephemeris, 1961.
 <P>Ref: https://legacy-www.math.harvard.edu/computing/javascript/Calendar/index.html
 <P>Ref: https://aa.usno.navy.mil/data/JulianDate
*/
public final class JulianCal {

  /** 
   Return the Julian Date (JD) corresponding to the given moment.
   The given moment must be in the Julian Calendar. 
   For dates in the Gregorian Calendar, see {@link GregorianCal}.
  */
  public static double jd(int year, int month, double day) {
    double result = 0.0;
    int sign = year < 0 ? -1 : 1;
    /* 
     Count blocks of time, from largest to smallest.
     There is asymmetry between + and - years. They aren't handled in the same way.
     
      There are 3 parts to consider:
        1. cycles (complete) - 0..N * 4 years 
        2. remainder years (complete),  0..3 years
        3. remainder days - in the last year, 1..366 days
    */
    if (sign > 0) {
      result = nonNegativeYears(year, month, day);
    }
    else {
      result = negativeYears(year, month, day);
    }
    return result;
  }
  
  /**
   The Julian Date as of Jan 0.0 TT (midnight) in the year 0, in the Julian Calendar (not Gregorian).
   That moment corresponds to the end of Dec 31 of the year -1. 
   Calculate the number of days +/- from this moment as a convenient basis for the calculation. Value - {@value} .
  */
  public static final double JAN_0_0_YEAR_0000 = 1721056.5;
  
  /** Number of days in a leap year - {@value}. */
  public static final int LEAP_YEAR = 366;
  /** Number of days in a non-leap year - {@value}. */
  public static final int NORMAL_YEAR = 365;
  /** Number of days in a complete cycle of 4 years - {@value}. */
  public static final int CYCLE_DAYS = 1 * LEAP_YEAR + 3 * NORMAL_YEAR; // 1461
  /** Number of year in the cycle of leap years and non-leap years - {@value}. */
  public static final int CYCLE_YRS = 4;

  /** 
   Calculation for non-negative years.
   
   <pre>
          L*********  L*********  L..  cycles start with a leap year
    y:    0  1  2  3  4  5  6  7  8.. 
    y/4:  0  0  0  0  1  1  1  1  2..  integer division
    
   </pre>  
  */
  private static double nonNegativeYears(int year, int month, double day) {
    double result = 0.0;
    
    //1. cycles (cycles have the same value of year/4, start with a leap year, and always have the same length)
    int numCycles = year / CYCLE_YRS;  // N, integer division!
    int cycles = numCycles * CYCLE_DAYS; // N * 1461  
    
    //2. remainder years - 0..3 whole years left after the cycles
    int startYr = numCycles * CYCLE_YRS;
    int remainderYears = daysInCompleteYears(startYr, year); //excluded end!
    
    //3. remainder days in the final year
    double remainderDays = remainderDaysFromJan0(month, day, isLeap(year));
    
    result = JAN_0_0_YEAR_0000 + cycles + remainderYears + remainderDays;
    return result;
  }
  
  /**
   Calculation for negative years.
   
    <pre>
            .. *  L*********  L*********  cycles start with a leap year
    y       ..-9 -8 -7 -6 -5 -4 -3 -2 -1    
    y + 1   ..-8 -7 -6 -5 -4 -3 -2 -1  0  
    (y+1)/4 ..-2 -1 -1 -1 -1  0  0  0  0  integer division
    
    </pre>
   */
  private static double negativeYears(int year, int month, double day) {
    double result = 0.0;
    
    //The zero point is for Dec 31 in the year -1. 
    //In the negative years, it's convenient to use year+1 for some items (see the diagram above).
    
    //1. cycles (cycles have the same value of y+1/4, start with a leap year, and always have the same length)
    int numCycles = Math.abs((year + 1) / CYCLE_YRS);  // N, integer division!
    int cycles = numCycles * CYCLE_DAYS; // N * 1461  
    
    //2. remainder years - 0..3 whole years left after the cycles
    int startYr = year + 1;
    int endYr = -numCycles * CYCLE_YRS; 
    int remainderYears = daysInCompleteYears(startYr, endYr); //excluded end!
    
    //3. remainder days in the given year
    double remainderDays = remainderDaysUntilDec32(month, day, isLeap(year));
    
    int OVERHANG = 1; // Jan 0.0 is already impinging onto the negative years, by 1 day
    result = JAN_0_0_YEAR_0000 + OVERHANG - (cycles + remainderYears + remainderDays);
    return result;
  }
  
  /** 
   Number of days in a full set of complete years.
   Includes the start, but excludes the end.
   Returns 0 if the start and end are the same year. 
  */
  private static int daysInCompleteYears(int startYr, int endYr) {
    int result = 0;
    for(int y = startYr; y < endYr; ++y) {
      int increment = isLeap(y) ? LEAP_YEAR : NORMAL_YEAR;
      result = result + increment;
    }
    return result;
  }
  
  /** Return the number of days since Jan 0.0 for the given year. */
  static double remainderDaysFromJan0(int month, double day, boolean isLeap) {
    int monthAccumulator = 0;
    for(int before = Month.JANUARY.getValue(); before < month; ++before) {
      monthAccumulator = monthAccumulator + Month.of(before).length(isLeap); 
    }
    double result = monthAccumulator + day;
    return result;
  }
  
  /** Return the number of days until Dec 32.0 in the given year. */
  static double remainderDaysUntilDec32(int month, double day, boolean isLeap) {
    int monthAccumulator = 0;
    //count backwards in time
    for(int after = Month.DECEMBER.getValue(); after > month; --after) {
      monthAccumulator = monthAccumulator + Month.of(after).length(isLeap); 
    }
    double result = monthAccumulator + dayComplement(month, day, isLeap);
    return result;
  }
  
  /** The number of days remaining in the given month, from the given day. */
  private static double dayComplement(int month, double day, boolean isLeap) {
    int length = Month.of(month).length(isLeap);
    return (length + 1) - day;
  }
  
  private static boolean isLeap(int year) {
    return year % CYCLE_YRS == 0;
  }
  
  /** Informal test harness. */
  public static void main(String... args) {
    test(0, 1, 1.0, JAN_0_0_YEAR_0000 + 1.0);
    test(0, 1, 31, JAN_0_0_YEAR_0000 + 31.0);
    test(0, 2, 1,  JAN_0_0_YEAR_0000 + 31.0 + 1.0);
    test(0, 2, 28, JAN_0_0_YEAR_0000 + 31.0 + 28.0);
    test(0, 2, 29, JAN_0_0_YEAR_0000 + 31.0 + 29.0);
    test(0, 3, 1, JAN_0_0_YEAR_0000 + 31.0 + 29.0 + 1.0);
    test(0, 12, 1, JAN_0_0_YEAR_0000 + 31.0 + 29.0 + 31.0 + 30.0 + 31.0 + 30.0 + 31.0 + 31.0 + 30.0 + 31.0 + 30.0 + 1.0);
    test(0, 12, 31, JAN_0_0_YEAR_0000 + 31.0 + 29.0 + 31.0 + 30.0 + 31.0 + 30.0 + 31.0 + 31.0 + 30.0 + 31.0 + 30.0 + 31.0);
    test(0, 12, 31, JAN_0_0_YEAR_0000 + 366.0);
    test(1, 1, 1, JAN_0_0_YEAR_0000 + 366.0 + 1.0);
    test(1, 2, 1, JAN_0_0_YEAR_0000 + 366.0 + 31.0 + 1.0);
    test(1, 3, 1, JAN_0_0_YEAR_0000 + 366.0 + 31.0 + 28.0 + 1.0);
    test(-1, 12, 31.0, JAN_0_0_YEAR_0000);
    test(-1, 12, 31.0, JAN_0_0_YEAR_0000);
    test(-1, 12, 30.0, JAN_0_0_YEAR_0000 - 1.0);
    test(-1, 12, 29.0, JAN_0_0_YEAR_0000 - 2.0);
    test(-1, 12, 1.0, JAN_0_0_YEAR_0000 - 30.0);
    test(-1, 11, 30.0, JAN_0_0_YEAR_0000 - 30.0 - 1.0);
    
    //Explanatory Supplement tables
    test(-5, 1, 1.5, 1719232.0);
    test(1,  1, 1.5, 2415385.0 + 1.0 - 693962.0);
    test(2,  1, 1.5, 2415750.0 + 1.0 - 693962.0);
    test(3,  1, 1.5, 2416115.0 + 1.0 - 693962.0);
    test(4,  1, 1.5, 2416480.0 + 1.0 - 693962.0);
    test(5,  1, 1.5, 2416846.0 + 1.0 - 693962.0);
    
    test(-98, 1, 1.5, 1684532.0 + 1.0 + LEAP_YEAR + NORMAL_YEAR); 
    test(-99, 1, 1.5, 1684532.0 + 1.0 + LEAP_YEAR); 
    test(-100, 1, 1.5, 1684532.0 + 1.0); 
    test(-101, 1, 1.5, 1684532.0 + 1.0 - NORMAL_YEAR);
    test(-102, 1, 1.5, 1684532.0 + 1.0 - 2*NORMAL_YEAR);
    
    test(-600, 1, 1.5, 1501907.0 + 1.0); 
    test(-2000, 1, 1.5, 990557.0 + 1.0);
    
    test(-1000, 1, 1.5, 1355807.0 + 1.0);
    test(-1001, 1, 1.5, 1355807.0 + 1.0 - NORMAL_YEAR);
    
    test(100, 1, 1.5, 1757582.0 + 1.0);
    test(200, 1, 1.5, 1794107.0 + 1.0);
    test(300, 1, 1.5, 1830632.0 + 1.0);
    test(400, 1, 1.5, 1867157.0 + 1.0);
    test(500, 1, 1.5, 1903682.0 + 1.0);
    
    test(1234, 5, 5.5, 2171901.0);
    test(200, 1, 1.5, 1794108.0);
    test(1500, 1, 1.5, 2268933.0);
    test(1600, 1, 1.5, 2305458.0);
    test(1800, 1, 1.5, 2378507.0 + 1.0);
    test(1900, 1, 1.5, 2415032.0 + 1.0);
    
    //USNO https://aa.usno.navy.mil/data/JulianDate
    test(399, 12, 31.0, 1867156.5);
    
    //Meeus page 61 and 62
    test(333, 1, 27.5, 1842713.0);
    test(837, 4, 10.3, 2026871.8);
    test(-1000, 7, 12.5, 1356001.0);
    test(-1000, 2, 29.0, 1355866.5);
    test(-1001, 8, 17.9, 1355671.4); 
    test(-4712, 1, 1.5, 0.0);
    test(-4712, 1, 1.0, -0.5);
    test(-4713, 12, 31.0, -1.5);
    test(-4713, 12, 30.0, -2.5);
    
    LogUtil.log("Done.");
  }
  
  private static void test(int y, int m, double d, double expected) {
    double jd = JulianCal.jd(y, m, d);
    if (jd != expected) {
      throw new RuntimeException("Expected:" + expected + " calc:" + jd + " for " + y+"-"+m+"-"+d);
    }
  }
}
