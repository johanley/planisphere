package planisphere.draw;

/** 
 The bounds of the area shown by a chart, and related data derived from the bounds.
 WARNING: the units are in degrees and hours only! 
*/
public class Bounds {

  public Bounds(Double minDecDeg, Double maxDecDeg, Double minRaHours, Double maxRaHours) {
    this.minDecDeg = minDecDeg;
    this.maxDecDeg = maxDecDeg;
    this.minRaHours = minRaHours;
    this.maxRaHours = maxRaHours;
  }
  
  public Double minDecDeg;
  public Double maxDecDeg;
  public Double minRaHours;
  public Double maxRaHours;

  /** True only if the chart is for north of the celestial equator. */
  public boolean isNorthernHemisphere() {
    return maxDecDeg >= 89.0;
  }
  
  /** +1 for the northern hemisphere, -1 for the southern hemisphere.  */
  public int hemisphereSign() { return isNorthernHemisphere() ? 1 : -1; }
  
  /** The maximum declination minus the minimum declination. Always positive! */
  public double decRange() {
    return maxDecDeg - minDecDeg;
  }
  
  /** The declination away from the celestial pole. */
  public double declinationLimit() {
    return isNorthernHemisphere() ? minDecDeg : maxDecDeg;
  }
}
