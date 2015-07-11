package solver;

import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import utils.RandGen;
import utils.TwoOptHelper;

import java.util.ArrayList;

/**
 * Created by kyu on 4/7/15.
 */
public class HybridEALS extends Cosolver2SA {

  public HybridEALS(TTP1Instance ttp) {
    super(ttp);
  }


  @Override
  public TTPSolution solve() {

    //===============================================
    // generate initial solution
    //===============================================
    Constructive construct = new Constructive(ttp);
    TTPSolution s0 = construct.generate("lz");
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
      sol = ls2opt(sol);

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
        //Deb.echo(sol);
        Deb.echo("ob-best: "+sol.ob);
        Deb.echo("wend   : "+sol.wend);
        Deb.echo("---");
      }

      // stop when no improvements
    } while (improved);
    //===============================================

    return sol;
  }




  /**
   * deal with the KRP sub-problem
   * this function applies a simple bit-flip
   */
  public TTPSolution simulatedAnnealing(TTPSolution sol) {

    // copy initial solution into improved solution
    TTPSolution sBest = sol.clone();

    // TTP data
    int nbCities = ttp.getNbCities();
    int nbItems = ttp.getNbItems();
    int[] A = ttp.getAvailability();
    double maxSpeed = ttp.getMaxSpeed();
    double minSpeed = ttp.getMinSpeed();
    long capacity = ttp.getCapacity();
    double C = (maxSpeed - minSpeed) / capacity;
    double R = ttp.getRent();

    // initial solution data
    int[] tour = sol.getTour();
    int[] pickingPlan = sol.getPickingPlan();

    // delta parameters
    int deltaP, deltaW;

    // best solution
    double GBest = sol.ob;

    // neighbor solution
    long fp;
    double ft, G;
    long wc;
    int origBF;
    int k, r;
    int nbIter = 0;

    // SA params
    double T_abs = 1;
    double T = 100.0;
    double alpha = .95;

    //===============================================
    // start simulated annealing process
    //===============================================
    do {
      nbIter++;

      // cleanup and stop execution if interrupted
      if (Thread.currentThread().isInterrupted()) break;

      for (int u=0; u<nbItems/10; u++) {

        // browse items randomly
        k = RandGen.randInt(0, nbItems - 1);

        // check if new weight doesn't exceed knapsack capacity
        if (pickingPlan[k] == 0 && ttp.weightOf(k) > sol.wend) continue;

        // calculate deltaP and deltaW
        if (pickingPlan[k] == 0) {
          deltaP = ttp.profitOf(k);
          deltaW = ttp.weightOf(k);
        } else {
          deltaP = -ttp.profitOf(k);
          deltaW = -ttp.weightOf(k);
        }
        fp = sol.fp + deltaP;

        // handle velocity constraint
        // index where Bit-Flip happened
        origBF = sol.mapCI[A[k] - 1];
        // starting time
        ft = origBF == 0 ? .0 : sol.timeAcc[origBF - 1];
        // recalculate velocities from bit-flip city
        // to recover objective value
        for (r = origBF; r < nbCities; r++) {
          wc = sol.weightAcc[r] + deltaW;
          ft += ttp.distFor(tour[r] - 1, tour[(r + 1) % nbCities] - 1) / (maxSpeed - wc * C);
        }
        // compute recovered objective value
        G = fp - ft * R;

        //=====================================
        // update if improvement or
        // Boltzmann condition satisfied
        //=====================================
        double mu = Math.random();
        double energy_gap = G - GBest;
        boolean acceptance = energy_gap > 0 || Math.exp(energy_gap / T) > mu;
        if (acceptance) {

          GBest = G;

          // bit-flip
          pickingPlan[k] = pickingPlan[k] != 0 ? 0 : A[k];

          //===========================================================
          // recover accumulation vectors
          //===========================================================
          if (pickingPlan[k] != 0) {
            deltaP = ttp.profitOf(k);
            deltaW = ttp.weightOf(k);
          } else {
            deltaP = -ttp.profitOf(k);
            deltaW = -ttp.weightOf(k);
          }
          fp = sol.fp + deltaP;
          origBF = sol.mapCI[A[k] - 1];
          ft = origBF == 0 ? 0 : sol.timeAcc[origBF - 1];
          for (r = origBF; r < nbCities; r++) {
            // recalculate velocities from bit-flip city
            wc = sol.weightAcc[r] + deltaW;
            ft += ttp.distFor(tour[r] - 1, tour[(r + 1) % nbCities] - 1) / (maxSpeed - wc * C);
            // recover wacc and tacc
            sol.weightAcc[r] = wc;
            sol.timeAcc[r] = ft;
          }
          G = fp - ft * R;
          sol.ob = G;
          sol.fp = fp;
          sol.ft = ft;
          sol.wend = capacity - sol.weightAcc[nbCities - 1];
          //===========================================================

        }

      }

      // update best if improvement
      if (sol.ob > sBest.ob) {
        sBest = sol.clone();
      }

      if (this.debug) {
        Deb.echo(">> KRP: " + nbIter + " | ob-best=" +
          String.format("%.2f",sol.ob));
      }

      // cool down temperature
      T = T * alpha;

      // stop when temperature reach absolute value
    } while (T > T_abs);


    // in order to recover all history vector
    ttp.objective(sBest);

    return sBest;
  }


}
