/** 
 Generate a precise, customizable planisphere, as two PDF files (a star chart and a transparency).
 The two files are then printed (on a high-resolution printer), and the results are attached together in the middle.
 One file is meant for printing on card stock, and the other as a transparency.
 The transparency rotates around a central rivet.
 
 <P>The planisphere uses the sterographic projection, which is appropriate only for latitudes outside the tropics.
 (That is its main defect.)
 
 <P>When run as a stand-alone program, the top-level class for generating the two planisphere files is <code>planisphere.Build</code>.
 
 <P>The code can also be used in a servlet environment to 
 <a href='https://kb.itextsupport.com/home/it7kb/faq/how-can-i-serve-a-pdf-to-a-browser-without-storing-a-file-on-the-server-side'>generate PDFs as byte streams</a>
  that are served to the browser. In that case, the configuration comes from user input in a form, not a config.ini text file.
 
 <P>The main design goals of this planisphere are:
 <ul>
  <li>allow <b>customization</b> for a given location <em>and year</em>.
  <li>be much more <b>precise</b> than the typical planisphere. 
  (The main source of error will almost always be the mechanical mis-centering between the star chart and the transparency.)
  <li>allow for <b>time-travel</b> into the past or future, for thousands of years. 
  (This is possible using a precession algorithm valid over long time scales, and by including 3D proper motion).
  <li>optimize for people who already know the night sky well. 
  Include only <b>minimal text and markings</b>, and only what's necessary for the correct operation of the planisphere. 
 </ul>
 
 <P>The planisphere's star chart includes:
 <ul>
  <li>stars brighter than magnitude 5.02 (derived from <a href='https://github.com/johanley/star-catalog'>this catalog</a>)
  <li>the position of the Sun, every day at 18h (in the configured time zone)
  <li>the path of the Moon's orbit on July 1 of the configured year
  <li>the celestial equator
  <li>the positions of the radiant of the brightest meteor showers (configurable)
  <li>the constellations are shown in a simple way, without names (to reduce clutter for those who already know the night sky)
  <li>the date-scale indicates the observer's sidereal time at 20h each day of the year, in their own time zone
  <li>the back of the star chart includes tables with the time of transit of the Moon for every day of the year, and the time of transit 
  of all the bright planets, for the 15th of every month. These can be used to get approximate positions for the Moon and bright planets, 
  over the course of the year.
 </ul>
 
 <P>The planisphere's transparency goes on top of the star chart. 
 The transparency includes:
 <ul>
  <li>a time-scale with divisions for every 2 minutes (or 1 minute, if you choose)
  <li>markings for every degree of altitude on the meridian, and every degree of azimuth on the horizon
  <li>circles for every 10 degrees of altitude and azimuth
  <li>circles for the altitudes below the horizon to indicate civil, nautical, and astronomical twilight
  <li>a special circle at 0.9 degrees below the horizon, for computing the time of sunset and sunrise
 </ul>
 
 <P>Other general notes about the planisphere:
 <ul>
  <li>it's written for mid-latitude locations; as is, it's not really appropriate for locations near the equator
  <li>no allowance is made anywhere for a summer hour (daylight savings time)
  <li>the two output files are in PDF format, as greyscale, single-page documents
  <li>the two files need to be printed (one as a transparency), and attached together in the center
  <li>for best results, you should <b>use a high quality, high resolution printer</b>; many home printers lack high resolution
  <li>it has black stars and lines on a white background, for high contrast under dark conditions
  <li>the epoch used by the catalog is J2000, but precession is applied to the positions
  <li>no lines are included to delineate the approximate border of the Milky Way
  <li>a textual configuration file is used to let people change some settings without changing the code  
 </ul>
 
 An effort has been made to make as few approximations as possible:
 <ul>
  <li>the star catalog has precession applied to a given year, configured in a text file. 
  The effect of precession is detectable on a small-scale star chart, even after only 20 years.
  <li>the date-scale is generated specific to a given lat/long, offset from UT, <em>and year</em>.
  Creating a planisphere that is designed for a specific year is unusual. 
  The date-scale uses the local sidereal time for 20h local mean time every day. This increases precision.
  <li>a correction is applied for the difference in longitude between the observer and the central meridian of a time zone.
  This allows the time-scale to be in Standard Time (UT plus/minus an offset), not Local Mean Time.
  <li>the time-scale shows mean solar time (to match your clock), not sidereal time. The sidereal day is 
  shorter than the mean solar day, by about 3m 56s. This means that the time-scale shows slightly less than 24 hours.
  <li>by default, the time scale has divisions for every 2 minutes. This renders well with an 8 inch version. For a larger 
  version, you may want to change that to every minute.
  <li>for predicting sunrise and sunset, a special altitude of -0.9 degrees has been added, just below 0.0 degrees. 
  This accounts for refraction at the horizon, the Sun's mean semi-diameter, and for the observer being 
  about 2 meters above the real horizon. (See the Explanatory Supplement to the Ephemeris, 1961, page 401).
  Sunrise and sunset correspond to the center of the Sun's disk being at that altitude.
  For the stars, the rise/set altitude is near -0.57 degrees, not -0.9. 
  For the Moon, the rise/set altitude is in the range of +0.08 to +0.18 degrees, because of its large horizontal parallax.
 </ul>
 
 <p>Discontinuities in the planisphere (by design):
 <ul>
  <li>the date-scale usually shows a small discontinuity between Dec 31 and Jan 1.
  This is because the date-scale is specific to a given year. 
  It shows the observer's sidereal time at 20h in their configured time zone.
  <li>the time-scale on the tranparency has as discontinuity near 08h. 
  This is because the solar day and the sidereal day aren't the same length.
 </ul>
 
 <P>The goal is to predict the transit time of an object on the celestial equator to within approximately 1 minute.
 This is possible, but only if you generate a planisphere for a specific lat/long and year.
 In this way, the main source of error will usually be mechanical, from the inexact alignment of the centers 
 of the transparency and the star chart (instrumental error). 
 
 <P>For years other than that for which the planisphere was generated, the precision will of course be significantly reduced, 
 typically to no better than about 4 minutes of time.
 
 <P>One option is to print 4 sequential years, and use them in a 4 year cycle, if you wish.
 For example, you could reuse one printed for 2022 four years later in 2026, with an error in sidereal 
 time on the order of about 10 seconds. Eventually, precession will add up, so this won't work perpetually.  
 
 <P>Planispheres are seldom used as precise instruments.
 Nevertheless, it's always interesting to push an instrument to its limits, to see how far you can go.
 That said, many users will be perfectly content with planispheres having significantly lower precision.
 
 <P>Notes about the implementation:
 <ul>
  <li>the top-level main class, for running as a standalone program, is named Build.java
  <li>all text files, and all .java files, use the UTF-8 encoding; your dev environment must also treat them as UTF-8
  <li>the project uses an old version of the <a href='https://itextsupport.com/apidocs/iText5/5.5.9/>'iText java library (5.5.13)</a>
  <li>vector graphics are used (not raster graphics)
  <li>the fonts are embedded in the PDF
  <li>WARNING: different printers may render thin lines differently
  <li>the author has run this code only on Windows OS, not on Linux
  <li>the logging can be made silent simply by changing one setting in the {@link planisphere.util.LogUtil} class. 
 </ul>
 
 <P>Some data I used in building this tool:
 <ul>
  <li>stars: the star catalog is from <a href='https://github.com/johanley/star-catalog'>this project</a>
  <li>planets: the VSOP87D data for the planets, by <a href='https://ui.adsabs.harvard.edu/abs/1988A%26A...202..309B/abstract'>Bretagnon and Francou</a>.
  <li>Moon: the abridged form of ELP 2000-82 as published in Astronomical Algorithms, by Meeus 1991.
  <li>precession: algorithm and tables for P/Q, X/Y, and p/epsilon from 
    <a href='https://ui.adsabs.harvard.edu/abs/2011A%26A...534A..22V/abstract'>this paper</a> by Vondrák, Capitaine, and Wallace.
 </ul>
 
 <P>Things I learned in building this tool:
 <ul>
  <li>implementations of calculating a Julian Date from a calendar date are usually mediocre (NOVAS, SOFA). 
  Many implementations unnecessarily restrict the input range.
  Many implementations are deeply cryptic. (For this reason, I made my own algorithm.)
  <li>calculating 3D proper motions is not difficult (see  <a href='https://ui.adsabs.harvard.edu/abs/1989AJ.....97.1197K/abstract'>Kaplan et al 1989</a>, section IV, page 1203).
  <li>many tools, including those produced to implement IAU standards, are optimized to produce accurate results only within a few hundred years 
  of J2000, and are unsuitable for use outside that range, without careful scrutiny. 
  <li>calculating precession over millenial time scales has only really been possible since the publication of 
  <a href='https://ui.adsabs.harvard.edu/abs/2011A%26A...534A..22V/abstract'>this paper</a> by Vondrák, Capitaine, and Wallace, in 2011.
  <li>in precession, both poles move: the equator pole and the ecliptic pole. Most people neglect the motion of the latter.
  <li>some nearby bright stars have relatively rapid proper motion: Proxima Centauri, Sirius, Pollux, Arcturus.
  <li>in astronomy, a simple atan3(y, x) function, like atan2 but with the return value in the range 0..2pi, is a simple and useful addition
  <li>the JPL's <a href='https://ssd.jpl.nasa.gov/doc/de440_de441.html'>DE441</a> ephemeris is built to calculate over a long time scale, -13200..+17191.
  <li>for the position of the Moon, it would have been best to use ELP 2000-85, which is built for historical calculations. 
  An abridged version of that theory is published in <a href='https://ui.adsabs.harvard.edu/abs/1988A%26A...190..342C/abstract'>the original paper</a>, but I don't understand the paper. 
  It's also available in <a href='https://ui.adsabs.harvard.edu/abs/1991ltpf.book.....C/abstract'>book form</a>, but I can't find or view the book anywhere. 
  <li>to match YBS data with another star catalog, the HD Henry Draper identifier is the best candidate for linking records.
  <li>I was able to map YBS records to GAIA, using Hipparcos as an intermediary, and the HD identifier.
  <li>the GAIA database is poor for bright objects. Its detectors aren't designed for them. I considered using GAIA for this project, but it wasn't feasible.
  <li>the GAIA archive is a large relational database. Their tools use a flavour of SQL to do queries.
 </ul>
 
 <P>Things I'd like to have: 
 <ul>
  <li>an updated version of the YBS, with modern parallax and proper motion values.
  <li>access to ELP 2000-85 for the Moon (see above).
  <li>an expression for the inclination of the Moon's orbit, instead of a constant. 
  This may be in Mathematical Astronomy Morsels, by Meeus.
 </ul>
*/
package planisphere;