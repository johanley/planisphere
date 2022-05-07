package planisphere.astro.precession;

import static planisphere.util.LogUtil.log;

import java.io.IOException;
import java.util.Optional;

import planisphere.astro.star.Position;
import planisphere.astro.star.ProperMotion;
import planisphere.astro.star.Star;
import planisphere.astro.star.StarCatalog;
import planisphere.astro.time.AstroUtil;
import planisphere.astro.time.GregorianCal;
import planisphere.config.Config;
import planisphere.config.ConfigFromFile;
import planisphere.math.Maths;

/** 
 The closest approach of a star to the equatorial pole in a given interval centered on the year 0.
 
 <P>The paper on which the long-term precession algorithm is based states that its accuracy is within a few tenths of a degree 
 within 200,000 years of the present.
 
 <P>This algo uses the proper motion as defined by the Yale Bright Star catalog.
 If the data for 3D proper motion is available, then it is used.
 J2000 coords are used throughout.
 Proper motion is applied to the star's J2000 position, for a given year. 
 Then that position is compared with the position of the equator pole, also expressed in J2000 coords.

 <P>Example output for northern stars, in the range -100,000..+100,000 years:
<pre>
 Star  Year   Separation
-----------------------------------
α Lyr +63979 +0°04'8.452''  Vega
α Cyg -40833 +2°52'14.376'' Deneb
α UMi -74993 +0°04'10.233'' Polaris
β UMi -27241 +2°08'1.127''  Kochab
α Cep +58112 +0°07'19.003'' Alderamin
λ Cep -44125 +0°21'6.772''
α Dra - 2796 +0°5'42.758''  Thuban
τ Her - 7606 +0°31'9.014''
</pre>
<P>WARNING: the above numbers should be rounded, since the algorithm isn't that precise when the time scale 
approaches its limits. The separation could be rounded to about 5', and the years to about 10 years.

<P>
The results for the interval of -10,000..+10000, and the same stars as above:
<pre>
 Star  Year   Separation
-----------------------------------
α Lyr -10000 +12°49'13.045'' Vega
α Cyg +10000 +7°33'42.761''  Deneb
α UMi + 2102 +0°27'37.062''  Polaris
β UMi - 1060 +6°31'35.241''  Kochab
α Cep + 7539 +1°55'10.629''  Alderamin
λ Cep + 7317 +4°55'32.898''  
α Dra - 2796 +0°05'42.758''  Thuban
τ Her - 7606 +0°31'9.014''
</pre>

<P>Example output for southern stars, in the range -100,000..+100,000 years:
<pre>
 Star  Year   Separation
-------------------------------------
α Dor -32096 +4°1'40.739''
γ Dor -31853 +0°37'12.609''
α Eri +73425 +3°46'22.983'' Achernar
γ Cha -22457 +0°14'11.378''
α Car +91315 +5°28'58.134'' Canopus
ω Car -71406 +0°3'44.054''
δ Vel + 9253 +0°8'16.185''
γ Vel +36623 +0°9'23.857''  Regor
σ Pup -14090 +0°23'6.263''
</pre>

<P>This implementation only cycles through each year in the given range, as of Jan 1. 
A more accurate method would then interpolate/search for a more precise date. 
*/
public final class ClosestApproachToPole {

  /** Run the script. */
  public static void main(String... args) throws IOException {
    int MAX_YEAR = 15000;
    String[] STAR_NAMES_N = {"α Lyr", "α Cyg", "α UMi", "β UMi", "α Cep", "λ Cep", "α Dra", "τ Her"};
    String[] STAR_NAMES_S = {"α Dor", "γ Dor", "α Eri", "γ Cha", "α Car", "ω Car", "δ Vel", "γ Vel", "σ Pup"};
    log("Finding years of closest approach for stars near the pole, in the range -" + MAX_YEAR + "..+" + MAX_YEAR);
    
    ClosestApproachToPole approach = new ClosestApproachToPole(MAX_YEAR);
    for (String starName : STAR_NAMES_S) {
      Closest closest = approach.findTheClosestApproachToThePole(starName);
      if (closest != null) {
        log(starName + " " + closest.year + " " + closest.separation() + " " + closest.properName);
      }
    }
    log("Star / year / separation.");
    log("Done.");
  }
  
  public ClosestApproachToPole(int maxYear) throws IOException {
    if (maxYear > 200000) {
      throw new IllegalArgumentException("Max year cannot exceed 200,000.");
    }
    this.maxYear = maxYear;
    Config config = new ConfigFromFile().init();
    this.starCatalog = new StarCatalog(config);
    Double NO_PRECESSION = null;
    starCatalog.generateIntermediateStarCatalog(NO_PRECESSION);
  }
  
  /** Scan for the year of closest approach, in the given range. */
  public Closest findTheClosestApproachToThePole(String name) {
    Closest result = null;
    Optional<Star> star = starCatalog.findByName(name);
    if (star.isPresent()) {
      result = findTheClosestApproachToThePole(star.get());
    }
    else {
      log("Can't find the star named '" + name + "'.");
    }
    return result;
  }
  
  private int maxYear;
  private StarCatalog starCatalog;
  
  private Closest findTheClosestApproachToThePole(Star star) {
    Closest result = new Closest();
    LongTermPrecession precession = new LongTermPrecession();
    double dec = 0.0;
    int year = 0;
    for (int y = -maxYear; y <= maxYear; y = y + 1) {
      Double jd = GregorianCal.jd(y, 1, 1.0);
      
      //always apply proper motion before precession
      ProperMotion pm = new ProperMotion(ProperMotion.J1991_25, jd);
      //the proper motion code changes the state of the star in place; 
      //we need to preserve the state of the incoming star object, as of J2000
      Star copyWithPM = star.copy();
      pm.applyTo(copyWithPM);
      
      Position pos = precession.apply(copyWithPM.position(), jd);
      if (Math.abs(pos.δ) > dec) {
        dec = Math.abs(pos.δ);
        year = y;
      }
    }
    result.declination = dec;
    result.year = year;
    result.properName = star.PROPER_NAME;
    return result;
  }
  
  private static final class Closest {
    String properName;
    int year;
    double declination; //absolute value!
    String separation() {
      return AstroUtil.radsToDegreeString(Maths.HALF_PI - declination);
    }
  }
}
