import solver.Constructive;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import utils.TwoOptHelper;

/**
 * junk testing
 */
public class JunkTester {
  
  public static void main(String[] args) {
    
    /* instance information */
    String inst = "sample-data.ttp";
    TTP1Instance ttp = new TTP1Instance("./TTP1_data/"+inst);
    Deb.echo(ttp);
    
    /* initial solution s0 */
    Constructive construct = new Constructive(ttp);
    TTPSolution s0 = construct.generate("sg");
    ttp.objective(s0);
    Deb.echo("s0: \n"+s0);
    Deb.echo("ob: "+s0.ob);
    Deb.echo("we: "+s0.wend);
    Deb.echo("==================");
    
    
    /* before */
    int[] x = s0.getTour();
    Deb.echo(x);
    Deb.echo(s0.weightAcc);
    Deb.echo(s0.timeRec);
    Deb.echo();
    
    /* after */
    // swap
    int i=3;
    TwoOptHelper.do2opt(x, 2, 5);
    
    Deb.echo(x);
    ttp.objective(s0);
    
    Deb.echo(s0.weightAcc);
    Deb.echo(s0.timeRec);
    Deb.echo("ob: "+s0.ob);
  }
}
