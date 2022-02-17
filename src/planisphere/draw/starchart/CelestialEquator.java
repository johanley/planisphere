package planisphere.draw.starchart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import planisphere.config.Config;
import planisphere.draw.ChartUtil;
import planisphere.draw.Projection;
import planisphere.math.Maths;

/** Show the celestial equator as a circle centered on the pole. */
final class CelestialEquator {
  
  CelestialEquator(Projection projection, Graphics2D g, Config config){
    this.projection = projection;
    this.g = g;
    this.config = config;
  }
  
  void draw() {
    circularPath(ZERO_DECLINATION, LITTLE_DASH, false);
  }
  
  private Projection projection;
  private Graphics2D g;
  private Config config;
  
  private static final double ZERO_DECLINATION = 0.0;
  public static float LITTLE_DASH = 1.0f;
  
  private void circularPath(double dec, float dash, boolean offset) {
    Point2D.Double a = projection.project(dec, Maths.degToRads(90));
    double bDec = offset ? -dec : dec;
    Point2D.Double b = projection.project(bDec, Maths.degToRads(270));
    double radius = (a.y - b.y) / 2.0;
    Point2D.Double ctr = new Point2D.Double(a.x, b.y + radius);
    double w = 2 * radius;
    double h = w; //always a circular arc
    Shape circle = new Ellipse2D.Double(ctr.x - radius, ctr.y - radius, w, h);
    
    Color origColor = g.getColor();
    g.setColor(config.greyConstellationLines());
    Stroke orig = g.getStroke();
    float dash1[] = {dash};
    BasicStroke dashed = new BasicStroke(ChartUtil.STROKE_WIDTH_CONSTELLATION_LINE, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, dash1, 0.0f);
    g.setStroke(dashed); 
    g.draw(circle);
    g.setStroke(orig);
    g.setColor(origColor);
  }
}
