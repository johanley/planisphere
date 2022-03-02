package planisphere.draw.transparency;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import planisphere.config.Config;
import planisphere.config.Constants;
import planisphere.draw.ChartUtil;
import planisphere.draw.Projection;
import planisphere.math.Maths;

/** Circles to indicate an altitude above or below the horizon. */
final class AltitudeCircle {
  
  AltitudeCircle(double latitude, Projection projection, Graphics2D g, ChartUtil chartUtil, Config config){
    this.latitude = latitude;
    this.projection = projection;
    this.g = g;
    this.chartUtil = chartUtil;
    this.config = config;
  }
  
  void draw(double alt) {
    Details details = details(alt);
    chartUtil.clippingOn(projection, g);
    Color orig = g.getColor();
    if (Math.abs(alt) > 0) {
      //the horizon is always black
      g.setColor(config.greyAltAzLines());
    }
    g.draw(details.circle);
    chartUtil.clippingOff(g);
    g.setColor(orig);
    /*
    Not needed, when the one-degree scale is present anyways.
    if (Math.abs(alt) < 11 && Math.abs(alt) > 1.0) {
      showText(alt, details.upperDec, details.upperRa);
    }
    */
  }
  
  /** Pass degrees. */
  Details details(double alt) {
    double altitude = Maths.degToRads(alt);
    double upperDec = upperDec(altitude);
    double lowerDec = lowerDec(altitude);
    boolean crossesThePole = Math.abs(upperDec) > Math.PI/2;
    if (crossesThePole) {
      double POLE = sign() * Math.PI/2;
      double remainder = upperDec % POLE;
      upperDec = POLE - remainder;
    }
    //log("alt:" + Maths.radsToDegs(altitude) + " upperD:" + Maths.radsToDegs(upperDec) + " lowerD:" + Maths.radsToDegs(lowerDec));
    double upperRa = crossesThePole ? Constants.LOWER_RA : Constants.UPPER_RA; //crosses the pole for high altitudes
    Point2D.Double upper = projection.project(upperDec, Maths.hoursToRads(upperRa));
    Point2D.Double lower = projection.project(lowerDec, Maths.hoursToRads(Constants.LOWER_RA)); //never crosses the pole
    
    double radius = (lower.y - upper.y) / 2.0;
    Point2D.Double ctr = new Point2D.Double(lower.x, lower.y - radius);
    double w = 2 * radius;
    double h = w; //always a circular arc
    Shape circle = new Ellipse2D.Double(ctr.x - radius, ctr.y - radius, w, h);
    return new Details(circle, upperDec, upperRa);
  }

  /** This exists because there are two use cases for the Shape. */
  static final class Details {
    Details(Shape circle, double upperDec, double upperRa){
      this.circle = circle;
      this.upperDec = upperDec;
      this.upperRa = upperRa;
    }
    int alt;
    double upperDec;
    double upperRa;
    Shape circle;
  }

  // PRIVATE 

  private double latitude;
  private Projection projection;
  private Graphics2D g;
  private ChartUtil chartUtil;
  private Config config;
  
  private int sign() {
    return projection.getBounds().hemisphereSign();
  }

  /**
   WARNING: this place "crosses the celestial pole", and needs special care, 
   because it's right ascension changes discontinuously when that happens.
   You can detect the crossing of the pole when the return value is greater than pi/2. 
  */
  private double upperDec(double altitude) {
    return sign() * (altitude + colatitude());
  }
  
  /** Never crosses the celestial pole. */
  private double lowerDec(double altitude) {
    return sign() * (altitude - colatitude());
  }

  /** 
   This doesn't match the definition of co-latitude in the southern hemisphere.
   But that's OK in this context, since this angle is the one that's needed. 
  */
  private double colatitude() {
    return Math.PI/2 - sign() * latitude;
  }
  
  private void showText(double alt, double upperDec, double upperRa) {
    String text = Math.round(alt) + "°";
    Double decA = upperDec;
    Double decB = upperDec - sign() * Maths.degToRads(4.0);
    Double decMidway = (decA + decB)/2.0;
    Point2D.Double northish = projection.project(decMidway, Maths.hoursToRads(upperRa) + sign() * Maths.degToRads(6));
    Point2D.Double centered = chartUtil.centerTextOn(northish.x, northish.y, text, g);
    g.drawString(text, Maths.round(centered.x), Maths.round(centered.y));
  }
}