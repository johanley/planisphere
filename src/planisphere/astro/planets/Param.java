package planisphere.astro.planets;

/** 
 Enumeration of the included parameters.
 Some planets are missing some of the higher-power terms.
*/
enum Param {
  
  L0, L1, L2, L3, L4, L5, 
  B0, B1, B2, B3, B4, B5, 
  R0, R1, R2, R3, R4, R5;
  
  int power() {
    String name = this.toString();
    String lastChar = name.substring(name.length()-1);
    return Integer.valueOf(lastChar);
  }
  
}
