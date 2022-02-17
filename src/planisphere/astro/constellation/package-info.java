/**
 For each constellation, draw lines joining the stars, to make an arbitrary pattern.
  
 <P>The lines are arbitrarily defined, and don't follow any formal standard.
 Not all stars in a constellation are part of a line.
 
 <P>The data structure used here identifies stars with numbers.
 Those numbers are indexes into the list of stars in a MODIFIED version 
 of the Yale Bright Star catalog, and can't be reused as is with other catalogs.
 (To change that, you would likely use the HD identifier.) 
*/
package planisphere.astro.constellation;