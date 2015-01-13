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
  
  public static void main2(String[] args) {
    int nbCities = 10;
    int[] tour = {3,2,4,1,7,5,6,9,10,8};
    Deb.echo(tour);
    
    // map indices to their associated cities
    int[] mapIC = new int[nbCities];
    for (int i=0; i<nbCities; i++) {
      mapIC[tour[i]-1] = i+1;
    }
    Deb.echo(mapIC);
  }
  
  public static void main(String[] args) {
    
    /* instance information */
    String inst = "eil51-ttp/eil51_n50_bounded-strongly-corr_01.ttp";
    
    TTP1Instance ttp = new TTP1Instance("./TTP1_data/"+inst);
    Deb.echo(ttp);
    
    /* initial solution s0 */
    Constructive construct = new Constructive(ttp);
    TTPSolution s0 = construct.generate("lg");
    s0.setTour(new int[]{1, 26, 31, 8, 22, 28, 3, 36, 35, 20, 2, 29, 21, 16, 50, 34, 30, 9, 49, 10, 39, 33, 45, 15, 44, 42, 40, 19, 41, 13, 25, 14, 24, 43, 7, 23, 48, 6, 27, 51, 46, 12, 47, 18, 4, 17, 37, 5, 38, 11, 32});
    s0.setPickingPlan(new int[]{2, 3, 4, 5, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 36, 0, 38, 0, 0, 0, 0, 0, 44, 0, 46, 0, 0, 0, 50, 51});
    ttp.objective(s0);
//    Deb.echo("s0: \n"+s0);
//    Deb.echo("ob: "+s0.ob);
//    Deb.echo("we: "+s0.wend);
//    Deb.echo("==================");
    
    /* before */
    int[] x = s0.getTour();
    int[] z = s0.getPickingPlan();
    int[] A = ttp.getAvailability();
    Deb.echol("x:"); Deb.echo(x,"%4d");
    Deb.echol("w:"); Deb.echo(s0.weightAcc,"%4d");
    Deb.echol("t:"); Deb.echo(s0.timeAcc,"%4.0f");
    Deb.echo("ob: "+s0.ob);
    Deb.echo("ft: "+s0.ft);
    Deb.echo("fp: "+s0.fp);
    Deb.echo();
    
    /* after */
    int i=1, j=8; // swap
    //SwapHelper.doSwap(x, i);
    TwoOptHelper.do2opt(x, i, j);
    int k=34;      // bit-flip
    z[k] = z[k]!=0 ? 0 : A[k];
    
    
    Deb.echol("x:"); Deb.echo(x,"%4d");
    ttp.objective(s0);
    
    Deb.echol("w:"); Deb.echo(s0.weightAcc,"%4d");
    Deb.echol("t:"); Deb.echo(s0.timeAcc,"%4.2f");
    Deb.echol("wr:"); Deb.echo(s0.weightRec,"%4d");
    Deb.echo("ob: "+s0.ob);
    Deb.echo("ft: "+s0.ft);
    Deb.echo("fp: "+s0.fp);
    
  }
}
