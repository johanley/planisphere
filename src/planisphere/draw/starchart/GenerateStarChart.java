package planisphere.draw.starchart;
 
import static planisphere.util.LogUtil.log;

import java.awt.Graphics2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import com.itextpdf.text.DocumentException;

import planisphere.GeneratePdfABC;
import planisphere.astro.constellation.ConstellationLines;
import planisphere.astro.star.Star;
import planisphere.astro.star.StarCatalog;
import planisphere.astro.time.GregorianCal;
import planisphere.config.Config;
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
    
    StarCatalog starCatalog = new StarCatalog(config);
    Double janFirst = GregorianCal.jd(config.year(), 1, 1.0);
    log("Applying precession with JD " + janFirst + ", for Jan 1.0 " + config.year());
    starCatalog.generateIntermediateStarCatalog(janFirst);
    List<Star> stars = starCatalog.filterByMag(ChartUtil.LIMITING_MAG);
    log("Using " + stars.size() + " stars out of " + starCatalog.all().size());
    
    ConstellationLines constellationLines = new ConstellationLines();
    constellationLines.readData(config.discardPolaris());
    log("Size of constellation lines map: " + constellationLines.all().size());
    List<Star> missingStars = constellationLines.scanForAnyMissingStarsInThe(stars, starCatalog);
    log("Num stars referenced by the constellation lines data structure that are missing from the core data: " + missingStars.size());
    for (Star missingStar : missingStars) {
      log(" " + missingStar);
    }
    
    Projection projection = new StereographicProjection(config);

    BackOfStarChart back = new BackOfStarChart(document, config);
    back.addContent();
    startNewPage();
    
    DrawStarChart drawStarChart = new DrawStarChart(stars, constellationLines, projection, g, config);
    drawStarChart.draw();
  }
 }