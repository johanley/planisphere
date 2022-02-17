package planisphere.draw.transparency;

import static planisphere.util.LogUtil.log;

import java.awt.Graphics2D;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;

import com.itextpdf.text.DocumentException;

import planisphere.GeneratePdfABC;
import planisphere.config.Config;
import planisphere.draw.Projection;
import planisphere.draw.StereographicProjection;

/** 
 Generate the transparency on the front of the planisphere.
 This generates a PDF which needs to be printed on a transparency.
 Each latitude needs a different transparency. 
*/
public final class GenerateTransparency extends GeneratePdfABC {
  
  public GenerateTransparency(Config config) {
    super(config);
  }
  
  /**
   Circles for various altitudes, including the horizon (altitude = 0).
   Meridian, and the east-west circle.
   On the outer rim, a 24h scale used to match up with a date (on the star chart, on the back of the planisphere).
   Thus, for a given date and time and latitude, the stars within the horizon-circle are above the horizon.
  */
  @Override protected void addContentToTheDocument(Graphics2D g) throws DocumentException, MalformedURLException, IOException {
    log("Transparency, for altitude and azimuth.");
    Projection projection = new StereographicProjection(config);

    DrawTransparency drawTransparency = new DrawTransparency(projection, g, config);
    drawTransparency.draw();
  }
}
