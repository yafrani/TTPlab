package solver;

import ea.*;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import utils.Quicksort;

/**
 * Created by kyu on 10/25/15.
 */
public class EvoMPUX extends Evolution {

  public EvoMPUX() {
    super();
  }

  public EvoMPUX(TTP1Instance ttp) {
    super(ttp);
  }



  @Override
  public TTPSolution search() {

    //===============================================
    // determine GA params
    //===============================================
    // number of selected individuals
    int selectSize = (int)(SELECTION_RATE*POP_SIZE);
    int nbCities = ttp.getNbCities();

    //===============================================
    // generate initial solution
    //===============================================
    // to construct initial solutions
    Constructive construct = new Constructive(ttp);
    Initialization init = new Initialization(ttp);

    // population
    pop = new Population(Evolution.POP_SIZE);

    // use local search
    LocalSearch ls = new CS2SA(ttp);
    ls.bestfit();

    // reduce LS time
    ls.maxIterTSKP = 30;
    ls.maxIterKRP = 30;

    // initialize one using LK
    pop.sol[0] = new TTPSolution(
      construct.linkernTour(),
      construct.zerosPickingPlan()
    );
    // use one kick LK to initialize the tours
    for (int i=1; i<POP_SIZE; i++) {
      pop.sol[i] = new TTPSolution(
        init.rlinkern(),
        construct.zerosPickingPlan()
      );
      // sleep for 2 ms to randomize
      try {
        Thread.sleep(2);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      Deb.echo("> "+i+", tour initialized !");
    }

    // apply LS for all
    for (int i=0; i<POP_SIZE; i++) {
      // 2-opt heuristic on TSKP
      pop.sol[i] = ls.fast2opt(pop.sol[i]);
      // initialize pp
      pop.sol[i] = ls.insertAndEliminate(pop.sol[i]);
      // simple bit-flip on KRP
      // todo cancel this ?
      //pop.sol[i] = ls.lsBitFlip(pop.sol[i]);
      Deb.echo("> "+i+", pick plan initialized !");
    }

    Deb.echo("Initialization done !");


    //===============================================
    // start EA search
    //===============================================
    double bestSoFar = Double.MIN_VALUE;
    int nbGen = 0;
    int nbIdleSteps = 0;
    // max iteration in LS
    // todo depends on problem size (#items and #cities)
    ls.maxIterTSKP = 50;
    ls.maxIterKRP = 20; // todo depends of #items
    do {
      nbGen++;
      nbIdleSteps++;

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
//      if (true)return null;

      int j = POP_SIZE-1;

      // Crossover & LS some selected solutions
      for (int i = 0; i < selectSize; i++) {

        // Select parents
        TTPSolution[] p = Selection.tournament(pop);

        // Crossover parents
        TTPSolution c = MPUX.crossover(p[0], p[1], ttp);
        ttp.objective(c);

        // Apply local search
        double lsp = Math.random();
        if (lsp < LS_RATE) {
          c = ls.fast2opt(c);
          c = ls.lsBitFlip(c);
        }

        // check if it is identical
        boolean identical = false;
        for (int k = 0; k < POP_SIZE; k++) {
          // either tour or picking plan is identical
          if (pop.sol[k].ob == c.ob) {
            identical = true;
            break;
          }
        }

        // if not identical solutions
        if (!identical) {
          //..?
          // replace worst solutions
          pop.sol[idx[j--]] = c;
        }
        // use mutation to eliminate premature convergence
        else {
          Deb.echo("IDENTICAL: " + c.ob);
          if (p[0].ob < p[1].ob) {
            Deb.echo(">>>>>>>>> P1");
            int[] x = Mutation.doubleBridge(p[0].getTour());
            p[0].setTour(x);
            ttp.objective(p[0]);
            // apply LS
//            p[0] = ls.fast2opt(p[0]);
//            p[0] = ls.lsBitFlip(p[0]);
          }
          else {
            Deb.echo(">>>>>>>>> P2");
            int[] x = Mutation.doubleBridge(p[1].getTour());
            p[1].setTour(x);
            ttp.objective(p[1]);
            // apply LS
//            p[1] = ls.fast2opt(p[1]);
//            p[1] = ls.lsBitFlip(p[1]);
          }
        }

        // TODO use mutation !!?
        // TODO or explore somehow...
        // mutate offspring depending
        // on some very small probability
      }

      TTPSolution fittest = pop.fittest();

      // if improvement is made
      if (fittest.ob > bestSoFar) {
        bestSoFar = fittest.ob;
        nbIdleSteps = 0;
      }

      // debug msg
      if (this.debug) {
        Deb.echo("Best "+nbGen+":");
        Deb.echo(fittest.ob);
        Deb.echo("---");
      }

      // stop execution if interrupted
      if (Thread.currentThread().isInterrupted()) return fittest;

      // stop when no improvements
    } while (nbGen<MAX_GEN && nbIdleSteps<MAX_IDLE_STEPS);



    //===============================================
    // apply LS for all
    //===============================================
    ls.maxIterTSKP = 300;
    ls.maxIterKRP = 300;
    for (int i=0; i<POP_SIZE; i++) {

      // 2-opt heuristic on TSKP
      pop.sol[i] = ls.fast2opt(pop.sol[i]);
      // simple bit-flip on KRP
      pop.sol[i] = ls.lsBitFlip(pop.sol[i]);
      Deb.echo(i+" DONE !");
    }

    return pop.fittest();
  }
}
