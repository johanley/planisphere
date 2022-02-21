package planisphere.draw.starchart;

import static planisphere.util.LogUtil.log;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import planisphere.astro.constellation.ConstellationLines;
import planisphere.astro.star.Star;
import planisphere.config.Config;
import planisphere.draw.ChartUtil;
import planisphere.draw.Projection;

/**
 Draw the constellations and the date scale.
 This class has knowledge of the drawing context and the core data, but nothing else. 
*/
public final class DrawStarChart {
  
  public DrawStarChart(
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
  
  /** Draw the constellation lines and stars. */
  public void draw() {
    log("Creating chart h:"+ height + " w:"+width);

    drawProjectionBoundary();
    drawDateScale();
    
    findStarPositions(stars, projection);
    chartUtil.clippingOn(projection, g);
    drawConstellationLines();
    drawStarDots();
    crossForCelestialPole(); 
    equator();
    sun();
    moon();
    meteorShowerRadiants();
    chartUtil.clippingOff(g);
    drawGuidelinesForScissors();
  }
  
  //PRIVATE
  
  private Config config; 
  
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

  private void drawProjectionBoundary() {
    log("Projection boundary.");
    Shape boundary = projection.innerBoundary();
    g.draw(boundary);
  }
  
  /**  The date for which the corresponding Right Ascension is due south at 8pm Local Mean Time. */
  private void drawDateScale() {
    DateScale dateScale = new DateScale(projection, g, chartUtil, config);
    dateScale.drawDateScale();
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
  
  private void findStarPositions(List<Star> stars, Projection projection) {
    for (Star star : stars) {
      Point2D.Double where = projection.project(star.DEC, star.RA);
      starPoints.put(star.INDEX, where); 
    }
  }
  
  /** Even with Polaris nearby, it's more precise to show the pole explicitly. */
  private void crossForCelestialPole() {
    GeneralPath path = new GeneralPath();
    double size = 3.0;
    Point2D.Double pole = new Point2D.Double(chartUtil.getWidth()/2.0, chartUtil.getHeight()/2.0);
    //horizontal tick
    path.moveTo(pole.x - size, pole.y);
    path.lineTo(pole.x + size, pole.y);
    //vertical tick
    path.moveTo(pole.x, pole.y + size);
    path.lineTo(pole.x, pole.y - size);
    g.draw(path);
  }
  
  private void equator() {
    CelestialEquatorOrEcliptic eclipticAndEq = new CelestialEquatorOrEcliptic(projection, g, config);
    eclipticAndEq.drawEquator();
  }
  
  private void sun() {
    SunMarks sunDots = new SunMarks(projection, g, config);
    sunDots.draw();
  }
  
  private void moon() {
    MoonsPath moonsPath = new MoonsPath(projection, g, config);
    moonsPath.draw();
  }
  
  private void meteorShowerRadiants() {
    ShowerRadiant showerRadiant = new ShowerRadiant(projection, g, config);
    showerRadiant.draw();
  }

  private void drawGuidelinesForScissors() {
    if (config.width() < config.height()) {
      // make it square
      double diff = config.height() - config.width();
      double chopOffAtEachEnd = diff * 0.5;
      
      double bottomLineY = config.height() - chopOffAtEachEnd;
      drawHorizontalLineAt(bottomLineY);
      
      double topLineY = chopOffAtEachEnd;
      drawHorizontalLineAt(topLineY);
    }
  }
  
  private void drawHorizontalLineAt(double y) {
    GeneralPath path = new GeneralPath();
    path.moveTo(0, y);
    path.lineTo(config.width(), y);
    g.draw(path);
  }
}