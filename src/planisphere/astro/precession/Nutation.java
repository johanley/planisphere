package planisphere.astro.precession;

import planisphere.astro.time.AstroUtil;
import planisphere.astro.time.GregorianCal;
import planisphere.math.Maths;
import planisphere.util.LogUtil;

import static java.lang.Math.*;

/**
 Low-precision model of nutation.
 
 <P>Nutation is applied after precession.
  
 <P>In a planisphere, in which the target accuracy for calculations is 1 minute of arc, 
 the inclusion of nutation may not even be necessary. (But it can't hurt, and it eliminates it as a source of error.)
 
 <p>Nutation is both small (less than a minute of arc) and periodic. 
 Nutation doesn't accumulate over time.
 Neglecting it would not lead to growing errors over millenial time scales.
 
 <P>The time scale over which is this class is valid is the same as the 
 time scale over which its expressions for Ω (the longitude of the Moon's ascending node for its mean orbit) and 
 L (the mean longitude of the Sun) are valid. That time scale is not stated in the reference. 
 
 <P>This model is a very simple model of nutation, with only the largest two terms retained. 
  This results in errors on the order of 0.5 arcseconds (in modern times).

  <P>Ref: Astronomical Algorithms, Meeus 1991.
*/
public final class Nutation {

  /** This model approximates ΔT as being 0. */
  public Nutation(double jde) {
    this.T= AstroUtil.julianCenturiesSinceJ2000(jde);
    if (T > 50) {
      LogUtil.warn("The nutation model may not be reliable over extreme time scales.");
    }
  }
  
  /** Nutation in longitude. Return rads. */
  public double Δψ() {
    double result = -17.20 * sin(Ω(T)) - 1.32 * sin(2*L(T)); //arcseconds
    return Maths.arcsecToRads(result);
  }

  /** Nutation in obliquity. Return rads. */
  public double Δε() {
    double result = 9.20 * cos(Ω(T)) + 0.57 * cos(2*L(T)); //arcseconds
    return Maths.arcsecToRads(result);
  }
  
  private double T;
  
  /** Return rads. */
  private double Ω(double T) {
    double result = 125.04452 - 1934.136261*T + 0.0020708*T*T; //degs
    return Maths.degToRads(result);
  }
  
  /** Return rads. */
  private double L(double T) {
    double result = 280.4665 + 36000.7698*T; //degs
    return Maths.degToRads(result);
  }

  /** Informal test harness. */
  public static void main(String... args) {
    double jd = GregorianCal.jd(1987, 4, 10.0);
    Nutation nutation = new Nutation(jd);
    LogUtil.log("1987-04-10 nutation Δψ: " + AstroUtil.radsToDegreeString(nutation.Δψ()));  // -3.788", off by 0.333"
    LogUtil.log("1987-04-10 nutation Δε: " + AstroUtil.radsToDegreeString(nutation.Δε()));  // +9.443", off by 0.045"
  }
}
