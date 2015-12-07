package ea;

import ttp.TTPSolution;
import utils.RandGen;

/**
 * Created by kyu on 12/6/15.
 */
public class StrCrossover {

  public static int[] SPX(int[] s1, int[] s2) {
    int[] c = new int[s1.length];
    int cp = RandGen.randInt(0, s1.length- 1);
    for (int i=0; i<cp; i++) {
      c[i] = s1[i];
    }
    for (int i=cp;i<s1.length; i++) {
      c[i] = s2[i];
    }

    return c;
  }

}
