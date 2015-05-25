package mantesting;

import solver.Constructive;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import utils.Quicksort;
import utils.TwoOptHelper;

/**
 * junk testing
 */
public class TestJunk {


  public static void main(String[] args) {

    String inst = "berlin52-ttp/berlin52_n153_bounded-strongly-corr_10.ttp";
    TTP1Instance ttp = new TTP1Instance("./TTP1_data/"+inst);
    Deb.echo(ttp);

    Constructive construct = new Constructive(ttp);
//    TTPSolution s0 = construct.generate("lr");
//    ttp.objective(s0);
//    Deb.echo("ob  : "+s0.ob);
//    Deb.echo("wend: "+s0.wend);
//    Deb.echo("==================");

    // problem data
    int nbItems = ttp.getNbItems(), nbCities = ttp.getNbCities();
    long[][] D = ttp.getDist();
//    int[] tour = s0.getTour();
//    int[] pickingPlan = s0.getPickingPlan();
    int[] A = ttp.getAvailability();
    double maxSpeed = ttp.getMaxSpeed();
    double minSpeed = ttp.getMinSpeed();
    long capacity = ttp.getCapacity();
    double C = (maxSpeed - minSpeed) / capacity;
    double R = ttp.getRent();


    // include in TTP init...
//    ttp.clusterItems();
//    ArrayList<Integer> [] cl = ttp.getClusters();
//    for (ArrayList<Integer> x : cl) {
//      System.out.println(x);
//    }

    int N = 10;
    Double fit1[] = new Double[N], fit2[] = new Double[N];
    TTPSolution s = construct.generate("lg");
    ttp.objective(s); double obj = s.ob;
    int[] tour = s.getTour();
    int[] pickingPlan = s.getPickingPlan();
    Deb.echo(">>>"+obj);
    for (int k=0; k<N; k++) {

      // bit-flip
      pickingPlan[k] = pickingPlan[k] != 0 ? 0 : A[k];

      ttp.objective(s);
      fit1[k] = s.ob;

      /* fitness test... */
      long deltaW = pickingPlan[k]==0 ? -ttp.weightOf(k) : ttp.weightOf(k);
      int origBF = s.mapCI[A[k] - 1];
      double A1 = origBF == 0 ? 0 : s.timeAcc[origBF - 1];
      fit2[k] = A1 - (1.0/deltaW)*(obj - A1);

      Deb.echo(fit1[k]+" || "+fit2[k]);

      // bit-flip
      pickingPlan[k] = pickingPlan[k] != 0 ? 0 : A[k];
    }

    ttp.objective(s); obj = s.ob;
    Deb.echo(">>>"+obj);

    Quicksort<Double> qs = new Quicksort<>(fit1);
    qs.sort();
    int x1[] = qs.getIndices();
    Deb.echo(x1);

    qs = new Quicksort<>(fit2);
    qs.sort();
    int x2[] = qs.getIndices();
    Deb.echo(x2);
  }






  public static void mainx(String[] args) {
    int[] tour = {1,3,5,4,6,2};
    int nbCities = tour.length;

    // map indices to their associated cities
    int[] mapIC = new int[nbCities];
    for (int i=0; i<nbCities; i++) {
      mapIC[tour[i]-1] = i+1;
    }
    //Deb.echol("idx  : ");Deb.echo(new int[]{1,2,3,4,5,6});
    Deb.echol("tour : ");Deb.echo(tour);
    Deb.echol("wacc: "); Deb.echo(mapIC);
    Deb.echol("tacc: "); Deb.echo(mapIC);
    //Deb.echol(": "); Deb.echo(mapIC);
  }

  public static void main2(String[] args) {

    /* instance information */
    String inst = "my-ttp/sample-data-10.ttp";
    inst = "eil51-ttp/eil51_n50_uncorr_01.ttp";
    inst = "a280-ttp/a280_n279_uncorr_01.ttp";
    TTP1Instance ttp = new TTP1Instance("./TTP1_data/"+inst);
    Deb.echo(ttp);

    /* initial solution s0 */
    Constructive construct = new Constructive(ttp);
    TTPSolution s0 = construct.generate("sg");
    //s0.setTour(new int[]{1,3,5,4,6,2});
    ttp.objective(s0);

    /* before */
    int[] x = s0.getTour();
    int[] z = s0.getPickingPlan();
    int[] A = ttp.getAvailability();
    Deb.echo();
    //Deb.echol("z   : "); Deb.echo(z,"%4d");
    Deb.echol("x   :"); Deb.echo(x,"%4d");
    Deb.echol("wacc:"); Deb.echo(s0.weightAcc,"%4d");
    Deb.echol("tacc:"); Deb.echo(s0.timeAcc,"%4.0f");

    //Deb.echo("ob = "+s0.ob);
    //Deb.echo("ft = "+s0.ft);
    //Deb.echo("fp = "+s0.fp);
    Deb.echo();
    //if (true) return;

    /* after */
    int i=2, j=16; // 2-opt
    TwoOptHelper.do2opt(x, i, j);
    //int k=3;      // bit-flip
    //z[k] = z[k]!=0 ? 0 : A[k];

    // compute objective value
    ttp.objective(s0);

    Deb.echo("neighbor solution:");
    //Deb.echol("z   :"); Deb.echo(z,"%4d");
    Deb.echol("x   :"); Deb.echo(x,"%4d");
    Deb.echol("wacc:"); Deb.echo(s0.weightAcc,"%4d");
    Deb.echol("tacc:"); Deb.echo(s0.timeAcc,"%4.0f");

    //Deb.echol("wr:"); Deb.echo(s0.weightRec,"%4d");
    //Deb.echo("ob = "+s0.ob);
    //Deb.echo("ft = "+s0.ft);
    //Deb.echo("fp = "+s0.fp);

  }
}
