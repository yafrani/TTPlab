package solver;

import com.sun.javafx.geom.Edge;
import ea.EdgeRecombination;
import ea.Initialization;
import ea.Population;
import ea.Selection;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import utils.Quicksort;

/**
 * Created by kyu on 10/25/15.
 */
public class EvoTTP extends Evolution {

  public EvoTTP() {
    super();
  }

  public EvoTTP(TTP1Instance ttp) {
    super(ttp);
  }


  @Override
  public TTPSolution search() {

    //===============================================
    // determine GA params
    //===============================================
    // number of selected individuals
    int selectSize = (int)(SELECTION_RATE*POP_SIZE);

    //===============================================
    // generate initial solution
    //===============================================
    // to construct initial solutions
    Constructive construct = new Constructive(ttp);
    Initialization init = new Initialization(ttp);

    // use local search
    LocalSearch ls = new CS2SA(ttp);
    //ls.debug();
    // reduce LS time
    ls.maxIterTSKP = 10;
    ls.maxIterKRP = 10;
    pop = new Population(Evolution.POP_SIZE);
    // use Lin-Kernighan to initialize the tours
    for (int i=0; i<Evolution.POP_SIZE; i++) {
      int[] rtour = init.randomTour();
      pop.sol[i] = new TTPSolution(
        init.tsp2opt(rtour),
        construct.zerosPickingPlan()
      );
      pop.sol[i] = ls.insertAndEliminate(pop.sol[i]);
      ttp.objective(pop.sol[i]);
      //Deb.echo(">> "+i+" >> "+pop.sol[i].ob);
      // 2-opt heuristic on TSKP
      pop.sol[i] = ls.fast2opt(pop.sol[i]);
      // simple bit-flip on KRP
      pop.sol[i] = ls.lsBitFlip(pop.sol[i]);
//      pop.sol[i] = ls.simulatedAnnealing(pop.sol[i]);
//      Deb.echo(">> "+i+" >> "+pop.sol[i].ob);
    }

    Deb.echo("Initialization done !");
    //===============================================

    // number of iterations
    int nbIter = 0;
    // improvement tag
    boolean improved;

    //===============================================
    // start EA search
    //===============================================
    do {
      nbIter++;
      //improved = false;

      // get & sort fitness
      // use indices
      Double[] fits = new Double[POP_SIZE];
      for (int i=0; i<POP_SIZE; i++) {
        fits[i] = pop.sol[i].ob;
      }
      Quicksort<Double> qs = new Quicksort<>(fits);
      qs.sort();
      int[] idx = qs.getIndices();

      // DEBUG PRINT
      for (int u=0; u<Evolution.POP_SIZE; u++) Deb.echo(">> "+u+" >> "+pop.sol[idx[u]].ob);

      int j = POP_SIZE-1;

      // Crossover & mutate some solutions
      for (int i = 0; i < selectSize; i++) {

        // Select parents
        TTPSolution[] p = Selection.tournament(pop);

        // Crossover parents
        TTPSolution c = EdgeRecombination.ERX(p[0], p[1]);
        c.setTour(p[0].clone().getTour());
        ttp.objective(c);

        // Apply local search
        c = ls.fast2opt(c);
        // simple bit-flip on KRP
        c = ls.lsBitFlip(c);

        // replace worst solutions
        pop.sol[idx[j--]] = c;

        // mutate offspring depending
        // on some very small probability
//        mutate2opt(c1);

        // compute fitness

//        newpop.tours[i] = c1;
//        newpop.tours[i+1] = c2;
      }

      //for (int u=0; u<Evolution.POP_SIZE; u++) Deb.echo(">> "+u+" >> "+pop.sol[idx[u]].ob);

      // stop execution if interrupted
      if (Thread.currentThread().isInterrupted()) return null;

      // debug msg
      if (this.debug) {
        Deb.echo("Best "+nbIter+":");
        Deb.echo(pop.fittest().ob);
//        Deb.echo("ob-best: "+sol.ob);
//        Deb.echo("wend   : "+sol.wend);
        Deb.echo("---");
      }

      // stop when no improvements
    } while (nbIter<8);
    //===============================================

    return null;
  }
}
