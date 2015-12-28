package ea;

import ttp.TTPSolution;
import utils.Deb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * Created by kyu on 11/2/15.
 */
public class ERX2 {

  /**
   * Neighbors list element
   *
   * Created by kyu on 10/25/15.
   */
  public static class Neighbors {

    // city
    public int city;

    // number of neighbors
    public int degree = 2;

    // L1: left neighbor in first parent
    public int L1=-1;
    public int R1=-1;
    public int L2=-1;
    public int R2=-1;

    @Override
    public String toString() {
      return String.format("%3d",city)+": ["+degree+"] "+
        String.format("%3d %3d ", L1,L2);
    }
  }


  // Edge Recombination Crossover
  // applied to tour independently of the picking plan
  public static TTPSolution crossover(TTPSolution p1, TTPSolution p2) {

    // tours
    int[] pt1 = p1.getTour();
    int[] pt2 = p2.getTour();

    // picking plans
    int[] pp1 = p1.getPickingPlan();
    int[] pp2 = p2.getPickingPlan();

    // problem dimensions
    int nbCities = pt1.length;
    int nbItems = pp1.length;

    //------------------------
    // ci1 = mapCI...
    int[] pi1 = new int[nbCities];
    int[] pi2 = new int[nbCities];
    for (int c=0;c<nbCities;c++) {
      pi1[pt1[c]-1] = c;
      pi2[pt2[c]-1] = c;
    }
    //------------------------


    /////////////////////////
    // extract neighbors
    /////////////////////////
    Neighbors[] nlist = new Neighbors[nbCities];

    for (int c=0; c<nbCities; c++) {
      nlist[c] = new Neighbors();
      nlist[c].city = c;
      int i1 = pi1[c];
      int i2 = pi2[c];

      // tour 1
      int next1 = i1+1==nbCities ? 0:i1+1;
      int prev1 = i1-1==-1 ? nbCities-1:i1-1;
      nlist[c].L1 = pt1[next1]-1;
      nlist[c].R1 = pt1[prev1]-1;

      // tour 2
      int next2 = i2+1==nbCities ? 0:i2+1;
      int prev2 = i2-1==-1 ? nbCities-1:i2-1;
      nlist[c].L2 = pt2[next2]-1;
      nlist[c].R2 = pt2[prev2]-1;

      // in this cas, the degree is only related to the left neighbors
      if (nlist[c].L1==nlist[c].L2) {
        nlist[c].degree -= 1;
      }

//      Deb.echo(nlist[c]);
    }



    ///////////////////////
    // generate child
    ///////////////////////

    // child tour
    ArrayList<Integer> visited = new ArrayList<>(nbCities);
    int[] ct1 = new int[nbCities];

    // child picking plan
    int[] cp1 = new int[nbItems];

    // save parent ID
    // in order to crossover the pick plan
    int pid = 1; // default is 1
    int ones=0, twos=0;
    int x = pt1[0]-1;
    for (int i=0; i<nbCities; i++) {
      ct1[i] = x+1;
      visited.add(x);

      int imin = -1;

      if (nlist[x].L1!=-1 || nlist[x].L2!=-1) {

        // choose least full node
        int y1 = nlist[x].L1;
        int y2 = nlist[x].L2;
        if (y2==-1) {
          imin = y1;
          pid = 1;ones++;
        }
        else if (y1==-1) {
          imin = y2;
          pid = 2;twos++;
        }
        else if (nlist[y1].degree < nlist[y2].degree) {
          imin = y1;
          pid = 1;ones++;
        }
        else if (nlist[y1].degree > nlist[y2].degree) {
          imin = y2;
          pid = 2;twos++;
        }
        else {
          // choose randomly
          if (Math.random()>.5) {
            imin = y1;
            pid = 1;ones++;
          }
          else {
            imin = y2;
            pid = 2;twos++;
          }
        }

      }
      // handle the problem of empty neighbors list
      else {
        for (int j=0;j<nbCities;j++) {
          if (!visited.contains(j)) {
            imin = j;
            pid = 0;
            break;
          }
        }
      }

      // remove current node from neighbors list
      int z1 = nlist[x].R1;
      int z2 = nlist[x].R2;
      if (nlist[z1].L1 == x) {
        nlist[z1].L1 = -1;
        nlist[z1].degree--;
      }
      if (nlist[z1].L2 == x) {
        nlist[z1].L2 = -1;
        nlist[z1].degree--;
      }
      if (nlist[z2].L1 == x) {
        nlist[z2].L1 = -1;
        nlist[z2].degree--;
      }
      if (nlist[z2].L2 == x) {
        nlist[z2].L2 = -1;
        nlist[z2].degree--;
      }

//      Deb.echo(">"+x+":"+pid+" => "+ones+" / "+twos);

      x = imin;
    }

//    Deb.echo("||STATS|| ones:"+ones+" || twos:"+twos);

//    //==================================
//    // check resulting tour
//    // for redundancies
//    TreeSet<Integer> ts = new TreeSet<>();
//    for(int cc:ct1) ts.add(cc);
//    Deb.echo("OK? "+ts.size()+" / "+(ts.size()==nbCities));
//    //==================================

    return new TTPSolution(ct1, p1.getPickingPlan().clone());
  }


}
