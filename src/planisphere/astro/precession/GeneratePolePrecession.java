package planisphere.astro.precession;

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
import planisphere.config.Config;
import planisphere.draw.Bounds;
import planisphere.draw.ChartUtil;
import planisphere.draw.Projection;
import planisphere.draw.StereographicProjection;
import planisphere.draw.starchart.DrawPoles;

public final class GeneratePolePrecession extends GeneratePdfABC {

  public GeneratePolePrecession(Config config){
    super(config);
  }
  
  /**
   Stars and constellation lines. Circles for the ecliptic and the equator.
   Uses a stereographic projection.
   See {@link DrawPoles}.
  */
  protected void addContentToTheDocument(Graphics2D g) throws DocumentException, MalformedURLException, IOException {
    log("Star chart showing the movement of the poles of the ecliptic and equator.");
    
    Bounds bounds = config.starChartBounds();
    StarCatalog starCatalog = new StarCatalog(config);
    starCatalog.generateIntermediateStarCatalog(null);
    List<Star> stars = starCatalog.filterPolar(ChartUtil.LIMITING_MAG, bounds.minDecDeg, bounds.maxDecDeg, ChartUtil.EDGE_OVERLAP_DEGS);
    log("Using " + stars.size() + " stars out of " + starCatalog.all().size());
    
    ConstellationLines constellationLines = new ConstellationLines();
    constellationLines.readData();
    log("Size of constellation lines map: " + constellationLines.all().size());
    
    Projection projection = new StereographicProjection(config);

    startNewPage();
    
    DrawPoles drawPoles = new DrawPoles(stars, constellationLines, projection, g, config);
    drawPoles.draw();
  }  
}