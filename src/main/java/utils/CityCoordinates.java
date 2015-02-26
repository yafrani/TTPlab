package utils;

/**
 * city coordinates
 * 
 * @author kyu
 *
 */
public class CityCoordinates {
  private double x;
  private double y;
  
  public CityCoordinates() {
  }
  
  public CityCoordinates(double x, double y) {
    this.x = x;
    this.y = y;
  }
  
  @Override
  public String toString() {
    return "(" + String.format("%.2f", this.x) + "," + 
                 String.format("%.2f", this.y) + ")";
  }
  
  // setters
  public void setX(double x) {
    this.x = x;
  }
  public void setY(double y) {
    this.y = y;
  }
  
  // getters
  public double getX() {
    return x;
  }
  public double getY() {
    return y;
  }
  
  // Euclid distance
  public double distanceEuclid(CityCoordinates c2) {
    
    double P = this.x - c2.x;
    double Q = this.y - c2.y;
    
    return Math.sqrt(P*P + Q*Q);
  }
}
