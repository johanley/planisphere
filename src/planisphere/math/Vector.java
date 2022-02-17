package planisphere.math;

/** A 3-component vector. Immutable. */
public final class Vector {
  
  public Vector(double x, double y, double z){
    this.x = x;
    this.y = y;
    this.z = z;
  }
  
  public Vector(){}
  
  public Double x;
  public Double y;
  public Double z;

  /** Dot product of this vector with the given vector. */
  public double dot(Vector that) {
    return 
      this.x * that.x + 
      this.y * that.y + 
      this.z * that.z
    ;
  }
  
  /** Cross product of this vector with the given vector. Returns a new vector. */
  public Vector cross(Vector that) {
    Vector result = new Vector();
    result.x =   this.y * that.z - this.z * that.y;
    result.y = -(this.x * that.z - this.z * that.x);
    result.z =   this.x * that.y - this.y * that.x;
    return result;
  }
  
  /** Add the given vector to this vector, and return a new vector with the sum. */
  public Vector plus(Vector that) {
    Vector result = new Vector();
    result.x = this.x + that.x;
    result.y = this.y + that.y;
    result.z = this.z + that.z;
    return result;
  }
  
  /** Subtract the given vector from this vector, and return a new vector with the difference. */
  public Vector minus(Vector that) {
    Vector result = new Vector();
    result.x = this.x - that.x;
    result.y = this.y - that.y;
    result.z = this.z - that.z;
    return result;
  }

  /** Multiply this vector by a scalar, and return a new Vector with the result. */
  public Vector times(double val) {
    return new Vector(val*this.x, val*this.y, val*this.z);
  }

  /** 
   The angle between this vector and the given vector.
   Returns radians in range 0..pi. 
  */
  public double angleWith(Vector that) {
    double cosθ = dot(that) / (this.length() * that.length());
    return Math.acos(cosθ); //0..pi
  }
  
  public double length() {
    return Math.sqrt(x*x + y*y + z*z);
  }
  
  /** Return a unit vector in the direction of this vector. */
  public Vector unit() {
    return this.times(1.0/length());  
  }

  /** Debugging only. */
  @Override public String toString() {
    return "x:" + x + "  y:" + y + " z:" + z;
  }
}
