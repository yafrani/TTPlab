package ea;

import solver.Constructive;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import utils.TwoOptHelper;

import java.util.*;

/**
 * Created by kyu on 10/24/15.
 */
public class Crossover {

  /**
   * 2-opt search
   *
   * deal with the TSKP sub-problem
   * 2-opt heuristic with Delaunay candidate generator
   */
  public static TTPSolution ls2opt(TTPSolution sol, TTP1Instance ttp) {

    // TTP data
    int nbCities = ttp.getNbCities();
    int nbItems = ttp.getNbItems();
    double maxSpeed = ttp.getMaxSpeed();
    double minSpeed = ttp.getMinSpeed();
    long capacity = ttp.getCapacity();
    double C = (maxSpeed - minSpeed) / capacity;

    // initial solution data
    int[] tour;

    // delta parameters
    double deltaT;

    // improvement indicator
    boolean improved;

    // best solution
    ttp.objective(sol);
    int iBest=0, jBest=0;
    double ftBest = sol.ft;

    // neighbor solution
    double ft;
    long wc;
    int i, j, c1, c2, q;
    int nbIter = 0;

    // Delaunay triangulation
    ArrayList<Integer>[] candidates = ttp.delaunay();

    // current tour
    tour = sol.getTour();




    // search
    do {
    improved = false;
    nbIter++;

    // cleanup and stop execution if interrupted

    // fast 2-opt
    for (i = 1; i < nbCities - 1; i++) {
      int node1 = tour[i] - 1;
//      for (j=i+1; j<nbCities; j++) {
      for (int node2 : candidates[node1]) {
        j = sol.mapCI[node2];

        // calculate final time with partial delta
        ft = sol.ft;
        wc = i - 2 < 0 ? 0 : sol.weightAcc[i - 2]; // fix index...
        deltaT = 0;
        for (q = i - 1; q <= j; q++) {

          wc += TwoOptHelper.get2optValue(q, sol.weightRec, i, j);
          c1 = TwoOptHelper.get2optValue(q, tour, i, j) - 1;
          c2 = TwoOptHelper.get2optValue((q + 1) % nbCities, tour, i, j) - 1;

          deltaT += -sol.timeRec[q] + ttp.distFor(c1,c2) / (maxSpeed - wc * C);
        }

        // retrieve neighbor's final time
        ft = ft + deltaT;

        // update best
        if (ft < ftBest) { // soft condition
          iBest = i;
          jBest = j;
          ftBest = ft;
          improved = true;

        }

      } // END FOR j
    } // END FOR i


    //===================================
    // update if improvement
    //===================================
    if (improved) {

      // apply 2-opt move
      TwoOptHelper.do2opt(tour, iBest, jBest);

      // evaluate & update vectors
      ttp.objective(sol);
    }

    // debug msg
    Deb.echo(">> TSKP " + nbIter +
      ": ob=" + String.format("%.0f", sol.ob) +
      " | ft=" + String.format("%.0f", sol.ft));
    } while (improved);


    // in order to compute sol.timeAcc
    // we need to use objective function
    ttp.objective(sol);
    return sol;
  }

//je taime
  // partition crossover
  public static TTPSolution[] PX(TTPSolution p1, TTPSolution p2) {

    TTPSolution c1 = p1.clone(), c2 = p2.clone();
    int[] ct1 = c1.getTour();
    int[] ct2 = c2.getTour();
    int nbCities = ct1.length;

    //------------------------
    // ci1 = mapCI...
    int[] ci1 = new int[nbCities];
    int[] ci2 = new int[nbCities];
    for (int c=0;c<nbCities;c++) {
      ci1[ct1[c]-1] = c;
      ci2[ct2[c]-1] = c;
    }
    //------------------------

    int[] LIST1 = new int[nbCities];
    int[] LIST2 = new int[nbCities];

    Neighbors[] nlist = new Neighbors[nbCities];
    int j=0, k=nbCities-1;
    for (int c=0; c<nbCities; c++) {
      nlist[c] = new Neighbors();
      nlist[c].city = c;
      int i1 = ci1[c];
      int i2 = ci2[c];

      // tour 1
      int next1 = i1+1==nbCities ? 0:i1+1;
      int prev1 = i1-1==-1 ? nbCities-1:i1-1;
      nlist[c].L1 = ct1[next1]-1;
      nlist[c].R1 = ct1[prev1]-1;
      // tour 2
      int next2 = i2+1==nbCities ? 0:i2+1;
      int prev2 = i2-1==-1 ? nbCities-1:i2-1;
      nlist[c].L2 = ct2[next2]-1;
      nlist[c].R2 = ct2[prev2]-1;

      if (nlist[c].L1==nlist[c].L2) {
        nlist[c].degree -= 1;
      }
      if (nlist[c].R1==nlist[c].R2) {
        nlist[c].degree -= 1;
      }
      if (nlist[c].L1==nlist[c].R2) {
        nlist[c].degree -= 1;
      }
      if (nlist[c].R1==nlist[c].L2) {
        nlist[c].degree -= 1;
      }

      // initialize LIST1 and LIST2
      if (nlist[c].degree==2) {
        LIST1[j] = c;
        LIST2[c] = j;
        j++;
      }
      else {
        LIST1[k] = c;
        LIST2[c] = k;
        k--;
      }
      Deb.echo(nlist[c]);
    }

    Deb.echo(j+":j // k:"+k);
    Deb.echol("LST1: ");
    Deb.echo(LIST1);
    Deb.echol("LST2: ");
    Deb.echo(LIST2);
    //if (j==nbCities-1) return null;








    int[] FIFO = new int[nbCities];
    // head & tail
    int h,t;
    h = t = nbCities-1;
    //j--;
    for ( ; j<nbCities; j++) {
      if (nlist[LIST1[j]].degree>2) {
        Deb.echo("FF:"+LIST1[j]);
        FIFO[t] = LIST1[j];
        break;
      }
    }
    Deb.echo("j >> "+j);

    // ===============
    Deb.echol("IDX: ");
    for (int c=0; c<nbCities; c++)
      Deb.echol(String.format("%3d,",c));
    Deb.echo("\n-------");
    // ===============

    // tag for processed elements
    boolean[] tag = new boolean[nbCities];
    // tag for already in FIFO
    boolean[] tagFIFO = new boolean[nbCities];

    // process all elements
    int i = nbCities-1;
    do {
      int a = FIFO[t--];
      Deb.echo("-------");
      Deb.echo("AT: "+a);
      //nlist[a].degree--;

      // degree should be 3 or 4
      if (nlist[a].degree<=2) continue;

      // skip if already processed
      if (tag[a]) continue;

      // tag as already processed
      tag[a] = true;

      // index of a
      int ia = LIST2[a];
      Deb.echo("a:"+a+" > ia:"+ia);
      if (
          // not a common edge
          nlist[a].L1!=nlist[a].L2 && nlist[a].L1!=nlist[a].R2 &&
          // not yet processed
          !tag[nlist[a].L1] &&
          // not already in FIFO
          !tagFIFO[nlist[a].L1]
        ) {
        FIFO[--h] = nlist[a].L1;
        tagFIFO[nlist[a].L1] = true;
        //nlist[a].L1=-1;
      }
      if (
          nlist[a].R1!=nlist[a].R2 && nlist[a].R1!=nlist[a].L2 &&
          !tag[nlist[a].R1] &&
          !tagFIFO[nlist[a].R1]
        ) {
        FIFO[--h] = nlist[a].R1;
        tagFIFO[nlist[a].R1] = true;
        //nlist[a].R1=-1;
      }
      if (
          nlist[a].L2!=nlist[a].L1 && nlist[a].L2!=nlist[a].R1 &&
          !tag[nlist[a].L2] &&
          !tagFIFO[nlist[a].L2]
        ) {
        FIFO[--h] = nlist[a].L2;
        tagFIFO[nlist[a].L2] = true;
        //nlist[a].L2=-1;
      }
      if (
          nlist[a].R2!=nlist[a].R1 && nlist[a].R2!=nlist[a].L1 &&
          !tag[nlist[a].R2] &&
          !tagFIFO[nlist[a].R2]
        ) {
        FIFO[--h] = nlist[a].R2;
        tagFIFO[nlist[a].R2] = true;
        //nlist[a].R2=-1;
      }

      // partition 1
      nlist[a].partition = 1;

      // swap a=LIST1[ia] with LIST1[i]
      Deb.echo("SWAP: "+a+"("+ia+") & "+LIST1[i]+"("+i+")");
      int tmp = LIST1[i];
      LIST1[i] = LIST1[ia];
      LIST1[ia] = tmp;

      // update indexing list
      LIST2[a] = i;
      LIST2[tmp] = ia;
      i--;
      Deb.echol(h+"/"+t+":"); //Deb.echo(FIFO);
      //Deb.echol("LIST1:"); Deb.echo(LIST1);
      //Deb.echol("LIST2:"); Deb.echo(LIST2);
      Deb.echo("-------");
      //break;
      Deb.echo("iii " + i);
    } while (h<=t);



    // =============================================
    Deb.echo("-->>>>> " + i);
    Deb.echol("     ");
    for (int c=0; c<nbCities; c++)
      Deb.echol(String.format("%3d,",c));
    Deb.echo();
    Deb.echol("LIST1:"); Deb.echo(LIST1);
    // =============================================


    boolean feasible = false;
    for (int u=i; u>=k; u--) {
      int x = nlist[LIST1[u]].degree;
      Deb.echo(u+":"+x);
      if (x>2) {
        feasible = true;
        break;
      }
    }
    if (!feasible) return null;

    Deb.echo("/!\\ PX is feasible !");

    for (int u=0; u<=k; u++) {
      Deb.echo("Surrogate search: "+u);

      if (
        // both edges are in P1
        nlist[nlist[LIST1[u]].L1].partition==1 && nlist[nlist[LIST1[u]].R1].partition==1 ||
          // one edge is part... and another is common
          nlist[nlist[LIST1[u]].L1].partition==1 && nlist[nlist[LIST1[u]].R1].degree==2 ||
          nlist[nlist[LIST1[u]].R1].partition==1 && nlist[nlist[LIST1[u]].L1].degree==2

        ) {
        nlist[LIST1[u]].partition=1;
      }

    }

    // ====================================
    for (int u=0; u<nbCities; u++) {
      if (nlist[u].partition==1)
        Deb.echo("P1: "+nlist[u]);
    }









    // locate the partition
    int aMin = Integer.MAX_VALUE, aMax = Integer.MIN_VALUE;
    for (int u=i+1; u<nbCities; u++) {
      int curri = ci1[LIST1[u]];
      Deb.echol( LIST1[u] + "," );
      if (curri<aMin) aMin = curri;
      if (curri>aMax) aMax = curri;
    }
    Deb.echo();

    Deb.echo(aMin+"|"+aMax);
    Deb.echol("      "); Deb.echoz(ct1);
    Deb.echol("      "); Deb.echoz(ct2);
    for (int u=aMin; u<=aMax; u++) {
      int tmp = ct1[u];
      ct1[u] = ct2[u];
      ct2[u] = tmp;
    }
    Deb.echo();
    Deb.echol("      "); Deb.echoz(ct1);
    Deb.echol("      "); Deb.echoz(ct2);


    //==================================
    // check it
    TreeSet<Integer> ts = new TreeSet<>();
    for(int cc:ct1) ts.add(cc);
    Deb.echo("OK? "+ts.size()+"/"+(ts.size()==nbCities));
    //==================================

    return new TTPSolution[]{c1,c2};
  }





  // testing
  public static void main(String[] args) {

//    final TTP1Instance ttp = new TTP1Instance("./TTP1_data/u574-ttp/u574_n573_bounded-strongly-corr_01.ttp");
    final TTP1Instance ttp = new TTP1Instance("./TTP1_data/berlin52-ttp/berlin52_n51_bounded-strongly-corr_01.ttp");
//    final TTP1Instance ttp = new TTP1Instance("./TTP1_data/rl11849-ttp/rl11849_n11848_uncorr_07.ttp");
//    final TTP1Instance ttp = new TTP1Instance("./TTP1_data/a280-ttp/a280_n279_bounded-strongly-corr_01.ttp");
    Deb.echo(ttp);
    Deb.echo("------------------");
//    int[] A = ttp.getAvailability();
    Constructive construct = new Constructive(ttp);


    // parent 1
    TTPSolution p1 = construct.generate("rz");
//    p1.setTour(new int[]{1, 34, 35, 36,  3, 17,  7,  2, 42, 21, 31, 24,  5,  6, 15, 38, 40, 39, 37, 48, 46, 44, 23, 30, 20, 50, 16, 29, 47, 13, 14, 52, 11, 51, 33, 12, 28, 27, 26, 25,  4, 43, 45, 19, 41,  8,  9, 10, 32, 49, 18, 22});
//    int[] raw_pp = new int[]{2,3,4,19,20,21,33,34,35,43,49,50,51};
//    int[] pp = new int[ttp.getNbItems()];
//    for (int r:raw_pp) pp[r-1] = A[r-1];
//    p1.setPickingPlan(pp);
    p1 = ls2opt(p1, ttp);
    ttp.objective(p1);


    // parent 2
    TTPSolution p2 = construct.generate("rz");
//    p2.setTour(new int[]{1, 22, 31, 18,  3, 17, 50, 20, 23, 21, 42,  7,  2, 30, 29, 47, 26, 27, 13, 14, 52, 11, 51, 12, 28, 46, 16, 44, 37, 34, 35, 36, 39, 40, 38, 48, 24,  5, 15,  6, 25,  4, 43, 33, 10,  9,  8, 41, 19, 45, 32, 49});
//    int[] raw_pp2 = new int[]{2,3,4,19,20,21,33,34,35,43,49,50,51};
//    int[] pp2 = new int[ttp.getNbItems()];
//    for(int r:raw_pp) pp[r-1] = A[r-1];
//    p2.setPickingPlan(pp);
    p2 = ls2opt(p2,ttp);
    ttp.objective(p2);


    // ===

    Deb.echo(p1.getTour());
    Deb.echo("OBJ: " + p1.ob);
    Deb.echo(p2.getTour());
    Deb.echo("OBJ: " + p2.ob);

    PX(p1, p2);
  }
}
