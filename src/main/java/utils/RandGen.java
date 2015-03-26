package utils;

import java.util.Random;

public class RandGen {

  public static char randChar(){
    char min = 'A';
    char max = 'Z'+1;
    Random random = new Random();
    int r=random.nextInt(max - min) + min;
    return (char)r;
  }
  
  public static String randStr(int n) {
    String s = "";
    for (int i=0; i<n; i++) {
      s+=randChar();
    }
    return s;
  }

  public static int randInt(int min, int max) {

    Random rand = new Random();
    int randomNum = rand.nextInt((max - min) + 1) + min;

    return randomNum;
  }
}
