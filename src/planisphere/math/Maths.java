package planisphere.math;

import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.math.RoundingMode;

import planisphere.util.LogUtil;

/** Various utility methods of general use. */
public class Maths {
  
  public static double degToRads(double deg) {
    return deg * DEG_TO_RADS;   
  }
  
  public static double radsToDegs(double rad) {
    return rad * RADS_TO_DEG;   
  }
  
  public static double hoursToRads(double hours) {
    return degToRads(hours * HOURS_TO_DEGS);
  }
  
  public static double radsToHours(double rads) {
    return radsToDegs(rads) / HOURS_TO_DEGS;
  }
  
  public static double rightAscensionToRads(int hours, int minutes, double seconds) {
    double hoursDecimal = hours + (minutes/60.0) + (seconds/3600.0); // avoid int division
    return hoursToRads(hoursDecimal);
  }
  
  /** Inclusive. */
  public static boolean inRange(double min, double max, double val) {
    return min <= val && val <= max;
  }
  
  /** Inclusive. Ra is tricky because its cyclical. Things are different if the range straddles 0h. */
  public static boolean inRangeRa(Double min, Double max, Double val) {
    boolean result = false;
    if (min < max) {
      result = (val >= min && val <= max);
    }
    else {
      //overlaps 0h
      result = (val >= min || val <= max);
    }
    return result;
  }
  
  public static String roundMag(Double val) {
    BigDecimal num = new BigDecimal(val.toString());
    BigDecimal rounded = num.setScale(1, RoundingMode.HALF_EVEN);
    return rounded.toString();
  }
  
  public static Integer inchesToPixels(double inches) {
    return (int)(DOTS_PER_PIXEL * inches);
  }
  
  public static Double midpoint(Double a, Double b) {
    return (a + b)/2.0;
  }
  
  /** Format hh mm.m. No leading zeros for padding. */
  public static Double parseRa(String ra) {
    String[] parts = ra.trim().split(" ");
    double h = Double.parseDouble(parts[0]);
    double m = Double.parseDouble(parts[1]);
    h = h + m/60.0;
    return hoursToRads(h);
  }
  
  /** Format d m. Leading sign. For m, possible leading zeros for padding. */
  public static Double parseDec(String dec) {
    String[] parts = dec.trim().split(" ");
    double d = Double.parseDouble(parts[0]); //leading sign is ok
    double m = Double.parseDouble(parts[1]); //leading 0 is ok
    int sign = d < 0 ? -1 : 1;
    m = sign * m; //ensure the minutes has the same sign as the degrees
    d = d + m/60.0;
    return degToRads(d);
  }
  
  public static double radsToArcsecs(Double val) {
    double result = radsToDegs(val);
    result = result*3600;
    result = Math.round(result*100)/100.0;
    return result;
  }
  
  public static double arcsecToRads(Double val) {
    double degs = val / 3600.0;
    return degToRads(degs);
  }
  
  /**
   Round the given value. 
    
   <P>Many graphics operations take only an int.
   To preserve as much accuracy as possible when converting from double to int, 
   you need to call this method, instead of doing a cast.
   Casting simply abandons the decimal part. 
   I saw this cause a problem: in drawing a large circle, it was actually slightly oval, I believe. 
  */
  public static int round(double val) {
    long result = Math.round(val);
    return (int)result;
  }

  public static float asFloat(double val) {
    return (float)(val);
  }

  /** Similar to Math.atan2(y, x), but ensures the result is in 0..2pi. */
  public static double atan3(double y, double x) {
    double result = Math.atan2(y, x); //-pi .. +pi (range is 2pi, but the sign is usually undesirable)
    if (result < 0) {
      result = result + TWO_PI;
    }
    return result;
  }

  /** Ensure that the given value is placed the range 0..2pi. */
  public static double in2pi(double rads){
    double result = rads % TWO_PI;
    if (result < 0){
      //some rads are in -pi..+pi; this will manage them
      result = result + TWO_PI;
    }
    return result;
  };
  
  /** Ensure that the given value is placed the range 0..360. */
  public static double in360(double rads){
    double result = rads % 360.0;
    if (result < 0){
      //some angles are in -180..+180; this will manage them
      result = result + 360.0;
    }
    return result;
  };
  
  /** Chop off the non-integral part of a number. For negative numbers, not the same as floor.*/
  public static double truncate(double value) {
    double result = Math.floor(value);
    if (value < 0) {
      result = result + 1;
    }
    return result;
  }
  
  /** 
   Return the center of the circle that has the three given points on its circumference.
   Using the center you can draw the circle itself. The radius is simply the distance to any of 
   the points on its circumference.
   In a stereographic projection, all circles on the sphere are circles in the projected plane.
    
   Ref: http://www.ambrsoft.com/TrigoCalc/Circle3D.htm
  */
  public static Point2D.Double centerFor(Point2D.Double one, Point2D.Double two, Point2D.Double three){
    double A = 
        one.x * (two.y - three.y) 
      - one.y * (two.x - three.x) 
      + two.x * three.y 
      - three.x * two.y
    ;
    double B = 
       (sqr(one.x) + sqr(one.y)) * (three.y - two.y)  
     + (sqr(two.x) + sqr(two.y)) * (one.y - three.y) 
     + (sqr(three.x) + sqr(three.y)) * (two.y - one.y)
   ;
    double C = 
       (sqr(one.x) + sqr(one.y)) * (two.x - three.x)  
     + (sqr(two.x) + sqr(two.y)) * (three.x - one.x) 
     + (sqr(three.x) + sqr(three.y)) * (one.x - two.x)
    ;
    return new Point2D.Double(
     -B/(2*A), 
     -C/(2*A)
    );
  }
  
  public static double distance(Point2D.Double a, Point2D.Double b) {
    double Δx = b.x - a.x;
    double Δy = b.y - a.y;
    return Math.sqrt(sqr(Δx) + sqr(Δy));
  }
  
  public static double sqr(double val) {
    return val * val;
  }
  
  public static final double TWO_PI = 2 * Math.PI;
  public static final double HALF_PI = Math.PI / 2.0;
  
  //PRIVATE 
  
  private static final Double DEG_TO_RADS = TWO_PI/360.0;
  private static final Double RADS_TO_DEG = 360.0/(2*Math.PI);
  private static final Double HOURS_TO_DEGS = 15.0;
  private static final Integer DOTS_PER_PIXEL = 72; //iText's default
  
  public static void main(String... args) {
    Point2D.Double one = new Point2D.Double(-3.0, 4.0);
    Point2D.Double two = new Point2D.Double(4.0, 5.0);
    Point2D.Double three = new Point2D.Double(1.0, -4.0);
    Point2D.Double ctr = centerFor(one, two, three);
    LogUtil.log("x:" + ctr.x + "y:"+ctr.y);
    LogUtil.log("radius: " + Maths.distance(one, ctr));
  }
  
}