# planisphere
Generate a planisphere to show objects in the sky at a given date and time.
Free and open source.

[Main documentation](https://johanley.github.io/planisphere/index.html).

This planisphere is designed for the mid-latitudes. 

To generate the PDF, run the classes named planisphere.GenerateBack (the star chart) 
and planisphere.GenerateFront (the transparency). Only the transparency depends on latitude.

The source data for stars is a lightly modified version of the Yale Bright Star catalog, described <a href='https://github.com/johanley/bobcaygeon/blob/master/src/mag5/star/package-info.java'>here</a>.
Epoch J2000 is used, and no precession is applied.

This implementation puts 06h right ascension to the bottom of the star chart.
 
For the Greenwich meridian, on February 19 at sidereal time 06h00m00s, 
UT is in the range 20h00m to 20h03m (according to MICA), for the years 2020-2029. 
It of course varies a bit from year to year. 
I've chosen February 19, 20h Local Mean Time as corresponding to 06h Local Sidereal Time.

No accounting for leap years is made (there's no February 29 in the dates).

(The drawing context of a PDF document defaults to having the XY origin in the bottom left. Y increases upwards.)
