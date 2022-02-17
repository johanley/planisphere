package planisphere.astro.precession;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static planisphere.math.Maths.*;

/** A cosine-term and a sine-term having the same period. */
final class PeriodicTerm {
  
  /** Cosine: amplitude in arcseconds. */
  double C;
  
  /** Sine: amplitude in arcseconds. */
  double S;
  
  /** Period in Julian centuries. */
  double P;
  
  /** Returns arcseconds. T in Julian centuries. */
  double contributionToSum(double T) {
    return C * cos(TWO_PI * T / P) + S * sin(TWO_PI * T / P);  
  }
}