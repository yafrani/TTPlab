package mantesting;

import solver.Constructive;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import utils.TwoOptHelper;

/**
 * junk testing
 */
public class TestJunk {

  /* program n_select=1000 times selects one of n=4 elements with weights weight[i].
   * Selections are summed up in counter[i]. For the weights as given in the example
   * below one expects that elements 0,1,2 and 3 will be selected (on average)
   * 200, 150, 600 and 50 times, respectively.  In good agreement with exemplary run.
   */
  public static void main(String [] args) {
    int n=4;
    double [] weight = new double [n];
    weight[0]=0.4;
    weight[1]=0.3;
    weight[2]=1.2;
    weight[3]=0.1;
    double max_weight=1.2;
    int  [] counter = new int[n];
    int n_select=1000;
    int index=0;
    boolean notaccepted;
    for (int i=0; i<n_select; i++){
      notaccepted=true;
      while (notaccepted){
        index= (int)(n*Math.random());
        if(Math.random()<weight[index]/max_weight) {notaccepted=false;}
      }
      counter[index]++;
    }
    for (int i=0; i<n; i++){
      System.out.println("counter["+i+"]="+counter[i]);
    }
  }
  public static void main2b(String[] args) {

    String inst = "eil51-ttp/eil51_n50_uncorr_01.ttp";
    TTP1Instance ttp = new TTP1Instance("./TTP1_data/"+inst);
    Deb.echo(ttp);

    Constructive construct = new Constructive(ttp);
    TTPSolution s0 = construct.generate("lr");
    ttp.objective(s0);
    Deb.echo("ob  : "+s0.ob);
    Deb.echo("wend: "+s0.wend);
    Deb.echo("==================");

    // problem data
    int nbItems = ttp.getNbItems(), nbCities = ttp.getNbCities();
    long[][] D = ttp.getDist();
    int[] tour = s0.getTour();
    int[] pickingPlan = s0.getPickingPlan();
    int[] A = ttp.getAvailability();
    double maxSpeed = ttp.getMaxSpeed();
    double minSpeed = ttp.getMinSpeed();
    long capacity = ttp.getCapacity();
    double C = (maxSpeed - minSpeed) / capacity;
    double R = ttp.getRent();

    // algo data
    int i, k, origBF;
    double t1;
    // scores
    double[] scores = new double[nbItems];
    // distances map
    long[] L = new long[nbCities];
    L[nbCities-1] = D[tour[nbCities-1] - 1][0];
    for (i=nbCities-2; i >= 0; i--) {
      L[i] = L[i+1] + D[tour[i+1] - 1][tour[i] - 1];
    }

    for (k = 0; k < nbItems; k++) {
      // index where Bit-Flip happened
      origBF = s0.mapCI[A[k] - 1];
      // calculate time approximations
      t1 = L[origBF]*(1/(maxSpeed-C*ttp.weightOf(k)) - 1/maxSpeed);
      // affect score to item
      scores[k] = (ttp.profitOf(k)-R*t1) / ttp.weightOf(k);
      // empty the knapsack
      pickingPlan[k] = 0;
    }

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
