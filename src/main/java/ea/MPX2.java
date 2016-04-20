package ea;

import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;

/**
 * Created by kyu on 12/24/15.
 */
public class MPX2 {

  // maximal preservative tour
  // uniform picking plan
  public static TTPSolution crossover(TTPSolution p1, TTPSolution p2, TTP1Instance ttp) {

    // tours
    int[] pt1 = p1.getTour();
    int[] pt2 = p2.getTour();

    // picking plans
    int[] pp1 = p1.getPickingPlan();
    int[] pp2 = p2.getPickingPlan();

    // problem dimensions
    int nbCities = pt1.length;
    int nbItems = pp1.length;

    int[] ct1 = new int[nbCities];
    //int[] c2 = new Tour(nbCities);

    /*==================================
     * PART I: tour
     *==================================*/
    // generate 2 crossover cut points
    Random gen = new Random();
    int pos1, pos2;
    do {
      pos1 = gen.nextInt(nbCities-1);
      pos2 = gen.nextInt(nbCities-1);
    } while ( pos2<=pos1 || pos1==0 );

//    pos1=2;pos2=6;
//    Deb.echo(pos1 + "/" + pos2);

    // fill child's center
    for (int i=pos1; i<=pos2; i++) {
      ct1[i] = pt2[i];
    }

    int[] map = new int[nbCities+1];

    for (int i=pos1; i<=pos2; i++) {
      if ( map[pt1[i]]==0 && map[pt2[i]]==0 ) {
        map[pt1[i]] = pt2[i];
        map[pt2[i]] = pt1[i];
      }
      else {
        if (map[pt1[i]] != 0) { // OK

          int tmp = map[pt2[i]];
          map[pt2[i]] = map[pt1[i]];
          map[map[pt1[i]]] = pt2[i];
          map[pt1[i]] = 0;

          if (tmp != 0) {
            map[tmp] = map[pt2[i]];
            map[map[pt2[i]]] = tmp;//p1.tour[i];
            map[pt2[i]] = 0;
          }
        }

        else if (map[pt2[i]] != 0) {
          map[pt1[i]] = map[pt2[i]];
          map[map[pt2[i]]] = pt1[i];
          map[pt2[i]] = 0;
        }
      }
    }

    // fill child's allele
    for (int i=0; i<nbCities; i++) {
      // jump to other allele
      if (i==pos1) i=pos2+1;

      // affect mapped node if existing, parent node otherwise
      ct1[i] = map[pt1[i]]!=0 ? map[pt1[i]] : pt1[i];
      //c2.tour[i] = map[p2.tour[i]]!=0 ? map[p2.tour[i]] : p2.tour[i];
    }

//    //==================================
//    // check resulting tour
//    // for redundancies
//    TreeSet<Integer> ts = new TreeSet<>();
//    for(int cc:ct1) ts.add(cc);
//    Deb.echo("OK? "+ts.size()+" / "+(ts.size()==nbCities));
//    //==================================


//    // no pick plan crossover...
//    TTPSolution sol1 = new TTPSolution(ct1, pp1.clone());
//    if (true) return sol1;


    /*==================================
     * PART II: pick plan
     *==================================*/
    // fill cpp1 with bits according to cities
    // j is item ID
    // i is city ID
    // child pick plan
    int[] cpp1 = new int[nbItems];
    // TTP data
    ArrayList<Integer>[] clusters = ttp.getClusters();
    int[] weights = ttp.getWeights();
    double capacity = ttp.getCapacity();
    //for (int i=0; i<nbItems; i++) cpp1[i] = -1;
    int wacc = 0;
    int pid;
    boolean stopUX = false;
    for (int i=1; i<nbCities; i++) {
      if (i<pos1 || i>pos2) {
        pid = 1;
      }
      else {
        pid = 2;
      }

      // check only items contained in current city
      for (int j : clusters[ ct1[i]-1 ]) {
//        Deb.echo( "c: "+ct1[i] + " >> " + (j+1) );
        cpp1[j] = pid==1 ? pp1[j] : pp2[j];
        // if item is picked
        if (cpp1[j] != 0) {
          wacc += weights[j];
        }
        // check if capacity is exceeded
        if (wacc > capacity) {
          stopUX = true;
          cpp1[j] = 0;
          break;
        }
      }
      // enough items picked... get out
      if (stopUX) break;
    }

//    Deb.echo(">>>>>>>>>>>>>>"+wacc);

    // combine and return
    TTPSolution sol = new TTPSolution(ct1, cpp1);
//    ttp.objective(sol);
//    Deb.echo("f:"+sol.ob+" / we:"+sol.wend+" / w:"+(capacity-sol.wend));

    return sol;
  }

}
