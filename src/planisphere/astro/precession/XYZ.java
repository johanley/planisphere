package planisphere.astro.precession;

import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static planisphere.math.Maths.atan3;

import planisphere.astro.star.Position;
import planisphere.math.Vector;

/** 
 Rectangular equatorial coordinates.
 Many operations can be represented as matrix operations (rotations) on a vector made from these coordinates. 
 
 This is a right-handed coordinate system.
 The X-axis is towards the vernal equinox.
 The Y-axis is 90 degrees to the east, along the celestial equator.
 The Z-axis is towards the north celestial pole.  
*/
public final class XYZ {
  
  /** Assumes the object is at unit distance. */
  public static Vector xyzFrom(Position pos) {
    return new Vector(
      cos(pos.δ) * cos(pos.α),
      cos(pos.δ) * sin(pos.α),
      sin(pos.δ)
    );
  }
  
  /** The object is at distance r. */
  public static Vector xyzFrom(Position pos, double r) {
    return new Vector(
      r * cos(pos.δ) * cos(pos.α),
      r * cos(pos.δ) * sin(pos.α),
      r * sin(pos.δ)
    );
  }
  
  /** XYZ to equatorial spherical coords. Assumes the object is at unit distance. */
  public static Position positionWithUnitDistance(Vector xyz) {
    return new Position(
      atan3(xyz.y, xyz.x),
      asin(xyz.z)
    );
  }
  
  /** XYZ to equatorial spherical coords. The object is at any distance. */
  public static Position positionFrom(Vector xyz) {
    return new Position(
      atan3(xyz.y, xyz.x),
      asin(xyz.z/xyz.length())
    );
  }
}