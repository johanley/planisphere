package planisphere.astro.planets;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import planisphere.astro.moon.LunarPosition;
import planisphere.astro.star.Position;
import planisphere.astro.time.AstroUtil;
import planisphere.astro.time.GregorianCal;
import planisphere.astro.time.SiderealTime;
import planisphere.config.Config;
import planisphere.config.ConfigFromFile;
import planisphere.math.Maths;
import planisphere.util.LogUtil;

/** 
 The local time that an object is due south, on a given day.
 An object may not transit at all on a given day.
 This is especially true of the Moon, which moves quickly. There's almost always one day a month 
 near full Moon, in which the Moon will not transit.
 
 <P>The time of transit is used to find the approximate position of an object in the sky.
  On the planisphere, use the intersection of the meridian and the object's orbit at the given time (and date).
  For the planets, just take the ecliptic as representing the orbit.
 
 <P>For the Moon, note that it's orbit as of mid-year is indicated on the planisphere's chart.
 Because the Moon moves so quickly, the found position is usually approximate.
 Visual interpolation is usually necessary, using the fact that the Moon moves by about 13 degrees in a day.
  
 <P>The Moon's orbit is drawn for July 1 of the given year. There is considerable 
 motion of the orbit during the year, with the ascending node regressing by about 19 degrees per year. 
*/
public final class Transit {
  
  public Transit(Config config) {
    this.config = config;
  }
  
  /**
   Time of transit for the given date (if any) in the observer's time zone.
  
   @param date according to the observer's time zone.
   @param jdToPosition the function that returns the position of the transiting object.
   @return date and time of the object's transit, according to the observer's time zone.
   If no transit happens for that day, then return null.
  */
  public Optional<LocalDateTime> transit(LocalDate date, Function<Double, Position> jdToPosition){
    Optional<LocalDateTime> result = Optional.empty();
    List<HourAngle> hourlyHAs = hourAnglesOnTheGiven(date, jdToPosition);
    Optional<Bracket> transitHour = hourInWhichTheTransitOccurs(hourlyHAs);
    if (transitHour.isPresent()) {
      result = Optional.of(linearInterpolationWithinThe(transitHour.get()));
    }
    return result;
  }
  
  /** Transits for every day of the configured year. */
  public List<Optional<LocalDateTime>> transitsForEveryDayOfTheYear(Function<Double, Position> jdToPosition){
    List<Optional<LocalDateTime>> result = new ArrayList<>();
    LocalDate jan1 = LocalDate.of(config.year(), 1, 1);
    for(int i = 0; i < GregorianCal.numDaysIn(config.year()); ++i) {
      LocalDate local = jan1.plusDays(i);
      result.add(transit(local, jdToPosition));
    }
    return result;
  }
  
  /** Transits for the 15th of every month in the configured year. */
  public List<Optional<LocalDateTime>> transitsForMidMonth(Function<Double, Position> jdToPosition){
    List<Optional<LocalDateTime>> result = new ArrayList<>();
    LocalDate jan15 = LocalDate.of(config.year(), 1, 15);
    for(int i = 0; i < 12; ++i) {
      LocalDate local = jan15.plusMonths(i);
      result.add(transit(local, jdToPosition));
    }
    return result;
  }

  private Config config;

  /** The hour angle of the Moon at a given local date-time. */
  private static final class HourAngle {
    HourAngle(LocalDateTime local, Double HA){
      this.time = local;
      this.HA = HA; //rads
    }
    LocalDateTime time;
    Double HA;
  }
  
  /** The data that comes before and after the transit. */
  private static final class Bracket {
    Bracket(HourAngle start, HourAngle end){
      this.start = start;
      this.end = end;
    }
    HourAngle start;
    HourAngle end;
  }

  /**
   The hour angle of the object, for every hour of the day. 
   Ordered by time, 0h to 24h. 
  */
  private List<HourAngle> hourAnglesOnTheGiven(LocalDate localDate,  Function<Double, Position> jdToPosition){
    List<HourAngle> result = new ArrayList<>();
    LocalDateTime midnight = LocalDateTime.of(config.year(), localDate.getMonthValue(), localDate.getDayOfMonth(), 0, 0);
    for(int hour = 0; hour <= 24; ++hour) { // include 24h, in case the transit happens in the final hour
      LocalDateTime local = midnight.plusHours(hour);
      /*
       Be careful with the exact meaning of the date and time. 
       Need to translate from the observer's time zone into UT.
      */
      SiderealTime sidereal = new SiderealTime(config);
      double lst = sidereal.siderealTime(
        local.getYear(), local.getMonthValue(), local.getDayOfMonth(), 
        local.getHour(), local.getMinute(), local.getSecond(), 
        local.getNano(), config.hoursOffsetFromUT(), config.minutesOffsetFromUT(),
        config.longitude()
      ); // 0..2pi
      double jd = GregorianCal.jdForLocal(
        local.getYear(), local.getMonthValue(), local.getDayOfMonth(), 
        local.getHour(), local.getMinute(), local.getSecond(), 
        local.getNano(), config.hoursOffsetFromUT(), config.minutesOffsetFromUT()
      );
      Position pos = jdToPosition.apply(jd);
      double ha = Maths.in2pi(lst - pos.α); 
      result.add(new HourAngle(local, ha));
    }
    return result;
  }

  /** 
   Return the pair which bracket the time of transit.
   WARNING: some days will have no transit: the return value may be null. 
  */
  private Optional<Bracket> hourInWhichTheTransitOccurs(List<HourAngle> hourAngles) {
    Bracket result = null;
    //examine pair-wise; see if the hour angle DECREASES at any point
    for(int i = 0; i < hourAngles.size() - 1; ++i) {
      HourAngle start = hourAngles.get(i);
      HourAngle end = hourAngles.get(i+1); //pair-wise (the last HA never starts a pair)
      if (start.HA > end.HA) {
        result = new Bracket(start, end);
        break;
      }
    }
    return Optional.ofNullable(result);
  }
  
  private LocalDateTime linearInterpolationWithinThe(Bracket bracket) {
    double p = (Maths.TWO_PI - bracket.start.HA) / (Maths.TWO_PI + bracket.end.HA - bracket.start.HA); //fraction of an hour
    long minutes = Math.round(p * AstroUtil.MINUTES_PER_HOUR);
    return bracket.start.time.plusMinutes(minutes);
  }
  
  public static void main(String... args) {
    //this depends on Config settings
    //for comparison with tables, you'll need to config a location on the prime meridian
    Config config = new ConfigFromFile().init();
    
    Transit moon = new Transit(config);
    LunarPosition sourceOf = new LunarPosition();
    LogUtil.log(moon.transit(LocalDate.of(2022, 2, 11), sourceOf::position)); //20h 47m 17s
    for(Optional<LocalDateTime> transit : moon.transitsForEveryDayOfTheYear(sourceOf::position)) {
      LogUtil.log(transit); 
    }
  }
}
