package planisphere.draw.transparency;

import planisphere.config.Config;
import planisphere.config.Constants;
import planisphere.draw.Projection;
import planisphere.math.Maths;

/** 
 An ad hoc angle (altBar) that uniquely identifies the altitude divisions on the meridian.
 
 The problem is to make divisions on the meridian corresponding to the altitude.
 In the northern (southern) hemisphere, pass along the meridian from the south (north) to the zenith, 
 and then to the north (south).
 Then the angle of this direction with the southern (northern) horizon, here called altBar, 
 goes from 0 to 180 degrees, while the altitude itself goes from 0 to 90, and then back down to 0.
 
 <P>All angles are in radians.
 
 <P>In some cases, altBar can correspond to places below the visible horizon. That happens in the far north,
 when the configured declination limit is actually below the horizon. This can be desirable, in order to see 
 the sun as it grazes just the horizon.
*/
final class AltitudeBar {

  AltitudeBar(Double altBar, Projection projection, Config config){
    this.altBar = altBar;
    this.projection = projection;
    this.config = config;
  }
  
  /** The altitude corresponding to altBar. Returns a value in the range 0..pi/2. */
  double altitude() {
    return altBar <= HALF_PI ? altBar : Math.PI - altBar;
  }
  
  /** The declination corresponding to altBar. Returns a value in the range -pi/2..pi/2. */
  double declination() {
    double result = 0.0;
    //there are two regions for this calculation, relative to the celestial pole
    if (isBeyondTheCelestialPole()) {
      result = sign() * (Math.PI - altBar + colatitude());
    }
    else {
      result = sign() * (altBar - colatitude());
    }
    return result;
  }

  /**
   The right ascension corresponding to altBar.
   When drawing this planisphere, 6h is 'at the bottom', and 18h is 'at the top'.
   Returns radians. 
  */
  double rightAscension() {
    double hours = isBeyondTheCelestialPole() ? Constants.UPPER_RA : Constants.LOWER_RA;
    return Maths.hoursToRads(hours);
  }
  
  boolean isUpperCulmination() {
    return !isLowerCulmination();
  }
  
  boolean isLowerCulmination() {
    return isBeyondTheCelestialPole();
  }

  /** 
   THIS ISN'T NECESSARY. JUST USE THE CLIP AREA, AND START FROM THE UPPER END.
   On the planisphere, the meridian often starts at a point having a non-zero altitude.
   In the mid-latitudes, the south is usually partially cut off (altBar starts at +10 degrees, for example).
   In the far north, the south might actually be extended beyond the visible horizon, so that you can see 
   the Sun grazing the horizon (altBar starts at -10 degrees, for example).
   This method returns the altBar value that corresponds to the start of the meridian at the bottom of the chart.
 
   <P>This returns an integer value (for example, 4.2 degrees is pushed up to 5.0 degrees).
   The return value is in radians. 
  */
  double firstChartableAltBarValue() {
    double horizonDec = - sign() * colatitude();
    double decLimit = Maths.degToRads(config.declinationLimit());
    double rads = sign() * (decLimit - horizonDec);
    //temporarily work in degrees, to find the correct integral value
    double degrees = Maths.radsToDegs(rads);
    double result = Math.ceil(degrees);
    return Maths.degToRads(result); //back to rads now
  }
  
  boolean isChartable() {
    return altBar >= firstChartableAltBarValue();
  }
  
  private double altBar;
  private Projection projection;
  private Config config;
  private static final double HALF_PI = Math.PI/2;
  
  private int sign() {
    return projection.getBounds().hemisphereSign();
  }

  /**
   Not the conventional definition for the southern hemisphere, but suitable for the present purpose. 
   Always positive. 
  */
  private double colatitude() {
    return HALF_PI - sign() * config.latitude();
  }
  
  private double altBarForTheCelestialPole() {
    return HALF_PI + colatitude();
  }

  private boolean isBeyondTheCelestialPole() {
    return altBar > altBarForTheCelestialPole();
  }
  
}
