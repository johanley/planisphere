package planisphere.draw.transparency;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import planisphere.config.Config;
import planisphere.config.Constants;
import planisphere.draw.ChartUtil;
import planisphere.draw.Projection;
import planisphere.math.Maths;

/** Draw the time scale and the altitude/azimuth indicators. */
public final class DrawTransparency {
  
  public DrawTransparency(Projection projection, Graphics2D g, Config config) {
    this.g = g;
    this.projection = projection;
    this.config = config;
    this.chartUtil = new ChartUtil(config.width(), config.height());
  }
  
  /** Draw the altitude and azimuth circles, and a 24h time scale. */
  public void draw() {
    drawProjectionBoundary();
    drawTimeScale();
    //centeringAffordance();
    altitudes();
    meridian();
    altitudeScaleOnMeridian();
    eastWestAzimuthsAndOthers();
    azimuthTickMarks();
    textLatitudeAndTimeCorr();
  }
  
  //PRIVATE
  
  /** Various utility methods for drawing, and data. */
  private ChartUtil chartUtil;
  
  /** What projection is used to draw the chart. */
  private Projection projection;
  
  /** The graphics context. */
  private Graphics2D g;
  
  private Config config;

  private void drawProjectionBoundary() {
    Shape boundary = projection.innerBoundary();
    g.draw(boundary);
  }
  
  private int sign() {
    return projection.getBounds().hemisphereSign();
  }
  
  /** The date for which the corresponding Right Ascension is due south at 8pm Local Mean Time. */
  private void drawTimeScale() {
    TimeScale timeScale = new TimeScale(projection, g, chartUtil, config);
    timeScale.drawTimeScale();
  }
  
  private void altitudes() {
    AltitudeCircle alts = new AltitudeCircle(config.latitude(), projection, g, chartUtil, config);
    int[] TWILIGHTS = {-18, -12, -6};
    for (int i = 0; i < TWILIGHTS.length; ++i){
      alts.draw(TWILIGHTS[i]);
    }
    for (int alt = 0; alt < 81; alt = alt + 10){
      alts.draw(alt);
    }
    double SOLAR_ALT_AT_RISE_SET = -0.9; //-0.8333 used by USNO, Ephemeris (34 arcmin refraction  + 16 arcmin semi-diameter)
    alts.draw(SOLAR_ALT_AT_RISE_SET);
  }
  
  private void altitudeScaleOnMeridian() {
    AltitudeBarScale abs = new AltitudeBarScale(projection, g, chartUtil, config);
    abs.draw();
  }
  
  private void meridian() {
    GeneralPath path = new GeneralPath();
    Point2D.Double midline = new Point2D.Double(chartUtil.getWidth()/2.0, 0);
    path.moveTo(midline.x, midline.y);
    path.lineTo(midline.x, midline.y + chartUtil.getHeight());
    chartUtil.clippingOn(projection, g);
    g.draw(path);
    chartUtil.clippingOff(g);
  }
  
  /** 
   Draw a circular arc from the east to the west, going through the zenith.
   Draw arcs as well for other azimuths.
   */
  private void eastWestAzimuthsAndOthers() {
    AzimuthCircle azCircles = new AzimuthCircle(projection, g, chartUtil, config);
    azCircles.eastWest();
    azCircles.otherAzimuths();
  }
  
  private void textLatitudeAndTimeCorr() {
    String latText = "φ " + degrees(config.latitude());
    String longText = "λ " + degrees(config.longitude());
    
    String plus = config.hoursOffsetFromUT() > 0 ? "+" : "";
    String timeCorrText = "UT"+ plus + config.hoursOffsetFromUT() + "h" + config.minutesOffsetFromUT() + "m";
    
    //position the text near the top of the meridian
    Double dec = Maths.degToRads(config.declinationLimit() + sign() * 4.0);
    Double ra = Maths.hoursToRads(Constants.UPPER_RA);
    Double raTweak = sign() * Maths.degToRads(12);
    Double yTweak = 10.0;
    
    Point2D.Double latPoint = projection.project(dec, ra - raTweak);
    g.drawString(latText, Maths.round(latPoint.x), Maths.round(latPoint.y));
    g.drawString(longText, Maths.round(latPoint.x), Maths.round(latPoint.y + yTweak));
    g.drawString(timeCorrText, Maths.round(latPoint.x), Maths.round(latPoint.y + 2*yTweak));
    
    g.drawString(config.location(), Maths.round(latPoint.x + chartUtil.percentWidth(12)), Maths.round(latPoint.y));
    g.drawString(config.year().toString(), Maths.round(latPoint.x + chartUtil.percentWidth(12)), Maths.round(latPoint.y + yTweak));
  }
  
  private String degrees(Double rads) {
    Double angle = Maths.radsToDegs(rads);
    angle = Maths.round(angle*100) / 100.0;
    return angle.toString() + "°";
  }
  
  private void azimuthTickMarks() {
    AzimuthScale azimuthScale = new AzimuthScale(projection, g, chartUtil, config);
    azimuthScale.tickMarks();
  }

}