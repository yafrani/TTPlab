package ea;

import ttp.TTPSolution;
import utils.Deb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * Created by kyu on 11/2/15.
 */
public class EdgeRecombination {

  /**
   * Neighbors list element
   *
   * Created by kyu on 10/25/15.
   */
  public static class Neighbors {

    // city
    public int city;

    // number of neighbors
    public int degree;

    // neighbors list
    HashSet<Integer> list = new HashSet<>(2);
    HashSet<Integer> rlist = new HashSet<>(2);

    @Override
    public String toString() {
      return String.format("%3d",city)+": ["+degree+"] "+list;
    }
  }

  // partition crossover
  public static TTPSolution ERX(TTPSolution p1, TTPSolution p2) {

    //TTPSolution c1 = p1.clone(), c2 = p2.clone();
    int[] pt1 = p1.getTour();
    int[] pt2 = p2.getTour();
    int nbCities = pt1.length;

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

    //int j=0, k=nbCities-1;
    for (int c=0; c<nbCities; c++) {
      nlist[c] = new Neighbors();
      nlist[c].city = c;
      int i1 = pi1[c];
      int i2 = pi2[c];

      // tour 1
      int next1 = i1+1==nbCities ? 0:i1+1;
      int prev1 = i1-1==-1 ? nbCities-1:i1-1;
      nlist[c].list.add(pt1[next1]-1); // L1
      nlist[c].rlist.add(pt1[prev1]-1); // R1
      // tour 2
      int next2 = i2+1==nbCities ? 0:i2+1;
      int prev2 = i2-1==-1 ? nbCities-1:i2-1;
      nlist[c].list.add(pt2[next2]-1); // L2
      nlist[c].rlist.add(pt2[prev2]-1); // R2

      nlist[c].degree = nlist[c].list.size();

    }



    ///////////////////////
    // generate child
    ///////////////////////
    ArrayList<Integer> visited = new ArrayList<>(nbCities);
    int[] ct1 = new int[nbCities];

    int x = pt1[0]-1;
    for (int i=0; i<nbCities; i++) {
//      Deb.echo("****");
      ct1[i] = x+1;
      visited.add(x);
//      Deb.echo("@ "+x);
      // choose least full node
      int min=10, imin=-1;
      for (int y : nlist[x].list) {
//        Deb.echol(""+y+"("+nlist[y].degree + ") // ");
        if (nlist[y].degree<min) {
          min = nlist[y].degree;
          imin = y;
        }
      }
//      Deb.echo("\n"+imin+"/"+min);

      // remove current node from neighbors list
      for (int y : nlist[x].rlist) {
//        Deb.echo("y:"+y+" x:"+x);
        nlist[y].list.remove(x);
        nlist[y].degree--;
      }
      if (imin==-1) { // handle problem
//        Deb.echo("/!\\ PROBLEM !!");
        for (int j=0;j<nbCities;j++) {
          if (!visited.contains(j)) {
            imin = j;
            break;
          }
        }
      }

      // print nlist
//      for (int c=0;c<nbCities;c++)
//        Deb.echo(nlist[c]);

      x = imin;

//      Deb.echo(nlist[x]);
//      Deb.echoz(ct1);
    }


    //==================================
//    Deb.echo("\nRESULT:");
//    Deb.echoz(pt1);
//    Deb.echoz(pt2);
//    Deb.echoz(ct1);




    //==================================
    // check resulting tour
    // for redundancies
//    TreeSet<Integer> ts = new TreeSet<>();
//    for(int cc:ct1) ts.add(cc);
//    Deb.echo("OK? "+ts.size()+" / "+(ts.size()==nbCities));
    //==================================

    return new TTPSolution(ct1, p1.getPickingPlan());
  }



}
