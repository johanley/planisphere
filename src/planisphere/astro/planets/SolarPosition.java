package planisphere.astro.planets;

import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import planisphere.astro.precession.LongTermPrecession;
import planisphere.astro.precession.Nutation;
import planisphere.astro.star.Position;
import planisphere.astro.time.AstroUtil;
import planisphere.astro.time.GregorianCal;
import planisphere.math.Maths;
import planisphere.util.LogUtil;

/** 
 The position of the Sun at a given time.
 This implementation uses VSOP87D, which has formulae for the LBR heliocentric spherical coordinates of the Earth, 
 referred to the mean ecliptic and equinox of date. 
 A simple transformation yields the geocentric position of the Sun.
 
 <P>The max error is about 1 arcsecond in the timespan of J2000 +/- 4,000 years (-2000..+6000).
 
 <P>Only the longitude of the Sun along the ecliptic of date is found by this class.
 The latitude is approximated as 0 arcseconds, and the distance is not of interest 
 in this context.
 
 <P>This class can apply nutation and aberration, to get the apparent position of the Sun.
 
 <P>
 <a href='https://ui.adsabs.harvard.edu/abs/1988A&A...202..309B/abstract'>Planetary Theories in rectangular and spherical variables: VSOP87 solution.</a>
  Bretagnon P., Francou G. Astron. Astrophys. 202, 309 (1988).

 <P>Ref: https://github.com/ctdk/vsop87/blob/master/VSOP87D.ear
 <P>Ref: https://www.caglow.com/info/compute/vsop87
*/
public final class SolarPosition {
  
  /** Return the apparent RA and DEC (rads) of the Sun for the given moment and obliquity. */
  public Position apparentPosition(double jd, double ε) {
    double λ = apparentλ(jd);
    Position pos = new Position();
    pos.α = Maths.atan3(sin(λ) * cos(ε), cos(λ));
    pos.δ = asin(sin(ε) * sin(λ));
    return pos;
  }

  /** Radians. With nutation and aberration. */
  public double apparentλ(double jd) {
    double λ = geometricλ(jd);
    Nutation nutation = new Nutation(jd);
    return Maths.in2pi(λ + nutation.Δψ() + aberration()); 
  }
  
  /** Radians. */
  public double geometricλ(double jd) {
    double L = meanLEarth(jd);
    return Maths.in2pi(L + Math.PI);  
  }
  
  private PlanetPosition earth;

  /** Mean longitude L of the Earth, in radians. Heliocentric. The core calculation. */
  private double meanLEarth(double jd) {
    if (earth == null) {
      earth = new PlanetPosition(Planet.Earth);
    }
    return earth.lbr(jd).L;
  }
  
  /** 
   The aberration of the Sun is approximately constant; for a planisphere, this is adequate.
   Ref: Meeus p155.
  */
  private double aberration() {
    return Maths.arcsecToRads(-20.4898);
  }
  
  /** Informal test harness. */
  public static void main(String... args) {
    double jd = GregorianCal.jd(1992, 10, 13.0);
    SolarPosition sun = new SolarPosition();
    LogUtil.log("1992-10-13.0 Julian date: " + jd + " τ:" + AstroUtil.julianMilleniaSinceJ2000(jd));
    LogUtil.log("Mean heliocentric λ of Earth: " + Maths.radsToDegs(sun.meanLEarth(jd))); // L 
    LogUtil.log("Mean geocentric λ of Sun: " + Maths.radsToDegs(sun.geometricλ(jd)));
    LogUtil.log("Apparent geocentric λ of Sun : " + AstroUtil.radsToDegreeString(sun.apparentλ(jd)) + " , with nutation and aberration.");
    
    LongTermPrecession precession = new LongTermPrecession();
    double ε = precession.obliquity(jd); //rads
    Position pos = sun.apparentPosition(jd, ε);
    LogUtil.log("Equatorial coords: α:" + AstroUtil.radsToTimeString(pos.α) + " δ:" + AstroUtil.radsToDegreeString(pos.δ));
  }
}