 package planisphere;

import static planisphere.util.LogUtil.log;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Set;
import java.util.TreeSet;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import planisphere.config.Config;
import static planisphere.config.Constants.*;
import planisphere.draw.ChartUtil;

/** 
 Abstract base class for generating the PDF files that form the planisphere.
 Uses a template method design.
*/
public abstract class GeneratePdfABC {

  protected GeneratePdfABC(Config config){
    this.config = config;
  }
  
  /** 
   Build a single-page PDF file/stream from scratch. 
   Calls the template method to create the page content. 
  */
  public void outputTo(OutputStream outputStream) throws DocumentException, MalformedURLException, IOException {
    openTheDocument(outputStream, config.fontDir());
    addMetadataToTheDocument();
    initGraphicsContext();
    startNewPage();
    
    addContentToTheDocument(g);
    
    disposeGraphicsContext();
    closeTheDocument();
  }
  
  /** Create the content of the PDF. Template method. */
  protected abstract void addContentToTheDocument(Graphics2D g) throws DocumentException, MalformedURLException, IOException;

  /** Called by subclasses, if they need MORE than one page. */
  protected void startNewPage() {
    document.newPage();
  }

  // PRIVATE

  protected Config config;
  protected Document document;  
  private PdfWriter writer;
  private Graphics2D g;
  private PdfContentByte contentByte;
  private PdfTemplate template;
  
  /**
   Read in settings.
   Set page size, margins, register fonts, etc.
   Fonts need to be in the system's hard drive somewhere.
   The font is not attached to the Document as a whole; it's attached to lower level items. 
  */
  private void openTheDocument(OutputStream outputStream, String fontDir) throws FileNotFoundException, DocumentException {
    log("Open the doc. Initial setup of pdf Document. Setting page size, margins. Reading in fonts.");
    
    embedFonts();
    registerAllFontsIn(fontDir, false);
    
    document = new Document();
    Rectangle rect = new Rectangle(config.width(), config.height());
    document.setPageSize(rect);
    document.setMargins(MARGIN_LEFT, MARGIN_RIGHT, MARGIN_TOP, MARGIN_BOTTOM);

    //should this be passed an encoding, I wonder?
    writer = PdfWriter.getInstance(document, outputStream);
    writer.setPdfVersion(PDF_VERSION); 
    writer.setViewerPreferences(PdfWriter.PageLayoutSinglePage);
    document.open(); //need to call this early!
  }
  
  private void embedFonts() {
    FontFactory.defaultEmbedding = true;
  }
  
  private void registerAllFontsIn(String fontDir, boolean log) {
    log("Registering all fonts in " + fontDir);
    FontFactory.registerDirectory(fontDir);
    if (log) {
      Set<String> fonts = new TreeSet<String>(FontFactory.getRegisteredFonts());
      for (String fontname : fonts) {
          log(fontname);
      } 
    }
  }
  
  private void addMetadataToTheDocument() {
    document.addAuthor(AUTHOR); 
    document.addTitle("Planisphere");
    document.addSubject("Planisphere for the night sky, for amateur astronomers.");
  }

  private void initGraphicsContext() {
    log("Fresh graphics context, and a new page.");
    contentByte = writer.getDirectContent();
    template = contentByte.createTemplate(config.width(), config.height());
    g = new PdfGraphics2D(template, config.width(), config.height(), new MyFontMapper());
    
    BasicStroke thinStroke = new BasicStroke(ChartUtil.STROKE_WIDTH_DEFAULT);
    g.setStroke(thinStroke);
    g.setFont(ChartUtil.baseFont());
    log("Graphics font: " + g.getFont().getFontName());
    
    //rendering hints (MOVED from DrawStarCharts)
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    //g = template.createGraphics(PdfConfig.WIDTH, PdfConfig.HEIGHT, new DefaultFontMapper()); //watch out! : deprecated!
  }
  
  /** You need to call this to actually draw the items to the page. */
  private void disposeGraphicsContext() {
    log("Flushing graphics.");
    g.dispose();
    contentByte.addTemplate(template, 0, 0); // x,y positioning of graphics in PDF page; yes, AFTER the disposal
  }
  
  private void closeTheDocument() {
    log("Closing the doc.");
    document.close(); 
  }
}