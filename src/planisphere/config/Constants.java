package planisphere.config;

import com.itextpdf.text.pdf.PdfWriter;

/** Various constants that aren't configurable. */
public final class Constants {

  public static final String FONT_NAME = "Times New Roman";

  public static final String NL = System.getProperty("line.separator");

  /** The right ascension on the star chart that's above the celestial pole, when the page is held upright. */
  public static double UPPER_RA = 18.0;

  /** The right ascension on the star chart that's below the celestial pole, when the page is held upright. */
  public static double LOWER_RA = 6.0;
  
  /** A representative time in the evening, when an amateur astronomer might start observing. */
  public static Integer LOCAL_EVENING_HOUR = 20;
  
  /** File name for the output PDF for the front (transparency) - {@value}.  */
  public static final String TRANSPARENCY_FILE = "transparency.pdf";

  /** File name for the output PDF for the back (star chart) - {@value}.  */
  public static final String STAR_CHART_FILE = "starchart.pdf";
  
  /** File name for the output PDF for pole precession demo - {@value}.  */
  public static final String PRECESSION_DEMO_FILE = "pole_precession.pdf";
  
  /** File name for the output PDF for a basic star chart - {@value}.  */
  public static final String BASIC_CHART_FILE = "basic_chart.pdf";
  
  /** Name of the document's creator. */
  public static final String AUTHOR = "John O'Hanley";
  
  public static final char PDF_VERSION = PdfWriter.VERSION_1_3;
  public static final float MARGIN_LEFT = pointsFromIn(0.75f);
  public static final float MARGIN_RIGHT = pointsFromIn(0.75f);
  public static final float MARGIN_TOP = pointsFromIn(0.75f);
  public static final float MARGIN_BOTTOM = pointsFromIn(0.5f);
  public static final float FONT_SIZE_NORMAL = 12F; 
  
  //PRIVATE 
  
  private static final int POINTS_PER_INCH = 72; //the itext default is 72

  private static float pointsFromIn(double inches) {
    return (float) inches * POINTS_PER_INCH;
  }


}