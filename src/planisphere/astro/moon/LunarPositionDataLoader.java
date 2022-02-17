package planisphere.astro.moon;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import planisphere.util.DataFileReader;
import planisphere.util.LogUtil;

/** 
 Load ELP2000-82 data for the Moon, from a file in the native directory.
 This data is used to calculate the geocentric position of the Moon. 
*/
final class LunarPositionDataLoader {

  /** Return a data structure to the caller containing all periodic terms for the Moon's λ. */
  static List<PeriodicTerm> periodicTermsλ() {
    return PERIODIC_TERMS_λ;
  }

  /** Return a data structure to the caller containing all periodic terms for the Moon's β. */
  static List<PeriodicTerm> periodicTermsβ() {
    return PERIODIC_TERMS_β;
  }
  
  // PRIVATE 
  
  private static List<PeriodicTerm> PERIODIC_TERMS_λ;
  private static List<PeriodicTerm> PERIODIC_TERMS_β;
  private static final String FILE_NAME_λ = "elp-2000-82-long-r.utf8";
  private static final String FILE_NAME_β = "elp-2000-82-lat.utf8";
  
  private static List<PeriodicTerm> readPeriodicTermData(String fileName) {
    List<PeriodicTerm> result = new ArrayList<PeriodicTerm>();
    DataFileReader reader = new DataFileReader();
    List<String> lines = reader.readFile(LunarPositionDataLoader.class, fileName);
    for(String line : lines) {
      if (!line.isBlank()) {
        result.add(periodicTermFor(line));
      }
    }
    return result;
  }
  
  /*  0,0,1,0,6288774,-20905355     */
  private static PeriodicTerm periodicTermFor(String line) {
    PeriodicTerm result = new PeriodicTerm();
    String[] parts = line.split(Pattern.quote(","));
    result.D = intFrom(parts, 0); 
    result.M = intFrom(parts, 1); 
    result.Mp = intFrom(parts, 2); 
    result.F = intFrom(parts, 3); 
    result.amplitude = dblFrom(parts,4);
    //part 5 is ignored here: the distance 'r' is not implemented
    return result;
  }
  
  private static Integer intFrom(String[] parts, int idx) {
    return Integer.valueOf(parts[idx].trim()); 
  }
  
  private static Double dblFrom(String[] parts, int idx) {
    return Double.valueOf(parts[idx].trim()); 
  }
  
  private static void countTheTerms() {
    LogUtil.log("Number of terms in λ:  " + PERIODIC_TERMS_λ.size() + ", and β:" + PERIODIC_TERMS_β.size());
  }

  /** Executed once automatically when this class loads. */
  static {
    LogUtil.log("Populating ELP 2000-82 data for the Moon's periodic terms (without distance r).");
    PERIODIC_TERMS_λ = readPeriodicTermData(FILE_NAME_λ);
    PERIODIC_TERMS_β = readPeriodicTermData(FILE_NAME_β);
    countTheTerms();
  }
}