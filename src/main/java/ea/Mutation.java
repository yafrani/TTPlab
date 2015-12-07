package ea;

import solver.Evolution;

import java.util.Random;

/**
 * Created by kyu on 12/6/15.
 */
public class Mutation {


  // mutate2opt by swapping two cities
  // use 2-opt
  // TODO must be adjusted to [0,n-1]
  public void mutate2opt(int[] t) {

    Random gen = new Random();
    int pos2;

    for (int pos1=2; pos1<=t.length; pos1++) {
      if (Math.random()< Evolution.MUTATION_RATE) {
        pos2 = gen.nextInt(t.length-1) + 2;
        int pos=pos1+pos2+1;
        for (int i=pos1+1; i<=(pos1+pos2)/2; i++) {
          int tmp = t[i];
          t[i] = t[pos-i];
          t[pos-i] = tmp;
        }
      }
    }
  }

}
