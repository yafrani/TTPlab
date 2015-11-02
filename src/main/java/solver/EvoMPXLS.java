package solver;

import ea.Population;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;

/**
 * Created by kyu on 10/25/15.
 */
public class EvoMPXLS extends Evolution {

  public EvoMPXLS() {
    super();
  }

  public EvoMPXLS(TTP1Instance ttp) {
    super(ttp);
  }


  @Override
  public TTPSolution search() {

    //===============================================
    // generate initial solution
    //===============================================
    // to construct initial solutions
    Constructive construct = new Constructive(ttp);
    // use local search
    LocalSearch ls = new CS2SA(ttp);
    ls.debug();
    ls.maxIterTSKP = 10;
    ls.maxIterKRP = 10;
    pop = new Population(Evolution.POP_SIZE);
    // use Lin-Kernighan to initialize the tours
    for (int i=0; i<Evolution.POP_SIZE; i++) {
      pop.sol[i] = new TTPSolution(
        construct.linkernTour(),
        construct.randomPickingPlan()
      );
      pop.sol[i] = ls.insertAndEliminate(pop.sol[i]);
      // 2-opt heuristic on TSKP
      pop.sol[i] = ls.ls2opt(pop.sol[i]);
      // simple bit-flip on KRP
      pop.sol[i] = ls.lsBitFlip(pop.sol[i]);
    }

    Deb.echo("Initialization done !");
    //===============================================

    // number of iterations
    int nbIter = 0;
    // improvement tag
    boolean improved;

    //===============================================
    // start cosolver search
    //===============================================
//    do {
//      nbIter++;
//      improved = false;
//
//      // 2-opt heuristic on TSKP
//      sol = ls2opt(sol);
//      //if (true) break;
//
//      // simple bit-flip on KRP
//      sol = simulatedAnnealing(sol);
//
//      // update best if improvement
//      if (sol.ob > GBest) {
//        GBest = sol.ob;
//        improved = true;
//      }
//
//      // stop execution if interrupted
//      if (Thread.currentThread().isInterrupted()) return sol;
//
//      // debug msg
//      if (this.debug) {
//        Deb.echo("Best "+nbIter+":");
//        Deb.echo("ob-best: "+sol.ob);
//        Deb.echo("wend   : "+sol.wend);
//        Deb.echo("---");
//      }
//
//      // stop when no improvements
//    } while (improved);
    //===============================================

    return null;
  }
}
