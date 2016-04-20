package solver;

import ea.*;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import utils.Quicksort;

/**
 * Created by kyu on 12/29/15.
 */
public class MA2B extends Evolution {

  public MA2B() {
    super();
  }

  public MA2B(TTP1Instance ttp) {
    super(ttp);
  }



  @Override
  public TTPSolution search() {

    //===============================================
    // determine GA params
    //===============================================
    // number of selected individuals
    int selectSize = (int)(SELECTION_RATE*POP_SIZE);
    if (debug) Deb.echo(">>>"+selectSize);
    int nbCities = ttp.getNbCities();
    int nbItems = ttp.getNbItems();

    //===============================================
    // generate initial population
    //===============================================
    // to construct initial solutions
    Constructive construct = new Constructive(ttp);
    Initialization init = new Initialization(ttp);

    // current population & offspring
    pop = new Population(Evolution.POP_SIZE);
    Population offpop = new Population(selectSize);
    int offpopSize;

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
      if (debug) Deb.echo("> " + i + ", tour initialized !");
    }

    // use local search
    LocalSearch ls = new CS2SA(ttp);
    // reduce LS time
    ls.maxIterTSKP = 50;
    ls.maxIterKRP = 50;
    ls.firstfit();
    // apply LS for all
    for (int i=0; i<POP_SIZE; i++) {
      ttp.objective(pop.sol[i]);
      //if (nbItems < 100000) pop.sol[i] = ls.fast2opt(pop.sol[i]);
      // initialize pp
      pop.sol[i] = ls.insertT2(pop.sol[i]);
      // simple bit-flip on KRP
      if (nbItems < 100000)
        pop.sol[i] = ls.lsBitFlip(pop.sol[i]);
      if (debug) Deb.echo("> "+i+", pick plan initialized ! >> "+pop.sol[i].ob);
    }

    if (debug) Deb.echo("Initialization done !");

    //===============================================
    // start EA search
    //===============================================
    double bestSoFar = Double.MIN_VALUE;
    int nbGen = 0;
    int nbIdleSteps = 0;
    // max iteration in LS
    ls.maxIterTSKP = 10;
    ls.maxIterKRP = 10;
    do {

      nbGen++;
      nbIdleSteps++;
      //Deb.echo("NBGEN: "+nbGen);

      //===============================================
      // get & sort fitness, use indices
      //===============================================
      Double[] fits = new Double[POP_SIZE];
      for (int i=0; i<POP_SIZE; i++) {
        fits[i] = pop.sol[i].ob;
      }
      Quicksort<Double> qs = new Quicksort<>(fits);
      qs.sort();
      int[] idx = qs.getIndices();

      // get fittest
      TTPSolution fittest = pop.sol[idx[0]];
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
      // stop execution if interrupted (runtime limit: 600sec)
      if (Thread.currentThread().isInterrupted() || pop.sol[idx[POP_SIZE-1]].ob==fittest.ob )
        return fittest;

      // DEBUG PRINT
      if (debug)
        for (int u=0; u<Evolution.POP_SIZE; u++)
          Deb.echo(">> "+u+" >> "+pop.sol[idx[u]].ob);


      int j = POP_SIZE-1;
      offpopSize = 0;

      //===============================================
      // Genetic evolution
      //===============================================
      for (int i = 0; i < selectSize; i++) {

        /* Select parents */
        TTPSolution[] p = Selection.tournament(pop);

        /* Crossover parents */
        TTPSolution c = MPX2.crossover(p[0], p[1], ttp);
        ttp.objective(c);

        /* Apply local search */
        double lsp = Math.random();
        if (lsp < LS_RATE) {
          //Deb.echo("APPLY LS");
          c = ls.fast2opt(c);
          c = ls.lsBitFlip(c);
        }

        /* add to offspring population */
        // check if already in offpop
        boolean identical = false;
        for (int k = 0; k < POP_SIZE; k++) {
          // either tour or picking plan is identical
          if (pop.sol[k].ob == c.ob) {
            identical = true;
            break;
          }
          if (k<offpopSize && offpop.sol[k].ob == c.ob) {
            identical = true;
            break;
          }
        }

        // if not existent
        if (!identical) {
          //Deb.echo("~~==============================~~ " + c.ob);

          // add to offspring population
          offpop.sol[offpopSize++] = c;
        }
        // use mutation to eliminate premature convergence
        else {
          //Deb.echo("IDENTICAL: " + c.ob);
          int[] x;
          x = Mutation.doubleBridge(c.getTour());
          x = Mutation.doubleBridge(x);
          c.setTour(x);

          //Deb.echo("APPLY MUTATION PP");
          //c = Mutation.randomFlips(c, MUTATION_STRENGTH_PP, ttp); // objective included

          // create new pick plan
          c = ls.insertT2(c);

          //Deb.echo("ID. APPLY LS");
          c = ls.fast2opt(c);
          c = ls.lsBitFlip(c);

          offpop.sol[offpopSize++] = c;

        }
      }

      //===============================================
      // Add offspring to population
      //===============================================
      for (int i=0; i<offpopSize; i++) {
        TTPSolution c = offpop.sol[i];
        // replace worst solutions
        pop.sol[idx[j--]] = c;
      }


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
      //Deb.echo(i+" DONE !");
    }

    return pop.fittest();
  }
}
