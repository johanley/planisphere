package planisphere.draw.starchart;

import static java.util.Comparator.comparing;
import static planisphere.math.Maths.inRange;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import planisphere.astro.star.Star;

/** 
 Render the stars attached to a chart.
 By the time this class is used, the positions of the stars have already been found (but not yet rendered). 
*/
public class StarDots {
  
  StarDots(List<Star> stars, Map<Integer, Point2D.Double> starPoints, Graphics2D g) {
    this.stars = stars;
    this.starPoints = starPoints;
    this.g = g;
  }
  
  /** 
   The star positions have already been found.
   Sort the stars first by magnitude, so that smaller star-dots will overwrite larger star-dots in the background.
   Black circle, with a small white border around it.
   The small white border looks good when 2 stars are near each other.
   It also looks better for the constellation lines.
  */
  void draw() {
     Collections.sort(stars, comparing(Star::getMagnitude));
     for (Star star : stars) {
       drawStarDot(star);
     }
  }

  //PRIVATE 
  
  private List<Star> stars;
  private Map<Integer, Point2D.Double> starPoints;
  private Graphics2D g;

  private void drawStarDot(Star star) {
    Point2D.Double where = starPoints.get(star.INDEX); 
    double r = starSize(star); 
    Shape circle = new Ellipse2D.Double(where.x - r, where.y - r, r * 2, r * 2);
    g.draw(circle);
    g.fill(circle);
  }

  /** In this implementation, there are only two sizes of dots for stars: big for bright stars, and small for all the rest. */
  private double starSize(Star star) {
    double BRIGHT_STAR = 1.0;
    double DIM_STAR = 0.5;
    double mag = star.MAG;
    boolean isBright = inRange(-5,1.6, mag);
    return isBright ? BRIGHT_STAR : DIM_STAR;
  }
}
