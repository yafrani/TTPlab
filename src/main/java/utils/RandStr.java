package utils;

import java.util.Random;

public class RandStr {
  public static char randChar(){
    char min = 'A';
    char max = 'Z'+1;
    Random random = new Random();
    int r=random.nextInt(max - min) + min;
    return (char)r;
  }
  
  public static String rand(int n) {
    String s = "";
    for (int i=0; i<n; i++) {
      s+=randChar();
    }
    return s;
  }
}
