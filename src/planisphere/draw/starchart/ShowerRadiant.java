package planisphere.draw.starchart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import planisphere.astro.precession.LongTermPrecession;
import planisphere.astro.star.Position;
import planisphere.astro.time.GregorianCal;
import planisphere.config.Config;
import planisphere.draw.ChartUtil;
import planisphere.draw.Projection;
import planisphere.math.Maths;

/** 
 Indicate the position of a meteor shower radiant, at its peak.
 The time of the peak corresponds to a specific solar longitude.
 
 <P>The position of the radiant changes from day to day, 
 but the position of the radiant at the peak of the shower is treated here as constant.
 
 <P>One can likely consider this data valid at least for a few centuries.
 The J2000 position of the radiant must be precessed.
 
 <P>Ref: https://www.imo.net/files/data/vmdb/vmdbrad.txt 
*/
public final class ShowerRadiant {
  
  public ShowerRadiant(Projection projection, Graphics2D g, Config config) {
    this.projection = projection;
    this.g = g;
    this.config = config;
  }
  
  /** 
   @param peakRadiant the J2000 coordinates of the radiant, at the moment of its peak.
  */
  public void draw() {
    double jd = GregorianCal.jd(config.year(), 7, 1.0);
    LongTermPrecession precession = new LongTermPrecession();
    for (Position radiant : radiants()) {
      Position position = precession.apply(radiant, jd);
      showMark(position);
    }
  }
 
  private Projection projection;
  private Graphics2D g;
  private Config config;

  /* radiants = perseids:46.2,57.4 | eta-aquarids:338.0,-1.0 | quadrantids:230.1,48.5 | geminids:112.3,32.5 */
  private List<Position> radiants() {
    List<Position> result = new ArrayList<>();
    String text = config.meteorShowerRadiants();
    String[] parts = text.split(Pattern.quote("|"));
    for (String shower : parts) {
      int colon = shower.indexOf(":");
      String numbers = shower.substring(colon + 1);
      String[] details = numbers.split(Pattern.quote(","));
      double ra = Double.valueOf(details[0]); //degrees
      double dec = Double.valueOf(details[1]); //degrees
      result.add(new Position(Maths.degToRads(ra), Maths.degToRads(dec)));
    }
    return result;
  }

  /** Small 'star' centered on the position. */
  private void showMark(Position position) {
    double TWEAK = 1.0;
    Point2D.Double ctr = projection.project(position.δ, position.α);
    Point2D.Double a = new Point2D.Double(ctr.x + TWEAK, ctr.y);
    Point2D.Double b = new Point2D.Double(ctr.x - TWEAK, ctr.y);
    tickMark(a, b);
    a = new Point2D.Double(ctr.x , ctr.y + TWEAK);
    b = new Point2D.Double(ctr.x, ctr.y  - TWEAK);
    tickMark(a, b);
    double TWEAK_45 = TWEAK / Math.sqrt(2.0);
    a = new Point2D.Double(ctr.x + TWEAK_45 , ctr.y + TWEAK_45);
    b = new Point2D.Double(ctr.x - TWEAK_45, ctr.y - TWEAK_45);
    tickMark(a, b);
    a = new Point2D.Double(ctr.x - TWEAK_45 , ctr.y + TWEAK_45);
    b = new Point2D.Double(ctr.x + TWEAK_45, ctr.y - TWEAK_45);
    tickMark(a, b);
  }
  
  private void tickMark(Point2D.Double start, Point2D.Double end) {
    GeneralPath path = new GeneralPath();
    path.moveTo(start.x, start.y);
    path.lineTo(end.x, end.y);
    Stroke orig = g.getStroke();
    Color origColor = g.getColor();
    g.setColor(config.greyConstellationLines());
    g.setStroke(new BasicStroke(ChartUtil.STROKE_WIDTH_CONSTELLATION_LINE)); 
    g.draw(path);
    g.setColor(origColor);
    g.setStroke(orig);
  }
}
