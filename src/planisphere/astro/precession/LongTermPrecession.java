package planisphere.astro.precession;

import static planisphere.astro.time.AstroUtil.radsToDegreeString;
import static planisphere.astro.time.AstroUtil.radsToTimeString;

import planisphere.astro.star.Position;
import planisphere.astro.star.Star;
import planisphere.astro.time.AstroUtil;
import planisphere.astro.time.GregorianCal;
import planisphere.math.Maths;
import planisphere.math.Matrix;
import planisphere.math.Vector;
import planisphere.util.LogUtil;

/** 
 Precession algorithm suitable for both short and long time scales.
 
  <P>Ref: https://www.pecny.cz/cedr/download/LongPerPrec.pdf  (IAU long-term precession, 2006). 
  Paper Oct 2011 https://ui.adsabs.harvard.edu/abs/2011A%26A...534A..22V/abstract  "New precession expressions, valid for long time intervals"
  https://www.researchgate.net/profile/Patrick-Wallace-2  retired, UK
  https://www.researchgate.net/profile/Jan-Vondrak  Czech
    
  <p>Note that their solution is good for both long-term and short-term, since it's fitted to the IAU standard 
  in the range J2000 +/- 1000 years. 
  
  <P>Their model is valid only within the range of +/- 200,000 years from J2000. At the end of the span, the 
  results are good to less than a degree.
  
  <P>They avoid the traditional zeta_a, theta_a, z_a, because they have discontinuous jumps every 26,000 years or so.
  
  <P>The precession is represented completely by two pairs of parameters: P<sub>A</sub> and Q<sub>A</sub> for the ecliptic pole, 
  and X<sub>A</sub> and Y<sub>A</sub> for the equatorial pole. Those 4 params are treated as the "primary" parameters here, out 
  of which other representations of the precession may be created.
  
  <P>In table 8, periodic terms have the form C * cos(2piT/P) + S * sin(2piT/P).
  C and S are the amplitudes in arcseconds, and P is the period in centuries.
*/
public final class LongTermPrecession {
  
  /** Precess from a J2000 position to the target jd. */
  public Position apply(Position pos, double jd) {
    Vector before = XYZ.xyzFrom(pos);
    Matrix rot = rotationMatrix(jd);
    Vector after = rot.times(before);
    return XYZ.positionWithUnitDistance(after);
  }
  
  /** 
   For applying precession from J2000 to the target Julian date. Equation (23).
   The matrix is applied to equatorial rectangular coordinates.
   See Explanatory Supplement page 105. https://archive.org/details/explanatorysuppl00pken/page/104/mode/2up?view=theater
   See <a href='https://ui.adsabs.harvard.edu/abs/1977A%26A....58....1L/abstract'>Lieske et al 1977</a>. 
  */
  public Matrix rotationMatrix(double jd) {
    Vector k = eclipticNorthPole(jd);
    Vector n = equatorialNorthPole(jd);
    Vector w = n.cross(k).unit();
    return new Matrix(
      w,
      n.cross(w),
      n
    );
  }
  
  /**
   Return a unit vector that points to the north ecliptic pole for the given date, using J2000 mean equator and equinox.
   Computes P<sub>A</sub> and Q<sub>A</sub>, parameters that represent the direction of the secularly-moving ecliptic pole.
   @param jd should be in TT, terrestrial time, which involves ΔT from UT; I'm not implementing that here. 
  */
  public Vector eclipticNorthPole(double jd){
    double T = AstroUtil.julianCenturiesSinceJ2000(jd);
    double P = Maths.arcsecToRads(P(T)); //rads
    double Q = Maths.arcsecToRads(Q(T)); //rads
    
    double Z = Math.sqrt(Math.max(1 - P*P - Q*Q, 0));
    double S = Math.sin(ε0);
    double C = Math.cos(ε0);
    
    return new Vector(
      P, 
      -Q*C - Z*S, 
      -Q*S + Z*C
    );
  }
  
  /** Return the position of the north ecliptic pole for the given date, using J2000 mean equator and equinox. */
  public Position eclipticNorthPoleRaDec(double jd) {
    Vector v = eclipticNorthPole(jd);
    Position result = XYZ.positionWithUnitDistance(v);
    return result;
  }
 
  /**
   Return a unit vector that points to the north equatorial pole for the given date, using J2000 mean equator and equinox.
   Computes X<sub>A</sub> and Y<sub>A</sub>, parameters that represent the direction of the secularly-moving equatorial pole.
   @param jd should be in TT, terrestrial time, which involves ΔT from UT; I'm not implementing that here. 
  */
  Vector equatorialNorthPole(double jd){
    double T = AstroUtil.julianCenturiesSinceJ2000(jd);
    double X = Maths.arcsecToRads(X(T)); //rads
    double Y = Maths.arcsecToRads(Y(T)); //rads
    
    double W = X*X + Y*Y;
    double Z = W < 1.0 ? Math.sqrt(1.0 - W) : 0.0; 
    return new Vector(
      X, 
      Y, 
      Z
    );
  }
  
  /** Return the position of the north equatorial pole for the given date, using J2000 mean equator and equinox. */
  public Position equatorialNorthPoleRaDec(double jd) {
    Vector v = equatorialNorthPole(jd);
    Position result = XYZ.positionWithUnitDistance(v);
    return result;
  }
  
  /** Return p<sub>A</sub> in arcseconds. */
  public double generalPrecession(double jd) {
    double T = AstroUtil.julianCenturiesSinceJ2000(jd);
    double exponentialTerms =
      + 8134.017132 
      + 5043.0520035 *T
      - 0.00710733   *T*T 
      + 271E-9       *T*T*T
    ;
    return exponentialTerms + periodicTerms(Param.p, T);
  }
  
  /** Radians. */
  public double obliquity(double jd) {
    return Maths.arcsecToRads(obliquityArcseconds(jd));
  }

  /** Return ε<sub>A</sub> in arcseconds. */
  public double obliquityArcseconds(double jd) {
    double T = AstroUtil.julianCenturiesSinceJ2000(jd);
    double exponentialTerms =
      + 84028.206305 
      + 0.3624445   *T
      - 0.00004039  *T*T
      - 110E-9      *T*T*T
    ;
    return exponentialTerms + periodicTerms(Param.epsilon, T);
  }

  /** Obliquity at J2000.0 (radians). */
  private static final double ε0 = Maths.arcsecToRads(84381.406);
  
  /** Returns arcseconds. */
  private double P(double T) {
    double exponentialTerms = 
      + 5851.607687 
      -    0.1189000  *T 
      -    0.00028913 *T*T 
      +    (101e-9)   *T*T*T
    ;
    return exponentialTerms + periodicTerms(Param.P, T);
  }
  /** Returns arcseconds. */
  private double Q(double T) {
    double exponentialTerms =
      - 1600.886300 
      +    1.1689818  *T 
      -    0.00000020 *T*T
      -    437E-9     *T*T*T
    ;
    return exponentialTerms + periodicTerms(Param.Q, T);
  }
  
  /** Returns arcseconds. */
  private double X(double T) {
    double exponentialTerms =
      + 5453.282155 
      + 0.4252841  *T
      - 0.00037173 *T*T
      - 152E-9     *T*T*T
    ;
    return exponentialTerms + periodicTerms(Param.X, T);
  }
  /** Returns arcseconds. */
  private double Y(double T) {
    double exponentialTerms =
     - 73750.930350
     - 0.7675452   *T
     - 0.00018725  *T*T
     + 231E-9      *T*T*T
    ;
    return exponentialTerms + periodicTerms(Param.Y, T);
  }
  
  private double periodicTerms(Param param, double T) {
    double result = 0.0; 
    for (PeriodicTerm periodicTerm : PrecessionDataLoader.periodicTerms().get(param)) {
      result = result + periodicTerm.contributionToSum(T); // arcseconds
    }
    return result;
  }
  
  /** Informal test harness. */
  public static void main(String... args) {
    LongTermPrecession p = new LongTermPrecession();
    //  1374 (i.e. 1375 BCE) May 3 (Gregorian calendar) at 13:52:19.2 TT
    double jd = GregorianCal.jdForLocal(-1374, 5, 3, 13, 52, 19, 200_000_000, 0, 0); // 1219339.078000
    LogUtil.log(jd + " JD for -1374 (1375 BC) May 3 at  13:52:19.2, Gregorian proleptic calendar.");
    
    // should be pecl = ( +0.0004 1724 7857 6400 1342 −0.4049 5491 1045 7616 2693 +0.9143 3656 0531 2655 2350 ) 
    Vector eclipticPole = p.eclipticNorthPole(jd);
    LogUtil.log("Ecliptic pole: " + eclipticPole);
    
    // should be pequ = = ( −0.2943 7643 7973 6903 1532 −0.1171 9098 0233 7025 7855 +0.9484 7708 8240 8209 1796 ) 
    Vector equatorialPole = p.equatorialNorthPole(jd);
    LogUtil.log("Equatorial pole: " + equatorialPole);
    
    Matrix rotatioMatrix = p.rotationMatrix(jd);
    LogUtil.log(rotatioMatrix);
    
    //do the example found in Meeus page 127
    jd = GregorianCal.jd(2028,  11, 13.19); //2462088.69
    Star star = new Star();
    star.RA = Maths.degToRads(41.054063); // after proper motion applied
    star.DEC = Maths.degToRads(49.227750);
    Position pos = p.apply(star.position(), jd);
    LogUtil.log("Precessed position of theta Persei, ra:" + radsToTimeString(pos.α) + " dec:" + radsToDegreeString(pos.δ));
    
    testObliquity();
  }
  
  private static void testObliquity() {
    LogUtil.log("Test obliquity with the Gregorian proleptic calendar.");
    testObliquity(2000);
    testObliquity(1750);
    testObliquity(1500);
    testObliquity(1000);
    testObliquity(500);
    testObliquity(0);
    testObliquity(-500);
    testObliquity(-1000);
    testObliquity(-2000);
    testObliquity(-5000);
    testObliquity(-6000);
    testObliquity(-7000);
    testObliquity(-8000);
    testObliquity(-9000);
    testObliquity(-10000);
    testObliquity(-13000);
  }
  private static void testObliquity(int year) {
    LongTermPrecession p = new LongTermPrecession();
    double jd = GregorianCal.jd(year, 1, 0.0);
    double val = p.obliquityArcseconds(jd);
    val = Maths.arcsecToRads(val);
    LogUtil.log("  ε " + year + "-01-01: " + AstroUtil.radsToDegreeString(val));
  }
}
