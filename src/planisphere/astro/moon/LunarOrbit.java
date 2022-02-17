package planisphere.astro.moon;

import planisphere.astro.time.AstroUtil;
import planisphere.math.Maths;

/** 
 Simplified view of the Moon's orbit, sufficient to draw it on the planisphere.
 
 The Moon's orbit changes quite rapidly.
 In the course of a year, the longitude of its ascending node moves by about 20 degrees.
 This makes it hard to represent in on a planisphere.
*/
public final class LunarOrbit {

  public LunarOrbit(double jd) {
    double T = AstroUtil.julianCenturiesSinceJ2000(jd);
    
    //Meeus 1991, page 313 [Chapront-Touzé and Chapront, 1988, page 346]
    double longitudeNode = 125.044555 - 1934.1361849 * T + 0.0020762 * T*T + T*T*T/467410 - T*T*T*T/60616000; //degrees
    //Meeus 1991, page 132 [IAU, Astronomical Almanac 1984]
    //double longitudeNode = 125.04452 - 1934.136261 * T + 0.0020708 * T*T + T*T*T/450000;

    Ω = Maths.degToRads(longitudeNode); //rads
    Ω = Maths.in2pi(Ω);
  }

  /** The λ,β in radians for specific places in the Moon's orbit. */
  public EclipticCoords latLongCoordsFor(Place place) {
    EclipticCoords result = new EclipticCoords();
    if (Place.ASCENDING_NODE == place) {
      result.λ = Ω;
      result.β = 0;
    }
    else if (Place.DESCENDING_NODE == place) {
      result.λ = Maths.in2pi(Ω + Math.PI);
      result.β = 0;
    }
    else if (Place.HIGHEST_POINT == place) {
      result.λ = Maths.in2pi(Ω + Maths.HALF_PI);
      result.β = i;
    }
    return result;
  }
  
  /** Inclination of the plane of the orbit with respect to the ecliptic. Radians. */
  public double i() {
    return i;
  }
  
  /** Longitude of the mean ascending node, from the mean equinox of date. Radians. */
  public double Ω() {
   return Ω; 
  }

  /** Specific places on the lunar orbit that are easy to find, and can be used to draw the orbit. */
  public enum Place {
    ASCENDING_NODE, 
    DESCENDING_NODE,
    /** Rough analog of the summer solstice.  */
    HIGHEST_POINT; 
  }

  /** 
   Is there a better model I could use for this? Would be better if I could find an expression.
   Meeus Mathematical Astronomy Morsels has something for this, but I have no access to that book.
   (Apparently the range is 4.99-5.30 degrees.) 
  */
  private static final double i = Maths.degToRads(5.1454); //Explanatory Supplement 1961, page 107, constant of inclination
  private double Ω;
}
