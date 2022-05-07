package planisphere.astro.star;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static planisphere.astro.time.AstroUtil.DAYS_PER_JULIAN_YEAR;
import static planisphere.astro.time.AstroUtil.KM_PER_AU;
import static planisphere.astro.time.AstroUtil.SECONDS_PER_DAY;
import static planisphere.astro.time.AstroUtil.angularSeparation;

import planisphere.astro.precession.XYZ;
import planisphere.math.Maths;
import planisphere.math.Matrix;
import planisphere.math.Vector;

/** 
 Proper motion of a star across the sky.

 <P>
 3D proper motion (for foreshortening effects) is applied as a base, and 
 2D proper motion is applied if data is missing (rare). 
 Negative parallaxes are ignored.
 
 <P>Proper motion is always applied before applying precession.
 There are two methods of calculating proper motion: 2D and 3D.
 The classical 2D proper motion is for the motion across the sky, and ignores radial motion.
 The 2D style should only be used when foreshortening effects due to 3D motion are negligible.
 For long time scales, with the full sky, it's necessary to use the 3D algorithm, 
 as described by <a href='https://ui.adsabs.harvard.edu/abs/1989AJ.....97.1197K/abstract'>Kaplan et al 1989</a>, section IV, page 1203.
 
 <P>The 3D method requires more data than the 2D method. 
 It also requires parallax and radial velocity, along with the regular proper motion in right ascension and declination.
 
 <P>Proper motion can be applied only within certain limits of historical (and future) time,
 but I'm not sure what exactly those limits are in my case, which requires only 
 1 arcminute precision in the overall position.
 
 <P>Over 5000 years, the change in the visual night sky is non-trivial.
  Max proper motion : +10°12'.
  Number of stars that whose proper motion exceeded 1 degree: 88.
  Some notable bright stars have large proper motion: Proxima Centauri, Procyon, Sirius, Arcturus.
*/
public final class ProperMotion {
  
  /** The proper motion epoch of the Hipparcos catalog. */
  public static double J1991_25 = 2448349.0625;
  
  /** 
   Constructor.
   @param jdStart when the proper motion begins
   @param jdEnd when the proper motion ends
  */
  public ProperMotion(double jdStart, double jdEnd){
    this.jdStart = jdStart;
    this.jdEnd = jdEnd;
  }
  
  /** 
   Changes the star's positional data in place.
   The given star's position initially corresponds to jdStart passed to the constructor.
   After this method returns, the star's position corresponds to jdEnd passed to the constructor.
   
   <P>Returns the amount of proper motion applied, in arcseconds.
   The star's proper motion is in arcseconds, and its position is in rads. 
  */
  public double applyTo(Star star) {
    boolean hasAllData = star.PARALLAX != null && star.PARALLAX > 0 && star.RADIAL_VELOCITY != null;
    double result = hasAllData ? threeD(star) : twoD(star);
    return result;
  }

  // PRIVATE 
  
  private double jdEnd;
  private double jdStart;

  private double julianYears() {
    return julianDays() / DAYS_PER_JULIAN_YEAR; 
  }
  
  private double julianDays() {
    return jdEnd - jdStart;
  }
  
  /** Classical 2D proper motion across the sky. Returns arcseconds. */
  private double twoD(Star star) {
    //note the factor for declination! note as well the behavior near the pole:
    double years = julianYears();
    double Δα = (star.PROPER_MOTION_RA * years)/Math.cos(star.DEC); //arcsecs
    double Δδ = star.PROPER_MOTION_DEC * years; //arcsecs
    star.RA = star.RA + Maths.degToRads(Δα/3600.0); //no int div
    star.DEC = star.DEC + Maths.degToRads(Δδ/3600.0); //no int div
    
    double result = Math.sqrt(Math.pow(star.PROPER_MOTION_RA * years, 2) + Math.pow(star.PROPER_MOTION_DEC * years, 2)); //arcsecs
    return result; //arcsecs
  }
  
  /**  3D proper motion. Returns arcseconds. */
  private double threeD(Star star) {
    double pRads = Maths.arcsecToRads(star.PARALLAX); //rads
    double r = 1/pRads; //AU
    Vector u0 = XYZ.xyzFrom(new Position(star.RA, star.DEC), r); //AU, equatorial rectangular coords
    
    //convert proper motion (arcsec/year) and radial velocity (km/s) to units of AU/day
    double pmRA = Maths.arcsecToRads(star.PROPER_MOTION_RA) / (DAYS_PER_JULIAN_YEAR * pRads);
    double pmDEC = Maths.arcsecToRads(star.PROPER_MOTION_DEC)/ (DAYS_PER_JULIAN_YEAR * pRads);
    
    double rDot = (SECONDS_PER_DAY * star.RADIAL_VELOCITY)/KM_PER_AU; //from km/s
    Vector velocityComponents = new Vector(pmRA, pmDEC, rDot); // AU/day, in weird rotated system of coords
    
    //two simple rotations are needed in order to get the components into the same rectilinear coordinate system as the position vector u0
    Vector row1 = new Vector( -sin(star.RA),   -cos(star.RA)*sin(star.DEC),   cos(star.RA)*cos(star.DEC));
    Vector row2 = new Vector(  cos(star.RA),   -sin(star.RA)*sin(star.DEC),   sin(star.RA)*cos(star.DEC));
    Vector row3 = new Vector(   0,                  cos(star.DEC),                   sin(star.DEC));
    Matrix rotations = new Matrix(row1, row2, row3);
    Vector udot0 = rotations.times(velocityComponents); //  AU/day, in 'standard' coordinates with axes in the right direction

    Vector u2 = u0.plus(udot0.times(julianDays()));
    Position newPos = XYZ.positionFrom(u2);
    Position oldPos = new Position(star.RA, star.DEC);
    
    double result = angularSeparation(oldPos, newPos);
    
    //finally, update the coordinates in place
    star.RA = newPos.α;
    star.DEC = newPos.δ;
    
    return Maths.radsToArcsecs(result);
  }
}
