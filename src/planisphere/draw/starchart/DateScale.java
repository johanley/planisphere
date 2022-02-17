package planisphere.draw.starchart;

import static planisphere.util.LogUtil.log;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import planisphere.astro.time.DailySiderealTime;
import planisphere.astro.time.SiderealTime;
import planisphere.config.Config;
import planisphere.draw.ChartUtil;
import planisphere.draw.Projection;
import planisphere.math.Maths;

/** Date scale on the outer rim of the star chart. */
public final class DateScale {
  
  DateScale(Projection projection, Graphics2D g, ChartUtil chartUtil, Config config){
    this.projection = projection;
    this.g = g;
    this.chartUtil = chartUtil;
    this.config = config;
    this.months = months(config.monthNames());
  }

  void drawDateScale() {
    log("Date scale.");
    //don't draw the inner radius, since it's already present as the bounds of the projection.
    g.draw(circle(outerRadius()));
    g.draw(circle(sizeOfTransparency())); //for cutting with scissors
    log("Tick marks for days. Almost all years will show a discontinuity at year end. That's expected.");
    SiderealTime sidTime = new SiderealTime(config);
    for(DailySiderealTime dst : sidTime.everyDayOfTheYear(config.year())) {
      double theta = dst.getRa();
      int unitLength = 3;
      double r1 = outerRadius();
      double r2 = r1 - multiplier(dst) * unitLength; //how big to make the tick mark
      Point2D.Double start = convertToXY(r1, theta);
      Point2D.Double end = convertToXY(r2, theta);
      tickMark(start, end); 
      if (dst.getDay() == 1) {
        showMonth(theta, months.get(dst.getMonth()-1)); 
      }
    }
  }
  
  private Projection projection;
  private Graphics2D g;
  private ChartUtil chartUtil;
  private List<String> months;
  private Config config;
  
  private List<String> months(String raw){
    List<String> result = new ArrayList<>();
    String[] parts = raw.split(Pattern.quote(","));
    for(String part : parts) {
      result.add(part.trim());
    }
    
    if (result.size() != 12) {
      throw new RuntimeException("You don't have 12 month names specified in the config file.");
    }
    return result;
  }
  
  /** Reflect the x for the southern hemisphere. */
  private int sign() {
    return projection.getBounds().hemisphereSign();
  }

  private Point2D.Double convertToXY(double r, double theta){
    return new Point2D.Double(
      projection.centerOfProj().x + sign() * r * Math.cos(theta),
      projection.centerOfProj().y + r * Math.sin(theta)
    );    
  }
  
  private void tickMark(Point2D.Double start, Point2D.Double end) {
    GeneralPath path = new GeneralPath();
    path.moveTo(start.x, start.y);
    path.lineTo(end.x, end.y);
    Stroke orig = g.getStroke();
    g.setStroke(new BasicStroke(ChartUtil.STROKE_WIDTH_CONSTELLATION_LINE)); 
    g.draw(path);
    g.setStroke(orig);
  }

  private Shape circle(double radius) {
    double x = chartUtil.getWidth()/2.0 - radius;
    double y = chartUtil.getHeight()/2.0 - radius;
    double w = 2 * radius;
    double h = w; //always a circular arc
    return new Ellipse2D.Double(x, y, w, h);
  }
  
  private double outerRadius() {
    return chartUtil.getWidth()/2.0 - chartUtil.borderWidth() - chartUtil.scaleWidth();
  }
  
  /** Not really desirable to draw this, since it's already present, as the bounds of the projection. */
  private double innerRadius() {
    return outerRadius() - chartUtil.scaleWidth();
  }
  
  private double middleRadius() {
    return (outerRadius() + innerRadius()) / 2.0;
  }
  
  /** This is convenient for matching the size of the chart to the size of the tranparency. */
  private double sizeOfTransparency() {
    return chartUtil.getWidth()/2.0 - chartUtil.borderWidth();
  }

  private double multiplier(DailySiderealTime gstDaily) {
    double result = 1.0;
    if (gstDaily.getDay() == 1) {
      result = 4.0;
    }
    else if (gstDaily.getDay() % 10 == 0 && gstDaily.getDay() < 30) {
      result = 2.75;
    }
    else if (gstDaily.getDay() % 5 == 0) {
      result = 2.0;
    }
    return result;
  }
  
  private void showMonth(Double gstRa, String text) {
    Double raTweak = Maths.degToRads(2.0);
    Point2D.Double target = convertToXY(middleRadius(), gstRa + raTweak);

    double rotationAngle = chartUtil.rotationAngle(projection, target) + Math.PI;
    Consumer<Graphics2D> drawer = x -> {
      Point2D.Double centered = chartUtil.centerTextOn(0, 0, text, g);
      x.drawString(text, Maths.round(centered.x), Maths.round(centered.y));
    };
    chartUtil.drawRotated(g, rotationAngle, target, drawer);
  }
}