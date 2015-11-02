package utils;

/**
 * simplify printing and debugging
 * 
 * @author kyu
 *
 */
public class Deb {
  
  public static void echo(Object s) {
    System.out.println(s);
  }
  public static void echol(Object s) {
    System.out.print(s);
  }
  
  public static void echo() {
    System.out.println();
  }

  public static void echoz(int[] x) {
    String s="";
    for (int i=0; i<x.length; i++)
      s += String.format("%2d",x[i]-1)+", ";
    s = s.substring(0,s.length()-2);
    echo(s+" #");
  }
  public static void echo(int[] x) {
    echo(x,"%2d");
  }
  public static void echo(int[] x,String forma) {
    String s="";
    for (int i=0; i<x.length; i++)
      s += String.format(forma,x[i])+", ";
    s = s.substring(0,s.length()-2);
    echo(s+" #");
  }

  public static void echo(long[] x) {
    echo(x,"%2d");
  }
  public static void echo(long[] x,String forma) {
    String s="";
    for (int i=0; i<x.length; i++)
      s += String.format(forma,x[i])+", ";
    s = s.substring(0,s.length()-2);
    echo(s+" #");
  }

  public static void echo(double[] x) {
    echo(x,"%.2f");
  }
  public static void echo(double[] x, String forma) {
    String s="";
    for (int i=0; i<x.length; i++)
      s += String.format(forma, x[i])+", "; // testing
    s = s.substring(0,s.length()-2);
    echo(s);
  }
}
