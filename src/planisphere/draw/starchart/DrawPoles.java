package planisphere.draw.starchart;

import static planisphere.util.LogUtil.log;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import planisphere.astro.constellation.ConstellationLines;
import planisphere.astro.precession.LongTermPrecession;
import planisphere.astro.star.Position;
import planisphere.astro.star.Star;
import planisphere.astro.time.GregorianCal;
import planisphere.config.Config;
import planisphere.draw.ChartUtil;
import planisphere.draw.Projection;

/** 
 Draw the movement of the poles of the equator and ecliptic, over millenia.
 Not part of the planisphere. Done simply as an interesting exercise, in order to see the result. 
*/
public final class DrawPoles {

  public DrawPoles(
    List<Star> stars, ConstellationLines constellationLines, 
    Projection projection, Graphics2D g, Config config
  ) {
    this.width = config.width();
    this.height = config.height();
    this.stars = stars;
    this.lines = constellationLines.all();
     
    this.g = g;
    this.projection = projection;
    this.chartUtil = new ChartUtil(width, height);
    this.config = config;
  }
  
  /** 
   Draw the poles against the J2000 constellation lines and stars.
   No precession or proper motion is applied. 
  */
  public void draw() {
    drawProjectionBoundary();
    findStarPositions(stars, projection);
    chartUtil.clippingOn(projection, g);
    drawConstellationLines();
    drawStarDots();
    movementOfPoles();
    chartUtil.clippingOff(g);
  }
  
  //PRIVATE
  
  /** Various utility methods for drawing, and data. */
  private ChartUtil chartUtil;
  
  /** What projection is used to draw the chart. */
  private Projection projection;
  
  /** 
   The graphics context.
   IMPORTANT: pdf files and libraries have a built-in graphics context. 
   You can draw directly into the pdf. 
  */
  private Graphics2D g;

  /** Filtered using settings. */
  private List<Star> stars;
  
  /** All constellation lines, for the whole sky. */
  private Map<String, List<List<Integer>>> lines;
  
  /** 
   Remember where each star is drawn.
   This exists in order to remember how to draw the lines joining the stars.
   The key is the (slightly modified) YBS index.  
  */
  private Map<Integer, Point2D.Double> starPoints = new LinkedHashMap<>();
  
  private double width;
  private double height;
  private Config config;

  private void drawProjectionBoundary() {
    log("Projection boundary.");
    Shape boundary = projection.innerBoundary();
    g.draw(boundary);
  }
  
  private void findStarPositions(List<Star> stars, Projection projection) {
    for (Star star : stars) {
      Point2D.Double where = projection.project(star.DEC, star.RA);
      starPoints.put(star.INDEX, where); 
    }
  }

  private void drawConstellationLines() {
    log("Constellation lines.");
    Color origColor = g.getColor();
    g.setColor(config.greyConstellationLines());
    Constellations constellations = new Constellations(lines, starPoints, g);
    constellations.draw();
    g.setColor(origColor);
  }
  
  private void drawStarDots() {
    log("Star dots.");
    StarDots starDots = new StarDots(stars, starPoints, g);
    starDots.draw();
  }
  
  /** Open circles. */
  private void movementOfPoles() {
    log("Ecliptic pole, back in time, over 52,000 years.");
    LongTermPrecession precession = new LongTermPrecession();
    int START_YEAR = 2000;
    for(int i = 0; i < 260; ++i) {
      double jd = GregorianCal.jd(START_YEAR - (i * 200), 1, 0.0); //back in time
      
      Position equatorialNorthPole = precession.equatorialNorthPoleRaDec(jd);
      draw(equatorialNorthPole);
      
      Position eclipticNorthPole = precession.eclipticNorthPoleRaDec(jd);
      draw(eclipticNorthPole);
    }
  }
  
  /** Draws the south pole if the latitude is negative. */
  private void draw(Position polePosition) {
    if (!config.isNorthernHemisphere()) {
      polePosition = polePosition.opposite(); //south pole!
    }
    
    Point2D.Double where = projection.project(polePosition.δ, polePosition.α);
    double r = 0.5;
    Shape circle = new Ellipse2D.Double(where.x - r, where.y - r, r * 2, r * 2);
    g.draw(circle);
  }
}