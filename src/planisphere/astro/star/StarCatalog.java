package planisphere.astro.star;

import static planisphere.util.LogUtil.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import planisphere.astro.precession.LongTermPrecession;
import planisphere.astro.time.AstroUtil;
import planisphere.config.Config;
import planisphere.config.Constants;
import planisphere.math.Maths;
import planisphere.util.DataFileReader;

/** 
 Open-source bright star catalog, based on Hipparcos data.
 <P>Source: <a href='https://github.com/johanley/star-catalog'>link</a>
 
 <P>A subset of catalog data is read into memory. As a side-effect for developer convenience, the data
 used by this project is dumped into a text file. (See logging output for the file's location.)
 
 <P>WARNING: the IDs used here need to match the IDs used by the constellation lines and other items.
*/
public final class StarCatalog {

  public StarCatalog(Config config) {
    this.config = config;
  }
  
  /**
   Generate the desired star catalog data.
   WARNING: this method must be called first!
   
   <P>Actions taken on the data:
   <ul>
    <li>apply proper motion from J1991.25 to the target date
    <li>apply precession from J2000 to the target date 
    <li>add proper names to stars ('Vega', for instance)
   </ul>
   
   <P>As a side effects, this method saves the catalog data used here as a file. 
   This is meant for developer convenience, to easily examine the data.
   
   <P>If the precessionJd is null, then proper motion and precession are not applied at all.   
  */
  public void generateIntermediateStarCatalog(Double precessionJd) throws IOException {
    readInRawCatalogData();
    if (precessionJd != null) {
      log("Applying proper motion / precession to the star catalog. Year: " + config.year());
      applyProperMotion(precessionJd);
      applyPrecession(precessionJd);
    }
    else {
      log("Not applying proper motion / precession or proper motion. Year: " + config.year());
    }
    addProperNamesToStars();
    saveToIntermediateFile();
    scanForMissingItems();
  }
  
  /** 
   Filter the whole catalog by a limiting magnitude.
   In this project, it's not prudent to filter by geometry, a range of declination, say. That's because there's 
   a high risk of leaving things out: precession and proper motion changes the relative positions a great deal over 
   long time scales. 
  */
  public List<Star> filterByMag(Double limitingMag){
    List<Star> result = new ArrayList<>();
    for (Star star : stars) {
      if (Maths.inRange(-5.0, limitingMag, star.MAG)) {
        result.add(star);
        //there's no real need to make a copy of the star object, since the data is treated as read-only
      }
    }
    return result;
  }

  /** Return all of the stars in the catalog, with no filter. */
  public List<Star> all(){
    return Collections.unmodifiableList(stars);
  }
  
  public Optional<Star> findByProperName(String properName) {
    Optional<Star> result = Optional.empty();
    for (Star star : stars) {
      if (star.PROPER_NAME.equalsIgnoreCase(properName)) {
        result = Optional.of(star);
      }
    }
    return result;
  }
  
  public Optional<Star> findByName(String name) {
    Optional<Star> result = Optional.empty();
    for (Star star : stars) {
      if (star.NAME.equalsIgnoreCase(name)) {
        result = Optional.of(star);
      }
    }
    return result;
  }
  
  // PRIVATE 

  private Config config;
  private List<Star> stars = new ArrayList<>();
  
  private void readInRawCatalogData() {
    log("Read in raw catalog.");
    DataFileReader reader = new DataFileReader();
    List<String> lines = reader.readFile(this.getClass(), "os-bright-star-catalog-hip.utf8");
    int lineCount = 0;
    Star star = null;
    for(String line : lines) {
      ++lineCount;
      star = processLine(line);
      if (star.INDEX.equals(Constants.POLARIS) && config.discardPolaris()) {
        log("Discarding Polaris from star catalog: " + Constants.POLARIS);
      }
      else {
        stars.add(star);
      }
    }
    log("Read this many lines: " + lineCount);
    log("Using this number of stars from catalog: " + stars.size());
  }
  
  private void addProperNamesToStars() {
    log("Add proper names to stars.");
    StarName starName = new StarName();
    starName.readData();
    int count = 0;
    for (Star star : stars) {
      String name = star.NAME; //can be blank
      if (name.length() > 0) {
        int firstBlank = name.indexOf(" ");
        String constellationAbbr = name.substring(firstBlank).trim();
        String bayerOrFlamsteed = name.substring(0, firstBlank);
        String properName = starName.nameFor(constellationAbbr, bayerOrFlamsteed);
        if (properName.length() > 0) {
          ++count;
          star.PROPER_NAME = properName;
        }
      }
    }
    log("Added " + count + " proper names for stars.");
  }

  private void saveToIntermediateFile() throws IOException {
    log("Save to an intermediate file. Only needed for dev/debugging purposes.");
    String outputFileName = "stars.utf8"; 
    finalOutput(stars, outputFileName);
  }
  
  private Star processLine(String line){
    Star result = new Star();
    result.INDEX = sliceInt(line, 1, 6);
    
    //prefer bayer to flamsteed
    result.NAME = slice(line, 201, 7); //possibly empty
    if (isEmpty(result.NAME)){
      result.NAME = slice(line, 209, 7); //possibly empty
    }
    
    result.MAG = Double.valueOf(slice(line, 148, 5)); // Vmag, possible leading minus sign; that's ok
    
    result.RA = sliceDbl(line, 45, 12); // rads
    result.DEC = sliceDbl(line, 59, 13); // rads
    
    result.PROPER_MOTION_RA = masToArcseconds(line, 81, 8); // (arcsec per yr) * cos(dec) 
    result.PROPER_MOTION_DEC = masToArcseconds(line, 90, 8); // arcsec per yr 
    
    result.PARALLAX = masToArcseconds(line, 73, 7); // arcseconds
    result.RADIAL_VELOCITY = optionalDouble(line, 99, 7); //km per sec
    
    result.HD_DESIGNATION = slice(line, 189, 6);

    return result;
  }
  
  
  private String slice(String line, int start /*1-based*/, int numchars){
    return line.substring(start-1, start-1+numchars).trim();
  }
  
  private Integer sliceInt(String line, int start, int numchars){
    return Integer.valueOf(slice(line, start, numchars));
  }
  
  private Double sliceDbl(String line, int start, int numchars){
    return Double.valueOf(slice(line, start, numchars));
  }
  
  private double masToArcseconds(String line, int start, int numchars) {
    return sliceDbl(line, start, numchars) / 1000.0D;  
  }
  
  private boolean isEmpty(String text){
    return text == null || text.trim().length() == 0; 
  }
  
  private void finalOutput(List<Star> brightstars, String filename) throws FileNotFoundException, IOException {
    File out = new File(filename);
    log("Writing to file. The file is for info/debugging purposes only. File name: " + out.getCanonicalPath());
    FileOutputStream fos = new FileOutputStream(out);
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, DataFileReader.ENCODING));
    writer.write("index,constellation,right_ascension,declination,magnitude,designation,proper_name,pm_ra,pm_dec,parallax,radial_velocity,hd");
    writer.newLine();
    for(Star nearbystar: brightstars){
      writer.write(nearbystar.toString());
      writer.newLine();
    }
    writer.newLine();
    writer.close();    
  }
  
  private void applyProperMotion(Double jd) {
    log("Applying proper motion from " + ProperMotion.J1991_25 + " to the target year " + config.year());
    List<Stat> stats = new ArrayList<>();
    Star fastest = null;
    ProperMotion properMotion = new ProperMotion(ProperMotion.J1991_25, jd);
    int countAboveOneDegree = 0;
    double maxArcSecs = 0.0;
    for(Star star : stars) {
      double val = properMotion.applyTo(star);
      if (val > maxArcSecs) {
        maxArcSecs = val;
        fastest = star;
      }
      if (val > 3600.0) {
        ++countAboveOneDegree;
      }
      stats.add(new Stat(star, val));
    }
    Collections.sort(stats);
    double maxRads = Maths.degToRads(maxArcSecs / 3600.0);
    log("Finished applying proper motion.");
    log("  Max proper motion : " + AstroUtil.radsToDegreeString(maxRads) + " " + fastest.NAME + " mag " + fastest.MAG + " HD:" + fastest.HD_DESIGNATION);
    log("  Number of stars that whose proper motion exceeded 1 degree: " + countAboveOneDegree);
    log("  Top 25 proper motions: ");
    for(int count = 0; count < 25; ++count) {
      Star s = stats.get(count).star;
      String pm = AstroUtil.radsToDegreeString(Maths.arcsecToRads(stats.get(count).properMotion));
      log("    " + pm + " " + s.NAME + " mag " + s.MAG + " HD:" + s.HD_DESIGNATION);
    }
    log("  Proper motions for stars of magnitude <= 1.5: ");
    for(Stat stat : stats) {
      if (stat.star.getMagnitude() <= 1.5) {
        String pm = AstroUtil.radsToDegreeString(Maths.arcsecToRads(stat.properMotion));
        Star s = stat.star;
        log("    " + pm + " " + s.NAME + " mag " + s.MAG + " HD:" + s.HD_DESIGNATION);
      }
    }

  }
  
  private static final class Stat implements Comparable<Stat>{
    Stat(Star star, double pm){
      this.star = star;
      this.properMotion = pm;
    }
    Star star;
    /** Arcseconds. Proper motion of the star from catalog epoch to the target epoch. */
    double properMotion;
    /** Ascending order. */
    @Override public int compareTo(Stat that) {
      return that.properMotion >= this.properMotion ? 1 : -1;
    }
  }
  
  private void applyPrecession(Double precessionJd) {
    log("Applying precession from J2000 to target JD: " + precessionJd);
    LongTermPrecession precession = new LongTermPrecession();
    for(Star star : stars) {
      Position newPos = precession.apply(star.position(), precessionJd);
      star.RA = newPos.α;
      star.DEC = newPos.δ;
    }
    log("Finished applying precession.");
  }
  
  private void scanForMissingItems() {
    int countParallax = 0;
    int countRadialVelocity = 0;
    int countHD = 0; 
    for(Star star : stars) {
      countParallax = countParallax + missing(star.PARALLAX);
      countRadialVelocity = countRadialVelocity + missing(star.RADIAL_VELOCITY);
      countHD = countHD + missing(star.HD_DESIGNATION);
    }
    log("Num stars missing parallax: " + countParallax);
    log("Num stars missing radial velocity: " + countRadialVelocity);
    log("Num stars missing HD: " + countHD);
  }
  
  private int missing(Object thing) { 
    return thing == null ? 1 : 0;
  }
  
  private int missing(String thing) { 
    return (thing == null || thing.trim().length() == 0) ? 1 : 0;
  }

  
  private Double optionalDouble(String line, int start, int numchars) {
    Double result = null;
    if (line.length() >= start + numchars) {
      String text = line.substring(start-1, start-1+numchars).trim();
      if (text != null && text.trim().length() > 0) {
        result = Double.valueOf(text);
      }
    }
    return result;
  }
}