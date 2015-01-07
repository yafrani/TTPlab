package mantesting;
import solver.Constructive;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import utils.TwoOptHelper;
/**
 * junk testing
 */
public class JunkTester {
  
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
    String inst = "my-ttp/sample-data1.ttp";
    
    TTP1Instance ttp = new TTP1Instance("./TTP1_data/"+inst);
    Deb.echo(ttp);
    
    /* initial solution s0 */
    Constructive construct = new Constructive(ttp);
    TTPSolution s0 = construct.generate("sg");
//    s0.setTour(new int[]{1, 3, 2, 5, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 16, 15, 17, 19, 18, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51});
//    s0.setPickingPlan(new int[]{2, 3, 4, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 0, 0, 0, 41, 0, 0, 44, 45, 46, 47, 48, 49, 50, 51});
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
    int i=2, j=6; // swap
    //SwapHelper.doSwap(x, i);
    TwoOptHelper.do2opt(x, i, j);
    int k=0;      // bit-flip
    //z[k] = z[k]!=0 ? 0 : A[k];
    
    
    Deb.echol("x:"); Deb.echo(x,"%4d");
    ttp.objective(s0);
    
    Deb.echol("w:"); Deb.echo(s0.weightAcc,"%4d");
    Deb.echol("t:"); Deb.echo(s0.timeAcc,"%4.0f");
    Deb.echo("ob: "+s0.ob);
    Deb.echo("ft: "+s0.ft);
    Deb.echo("fp: "+s0.fp);
    
  }
}
