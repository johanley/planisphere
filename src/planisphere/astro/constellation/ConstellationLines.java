package planisphere.astro.constellation;

import static planisphere.util.LogUtil.log;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import planisphere.astro.star.Star;
import planisphere.astro.star.StarCatalog;
import planisphere.config.Constants;
import planisphere.util.DataFileReader;

/** Stick figures for outlining the constellations.  */
public class ConstellationLines {

  /** Read in the data file. The data file exists in the same directory as this class. */
  public void readData(Boolean discardPolaris) {
    parseInputFile(discardPolaris);
  }

  /**
   All polylines for all constellations. 
   The key is the abbreviation for the constellation name, for example Peg (for Pegasus).
   The value is a list of 'polylines', consisting of N line segments. 
   Each line segment is a list of identifiers for stars in an underlying catalog.
  */
  public Map<String/*Ari*/ , List<List<Integer>> /*1..N polylines*/> all(){
    return lines;
  }

  /**
   For debugging only.
   The problem is that the polyline only gets drawn if ALL stars identified in the polyline are 
   actually present. For example, if the stars are filtered to only mag 4, and a polyline has a mag 4.5 star, 
   then the polyline will not be drawn. 
    
   <P>As a diagnostic, this method finds the polyline points (as Stars) that are MISSING from the given starlist, 
   and returns them in a list. The star list has a limiting mag which may exclude items used in a polyline.
  */
  public List<Star> scanForAnyMissingStarsInThe(List<Star> givenStarList, StarCatalog starCatalog){
    List<Star> result = new ArrayList<>();
    Set<String> constellations = lines.keySet();
    for (String constellation : constellations) {
      List<List<Integer>> polys = lines.get(constellation);
      for (List<Integer> poly : polys) {
        for (Integer id : poly) {
          boolean missing = true;
          for(Star star : givenStarList) {
            if (star.INDEX.equals(id)) {
              missing = false;
              break;
            }
          }
          if (missing) {
            result.add(lookUpStar(id, starCatalog));
          }
        }
      }
    }
    return result;
  }
  
  // PRIVATE 
  
  private Map<String/*Ari*/ , List<List<Integer>> /*1..N polylines*/> lines = new LinkedHashMap<>();
  
  private void parseInputFile(Boolean discardPolaris) {
    DataFileReader reader = new DataFileReader();
    List<String> lines = reader.readFile(this.getClass(), "constellation-lines-hip.utf8");
    for (String line : lines) {
      processLine(line.trim(), discardPolaris);
    }
  }
  
  /**
   Source data example (came from another project, in javascript-world):
     Cnc = [43103, 42806, 42911, 44066];[40526, 42911]
   Each line is a single constellation, and almost every constellation is present. 
   Some constellations are faint, and have no stars to join (in this implementation).
  */
  private void processLine(String line, Boolean discardPolaris) {
    int equals = line.indexOf("=");
    String constellationAbbr = line.substring(0, equals).trim();
    List<List<Integer>> polylinesIds = new ArrayList<>();
    
    String polylines = line.substring(equals+1).trim(); // [43103, 42806, 42911, 44066];[40526, 42911]
    //use regexes to grab each single-line
    String START = Pattern.quote("[");
    String END = Pattern.quote("]");
    String COMMA = Pattern.quote(",");
    Pattern singleLine = Pattern.compile(START + "(.*?)" + END); //1 matching group: '43103, 42806, 42911, 44066' Reluctant qualifier!
    
    //split the matching group around the comma
    Matcher matcher = singleLine.matcher(polylines);
    while (matcher.find()) {
      String oneLine = matcher.group(1);
      String[] parts = oneLine.split(COMMA);
      List<Integer> polylineIds = new ArrayList<>();
      for(String part : parts) {
        Integer id = Integer.valueOf(part.trim());
        if (id.equals(Constants.POLARIS) && discardPolaris) {
          log("Discarding Polaris from constellation lines: " + Constants.POLARIS);
        }
        else {
          polylineIds.add(id);
        }
      }
      polylinesIds.add(polylineIds);
    }
    lines.put(constellationAbbr, polylinesIds);
  }
  
  /** Returns null if not found. */
  private Star lookUpStar(Integer id, StarCatalog catalog) {
    Star result = null;
    for (Star star : catalog.all()) {
      if (star.INDEX.equals(id)) {
        result = star;
        break;
      }
    }
    return result;
  }
}