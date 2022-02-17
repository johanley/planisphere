package planisphere.astro.moon;

import static java.lang.Math.abs;
import static java.lang.Math.sin;
import static planisphere.math.Maths.sqr;

/** This implementation excludes the distance r of the Moon. */
final class PeriodicTerm {
  
  int D;
  int M;
  int Mp;
  int F;
  
  /** Degrees. */
  double amplitude; 

  /** All angles passed in are in rads, but the return value is in degrees. */
  double contribution(double Dval, double Mval, double Mpval, double Fval, double T) {
    double angle = D*Dval + M*Mval + Mp*Mpval + F*Fval;
    double val = amplitude * sin(angle);
    return accountForEccentricity(val, T);
  }
  
  private double accountForEccentricity(double val, double T) {
    double result = val;
    if (abs(M) == 1) {
      result = eccentricityFactor(T) * val;
    }
    else if (abs(M) == 2) {
      result = sqr(eccentricityFactor(T)) * val;
    }
    return result;
  }
  
  private double eccentricityFactor(double T) {
    return 1.0 - 0.002_516*T - 0.000_0074*T*T;
  }
}