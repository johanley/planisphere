/** 
 Calendars, and the sideral time at Greenwich. 
 
 <P>For a given date/time, a certain right ascension will be on the observer's meridian.
 This package implements that translation.
 
 <P>In the given context of a low-precision planisphere, the sidereal time at Greenwich, 
 for a given date/time, is taken as a rough approximation of LOCAL sidereal time, for all locations, 
 for the same date/time. This results in a maximum error of about 4 minutes.
 
 <P>There's no accounting for leap years in this implementation.
*/
package planisphere.astro.time;