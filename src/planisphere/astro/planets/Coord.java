package planisphere.astro.planets;

/** Heliocentric ecliptical coords. */
enum Coord {
  
  L, B, R;
  
  /** Map 1,2,3 into L,B,R. */
  static Coord valueFrom(int idx) {
    return values()[idx -1 ];
  }
}
