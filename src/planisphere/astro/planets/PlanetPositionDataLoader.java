package planisphere.astro.planets;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import planisphere.util.DataFileReader;
import planisphere.util.LogUtil;

/** 
 Load VSOP87D data for a planet, using data files in the native directory.
*/
final class PlanetPositionDataLoader {

  /** 
   Return a data structure to the caller containing all periodic terms for the given planet, 
   for all parameters defined in the {@link Param} enumeration. 
  */
  Map<Param, List<PeriodicTerm>> periodicTermsForThe(Planet planet) {
    LogUtil.log("Loading periodic terms for " + planet);
    return readPeriodicTermData(planet);
  }
  
  // PRIVATE 
  
  private static final String FILE_NAME_START = "vsop87D-"; 
  private static final String FILE_NAME_END = ".utf8";
  private static final String PARAM_HEADER = " VSOP87";
  
  private Map<Param, List<PeriodicTerm>> readPeriodicTermData(Planet planet) {
    Map<Param, List<PeriodicTerm>> result = new LinkedHashMap<Param, List<PeriodicTerm>>();
    DataFileReader reader = new DataFileReader();
    List<String> lines = reader.readFile(PlanetPositionDataLoader.class, dataFileNameFor(planet));
    Param param = null;
    List<PeriodicTerm> periodicTerms = null;
    for(String line : lines) {
      if (isParamHeader(line)) {
        if (param != null) {
          //finish old
          result.put(param, periodicTerms);
        }
        //start new
        param = paramFor(line);
        periodicTerms = new ArrayList<>();
      }
      else {
        //regular line
        periodicTerms.add(periodicTermFor(line));
      }
    }
    //finish last line
    result.put(param, periodicTerms);
    //countTheTerms(result, planet);
    return result;
  }
  
  private String dataFileNameFor(Planet planet) {
    return FILE_NAME_START + planet.name().toLowerCase() + FILE_NAME_END;
  }
  
  private boolean isParamHeader(String line) {
    return line.contains(PARAM_HEADER);
  }
  
  /*
   VSOP87 VERSION D4    EARTH     VARIABLE 1 (LBR)       *T**0    559 TERMS    HELIOCENTRIC DYNAMICAL ECLIPTIC AND EQUINOX OF THE DATE
     0      1      2     3          4      5   6           7  
  */
  private  Param paramFor(String line) {
    String power = slice(line, 60, 61);
    Integer index = Integer.valueOf(slice(line, 42, 43));
    Coord coord = Coord.valueFrom(index);
    return Param.valueOf(coord.name() + power);  
  }
  
  /* 
   4310    2  0  0  1  0  0  0  0  0  0  0  0  0 -0.00748171065    -0.03256824823     0.03341656456 4.66925680417    6283.07584999140
  */
  private PeriodicTerm periodicTermFor(String line) {
    PeriodicTerm result = new PeriodicTerm();
    //careful! make sure you get all of the data
    result.A = Double.valueOf(slice(line, 80,98)); //amplitude rads
    result.B = Double.valueOf(slice(line, 99,112)); //phase rads
    result.C = Double.valueOf(slice(line, 112,132)); //freq rads/millenia
    return result;
  }
  
  private void countTheTerms(Map<Param, List<PeriodicTerm>> periodicTerms, Planet planet) {
    int count = 0;
    for(Param param : periodicTerms.keySet()) {
      int contribution = periodicTerms.get(param).size();
      LogUtil.log("  " + param + ": " + contribution + " terms.");
      count = count + contribution;
    }
    LogUtil.log("  Total number of terms for " + planet + ": "  + count);
  }

  private String slice(String line, int start /*1-based*/, int end /*excluded*/){
    return line.substring(start-1, end).trim();
  }

}