package planisphere.math;

import planisphere.config.Constants;

/** A 3x3 rotation matrix. */
public final class Matrix {
  
  public Matrix(Vector row1, Vector row2, Vector row3){
    this.row1 = row1;
    this.row2 = row2;
    this.row3 = row3;
  }
  
  public Vector times(Vector v) {
    return new Vector(
      row1.dot(v),
      row2.dot(v),
      row3.dot(v)
    );
  }
  
  /** Debugging only. */
  @Override public String toString() {
    return row(row1) + Constants.NL + row(row2) + Constants.NL + row(row3); 
  }

  private Vector row1;
  private Vector row2;
  private Vector row3;
  private static final String BAR = "|";
  private static final String SEP = " ";
  
  private String row(Vector v) {
    return BAR + v.x + SEP + v.y + SEP + v.z + BAR;
  }
}
