package planisphere.draw.transparency;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import planisphere.astro.star.Position;
import planisphere.config.Config;
import planisphere.config.Constants;
import planisphere.draw.ChartUtil;
import planisphere.draw.Projection;
import planisphere.math.Maths;

/** 
 Tick marks along the horizon, denoting azimuth values.
 The ticks run just above the horizon, to leave the space clear just below the horizon.
 This is to aid in using the sunrise-sunset line, which is less than a degree below the horizon. 
*/
final class AzimuthScale {
  
  AzimuthScale(Projection projection, Graphics2D g, ChartUtil chartUtil, Config config){
    this.projection = projection;
    this.g = g;
    this.chartUtil = chartUtil;
    this.config = config;
  }
  
  /** Along the visible parts of the horizon. */
  void tickMarks() {
    //just do the whole horizon, and let cropping remove what shouldn't be showing
    //at high latitudes, the whole horizon may be visible
    double STEP_SIZE= Maths.degToRads(1.0);
    double LOCAL_SIDEREAL_TIME = Maths.hoursToRads(Constants.LOWER_RA);
    double HORIZON = 0.0;
    double TWEAK = Maths.degToRads(1.0);
    for(int step = 0; step < 360; ++step) {
      double az = step * STEP_SIZE;
      Azimuth azimuth = new Azimuth(config.latitude(), LOCAL_SIDEREAL_TIME);
      
      Position position = azimuth.fromAltAz(HORIZON, az);
      Point2D.Double horizonPoint = projection.project(position.δ, position.α);
      
      position = azimuth.fromAltAz(HORIZON + multiplier(step)*TWEAK, az);
      //position = azimuth.fromAltAz(HORIZON + TWEAK, az);
      Point2D.Double tweakPoint = projection.project(position.δ, position.α);

      GeneralPath path = new GeneralPath();
      path.moveTo(horizonPoint.x, horizonPoint.y);
      path.lineTo(tweakPoint.x, tweakPoint.y);
      Stroke orig = g.getStroke();
      g.setStroke(new BasicStroke(ChartUtil.STROKE_WIDTH_CONSTELLATION_LINE));
      g.setClip(projection.innerBoundary());
      g.draw(path);
      chartUtil.clippingOff(g);
      g.setStroke(orig);
    }
  }
  
  private double multiplier(int step) {
    double result = 1.0; //default
    if (step % 45 == 0) {
      result = 4.0;
    }
    else if (step % 5 == 0) {
      result = 1.5;
    }
    return result;
  }
  
  private Projection projection;
  private Graphics2D g;
  private ChartUtil chartUtil;
  private Config config;
}
