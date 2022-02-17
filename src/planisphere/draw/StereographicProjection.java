package planisphere.draw;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import planisphere.config.Config;
import planisphere.math.Maths;

/** 
 <a href='https://en.wikipedia.org/wiki/Stereographic_map_projection'>Stereographic projection</a>.
 This projection projects circles as circles. This makes them easy to draw.
 There is some distortion of course: areas away from the celestial pole are larger than areas near 
 the celestial pole.
*/
public final class StereographicProjection implements Projection {

  /**
   * Constructor. 
   * @param chartWidth the width of the paper
   * @param chartHeight the height of the paper
   */
  public StereographicProjection(Config config) {
     this.bounds = config.starChartBounds();
     this.centerOfProj = new Point2D.Double(config.width()/2, config.height()/2);
     this.chartUtil = new ChartUtil(config.width(), config.height());
     this.config = config;
   }
   
   @Override public Point2D.Double project(Double dec, Double ra) {
     Point2D.Double result = new Point2D.Double();
     
     //see: https://en.wikipedia.org/wiki/Stereographic_map_projection
     double θ = ra; 
     double r = scale() * radialProjectionFor(dec);
     
     //convert to x y; x to the right, y going down (not up, as is the default in a PDF) 
     double dx = r * sign() * Math.cos(θ); // pixels from the center of proj
     double dy = r * Math.sin(θ); // pixels "
     result.x = centerOfProj.x + dx;
     result.y = centerOfProj.y + dy;    
     return result;
   }
   
   /** Circle centered on the celestial pole. */
   @Override public Shape innerBoundary() {
     return circleInsideTheDateScale();
   }
   
   /** The declination is bounded by a one-sided limit. */
   @Override public Bounds getBounds() {
     return bounds;
   }
   
   /** Doesn't apply to this projection. Returns 1. */
   @Override public Double distancePerRad() {
    return 1.0;
   }

   /** The center of the paper/chart. */
   @Override public Point2D.Double centerOfProj() {
     return centerOfProj;
   }

   // PRIVATE
   
   private Point2D.Double centerOfProj;
   private Bounds bounds;
   private ChartUtil chartUtil;
   private static double QUARTER_PI = Math.PI/4.0;
   private Config config;

   /** Circle centered on the celestial pole, out to the inside of the date scale. */
   private Shape circleInsideTheDateScale() {
     double radius = radius();
     double x = centerOfProj.getX() - radius;
     double y = centerOfProj.getY() - radius;
     double w = 2 * radius;
     double h = w; //always a circular arc
     return new Ellipse2D.Double(x, y, w, h);
   }

   private double radius() {
     return config.width()/2.0 - chartUtil.borderWidth() - 2 * chartUtil.scaleWidth(); 
   }
   
   /**
    Size of the full declination range in pixels, from the pole to the limit at the edge.
    The radius() must map to the declination range. That defines the scale of the projection. 
   */
   private double scale() {
     double decLimit = Maths.degToRads(bounds.declinationLimit());
     double result = radius() / radialProjectionFor(decLimit);
     return result;
     /*
     double minDeclination = Maths.degToRads(bounds.minDecDeg);
     double result = radius() / Math.tan(QUARTER_PI - minDeclination/2);
     return result;
     */
   }
   
   /** 
    For the southern hemisphere, we do two things. 
    <ul>
     <li>flip the sign of the declination, and pretend its the north
     <li>do a parity flip in x
    </ul>
   */
   private int sign() {
     return bounds.hemisphereSign();
   }

   private double radialProjectionFor(double dec) {
     return Math.tan(QUARTER_PI - sign() * dec/2);    
   }
}