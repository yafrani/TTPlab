package ea;

/**
 * Neighbors list element
 *
 * Created by kyu on 10/25/15.
 */
public class Neighbors {

  // city
  public int city;

  // number of neighbors
  public int degree = 4;

  // L1: left neighbor in first parent
  public int L1=-1;
  public int R1=-1;
  public int L2=-1;
  public int R2=-1;

  // assigned partition
  public int partition = 0;

  @Override
  public String toString() {
    // true if common edge
    boolean L1c, R1c, L2c, R2c;
    L1c = L2c = L1==L2;
    R1c = R2c = R1==R2;

    return String.format("%3d",city)+": ["+degree+"] "+
      String.format("%3d"+(L1c?"*":" ")+
          " %3d"+(R1c?"*":" ")+
          " %3d"+(L2c?"*":" ")+
          " %3d"+(R2c?"*":" ")+" ",
        L1,R1,L2,R2);
  }
}
