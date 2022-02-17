package planisphere.draw.transparency;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import planisphere.config.Config;
import planisphere.draw.ChartUtil;
import planisphere.draw.Projection;
import planisphere.math.Maths;

/** The vertical scale that measures the altitude of objects on the meridian. */
final class AltitudeBarScale {
  
  AltitudeBarScale(Projection projection, Graphics2D g, ChartUtil chartUtil, Config config){
    this.projection = projection;
    this.g = g;
    this.chartUtil = chartUtil;
    this.config = config;
  }
  
  void draw() {
    AltitudeBar nominal = new AltitudeBar(0.0, projection, config);
    double altBar = Maths.degToRads(180.0 + 18.0);
    //start from -18 at the top, and go down: the first chartable is variable, while -18 is not
    double STEP_SIZE = Maths.degToRads(ONE_DEGREE);
    double endPoint = nominal.firstChartableAltBarValue();
    while (altBar >= endPoint) {
      AltitudeBar aBar = new AltitudeBar(altBar, projection, config);
      Point2D.Double point = projection.project(aBar.declination(), aBar.rightAscension());
      tickMark(point, aBar, multiplier(altBar));
      altBar = altBar - STEP_SIZE;
    }
  }
  
  private Projection projection;
  private Graphics2D g;
  private ChartUtil chartUtil;
  private Double ONE_DEGREE = 1.0;
  private Config config;
  
  /** 
   Small horizontal mark on the meridian.
   Place the mark such that with increasing time, the mark is on the opposite side of the meridian 
   from the approaching object that is about to transit.

   <P> (The following remarks are for placing 6h 'at the bottom'.)
   
   <P>In the northern hemisphere, that means west/right (west/left) of the meridian for upper culmination
   above the pole, and east/right for lower culmination below the pole.
   
   <P>In the southern hemisphere, the placement is the opposite, because the sense of rotation of the 
   transparency is opposite.
  */ 
  private void tickMark(Point2D.Double start, AltitudeBar aBar, int multiplier) {
    double size = chartUtil.percentWidth(0.35);
    Point2D.Double end = new Point2D.Double(start.x + direction(aBar) * multiplier * size, start.y);
    GeneralPath path = new GeneralPath();
    path.moveTo(start.x, start.y);
    path.lineTo(end.x, end.y);
    Stroke orig = g.getStroke();
    g.setStroke(new BasicStroke(ChartUtil.STROKE_WIDTH_CONSTELLATION_LINE)); 
    g.draw(path);
    g.setStroke(orig);
  }

  /** 
   Indicate if the tick mark runs to the right (+1) or left (-1) of the meridian, as seen when
   the transparency is held 'upright in the normal position.' 
  */
  private int direction(AltitudeBar aBar) {
    int RIGHT = 1;
    int LEFT = -1;
    int result = RIGHT; //by default
    if (config.isNorthernHemisphere()) {
      if (aBar.isLowerCulmination()) {
        result = LEFT;
      }
    }
    else {
      if (aBar.isUpperCulmination()) {
        result = LEFT;
      }
    }
    return result;
  }
  
  private int multiplier(double altBar) {
    double degrees = Maths.radsToDegs(altBar);
    int result = 1;
    long rounded = Math.round(degrees);
    if ((rounded % 5) == 0) {
      result = 2;
    }
    return result;
  }
}
