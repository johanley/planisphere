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

public final class GenerateBasicStarChart extends GeneratePdfABC {
  
  public GenerateBasicStarChart(Config config){
    super(config);
  }
  
  /**
   Stars and constellation lines. Circles for the ecliptic and the equator.
  */
  protected void addContentToTheDocument(Graphics2D g) throws DocumentException, MalformedURLException, IOException {
    log("Basic star chart.");
    
    StarCatalog starCatalog = new StarCatalog(config);
    Double janFirst = GregorianCal.jd(config.year(), 1, 1.0);
    log("Applying proper motion/precession with JD " + janFirst + ", for Jan 1.0 " + config.year());
    starCatalog.generateIntermediateStarCatalog(janFirst);
    List<Star> stars = starCatalog.filterByMag(ChartUtil.LIMITING_MAG);
    log("Using " + stars.size() + " stars out of " + starCatalog.all().size());
    
    ConstellationLines constellationLines = new ConstellationLines();
    constellationLines.readData(config.discardPolaris());
    log("Size of constellation lines map: " + constellationLines.all().size());
    
    Projection projection = new StereographicProjection(config);

    DrawBasicStarChart basic = new DrawBasicStarChart(stars, constellationLines, projection, g, config);
    basic.draw();
  }  

}
