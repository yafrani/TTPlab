package ea;

import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import utils.GraphHelper;
import utils.TwoOptHelper;

import java.util.*;

/**
 * Created by kyu on 10/24/15.
 */
public class PartitionCrossover {

  /**
   * Neighbors list element
   */
  public static class Neighbors {

    // city
    public int city;

    // number of neighbors
    public int degree = 4;

    // L1: left neighbor in first parent
    public int L1=-1;
    public int R1=-1;
    public int L2=-1;
    public int R2=-1;

    // assigned partition
    public int partition = 0;

    @Override
    public String toString() {
      // true if common edge
      boolean L1c, R1c, L2c, R2c;
      L1c = L2c = L1==L2;
      R1c = R2c = R1==R2;

      return String.format("%3d",city)+": ["+degree+"] "+
        String.format("%3d"+(L1c?"*":" ")+
            " %3d"+(R1c?"*":" ")+
            " %3d"+(L2c?"*":" ")+
            " %3d"+(R2c?"*":" ")+" ",
          L1,R1,L2,R2);
    }
  }


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

//    for (int u=0; u<=k; u++) {
//      Deb.echo("Surrogate search: "+u);
//
//      if (
//          // both edges are in P1
//          nlist[nlist[LIST1[u]].L1].partition==1 && nlist[nlist[LIST1[u]].R1].partition==1 ||
//          // one edge is part... and another is common
//          nlist[nlist[LIST1[u]].L1].partition==1 && nlist[nlist[LIST1[u]].R1].degree==2 ||
//          nlist[nlist[LIST1[u]].R1].partition==1 && nlist[nlist[LIST1[u]].L1].degree==2
//        ) {
//        nlist[LIST1[u]].partition=1;
//      }
//
//    }



    // MATLAB vars generation =============================
    Deb.echo("%% MATLAB code generated by TTPSolver");
    ArrayList<Integer> part1 = new ArrayList<>();
    ArrayList<Integer> t1 = new ArrayList<>();
    ArrayList<Integer> t2 = new ArrayList<>();
    for (int u=0; u<nbCities; u++) {
      if (nlist[u].partition==1) {
        part1.add(u);
      }
    }
    for (int u=0; u<nbCities; u++) t1.add(ct1[u]);
    for (int u=0; u<nbCities; u++) t2.add(ct2[u]);
    Deb.echo("t1=" + t1 + ";");
    Deb.echo("t2=" + t2 + ";");
    Deb.echo("P1=" + part1 + ";");
    //=====================================================







//    // locate the partition
//    int aMin = Integer.MAX_VALUE, aMax = Integer.MIN_VALUE;
//    for (int u=i+1; u<nbCities; u++) {
//      int curri = ci1[LIST1[u]];
//      Deb.echol( LIST1[u] + "," );
//      if (curri<aMin) aMin = curri;
//      if (curri>aMax) aMax = curri;
//    }
//    Deb.echo();
//
//    Deb.echo(aMin+"|"+aMax);
//    Deb.echol("      "); Deb.echoz(ct1);
//    Deb.echol("      "); Deb.echoz(ct2);
//    for (int u=aMin; u<=aMax; u++) {
//      int tmp = ct1[u];
//      ct1[u] = ct2[u];
//      ct2[u] = tmp;
//    }
//    Deb.echo();
//    Deb.echol("      "); Deb.echoz(ct1);
//    Deb.echol("      "); Deb.echoz(ct2);


    //==================================
    // check it
    TreeSet<Integer> ts = new TreeSet<>();
    for(int cc:ct1) ts.add(cc);
    Deb.echo("OK? "+ts.size()+"/"+(ts.size()==nbCities));
    //==================================

    return new TTPSolution[]{c1,c2};
  }





}
