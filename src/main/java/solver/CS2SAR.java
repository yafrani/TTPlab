package solver;

import ea.Initialization;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import utils.RandGen;


/**
 * CS2SA algorithm
 *
 * A CoSolver implementation that uses 2-opt
 * for the TSP component and SA for the KP
 * component
 *
 * Created by kyu on 4/7/15.
 */
public class CS2SAR extends LocalSearch {

  public double T_abs;       // absolute temperature
  public double T0;           // initial temperature
  public double alpha;       // cooling rate
  public double trialFactor; // number of trials (per temperature)


  public CS2SAR() {
    super();
    // use default config
    SAConfig();
  }

  public CS2SAR(TTP1Instance ttp) {
    super(ttp);
    // use default config
    SAConfig();
  }


  // SA params config
  // default config
  void SAConfig() {

    int nbItems = ttp.getNbItems();

    T_abs = 1;
    T0 = 100.0;
    alpha = 0.95;

    trialFactor = generateTFLinFitALT(nbItems);
  }


  /**
   * simulated annealing
   *
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

    double T = T0;

    long trials = Math.round(nbItems*trialFactor);

    if (debug) Deb.echo(">>>> TRIAL FACTOR: "+trialFactor);

    //===============================================
    // start simulated annealing process
    //===============================================
    do {
      nbIter++;

      // cleanup and stop execution if interrupted
      if (Thread.currentThread().isInterrupted()) break;

      for (int u=0; u<trials; u++) {

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
        Deb.echo(">> KRP " + nbIter + ": ob=" +
          String.format("%.0f",sol.ob));
      }

      // cool down temperature
      T = T * alpha;

      // stop when temperature reach absolute value
    } while (T > T_abs);


    // in order to recover all history vector
    ttp.objective(sBest);

    return sBest;
  }


  @Override
  public TTPSolution search() {

    //===============================================
    // generate initial solution
    //===============================================
    Constructive construct = new Constructive(ttp);
    Initialization init = new Initialization(ttp);
    if (s0==null) {
      // use Lin-Kernighan to initialize the tour
      s0 = new TTPSolution(
        init.rlinkern(),
        construct.zerosPickingPlan()
//        construct.randomPickingPlan()
      );

      // pre-process the knapsack
      // insert and eliminate items
      if (ttp.getNbCities() < 30000) s0 = insertAndEliminate(s0);
      else s0 = insertT2(s0);

      // boosting
      if (ttp.getNbCities() < 200) {
        s0 = lsBitFlip(s0);
      }
    }
    ttp.objective(s0);
    if (this.debug) {
      Deb.echo("STARTING SOL >> " + s0.ob);
    }
    //===============================================

    // copy initial solution into improved solution
    TTPSolution sol = s0.clone();

    // best found solution
    TTPSolution sBest = sol.clone();

    // number of iterations
    int nbIter = 0;
    // improvement tag
    int idleSteps = 0;

    //===============================================
    // start cosolver search
    //===============================================
    do {
      nbIter++;

      // 2-opt heuristic on TSKP
      sol = fast2opt(sol);

      // simple bit-flip on KRP
      sol = simulatedAnnealing(sol);

      // tag idle step
      if (sol.ob > sBest.ob) {
        idleSteps = 0;
        sBest = sol.clone();
      }
      else {
        idleSteps++;
      }

      // restart if no improvement
      if (idleSteps>0 && !Thread.currentThread().isInterrupted()) {
        if (debug) {
          Deb.echo("===> RESTART");
        }
        idleSteps++;
        // restart
        sol = new TTPSolution(
          init.rlinkern(),
          construct.randomPickingPlan()
        );
      }

      ttp.objective(sol);

      // debug msg
      if (this.debug) {
        Deb.echo("Best "+nbIter+":");
        Deb.echo("ob-best: "+sol.ob);
        Deb.echo("wend   : "+sol.wend);
        Deb.echo("---");
      }

      // stop when time expires
    } while (!Thread.currentThread().isInterrupted());
    //===============================================

    return sBest;
  }



  public static double generateTFLinFitALT(int xi) {
//    function yi = nit_linear(xi)
    int i=-1;
    int[] x = new int[]{50, 204, 609, 1147, 8034, 38875, 105318, 253568, 338090};
    double[] y = new double[]{1000, 500,  100,   50,    10,     1,   0.04,  0.03, 0.03};
    int n=y.length;
    for (int k=0; k<n-1; k++) {
      if (x[k] <= xi && xi < x[k + 1]) {
        i = k;
        break;
      }
    }

    // handle special cases
    if (xi <= x[0]) {
      return 57872.0;
    }
    if (xi >= x[n-1]) {
      return 0.03;
    }

    // create linear interpolation
    double m = ( y[i]-y[i+1] ) / ( x[i]-x[i+1] );
    double b = y[i]-m*x[i];

    double yi = m*xi + b;

    return yi;
  }

}
