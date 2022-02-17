package planisphere.astro.moon;

import java.util.List;

import planisphere.astro.star.Position;
import planisphere.astro.time.AstroUtil;
import planisphere.astro.time.GregorianCal;
import planisphere.math.Maths;
import planisphere.util.LogUtil;

import static planisphere.math.Maths.*;

import static java.lang.Math.*;

/** 
 The position of the Moon with respect to the mean equator and equinox of date.
 Uses the abridged version of ELP 2000 - 82 published in Meeus 1991, page 307.
 The approximate accuracy is 10'' in longitude and 4'' in latitude.
 
 <P>A better version, if I could find it, would be ELP 2000 - 85, which is built 
 for historical calculations. An abridged version of that theory is published in 
 <a href='https://ui.adsabs.harvard.edu/abs/1988A%26A...190..342C/abstract'>the original paper</a>. 
 but I don't understand the paper. 
 It's also available in <a href='https://ui.adsabs.harvard.edu/abs/1991ltpf.book.....C/abstract'>book form</a>, 
 but I can't find or view the book anywhere. 
*/
public final class LunarPosition {

  /** The position of the Moon with respect to the mean equator and equinox of date, in spherical ecliptic coords. */
  public EclipticCoords λβ(double jd) {
    //remember: the amplitudes are in degrees, and the rest are in rads
    double T = AstroUtil.julianCenturiesSinceJ2000(jd);
    Angles a = angles(T); //rads
    double λ = contributionsFrom(LunarPositionDataLoader.periodicTermsλ(), a, T); //degrees
    λ = λ + 3958 * sin(a.A1) + 1962*sin(a.Lp - a.F) + 318 * sin(a.A2); //degrees
    λ = a.Lp + degToRads(λ/1_000_000.0); //rads
    
    double β = contributionsFrom(LunarPositionDataLoader.periodicTermsβ(), a, T); //degrees
    β = β - 2235*sin(a.Lp) + 382*sin(a.A3) + 175*sin(a.A1 - a.F) + 175*sin(a.A1 + a.F) + 127*sin(a.Lp - a.Mp) - 115*sin(a.Lp + a.Mp); //degrees
    β = degToRads(β/1_000_000.0); //rads
    
    EclipticCoords result = new EclipticCoords(λ, β);
    return result;
  }
  
  /** The position of the Moon with respect to the mean equator and equinox of date, in spherical equatorial coords. */
  public Position position(double jd) {
    return λβ(jd).toRaDec(jd);
  }
  
  /** Radians, in range 0..2pi. */
  private Angles angles(double T) {
    Angles result = new Angles();
    result.Lp = degToRads(in360(218.316_4591 + 481_267.881_34236 * T - 0.001_3268 *T*T + T*T*T/538_841.0   - T*T*T*T/65_194_000.0)); 
    result.D =  degToRads(in360(297.850_2042 + 445_267.111_5168  * T - 0.001_6300 *T*T + T*T*T/545_868.0   - T*T*T*T/113_065_000.0));
    result.M =  degToRads(in360(357.529_1092 +  35_999.050_2909  * T - 0.000_1536 *T*T + T*T*T/24_490_000.0                     ));
    result.Mp = degToRads(in360(134.963_4114 + 477_198.867_6313  * T + 0.008_9970 *T*T + T*T*T/69_699.0    - T*T*T*T/14_712_000.0));
    result.F =  degToRads(in360( 93.272_0993 + 483_202.017_5273  * T - 0.003_4029 *T*T - T*T*T/3_526_000.0 + T*T*T*T/863_310_000.0));
    
    result.A1 = degToRads(in360(119.75 +     131.849 * T));
    result.A2 = degToRads(in360(53.09  + 479_264.290 * T));
    result.A3 = degToRads(in360(313.45 + 481_266.484 * T));
    return result;
  }
  
  private static final class Angles {
    double D;
    double M;
    double Mp;
    double F;
    double Lp;
    double A1;
    double A2;
    double A3;
  }
  
  /** Returns degrees. */
  private double contributionsFrom(List<PeriodicTerm> periodicTerms, Angles angle, double T) {
    double result = 0.0;
    for(PeriodicTerm pt : periodicTerms) {
      result = result + pt.contribution(angle.D, angle.M, angle.Mp, angle.F, T);
    }
    return result;
  }
  
  public static void main(String[] args) {
    LunarPosition moon = new LunarPosition();
    double jd = GregorianCal.jd(1992, 04, 12.0);
    EclipticCoords coords = moon.λβ(jd);
    LogUtil.log("λ:" + Maths.radsToDegs(coords.λ) + " β:"+ Maths.radsToDegs(coords.β) + " jd:" + jd);
  }
}
