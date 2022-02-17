package planisphere.astro.precession;

import static planisphere.astro.time.AstroUtil.julianCenturiesSinceJ2000;
import static planisphere.astro.time.AstroUtil.radsToDegreeString;
import static planisphere.astro.time.AstroUtil.radsToTimeString;

import planisphere.astro.star.Position;
import planisphere.astro.star.Star;
import planisphere.astro.time.GregorianCal;
import planisphere.math.Maths;
import planisphere.util.LogUtil;

/** 
 Apply precession to a J2000 position using algos valid only for a few centuries.
 NOT USED. FOR COMPARISON PURPOSES ONLY.
 
 <P>Always apply proper motion before applying precession.
 
 Uses <a href='https://www.aanda.org/articles/aa/pdf/2003/48/aa4068.pdf'>Capitaine 2003</a> (40).  
 It refers to Lieske 1977 (below), using A subscripts: eq (7), whereas Meeus has no A subscript for the same quantities.
 <a href='https://ascl.net/1202.003'>NOVAS</a> uses (37) and (39) from Capitaine 2003, which use a different set of variables.
 The <a href='https://archive.org/details/astronomicalalma0000unse_s2r1/page/n163/mode/2up?q=capitaine&view=theater'>Astronmical Almanac</a>
 also uses Capitaine 2003.
     
 <P>Also note <a href='https://ui.adsabs.harvard.edu/abs/1977A%26A....58....1L/abstract'>Lieske 1977</a>  Table 5, page 15.
 This is used by <em>Astronomical Algorithms</em> (1991) (20.3), by Jean Meeus.
 
 <P>https://owd.tcnj.edu/~pfeiffer/AST261/AST261Chap4,Preces.pdf 
 "For up to a few centuries in the past and the future, all formulas do not diverge very much. For up to a few thousand years in the past and the future, most
  agree to some accuracy. For eras farther out, discrepancies become too large — the exact rate and period of
  precession may not be computed using these polynomials even for a single whole precession period."
  
  <P>https://marcomaggi.github.io/docs/iausofa.html  SOFA documentation:
  The accumulated precession angles zeta, z, theta are expressed through canonical polynomials which are valid only for a limited time span. 
  In addition, the IAU 1976 precession rate is known to be imperfect. The absolute accuracy of the present formulation is better 
  than 0.1 arcsec from 1960AD to 2040AD, better than 1 arcsec from 1640AD to 2360AD, and remains below 3 arcsec for the whole of 
  the period 500BC to 3000AD. The errors exceed 10 arcsec outside the range 1200BC to 3900AD, exceed 100 arcsec outside 4200BC to 5600AD 
  and exceed 1000 arcsec outside 6800BC to 8200AD.
  
  <p>Lieske, J.H., 1979, Astron.Astrophys. 73, 282, equations (6) & (7), p283.
  
  <P>***** https://www.pecny.cz/cedr/download/LongPerPrec.pdf  (IAU long-term precession, 2006). 
  Interesting that they were working on the problem! Was there any result? Capitaine, Wallace, Vondrak. 
  Paper Oct 2011 https://ui.adsabs.harvard.edu/abs/2011A%26A...534A..22V/abstract  "New precession expressions, valid for long time intervals"
*/
public final class PrecessionOLD {
  
  public PrecessionOLD(Double jd) {
    this.jd = jd;
    this.angles = capitaine2003();
  }
  
  public Double jd() {
    return jd;
  }
  
  public Angles angles() {
    return angles;
  }
  
  @Override public String toString() {
    return 
      "Approx total: " + approximateTotalPrecession() + 
      " zeta:" + fmt(angles.zeta) + " z:" + fmt(angles.z) + " theta:" + fmt(angles.theta)
    ;
  }
  
  /** Changes the star's data in place. */
  void applyTo(Star star) {
    Position pos = new Position(star.RA, star.DEC);
    Position newPos = apply(pos);
    star.RA = newPos.α;
    star.DEC = newPos.δ;
  }

  Position apply(Position pos) {
    Position result = new Position();
    double A = Math.cos(pos.δ) * Math.sin(pos.α + angles.zeta);
    double B = Math.cos(angles.theta) * Math.cos(pos.δ) * Math.cos(pos.α + angles.zeta) - Math.sin(angles.theta) * Math.sin(pos.δ);
    double C = Math.sin(angles.theta) * Math.cos(pos.δ) * Math.cos(pos.α + angles.zeta) + Math.cos(angles.theta) * Math.sin(pos.δ);

    result.α = Maths.in2pi(Math.atan2(A, B) + angles.z); // 0..2pi
    
    if (Math.abs(pos.δ) < Maths.degToRads(85)){
      result.δ = Math.asin(C); //-pi/2..+pi/2
    }
    else {
      result.δ = Math.acos(Math.sqrt(A*A + B*B)); //0..pi, but always near 90 deg
      /*
      double temp = Math.acos(Math.sqrt(A*A + B*B)); //0..pi
      result.δ = Math.sin(pos.δ) * temp;
      */
    }
    return result;
  }
  
  public static class Angles {
    double zeta;
    double z;
    double theta;
  }
  
  // PRIVATE 
  
  /** The Julian Date of the target date. */
  private double jd;
  private Angles angles;
  
  /** Julian centuries. */
  private double t() {
    return julianCenturiesSinceJ2000(jd);
  }
  
  /** 
   Capitaine 2003. 
   Note the presence of the two constant terms, with the same value but opposite sign.
   It's a bit strange that they are present, but the result seems fine.  
  */
  private Angles capitaine2003() {
    Angles result = new Angles();
    double t = t();
    double t2 = t*t;
    double t3 = t*t*t;
    double t4 = t*t*t*t;
    double t5 = t*t*t*t*t;
    result.zeta =  secondsToRads(2.650545 + 2306.083227*t + 0.2988499*t2 + 0.01801828*t3 - 0.000005971*t4 - 0.0000003173*t5);
    result.z =    secondsToRads(-2.650545 + 2306.077181*t + 1.0927348*t2 + 0.01826837*t3 - 0.000028596*t4 - 0.0000002904*t5);
    result.theta =  secondsToRads(          2004.191903*t - 0.4294934*t2 - 0.04182264*t3 - 0.000007089*t4 - 0.0000001274*t5);
    return result;
  }
 
  /** Not used. Provided for developer comparison. */
  private Angles captaine2003NoConstants() {
    Angles result = new Angles();
    double t = t();
    double t2 = t*t;
    double t3 = t*t*t;
    double t4 = t*t*t*t;
    double t5 = t*t*t*t*t;
    result.zeta =  secondsToRads(2306.083227*t + 0.2988499*t2 + 0.01801828*t3 - 0.000005971*t4 - 0.0000003173*t5);
    result.z =    secondsToRads(2306.077181*t + 1.0927348*t2 + 0.01826837*t3 - 0.000028596*t4 - 0.0000002904*t5);
    result.theta =  secondsToRads(          2004.191903*t - 0.4294934*t2 - 0.04182264*t3 - 0.000007089*t4 - 0.0000001274*t5);
    return result;
  }
  
  /** Lieske 1977 and Mees 1991 (20.3). Not used. Provided for reference. */
  private Angles lieske1977() {
    Angles result = new Angles();
    double t = t();
    result.zeta = secondsToRads(2306.2181*t + 0.30188*t*t + 0.017998*t*t*t);
    result.z = secondsToRads(2306.2181*t + 1.09468*t*t + 0.018203*t*t*t);
    result.theta = secondsToRads(2004.3109*t - 0.42665*t*t - 0.041833*t*t*t);
    return result;
  }
  
  private double secondsToRads(Double arcsec) {
    return Maths.degToRads(arcsec / 3600.0);
  }
  
  private String fmt(double rads) {
    return radsToDegreeString(rads);
  }
  
  private String approximateTotalPrecession() {
    double julCenturies = t();
    double arcseconds = 5028.796165 * julCenturies; //Astronomical Almanac 2017, page K7, arcseconds per Julian Century
    double degrees = arcseconds / 3600.0;
    return fmt(Maths.degToRads(degrees));
  }
  
  /** 
   Informal test harness.
   Over long time spans, the presence of the constant term in Capitaine seems to have no cumulative effect.
   After 7000 years into the past, Lieske/Meeus starts to differ significantly from Capitaine.
   <code>
   Theta Persei (after Meeus, page 127).
   2028-11-13.9
    Capitaine  +2h46m11.325s  +49°20'54.508''
    Capitaine- +2h46m11.324s  +49°20'54.513'' without constant terms
    Meeus      +2h46m11.331s  +49°20'54.539''
   1000-01-01   
    Capitaine  +1h40m06.292s  +44°33'25.722''
    Capitaine- +1h40m06.307s  +44°33'25.588''
    Meeus      +1h40m06.158s  +44°33'24.823''
   -5000-01-01
    Capitaine  +20h46m00.297s  +13°45'18.22'' 
    Capitaine- +20h46m00.355s  +13°45'18.31''  
    Meeus      +20h45m58.197s  +13°44'56.587''
   -9000-01-01
    Capitaine  +17h40m17.448s  +06°56'59.788''
    Capitaine- +17h40m17.518s  +06°57'00.499''
    Meeus      +17h37m07.297s  +07°00'53.77''
   -13000-01-01
    Capitaine  +14h28m44.456s +19°18'49.693'
    Capitaine- +14h28m44.543s +19°18'50.867''
    Meeus      +14h10m42.422s +20°43'06.194''
   </code> 
  */
  public static void main(String... args) {
    //Meeus, page 127.
    //double jd = GregorianCal.jd(2028,  11, 13.19); //2462088.69
    
    //past dates
    double jd = GregorianCal.jd(-9000, 1, 1.0);
    
    PrecessionOLD precession = new PrecessionOLD(jd);
    Angles angles = precession.capitaine2003();
    //                    +0.1849341                 +0.1859524                   +0.1607080   
    LogUtil.log("zeta:" + degs(angles.zeta) + " z:" + degs(angles.z) + " theta:" + degs(angles.theta) );
    
    Star star = new Star();
    star.RA = Maths.degToRads(41.054063); // after proper motion applied
    star.DEC = Maths.degToRads(49.227750);
    
    precession.applyTo(star);
    LogUtil.log("ra:" + radsToTimeString(star.RA) + " dec:" + radsToDegreeString(star.DEC));
  }
  
  private static double degs(double rads) {
    return Maths.radsToDegs(rads);
  }
}