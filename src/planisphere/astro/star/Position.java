package planisphere.astro.star;

import planisphere.math.Maths;

/*** Struct for Right Ascension and Declination. */
public final class Position {
  
  /** Radians. */
  public double α;
  /** Radians. */
  public double δ;

  public Position(Double α, Double δ) {
    this.α = α;
    this.δ = δ;
  }
  
  public Position() { }

  /** Debugging only. */
  @Override public String toString() {
    return "α :" + Maths.radsToDegs(α) + " δ:" + Maths.radsToDegs(δ);
  }
}