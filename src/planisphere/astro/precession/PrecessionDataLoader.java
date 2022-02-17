package planisphere.astro.precession;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import planisphere.util.DataFileReader;
import planisphere.util.LogUtil;

/** 
 Load periodic terms upon startup from text data files.
 The text files are in the same directory as this class.
 The file names follow a brittle naming convention that helps in defining the data structure.
 
 <P>The file content is copy-pasted directly from the underlying paper that defines the algorithm, with minimal changes.
 (Minus sign characters from the PDF file are changed to be acceptable to Java.)
 
  <P>To add another pair of precession parameters:
  <ul>
   <li>add a pair to {@link Param}
   <li>edit the <code>fileNames</code> method of this class
   <li>following the naming convention for the file name
   <li>supply a text file with the data for the periodic terms
  </ul>
*/
final class PrecessionDataLoader {

  /** Executed once automatically when this class loads. */
  static {
    LogUtil.log("Populating precession data for periodic terms.");
    readPeriodicTermData();
  }
  
  /** Return a data structure to the caller containing all periodic terms for all parameters defined in the {@link Param} enumeration. */
  static Map<Param, List<PeriodicTerm>> periodicTerms() {
    return PERIODIC_TERMS;
  }
  
  // PRIVATE 
  
  private static Map<Param, List<PeriodicTerm>> PERIODIC_TERMS;

  /** Mandatory file naming convention. 'P_and_Q.utf8', for example. */
  private static final String SEPARATOR = "_and_";
  private static final String EXTENSION = ".utf8";
  private static String fileName(Param a, Param b) {
    return a.toString() + SEPARATOR + b.toString() + EXTENSION;
  }
  private static String fileCorrespondingToThis(Param p) {
    String result = "";
    for (String fileName : fileNames()) {
      if (fileName.contains(p.toString())) {
        result = fileName;
        break;
      }
    }
    if (result.length() == 0) {
      throw new RuntimeException("Can't find a file corresponding to this precession parameter " + p.toString());
    }
    return result;
  }
  /** 
   I do this hard-coding because I don't know how to get a file listing in the native dir.
   I could try using a path beneath user.dir, but that might not work in all env's.
   So I'm being conservative by coding the file names explicitly.
  */
  private static List<String> fileNames(){
    List<String> result = new ArrayList<>();
    //this implements the 'pairing' of parameters together
    result.add(fileName(Param.P, Param.Q)); 
    result.add(fileName(Param.X, Param.Y));
    result.add(fileName(Param.p, Param.epsilon));
    return result;
  }

  private static final int AMPLITUDE_COLUMN = 2;
  private static final int PERIOD_COLUMN = 4;
  
  /** 
   Read in tables containing periodic terms.
   <P>Convention: the file name reflects the column order of the two params. 
  */
  private static void readPeriodicTermData() {
    PERIODIC_TERMS = new LinkedHashMap<Param, List<PeriodicTerm>>();
    for(Param param : Param.values()) {
      List<PeriodicTerm> periodicTerms = new ArrayList<>();
      String fileName = fileCorrespondingToThis(param);
      boolean useFirstCol = isFirstColumn(fileName, param);
      DataFileReader reader = new DataFileReader();
      List<String> lines = reader.readFile(PrecessionDataLoader.class, fileName);
      //process pairs of lines, two at a time
      PeriodicTerm periodicTerm = null;
      for(String line : lines) {
        if ( isComment(line)) {
          //skip it
        }
        else if (isCosine(line)) {
          //start a new term
          periodicTerm = new PeriodicTerm(); 
          periodicTerm.P = chop(line, PERIOD_COLUMN); 
          periodicTerm.C = chop(line, AMPLITUDE_COLUMN, useFirstCol);
        }
        else {
          //finish an existing term 
          periodicTerm.S = chop(line, AMPLITUDE_COLUMN, useFirstCol);
          periodicTerms.add(periodicTerm); //finished
        }
      }
      PERIODIC_TERMS.put(param, periodicTerms);
    }
  }
  
  private static Double chop(String line, int col, boolean useFirstCol) {
    int idx = useFirstCol ? col : col + 1;
    return chop(line, idx);
  }
  
  private static Double chop(String line, int col) {
    String[] parts = line.trim().split(" ");
    return Double.valueOf(parts[col - 1]);
  }

  /**
   Convention: the order of params in the file name maps to the column location in the file of the param. 
   This will fail if the naming convention is not followed. Case sensitive!
  */
  private static boolean isFirstColumn(String fileName, Param p) {
    return fileName.startsWith(p.toString());
  }
  
  private static boolean isCosine(String line) {
    return line.trim().startsWith("C");
  }
  
  private static boolean isComment(String line) {
    return line.trim().startsWith(DataFileReader.COMMENT);
  }
}
