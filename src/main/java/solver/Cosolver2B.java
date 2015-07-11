package solver;

import ttp.TTP1Instance;

import ttp.TTPSolution;
import utils.Deb;


/**
 * Created by kyu on 4/7/15.
 */
public class Cosolver2B extends CosolverBase {

  public Cosolver2B() {
    super();
  }

  public Cosolver2B(TTP1Instance ttp) {
    super(ttp);
  }

  public Cosolver2B(TTP1Instance ttp, TTPSolution s0) {
    super(ttp, s0);
  }


  @Override
  public TTPSolution solve() {

    //==============================================
    // generate initial solution
    //==============================================
    Constructive construct = new Constructive(ttp);
    TTPSolution s0 = construct.generate("lz");
    // pre-process the knapsack
    // insert and eliminate items
    s0 = insertAndEliminate(s0);
    //==============================================

    // copy initial solution into improved solution
    TTPSolution sol = s0.clone();
    Deb.echo("init done");

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
      sol = ls2opt(sol);

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
        Deb.echo(sol);
        Deb.echo("ob-best: "+sol.ob);
        Deb.echo("wend   : "+sol.wend);
        Deb.echo("---");
      }

      // stop when no improvements
    } while (improved);

    return sol;
  }



  /**
   * deal with the KRP sub-problem
   * this function applies a simple bit-flip
   */
  public TTPSolution lsBitFlip(TTPSolution sol) {

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
    int k, r, kBest=0;
    int nbIter = 0;

    boolean improved;

    // start search
    do {
      improved = false;
      nbIter++;

      // browse items in the new order...
      for (k = 0; k < nbItems; k++) {

        // cleanup and stop execution if interrupted
        if (Thread.currentThread().isInterrupted()) break;

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


        // index where Bit-Flip happened
        origBF = sol.mapCI[A[k] - 1];

        // starting time
        ft = origBF == 0 ? 0 : sol.timeAcc[origBF - 1];

        // recalculate velocities from bit-flip city
        for (r = origBF; r < nbCities; r++) {
          wc = sol.weightAcc[r] + deltaW;
          ft += ttp.distFor(tour[r]-1,tour[(r + 1) % nbCities] - 1) / (maxSpeed - wc * C);
        }

        G = fp - ft * R;

        // update best
        if (G > GBest) {
          kBest = k;
          GBest = G;

          improved = true;
          if (firstfit) break;
        }

      } // END FOR k


      //=====================================
      // update if improvement
      //=====================================
      if (improved) {

        // bit-flip
        pickingPlan[kBest] = pickingPlan[kBest] != 0 ? 0 : A[kBest];


        //===========================================================
        // recover accumulation vectors
        //===========================================================
        if (pickingPlan[kBest] != 0) {
          deltaP = ttp.profitOf(kBest);
          deltaW = ttp.weightOf(kBest);
        } else {
          deltaP = -ttp.profitOf(kBest);
          deltaW = -ttp.weightOf(kBest);
        }
        fp = sol.fp + deltaP;
        origBF = sol.mapCI[A[kBest] - 1];
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

        // debug msg
        if (this.debug) {
          Deb.echo(">> KRP: " + nbIter +
            " | ob=" + String.format("%.2f",sol.ob) +
            " | ft=" + String.format("%.2f",sol.ft)
          );
        }
      }

    } while (improved);


    // in order to recover all history vectors
    ttp.objective(sol);

    return sol;
  }


}
