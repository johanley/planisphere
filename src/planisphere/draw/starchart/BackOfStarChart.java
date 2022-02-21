package planisphere.draw.starchart;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

import planisphere.astro.moon.LunarPosition;
import planisphere.astro.planets.Planet;
import planisphere.astro.planets.PlanetPosition;
import planisphere.astro.planets.Transit;
import planisphere.config.Config;
import planisphere.config.Constants;
import planisphere.math.Maths;
import planisphere.util.LogUtil;

/** Supplementary info on the back of the star chart. */
final class BackOfStarChart {
  
  public BackOfStarChart(Document document, Config config) {
    this.document = document;
    this.config = config;
  }
  
  /**
    A table showing the times of every lunar transit for the year.
    Months can have a day with no lunar transit. Those days occur near full Moon.
    Dates and times are in the observer's local time zone.   
  */
  void addContent() throws DocumentException  {
    LogUtil.log("Transit times on the back of the star chart: Moon.");
    emptyLines(5);
    title(config.location() + " " + config.year() + " " + latLongEtc());
    title(config.lunarTransitsTitle());
    emptyLines(1);
    Transit lunar = new Transit(config);
    LunarPosition sourceOf = new LunarPosition();
    tableFor(lunar.transitsForEveryDayOfTheYear(sourceOf::position));
    
    LogUtil.log("Transit times on the back of the star chart: Planets.");
    title(config.planetaryTransitsTitle());
    emptyLines(1);
    tableFor(planetaryTransitsForMidMonth());
    title(URL);
  }
  
  private Document document;
  private Config config;

  /* Cross-talk: various settings of the table affect each other. */
  private static final float FONT_SIZE = 6.0F;
  private static final int NUM_COLUMNS = 13;
  private static final String BLANK_ENTRY = "";
  private static final String TIME_FORMAT =  "HH:mm";
  
  private static final int PERCENTAGE_WIDTH_LUNAR = 45;
  
  private static final int PERCENTAGE_WIDTH_PLANETS = 50;
  
  private static final String URL = "github.com/johanley/planisphere";

  /** If there is no lunar transit on a given day, show entries as blanks. */
  private void tableFor(List<Optional<LocalDateTime>> lunarTransits) throws DocumentException {
    PdfPTable table = new PdfPTable(NUM_COLUMNS);
    table.setWidthPercentage(PERCENTAGE_WIDTH_LUNAR);
    table.setHorizontalAlignment(Element.ALIGN_CENTER);
    float[] relativeWidths = {1.75f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f};
    table.setWidths(relativeWidths);
    
    addHeaderCell(table, BLANK_ENTRY); 
    for(String monthName : config.monthNamesList()) {
      addHeaderCell(table, monthName.trim()); 
    }
    table.setHeaderRows(1);
    
    for(int day = 1; day <= 31; day++) { 
      for(int col = 0; col <= 12 ; ++col) {
        if (col == 0) {
          addRowCell(table, String.format("%2s", day));
        }
        else {
          Optional<LocalDateTime> dateTime = lookupTransitTime(col, day, lunarTransits);
          if (dateTime.isEmpty()) {
            addRowCell(table, BLANK_ENTRY);
          }
          else {
            addRowCell(table, timeOf(dateTime));
          }
        }
      }
    }
    document.add(table);
  }
  
  /** For the 15th of each month. */
  private Map<Planet, List<Optional<LocalDateTime>>> planetaryTransitsForMidMonth(){
    Map<Planet, List<Optional<LocalDateTime>>> result = new LinkedHashMap<>();
    for(Planet planet : Planet.WITHOUT_EARTH) {
      Transit tr = new Transit(config);
      PlanetPosition sourceOf = new PlanetPosition(planet);
      List<Optional<LocalDateTime>> monthlyTransits = tr.transitsForMidMonth(sourceOf::position);
      result.put(planet, monthlyTransits);
    }
    return result;
  }

  private void title(String title) throws DocumentException {
    Chunk chunk = new Chunk(title, normalFont());
    Paragraph tableTitle = new Paragraph();
    tableTitle.setAlignment(Element.ALIGN_CENTER);
    tableTitle.add(chunk);
    document.add(tableTitle);
  }
  
  private void tableFor(Map<Planet, List<Optional<LocalDateTime>>> planetaryTransits) throws DocumentException {
    PdfPTable table = new PdfPTable(NUM_COLUMNS);
    table.setWidthPercentage(PERCENTAGE_WIDTH_PLANETS);
    table.setHorizontalAlignment(Element.ALIGN_CENTER);
    float[] relativeWidths = {6.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f};
    table.setWidths(relativeWidths);
    
    addHeaderCell(table, BLANK_ENTRY); 
    for(String monthName : config.monthNamesList()) {
      addHeaderCell(table, monthName.trim()); 
    }
    table.setHeaderRows(1);
    
    for(Planet planet : Planet.WITHOUT_EARTH) { 
      for(int col = 0; col <= 12 ; ++col) {
        if (col == 0) {
          addRowCell(table, nameOf(planet));
        }
        else {
          Optional<LocalDateTime> dateTime = lookupTransitTime(planet, col, planetaryTransits.get(planet));
          if (dateTime.isEmpty()) {
            addRowCell(table, BLANK_ENTRY);
          }
          else {
            addRowCell(table, timeOf(dateTime));
          }
        }
      }
    }
    document.add(table);
  }
  
  /** The Moon has a transit (usually) for each day. */
  private Optional<LocalDateTime> lookupTransitTime(int month, int day, List<Optional<LocalDateTime>> lunarTransits){
    Optional<LocalDateTime> result = Optional.empty();
    for(Optional<LocalDateTime> lunarTransit : lunarTransits) {
      if (lunarTransit.isPresent()) {
        LocalDateTime ldt = lunarTransit.get();
        if (ldt.getMonthValue() == month && ldt.getDayOfMonth() == day) {
          result = lunarTransit;
          break;
        }
      }
    }
    return result;
  }
  
  /** The planets have a transit (usually) for the 15th of each month. */
  private Optional<LocalDateTime> lookupTransitTime(Planet planet, int month, List<Optional<LocalDateTime>> planetaryTransits){
    Optional<LocalDateTime> result = Optional.empty();
    for(Optional<LocalDateTime> planetaryTransit : planetaryTransits) {
      if (planetaryTransit.isPresent()) {
        LocalDateTime ldt = planetaryTransit.get();
        if (ldt.getMonthValue() == month) {
          result = planetaryTransit;
          break;
        }
      }
    }
    return result;
  }
  
  private String timeOf(Optional<LocalDateTime> dt) {
    return formatted(dt, TIME_FORMAT);
  }
  
  private String formatted(Optional<LocalDateTime> dt, String format) {
    return dt.isPresent() ?  dt.get().format(DateTimeFormatter.ofPattern(format)) : BLANK_ENTRY; 
  }
  
  private void addHeaderCell(PdfPTable table, String text) {
    Chunk chunk = new Chunk(text, normalFont());
    GrayColor grey = new GrayColor(0.8f);
    addChunk(table, chunk, grey);
  }

  private void addRowCell(PdfPTable table, String string) {
    Chunk chunk = new Chunk(string, normalFont());
    addChunk(table, chunk, null);
  }

  private void addChunk(PdfPTable table, Chunk chunk, BaseColor baseColor) {
    Phrase phrase = new Phrase(chunk);
    PdfPCell cell = new PdfPCell(phrase);
    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    cell.setHorizontalAlignment(Element.ALIGN_MIDDLE);
    //cell.setLeading(0f, 1.1f); // 0 + 1.5 times the font height
    //cell.setPaddingBottom(1);
    //cell.setPaddingLeft(1);
    if (baseColor != null) {
      cell.setBackgroundColor(baseColor);
    }
    table.addCell(cell);
  }
  
  private Font normalFont() {
    //WARNING: I added BaseFont.IDENTITY_H to make Greek letters appear; otherwise nothing showed
    /*
     * https://stackoverflow.com/questions/3858423/itext-pdf-greek-letters-are-not-appearing-in-the-resulting-pdf-documents
     * https://itextpdf.com/en/resources/faq/technical-support/itext-5-legacy/how-print-mathematical-characters
     */
    return FontFactory.getFont(Constants.FONT_NAME, BaseFont.IDENTITY_H, FONT_SIZE, Font.NORMAL);
  }

  /** Used only to control the vertical placement of the table on the page. */
  private void emptyLines(int num) throws DocumentException {
    Chunk chunk = new Chunk(someEmptyLines(num), normalFont());
    Paragraph space = new Paragraph();
    space.add(chunk);
    document.add(space);
  }

  /** 
  WARNING: using Paragraph.setSpacingBefore/After injects Helvetica (unembedded) references in the the document!
  Those references cause rejection by lulu.com, because it requires all fonts to be embedded. 
  Hence this method, which just puts empty lines into a para, instead of calling the setSpacingXXX methods.
  */
  private String someEmptyLines(int n) {
    StringBuilder result = new StringBuilder();
    for (int idx = 0; idx < n; ++idx) {
      result.append(Constants.NL);
    }
    return result.toString();
  }
 
  private String latLongEtc() {
    String latText = "φ=" + degrees(config.latitude());
    String longText = "λ=" + degrees(config.longitude());
    
    String plus = config.hoursOffsetFromUT() > 0 ? "+" : "";
    String timeCorrText = "UT"+ plus + config.hoursOffsetFromUT() + "h" + config.minutesOffsetFromUT() + "m";
    
    return "[" + latText + " " + longText + " " + timeCorrText + "]";
  }
  
  private String degrees(Double rads) {
    Double angle = Maths.radsToDegs(rads);
    angle = Maths.round(angle*100) / 100.0;
    return angle.toString() + "°";
  }
  
  private String nameOf(Planet planet) {
    String[] parts = config.planetNames().split(Pattern.quote(","));
    return parts[planet.ordinal()];
  }
}