package planisphere.draw.starchart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import planisphere.astro.moon.EclipticCoords;
import planisphere.astro.moon.LunarOrbit;
import planisphere.astro.star.Position;
import planisphere.astro.time.GregorianCal;
import planisphere.config.Config;
import planisphere.draw.ChartUtil;
import planisphere.draw.Projection;
import planisphere.math.Maths;
import planisphere.util.LogUtil;

/**
 Mark the orbit of the Moon.
 The lunar orbit changes rapidly, and is hard to represent well on a planisphere.
*/
final class MoonsPath {
  
  MoonsPath(Projection projection, Graphics2D g, Config config){
    this.projection = projection;
    this.g = g;
    this.config = config;
  }

  public void draw() {
    draw(7,1.0);
  }

  private void draw(int month, double day) {
    double jdJan1 = GregorianCal.jd(config.year(), month, day);
    LogUtil.log("Path of the Moon's orbit for " + config.year() + "-" + month + "-" + day);
    LunarOrbit lunarOrbit = new LunarOrbit(jdJan1);
    List<Point2D.Double> places = new ArrayList<>();
    for (LunarOrbit.Place place : LunarOrbit.Place.values()) {
      EclipticCoords coords = lunarOrbit.latLongCoordsFor(place);
      Position pos = coords.toRaDec(jdJan1);
      LogUtil.log("Moon's " + place + ": " + coords + " " + pos);
      Point2D.Double where = projection.project(pos.δ, pos.α);
      places.add(where);
    }
    Point2D.Double ctr = Maths.centerFor(places.get(0), places.get(1), places.get(2));
    double radius = Maths.distance(ctr, places.get(0));
    
    double w = 2 * radius;
    double h = w; //always a circular arc
    Shape circle = new Ellipse2D.Double(ctr.x - radius, ctr.y - radius, w, h);
    Color origColor = g.getColor();
    g.setColor(config.greyConstellationLines());
    Stroke orig = g.getStroke();
    float dash1[] = {CelestialEquatorOrEcliptic.LITTLE_DASH};
    BasicStroke dashed = new BasicStroke(ChartUtil.STROKE_WIDTH_CONSTELLATION_LINE, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, dash1, 0.0f);
    g.setStroke(dashed); 
    g.draw(circle);
    g.setStroke(orig);
    g.setColor(origColor);
  }

  private Projection projection;
  private Graphics2D g;
  private Config config;
  
}
