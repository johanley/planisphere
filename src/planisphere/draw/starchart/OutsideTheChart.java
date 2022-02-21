package planisphere.draw.starchart;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.function.Consumer;

import planisphere.config.Config;
import planisphere.draw.ChartUtil;
import planisphere.math.Maths;

/** 
 Place items outside the main body of the star chart. 
*/
final class OutsideTheChart {
  
  OutsideTheChart(Config config, Graphics2D g, ChartUtil chartUtil){
    this.config = config;
    this.g = g;
    this.chartUtil = chartUtil;
  }
 
  /**
   Guidelines for cutting with scissors, to make the chart square (if it isn't already).
   
   <P>Month names for each season are placed in a corner of the square.
   This helps when you pick up the chart, to orient it in an appropriate starting position.
   You place the desired month under your left thumb. (Biased for right-handed people.)
  */
  void draw() {
    guidelinesForScissors();
    seasonMonthInCorners();
  }
  
  private Config config;
  private Graphics2D g;
  private ChartUtil chartUtil;

  private void guidelinesForScissors() {
    if (config.width() < config.height()) {
      // make it square
      drawHorizontalLineAt(config.height() - chopOffAtEachEnd()); //bottom
      drawHorizontalLineAt(chopOffAtEachEnd()); //top
    }
  }
  
  private double chopOffAtEachEnd() {
    double diff = config.height() - config.width();
    return diff * 0.5;
  }
  
  private void drawHorizontalLineAt(double y) {
    GeneralPath path = new GeneralPath();
    path.moveTo(0, y);
    path.lineTo(config.width(), y);
    g.draw(path);
  }
  
  /** 
   Affordances to help quickly orient the chart when you pick it up.
   You put your left thumb over the month nearest the current date.
   Starting in the lower left corner, and going clockwise, the months are 
      Feb, May, Aug, Nov (02, 05, 08, 11). 
  */
  private void seasonMonthInCorners() {
    List<String> months = config.monthNamesList();
    for(int corner = 1; corner <= 4; ++corner) {
      String month = months.get(3 * corner - 2);
      printMonthInCorner(month, locationOf(corner), corner);
    }
  }
  
  private Point2D.Double locationOf(int corner){
    boolean isTop = (corner == 2 || corner == 3);
    boolean isLeft = (corner == 1 || corner == 2);
    double bottom = config.height() - chopOffAtEachEnd();
    double top = chopOffAtEachEnd();
    double y = isTop ? top : bottom;
    double x = isLeft ? 0 : config.width();
    
    //move in a bit from the corner
    double TWEAK = chartUtil.percentWidth(4.0);
    int sign = isTop ? +1 : -1;
    y = y + sign * TWEAK;
    sign = isLeft ? +1 : -1;
    x = x + sign * TWEAK;
    return new Point2D.Double(x, y);
  }
  
  private void printMonthInCorner(String month, Point2D.Double corner, int cornerIndex) {
    double rotationAngle = Maths.HALF_PI * (cornerIndex - 1);
    Consumer<Graphics2D> drawer = x -> {
      Point2D.Double centered = chartUtil.centerTextOn(0, 0, month, g);
      x.drawString(month, Maths.round(centered.x), Maths.round(centered.y));
    };
    chartUtil.drawRotated(g, rotationAngle, corner, drawer);
  }  
}
