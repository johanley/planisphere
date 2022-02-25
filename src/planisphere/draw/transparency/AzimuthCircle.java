package planisphere.draw.transparency;

import static java.lang.Math.cos;
import static java.lang.Math.tan;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import planisphere.config.Config;
import planisphere.config.Constants;
import planisphere.draw.ChartUtil;
import planisphere.draw.Projection;
import planisphere.math.Maths;

final class AzimuthCircle {
 
  AzimuthCircle(Projection projection, Graphics2D g, ChartUtil chartUtil, Config config) {
    this.projection = projection;
    this.g = g;
    this.chartUtil = chartUtil;
    this.config = config;
  }
  
  /** Draw an arc from the east to the west, going through the zenith. */
  void eastWest() {
    EastWestCircle eastWestCircle = eastWestCircle();
    drawCircleHere(eastWestCircle.ctr, eastWestCircle.radius, ClipZenith.NO);
  }
  
  void otherAzimuths() {
    double START = Maths.degToRads(10.0);
    EastWestCircle ewc = eastWestCircle();
    double R0 = ewc.radius;
    for(int step = 0; step < 8; ++step) {
      double angle = START + step * STEP_SIZE; //not the az! ray zenith to top, rotate counterclockwise.
      double radius = R0 / cos(angle);
      int sign = angle < Maths.HALF_PI ? -1 : 1;
      double Δx =  sign * R0 * tan(angle); //change from the center of the east-west circle
      Point2D.Double ctr = new Point2D.Double(ewc.ctr.x + Δx, ewc.ctr.y);
      drawCircleHere(ctr, radius, ClipZenith.YES);
      //do it again, but with a parity flip in x
      ctr = new Point2D.Double(ewc.ctr.x - Δx, ewc.ctr.y);
      drawCircleHere(ctr, radius, ClipZenith.YES);
    }
  }
  
  private Projection projection;
  private Graphics2D g;
  private ChartUtil chartUtil;
  private Config config;
  private static double STEP_SIZE = Maths.degToRads(10.0);
  private enum ClipZenith { YES, NO; }
  
  /** The east-west azimuth circle is used as the basis for all the other azimuth circles. */
  private static class EastWestCircle {
    public EastWestCircle(Point2D.Double ctr, double radius) {
      this.ctr = ctr;
      this.radius = radius;
    }
    Point2D.Double ctr;
    double radius;
  }
  
  private EastWestCircle eastWestCircle() {
    Point2D.Double a = projection.project(config.latitude(), Maths.hoursToRads(Constants.LOWER_RA));
    Point2D.Double b = projection.project(-config.latitude(), Maths.hoursToRads(Constants.UPPER_RA));
    Point2D.Double ctr = new Point2D.Double(a.x, (a.y + b.y)/2.0);
    double radius = (a.y - b.y) / 2.0;
    return new EastWestCircle(ctr, radius);
  }
  
  private Shape horizonCircle() {
    AltitudeCircle alts = new AltitudeCircle(config.latitude(), projection, g, chartUtil, config);
    AltitudeCircle.Details deets = alts.details(0);
    return deets.circle;
  }
  
  private void drawCircleHere(Point2D.Double ctr, double radius, ClipZenith clipZenith) {
    double w = 2 * radius;
    double h = w; //always a circular arc
    Shape circle = new Ellipse2D.Double(ctr.x - radius, ctr.y - radius, w, h);

    //multiple clipping regions
    Area cropArea = new Area(projection.innerBoundary());
    AltitudeCircle altCircle = new AltitudeCircle(config.latitude(), projection, g, chartUtil, config);
    //don't draw all the way to the zenith; too crowded, not very useful
    if (ClipZenith.YES == clipZenith) {
      cropArea.subtract(new Area(altCircle.details(80).circle));
    }
    g.setClip(cropArea);
    g.clip(horizonCircle()); //add a second clipping region
    g.draw(circle);
    chartUtil.clippingOff(g);
  }
}