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
  
  /** The opposite position on the sky with respect to this one (the antipodes). */
  public Position opposite() {
    Position result = new Position();
    result.δ = -1 * this.δ;
    result.α = Maths.in2pi(this.α + Math.PI);
    return result;
  }

  /** Debugging only. */
  @Override public String toString() {
    return "α :" + Maths.radsToDegs(α) + " δ:" + Maths.radsToDegs(δ);
  }
}