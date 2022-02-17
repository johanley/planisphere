package planisphere.draw.starchart;
 
import static planisphere.util.LogUtil.log;

import java.awt.Graphics2D;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.List;

import com.itextpdf.text.DocumentException;

import planisphere.GeneratePdfABC;
import planisphere.astro.constellation.ConstellationLines;
import planisphere.astro.star.Star;
import planisphere.astro.star.StarCatalog;
import planisphere.astro.time.GregorianCal;
import planisphere.config.Config;
import planisphere.draw.Bounds;
import planisphere.draw.ChartUtil;
import planisphere.draw.Projection;
import planisphere.draw.StereographicProjection;

/** Generate the star chart on the back of the planisphere. */
public final class GenerateStarChart extends GeneratePdfABC {

  public GenerateStarChart(Config config){
    super(config);
  }
  
  /**
   Stars and constellation lines. Circles for the ecliptic and the equator.
   Date scale on the outer rim, to indicate the local sidereal time at 20h local mean time, for the given date.
   Uses a stereographic projection.
   See {@link DrawStarChart}.
  */
  protected void addContentToTheDocument(Graphics2D g) throws DocumentException, MalformedURLException, IOException {
    log("Star chart.");
    
    Bounds bounds = config.starChartBounds();
    StarCatalog starCatalog = new StarCatalog();
    Double janFirst = GregorianCal.jd(config.year(), 1, 1.0);
    log("Applying precession with JD " + janFirst + ", for Jan 1.0 " + config.year());
    starCatalog.generateIntermediateStarCatalog(janFirst);
    List<Star> stars = starCatalog.filterPolar(ChartUtil.LIMITING_MAG, bounds.minDecDeg, bounds.maxDecDeg, ChartUtil.EDGE_OVERLAP_DEGS);
    log("Using " + stars.size() + " stars out of " + starCatalog.all().size());
    
    ConstellationLines constellationLines = new ConstellationLines();
    constellationLines.readData();
    log("Size of constellation lines map: " + constellationLines.all().size());
    
    Projection projection = new StereographicProjection(config);

    BackOfStarChart back = new BackOfStarChart(document, config);
    back.addContent();
    startNewPage();
    
    DrawStarChart drawStarChart = new DrawStarChart(stars, constellationLines, projection, g, config);
    drawStarChart.draw();
    
    /*
    DrawPoles drawPoles = new DrawPoles(stars, constellationLines, projection, g);
    drawPoles.draw();
    */
  }
 }