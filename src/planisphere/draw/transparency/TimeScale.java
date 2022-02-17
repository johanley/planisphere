package planisphere.draw.transparency;

import static planisphere.util.LogUtil.log;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.function.Consumer;

import planisphere.config.Config;
import planisphere.draw.ChartUtil;
import planisphere.draw.Projection;
import planisphere.math.Maths;

/** 
 Indicate 24h - 3m56s = 23h56m04s on the outer rim, increasing anticlockwise for the north, 
 and clockwise for the south.
 
 <P>The range is the length of the sidereal day, not the mean solar day (24h).
 In one sidereal day, the same star will return to the meridian. 
 One mean solar day is 86,400s. One sidereal day is 86,164s.
 The difference is 236s, or 3m56s. The ratio of the two is 1.002739.
 
 <P>The placement of 20h must be at the bottom. This will match the star chart's date scale, 
 which indicates the meridian at 20h standard time, in the configured time zone.
 
 <P>
 There is a small discontinuity placed near 08h (in the morning). 
 It corresponds to the fact that the sidereal day is not the same length as the solar day.
*/
public final class TimeScale {
  
  TimeScale(Projection projection, Graphics2D g, ChartUtil chartUtil, Config config){
    this.projection = projection;
    this.g = g;
    this.chartUtil = chartUtil;
    this.config = config;
  }

  void drawTimeScale() {
    log("Time scale.");
    //these two circles don't overlap with any others
    //there's a gap between the projection bounds and the inner circle.
    //that gap lets the date scale show through the transparency
    g.draw(circle(outerRadius()));
    g.draw(circle(innerRadius()));
    
    drawTimeScale(CLOCKWISE_FOR_NORTH);
    drawTimeScale(ANTI_CLOCKWISE_FOR_NORTH);
  }

  /** 
   Only go halfway around the circle, in the direction indicated by the 'sense' parameter.
   Start at the bottom (20h), and go to the top (8h). 
  */
  private void drawTimeScale(int sense) {
    //wrong; this was used when the sidereal time was local, not standard time: 
    //   double thetaOrigin = Math.PI/2 + config.radsWestOfCentralMeridian(); 
    double thetaOrigin = Math.PI/2; //RA=6h straight down (Standard Time=20h) 
    double thetaInterval = sense * Maths.degToRads(360.0/numDivisions()) * SOLAR_VERSUS_SIDEREAL;
    int beginPoint = sense > 0 ? 0 : 1; //avoid doing the start- and end-point twice
    int endPoint = sense > 0 ? (numDivisions()/2) : (numDivisions()/2) - 1;
    for (int div = beginPoint; div < endPoint ; ++div) {
      double theta = thetaOrigin + div * thetaInterval;
      int unitLength = 3;
      double r1 = innerRadius();
      double r2 = r1 + multiplier(div) * unitLength; //how big to make the tick mark
      Point2D.Double start = convertToXY(r1, theta);
      Point2D.Double end = convertToXY(r2, theta);
      tickMark(start, end);
      if (div % numDivisionsPerHour() == 0) {
        int hour = 20 - sense * (div / numDivisionsPerHour()) ;
        if (hour >= 24) {
          hour = hour - 24;
        }
        showHour(hour, theta);
      }
    }
  }
  
  private Projection projection;
  private Graphics2D g;
  private ChartUtil chartUtil;
  private Config config;
  private static double SOLAR_VERSUS_SIDEREAL = 86400.0/86164.0;
  
  private static final int CLOCKWISE_FOR_NORTH = 1;
  private static final int ANTI_CLOCKWISE_FOR_NORTH = -1;

  /** Number of divisions in the complete circle. */
  private int numDivisions() {
    return 24 * numDivisionsPerHour();
  }
  
  /** 60 or 30. */
  private int numDivisionsPerHour() {
    return 60 / config.smallestTimeDivision(); // int division!
  }
  
  private Point2D.Double convertToXY(double r, double theta){
    return new Point2D.Double(
      projection.centerOfProj().x + sign() * r * Math.cos(theta),
      projection.centerOfProj().y + r * Math.sin(theta)
    );    
  }

  /** Flips the x-coord. */
  private int sign() {
    return projection.getBounds().hemisphereSign();
  }
  
  private void tickMark(Point2D.Double start, Point2D.Double end) {
    GeneralPath path = new GeneralPath();
    path.moveTo(start.x, start.y);
    path.lineTo(end.x, end.y);
    Stroke orig = g.getStroke();
    g.setStroke(new BasicStroke(ChartUtil.STROKE_WIDTH_CONSTELLATION_LINE)); 
    g.draw(path);
    g.setStroke(orig);
  }

  private Shape circle(double radius) {
    double x = chartUtil.getWidth()/2.0 - radius;
    double y = chartUtil.getHeight()/2.0 - radius;
    double w = 2 * radius;
    double h = w; //always a circular arc
    return new Ellipse2D.Double(x, y, w, h);
  }

  private double outerRadius() {
    return chartUtil.getWidth()/2.0 - chartUtil.borderWidth(); 
  }
  
  private double innerRadius() {
    return outerRadius() - chartUtil.scaleWidth(); 
  }
  
  private double middleRadius() {
    return (outerRadius() + innerRadius()) / 2.0; 
  }
  
  private double multiplier(int division) {
    double result = 1.0;
    if (division % numDivisionsPerHour() == 0) {
      result = 4.0; //hours
    }
    else if (division % (numDivisionsPerHour() / 2) == 0) {
      result = 3.2; //half hour
    }
    else if (division % (numDivisionsPerHour() / 6) == 0) {
      result = 2.25; //10 mins
    }
    else {
      result = 1.5; //the smallest division  
    } 
    return result;
  }

  private void showHour(Integer div, double theta) {
    String text = div < 10 ? "0" + div.toString() : div.toString();
    //Point2D.Double target = convertToXY(middleRadius() * 1.01, theta); //also a good result
    Point2D.Double target = convertToXY(middleRadius() + chartUtil.percentWidth(0.45), theta);
    double rotationAngle = chartUtil.rotationAngle(projection, target) + Math.PI;
    Consumer<Graphics2D> drawer = x -> {
      Point2D.Double centered = chartUtil.centerTextOn(0, 0, text, g);
      x.drawString(text, Maths.round(centered.x), Maths.round(centered.y));
    };
    chartUtil.drawRotated(g, rotationAngle, target, drawer);
  }
}