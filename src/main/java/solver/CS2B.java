package solver;

import ttp.TTP1Instance;

import ttp.TTPSolution;
import utils.Deb;


/**
 * Created by kyu on 4/7/15.
 */
public class CS2B extends LocalSearch {

  public CS2B() {
    super();
  }

  public CS2B(TTP1Instance ttp) {
    super(ttp);
  }



  @Override
  public TTPSolution search() {

    //==============================================
    // generate initial solution
    //==============================================
    Constructive construct = new Constructive(ttp);
    TTPSolution s0 = construct.generate("lz");
    // pre-process the knapsack
    // insert and eliminate items
    s0 = insertT2(s0);
    //==============================================

    // copy initial solution into improved solution
    TTPSolution sol = s0.clone();

    // best found
    double GBest = sol.ob;
    // number of iterations
    int nbIter = 0;
    // improvement tag
    boolean improved;


    // start cosolver search
    do {
      nbIter++;
      improved = false;

      // 2-opt heuristic on TSKP
      sol = fast2opt(sol);

      // simple bit-flip on KRP
      sol = lsBitFlip(sol);

      // update best if improvement
      if (sol.ob > GBest) {
        GBest = sol.ob;
        improved = true;
      }

      // stop execution if interrupted
      if (Thread.currentThread().isInterrupted()) return sol;

      // debug msg
      if (this.debug) {
        Deb.echo("Best "+nbIter+":");
        //Deb.echo(sol);
        Deb.echo("ob-best: "+sol.ob);
        Deb.echo("wend   : "+sol.wend);
        Deb.echo("---");
      }

      // stop when no improvements
    } while (improved);

    return sol;
  }


}
