package solver;

import com.sun.javafx.geom.Edge;
import ea.*;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import utils.Quicksort;

/**
 * Created by kyu on 10/25/15.
 */
public class EvoERSP extends Evolution {

  public EvoERSP() {
    super();
  }

  public EvoERSP(TTP1Instance ttp) {
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
    ls.maxIterTSKP = 1;
    ls.maxIterKRP = 10;
    pop = new Population(Evolution.POP_SIZE);
    // use Lin-Kernighan to initialize the tours
    for (int i=0; i<Evolution.POP_SIZE; i++) {
      int[] rtour = init.randomTour();
      pop.sol[i] = new TTPSolution(
        init.tsp2opt(rtour),
        construct.randomPickingPlan()
      );
      //pop.sol[i] = ls.insertAndEliminate(pop.sol[i]);
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


    //===============================================
    // start EA search
    //===============================================
    int nbIter = 0;
    boolean improved;
    do {
      nbIter++;
      //improved = false;

      // get & sort fitness, use indices
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

      // Crossover & LS some selected solutions
      for (int i = 0; i < selectSize; i++) {

        // Select parents
        TTPSolution[] p = Selection.tournament(pop);

        // Crossover parents
        // ERX for tours
        int[] ct = EdgeRecombination.ERX(p[0].getTour(), p[1].getTour());
        // SPX for picking plans
        int[] cpp = StrCrossover.SPX(p[0].getPickingPlan(), p[1].getPickingPlan());
        // combine tour and picking plan
        TTPSolution c = new TTPSolution(ct,cpp);
        ttp.objective(c);

        // Apply local search
        ls.maxIterTSKP = 100;
        ls.maxIterKRP = 100;
        c = ls.fast2opt(c);
        c = ls.lsBitFlip(c);


        // check if it is identical
        boolean identical = true;
        for (int k = 0; k < POP_SIZE; k++) {
          // either tour or picking plan is identical
          if (pop.sol[k].ob == c.ob) {
            identical = false;
            Deb.echo("IDENTICAL: " + c.ob);
            break;
          }
        }


        // replace worst solutions
        if (identical)
          pop.sol[idx[j--]] = c;


        // TODO use mutation !!
        // TODO or explore somehow...
        // mutate offspring depending
        // on some very small probability
//        mutate2opt(c1);
      }

      //for (int u=0; u<Evolution.POP_SIZE; u++) Deb.echo(">> "+u+" >> "+pop.sol[idx[u]].ob);



      // stop execution if interrupted
      if (Thread.currentThread().isInterrupted()) return null;

      // debug msg
      if (this.debug) {
        Deb.echo("Best "+nbIter+":");
        Deb.echo(pop.fittest().ob);
        Deb.echo("---");
      }

      // stop when no improvements
    } while (nbIter<28);
    //===============================================

    return null;
  }
}
