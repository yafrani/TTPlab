package utils;

public class Deb {
  
  /** print functions... */
  public static void echo(Object s) {
    System.out.println(s);
  }
  public static void echo2(Object s) {
    System.out.print(s);
  }
  
  public static void echo() {
    System.out.println();
  }
  
  public static void echo(int[] x) {
    String s="";
    for (int i=0; i<x.length; i++)
      s += String.format("%4d",x[i])+", ";
    s = s.substring(0,s.length()-2);
    echo(s+" #");
  }
  public static void echo(double[] x) {
    String s="";
    for (int i=0; i<x.length; i++)
      s += String.format("%.2f", x[i])+", ";
    s = s.substring(0,s.length()-2);
    echo(s);
  }
  
}
