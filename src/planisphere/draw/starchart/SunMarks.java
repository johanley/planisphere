package planisphere.draw.starchart;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.time.LocalDateTime;
import java.time.Month;

import planisphere.astro.planets.SolarPosition;
import planisphere.astro.precession.LongTermPrecession;
import planisphere.astro.star.Position;
import planisphere.astro.time.GregorianCal;
import planisphere.config.Config;
import planisphere.draw.Projection;
import planisphere.util.LogUtil;

/**
 Mark the apparent position of the Sun at 18h local standard time, each day of the year.
 The marks trace out the ecliptic.
 
 <P>The Sun is drawn differently according to the day of the month, with the first of the month as the largest.
  
 <P>On a leap year, you will usually see some overlap of Jan 1 and Dec 31. That is intentional. 
*/
final class SunMarks {
  
  SunMarks(Projection projection, Graphics2D g, Config config){
    this.projection = projection;
    this.g = g;
    this.config = config;
  }
  
  void draw() {
    int NUM_DAYS = GregorianCal.numDaysIn(config.year());
    LongTermPrecession precession = new LongTermPrecession();
    double obliquity = precession.obliquity(GregorianCal.jd(config.year(), 7, 1)); //mid-year, fairly constant; rads
    LocalDateTime day = LocalDateTime.of(config.year(), Month.JANUARY, 1, HOUR_OF_DAY, 0, 0);
    LogUtil.log("Sun's position at 18h standard time (for the location), for each day:");
    SolarPosition sun = new SolarPosition();
    for(int i = 1; i <= NUM_DAYS; ++i) {
      double jd = GregorianCal.jdForLocal(day.getYear(), day.getMonthValue(), day.getDayOfMonth(), HOUR_OF_DAY, 0, 0, 0, config.hoursOffsetFromUT(), config.minutesOffsetFromUT());
      if (i == 1) {
        LogUtil.log("Starting date-time: " + day + " JD:" + jd);
      }
      Position apparentPos = sun.apparentPosition(jd, obliquity);
      LogUtil.log("  " + day + ": " + apparentPos);
      drawSunDot(apparentPos, day);
      day = day.plusDays(1); 
    }
  }

  /** The local observers time of day, for which the Sun's position is calculated. Value - {@value}. */
  public static final int HOUR_OF_DAY = 18;

  private Projection projection;
  private Graphics2D g;
  private Config config;
  
  private void drawSunDot(Position pos, LocalDateTime localDate) {
    Point2D.Double where = projection.project(pos.δ, pos.α); 
    double r = sunSize(localDate); 
    Shape circle = new Ellipse2D.Double(where.x - r, where.y - r, r * 2, r * 2);
    g.draw(circle);
  }

  private double sunSize(LocalDateTime localDate) {
    double result = 0.5; //default
    if (localDate.getDayOfMonth() == 1) {
      result = 1.0;
    }
    else if (localDate.getDayOfMonth() % 5 == 0) {
      result = 0.75;
    }
    return result;
  }
}
