package planisphere.astro.planets;

/** 
 The dimmer planets are excluded from this implementation.
 The heliocentric orbit of the Earth is used to infer the position of the Sun. 
*/
public enum Planet {
  
  Mercury, Venus, Earth, Mars, Jupiter, Saturn;
  
  /** All elements of this enum, minus the Earth. */
  public static Planet[] WITHOUT_EARTH =  {Mercury, Venus, Mars, Jupiter, Saturn}; 
  
}
