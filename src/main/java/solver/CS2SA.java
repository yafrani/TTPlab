package solver;

import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;


/**
 * CS2SA algorithm
 *
 * A CoSolver implementation that uses 2-opt
 * for the TSP component and SA for the KP
 * component
 *
 * Created by kyu on 4/7/15.
 */
public class CS2SA extends LocalSearch {

  public CS2SA() {
    super();
  }

  public CS2SA(TTP1Instance ttp) {
    super(ttp);
  }



  @Override
  public TTPSolution search() {

    //===============================================
    // generate initial solution
    //===============================================
    Constructive construct = new Constructive(ttp);
    // use Lin-Kernighan to initialize the tour
    TTPSolution s0 = new TTPSolution(
      construct.linkernTour(),
      construct.zerosPickingPlan()
    );
    // pre-process the knapsack
    // insert and eliminate items
    s0 = insertAndEliminate(s0);
    //===============================================

    // copy initial solution into improved solution
    TTPSolution sol = s0.clone();


    // best found
    double GBest = sol.ob;
    // number of iterations
    int nbIter = 0;
    // improvement tag
    boolean improved;

    //===============================================
    // start cosolver search
    //===============================================
    do {
      nbIter++;
      improved = false;

      // 2-opt heuristic on TSKP
      sol = fast2opt(sol);
      //if (true) break;

      // simple bit-flip on KRP
      sol = simulatedAnnealing(sol);

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
        Deb.echo("ob-best: "+sol.ob);
        Deb.echo("wend   : "+sol.wend);
        Deb.echo("---");
      }

      // stop when no improvements
    } while (improved);
    //===============================================

    return sol;
  }






}
