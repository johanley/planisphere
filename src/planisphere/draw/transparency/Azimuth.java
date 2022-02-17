package planisphere.draw.transparency;

import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static planisphere.math.Maths.TWO_PI;
import static planisphere.util.LogUtil.log;

import planisphere.astro.star.Position;
import planisphere.math.Maths;

final class Azimuth {
  
  Azimuth(double φ, double localSiderealTime){
    this.φ = φ;
    this.localSiderealTime = localSiderealTime;
  }
  
  /** Translate altitude a and azimuth A into equatorial coords. Radians. */
  Position fromAltAz(double a, double A) {
    /* 
     * Practical astronomy with your calculator, Duffet-Smith
     * ref: https://archive.org/details/practicalastrono0000duff/page/38/mode/2up?view=theater
     */
    double sinδ = sin(a) * sin(φ) + cos(a) *cos(A) * cos(φ);
    double δ = asin(sinδ); // -pi/2..pi/2
    double cosH = (sin(a) - sin(φ) * sin(δ)) / (cos(φ) * cos(δ));
    double h = acos(cosH);
    double quadrant = Math.sin(A) >= 0 ? 1 : -1;
    if (quadrant > 0) {
      h = TWO_PI - h;
    }
    double α = Maths.in2pi(localSiderealTime - h);
    return new Position(α, δ);
  }
  
  private double φ;
  private double localSiderealTime;
  
  /** Informal test for the quadrant. Use hour angle, not RA. */
  private static void main(String... args) {
    test(19, 283);
    /*
    test(0, 0*90+45);
    test(0, 1*90+45);
    test(0, 2*90+45);
    test(0, 3*90+45);
    */
  }
  
  private static void test(double aDegs, double Adegs) {
    double a = Maths.degToRads(aDegs);
    double A = Maths.degToRads(Adegs);
    Azimuth az = new Azimuth(Maths.degToRads(52), 0);
    log(az.fromAltAz(a, A).toString() + " sin A: " + sin(A) );
  }
}
