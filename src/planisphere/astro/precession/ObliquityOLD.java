package planisphere.astro.precession;

import planisphere.astro.time.AstroUtil;
import planisphere.astro.time.GregorianCal;
import planisphere.math.Maths;
import planisphere.util.LogUtil;

/**
 THIS CLASS IS NOT USED TO GENERATE THE PLANISPHERE.
  IT IS RETAINED ONLY FOR COMPARISON PURPOSES. 
 The mean obliquity of the ecliptic.
 
 <P>I'm assuming it's best, in my case, to use the Laskar algorithm, since it's meant for longer time scales.
 The others seem to be designed for time scales of a few centuries.
 It's unfortunate that it is limited to +/- 10,000 years, so it can't cover a full precession cycle.

 Comparison of some results:
<code>
2000-1-1.0
  +23°26'21.407'' Capitaine 2003
  +23°26'21.449'' Lieske 1977
  +23°26'21.449'' Laskar 1986
1750-1-1.0                       range about 0.03''
  +23°28'18.463'' Capitaine 2003
  +23°28'18.451'' Lieske 1977
  +23°28'18.436'' Laskar 1986
1500-1-1.0                       range about 0.10''
  +23°30'15.33'' Capitaine 2003
  +23°30'15.277'' Lieske 1977
  +23°30'15.236'' Laskar 1986
1000-1-1.0
  +23°34'7.741'' Capitaine 2003
  +23°34'7.717'' Lieske 1977
  +23°34'7.514'' Laskar 1986
500-1-1.0
  +23°37'57.144'' Capitaine 2003
  +23°37'57.407'' Lieske 1977
  +23°37'56.783'' Laskar 1986
0-1-1.0                         range about 1''
  +23°41'42.07'' Capitaine 2003  
  +23°41'42.99'' Lieske 1977
  +23°41'41.556'' Laskar 1986
-500-1-1.0
  +23°45'21.086'' Capitaine 2003
  +23°45'23.105'' Lieske 1977
  +23°45'20.358'' Laskar 1986
-1000-1-1.0                      range about 5''
  +23°48'52.816'' Capitaine 2003
  +23°48'56.391'' Lieske 1977
  +23°48'51.746'' Laskar 1986
-2000-1-1.0
  +23°55'29.305'' Capitaine 2003
  +23°55'37.041'' Lieske 1977
  +23°55'26.677'' Laskar 1986
-5000-1-1.0                     range about 30''
  +24°10'30.998'' Capitaine 2003
  +24°10'33.72'' Lieske 1977
  +24°9'57.457'' Laskar 1986
-6000-1-1.0                     range about 1'10''
  +24°13'40.029'' Capitaine 2003
  +24°13'14.597'' Lieske 1977
  +24°12'34.763'' Laskar 1986
-7000-1-1.0
  +24°15'53.215'' Capitaine 2003
  +24°14'28.337'' Lieske 1977
  +24°13'55.736'' Laskar 1986
-8000-1-1.0
  +24°17'16.239'' Capitaine 2003
  +24°14'4.064'' Lieske 1977
  +24°13'58.235'' Laskar 1986  the end of Laskar's stated range
-9000-1-1.0                     range about 6'10''
  +24°17'59.333'' Capitaine 2003
  +24°11'50.899'' Lieske 1977
-10000-1-1.0
  +24°18'17.795'' Capitaine 2003
  +24°7'37.966'' Lieske 1977
-13000-1-1.0                   range about 21'
  +24°20'45.372'' Capitaine 2003
  +23°41'11.779'' Lieske 1977
</code>
*/
public final class ObliquityOLD {

  /**
  Meeus recommends this as being superior over long time scales.
  Returns radians. The date cannot be more than 10,000 years from J2000!
  
  <P>See <a href='https://relojesdesol.info/files/Astronomy-and-Astrophysics-1986-J-Laskar-Obliquity.pdf'>this paper</a>,
  J. Laskar, Astronomy and Astrophysics, vol 157, page 68 (1986), Table 8.
  
  <P>NOT TO BE USED MORE THAN 10,000 years from J2000.
  
  <P>Estimated error: 0.02" after 1,000 years, a few arcseconds after 10,000 years.
  
  <P>Peaked about the year -7,530. Future minimum about +12,030. 
  By chance we are at present near the midpoint between the two peaks.
  */
  public double laskar1986(double jd) {
    double U = AstroUtil.julianCenturiesSinceJ2000(jd)/100; //10,000 Julian years
    if (Math.abs(U) > 1.01) {
      throw new IllegalArgumentException("You're trying to use Laskar's algo outside of its stated range of +/- 10,000 years.");
    }
    double arcseconds = 
      ε0 
      - 4680.93 * U
      -    1.55 * Math.pow(U,2)
      + 1999.25 * Math.pow(U,3)
      -   51.38 * Math.pow(U,4)
      -  249.67 * Math.pow(U,5)
      -   39.05 * Math.pow(U,6)
      +    7.12 * Math.pow(U,7)
      +   27.87 * Math.pow(U,8)
      +    5.79 * Math.pow(U,9)
      +    2.45 * Math.pow(U,10)
    ;
    return rads(arcseconds);
  }
  
  /**
   See <a href='https://www.aanda.org/articles/aa/pdf/2003/48/aa4068.pdf'>Capitaine 2003</a> (39) and (37).
   
   <P>Used by NOVAS code and the Astronomical Almanac (IAU standard).
   Returns radians.
   Estimated error over millenial time scales: unknown.
  */
  double capitaine2003(double jd) {
    double t = t(jd);
    double arcseconds = 
         84381.406 
       - 46.836769    *t 
       - 0.0001831    *t*t 
       + 0.00200340   *t*t*t 
       - 0.000000576  *t*t*t*t 
       - 0.0000000434 *t*t*t*t*t
    ;
    return rads(arcseconds);
  }
  
  /** 
   <P>From <a href='https://ui.adsabs.harvard.edu/abs/1977A%26A....58....1L/abstract'>Lieske 1977</a>  Table 5, page 15.
   <P>Same as Astronomical Algorithms by Meeus 1991 (21.2).Also see the Astronomical Almanac for 1984.
   <P>Returns radians. Estimated error about 1" after 2000 years, and about 10" after 4000 years.  
  */
  double lieske77(double jd) {
    double t = t(jd);
    double arcseconds = 
      ε0 
      - 46.815*t 
      - 0.00059*t*t 
      + 0.001813*t*t*t
    ;  
    return rads(arcseconds);
  }
  
  /** 84381.448 arcseconds */
  private static double ε0 = 23*3600 + 26*60 + 21.448;
  
  private double t(double jd) {
    return AstroUtil.julianCenturiesSinceJ2000(jd);
  }
  
  private double rads(double arcseconds) {
    return Maths.degToRads(arcseconds / 3600.0) ; // rads, no int div
  }
  
  /** Informal test harness. */
  public static void main(String... args) {
    test(2000, 1, 1.0);
    test(1750, 1, 1.0);
    test(1500, 1, 1.0);
    test(1000, 1, 1.0);
    test(500, 1, 1.0);
    test(0, 1, 1.0);
    test(-500, 1, 1.0);
    test(-1000, 1, 1.0);
    test(-2000, 1, 1.0);
    test(-5000, 1, 1.0);
    test(-6000, 1, 1.0);
    test(-7000, 1, 1.0);
    test(-8000, 1, 1.0);
    test(-9000, 1, 1.0);
    test(-10000, 1, 1.0);
    test(-13000, 1, 1.0);
  }
  
  private static void test(int year, int month, double day) {
    double jd = GregorianCal.jd(year, month, day);
    ObliquityOLD obliq = new ObliquityOLD();
    double answer = obliq.capitaine2003(jd);
    double answer2 = obliq.laskar1986(jd);
    double answer3 = obliq.lieske77(jd);
    LogUtil.log(year + "-" + month + "-" + day);
    LogUtil.log("  " + AstroUtil.radsToDegreeString(answer) + " Capitaine 2003");
    LogUtil.log("  " + AstroUtil.radsToDegreeString(answer3) + " Lieske 1977");
    LogUtil.log("  " + AstroUtil.radsToDegreeString(answer2) + " Laskar 1986");
  }

}
 