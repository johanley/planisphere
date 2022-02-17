package planisphere.astro.planets;

import static java.lang.Math.cos;

final class PeriodicTerm {

  /** Amplitude in radians. */
  double A;
  
  /** Phase in radians. */
  double B;
  
  /** Frequency in radians per millenia. */
  double C;
  
  /** Returns radians. τ in Julian millenia. */
  double contributionToSum(double τ) {
    return A * cos(B + C * τ);  
  }
  
  @Override public String toString() {
    return "A:" + A + " B:"+B + " C:"+C;
  }

}
