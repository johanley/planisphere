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

/** Draw a basic star chart for demonstrating various items. Not part of the planisphere.*/
public final class DrawBasicStarChart {

  public DrawBasicStarChart(
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
  
  /** Draw the constellation lines and stars, the celestial pole, the equator, and ecliptic. */
  public void draw() {
    log("Creating chart h:"+ height + " w:"+width);
    drawProjectionBoundary();
    findStarPositions(stars, projection);
    chartUtil.clippingOn(projection, g);
    drawConstellationLines();
    drawStarDots();
    crossForCelestialPole(); 
    equatorAndEcliptic();
    
    additionalItems();
    
    chartUtil.clippingOff(g);
  }
  
  /** Empty default implementation, for drawing custom items on top of the basic star chart. */
  protected void additionalItems() {}
  
  //PRIVATE
  
  private Config config; 
  private ChartUtil chartUtil;
  private Projection projection;
  private Graphics2D g;
  private List<Star> stars;
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
  
  private void equatorAndEcliptic() {
    CelestialEquatorOrEcliptic eclipticAndEq = new CelestialEquatorOrEcliptic(projection, g, config);
    eclipticAndEq.drawEquator();
    eclipticAndEq.drawEcliptic();
  }
}