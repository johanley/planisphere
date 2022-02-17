package planisphere.astro.planets;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.atan;
import static planisphere.math.Maths.atan3;
import static planisphere.math.Maths.in2pi;
import static planisphere.math.Maths.radsToDegs;
import static planisphere.math.Maths.sqr;

import java.util.List;
import java.util.Map;

import planisphere.astro.moon.EclipticCoords;
import planisphere.astro.star.Position;
import planisphere.astro.time.AstroUtil;
import planisphere.astro.time.GregorianCal;
import planisphere.util.LogUtil;

/** 
 The geometric position of a planet at a given time.
 
 <P>Geometric position, with no correction made for aberration, nutation, or light-time.
  
 <P>Only the bright planets are included. Uranus and Neptune are excluded.
 This implementation uses VSOP87D, which has formulae for the LBR heliocentric ecliptical coordinates of the planet,  
 referred to the mean ecliptic and equinox of date. A simple transformation yields the geocentric position.

 <P>The max error is about 1 arcsecond in the timespan of J2000 +/- 4,000 years (-2000..+6000).

 <P>
 <a href='https://ui.adsabs.harvard.edu/abs/1988A&A...202..309B/abstract'>Planetary Theories in rectangular and spherical variables: VSOP87 solution.</a>
 Bretagnon P., Francou G. Astron. Astrophys. 202, 309 (1988).

 <P>Ref: https://github.com/ctdk/vsop87/
 <P>Ref: https://www.caglow.com/info/compute/vsop87
*/
public final class PlanetPosition {

  public PlanetPosition(Planet planet) {
    PlanetPositionDataLoader loader = new PlanetPositionDataLoader();
    this.planet = planet;
    this.periodicTerms = loader.periodicTermsForThe(planet);
  }
  
  public LBR lbr(double jd) {
    LBR result = new LBR();
    double τ = AstroUtil.julianMilleniaSinceJ2000(jd);
    result.L = L(τ);
    result.B = B(τ);
    result.R = R(τ);
    return result;
  }

  /** Called only if the planet passed to the constructor is NOT the Earth. */
  public Position position(double jd) {
    if (planet == Planet.Earth) {
      throw new RuntimeException("Can't pass planet Earth for this method.");
    }
    
    LBR planet = lbr(jd);
    if (earthHelio == null) {
      earthHelio = new PlanetPosition(Planet.Earth);
    }
    LBR earth = earthHelio.lbr(jd);
    double x = x_c(planet) - x_c(earth);
    double y = y_c(planet) - y_c(earth);
    double z = z_c(planet) - z_c(earth);
    double λ = atan3(y, x); //0..2pi
    double β = atan(z / (Math.sqrt(sqr(x) + sqr(y)))); // -pi/2..+pi/2
    EclipticCoords ecl = new EclipticCoords(λ, β);
    return ecl.toRaDec(jd);
  }
  
  /** Heliocentric mean ecliptic longitude L of the planet, in radians 0..2pi. */
  private double L(double τ) {
    return in2pi(coord(τ, Coord.L));
  }
  
  /** Heliocentric mean ecliptic latitude B of the planet, in radians, -pi..+pi. */
  private double B(double τ) {
    return coord(τ, Coord.B);
  }
  
  /** Heliocentric mean radius vector R of the planet, in AU. */
  private double R(double τ) {
    return coord(τ, Coord.R);
  }

  private Map<Param, List<PeriodicTerm>> periodicTerms;
  private Planet planet;
  private PlanetPosition earthHelio;
  
  private double coord(double τ, Coord coord) {
    double result = 0.0;
    for(Param param : Param.values()) { //not all of these will be in the data! 
      //filter-in params for the given coord
      if (param.name().startsWith(coord.name())) {
        if (periodicTerms.get(param) != null) { //not all of these will be in the data!
          double sum = 0.0;
          for(PeriodicTerm pt : periodicTerms.get(param)) {
            sum = sum + pt.contributionToSum(τ); 
          }
          result = result + sum * Math.pow(τ, param.power()); 
        }
      }
    }
    return result; 
  }
  
  private double x_c(LBR lbr) {
    return lbr.R * cos (lbr.B) * cos (lbr.L);
  }
  
  private double y_c(LBR lbr) {
    return lbr.R * cos (lbr.B) * sin (lbr.L);
  }
  
  private double z_c(LBR lbr) {
    return lbr.R *  sin (lbr.B);
  }
  
  /** Informal test. */
  public static void main(String... args) {
    //Meeus 1991, page 207; Meeus has an abridged version, with fewer terms, so it's not exactly the same
    double jd = GregorianCal.jd(1992, 12, 20.0);
    PlanetPosition venus = new PlanetPosition(Planet.Venus);
    LBR lbr = venus.lbr(jd);
    LogUtil.log("L: " + radsToDegs(lbr.L) + " degs.");
    LogUtil.log("B: " + radsToDegs(lbr.B) + " degs.");
    LogUtil.log("R: " + lbr.R + " AU.");
    
    LogUtil.log("Position: " + venus.position(jd));
  }
  
}