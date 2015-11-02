package oldsolver;

import solver.LocalSearch;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import utils.RandGen;
import utils.TwoOptHelper;

import java.util.ArrayList;

/**
 * Created by kyu on 4/7/15.
 */
public class CSLASA extends LocalSearch {

  public CSLASA() {
    super();
  }

  public CSLASA(TTP1Instance ttp) {
    super(ttp);
  }

  public CSLASA(TTP1Instance ttp, TTPSolution s0) {
    super(ttp, s0);
  }


  @Override
  public TTPSolution search() {

    // calculate initial objective value
    ttp.objective(s0);

    // copy initial solution into improved solution
    TTPSolution sol = s0.clone();//
    TTPSolution sBest = s0.clone();

    // TTP data
    int nbCities = ttp.getNbCities();
    int nbItems = ttp.getNbItems();
    long[][] D = ttp.getDist();
    int[] A = ttp.getAvailability();
    double maxSpeed = ttp.getMaxSpeed();
    double minSpeed = ttp.getMinSpeed();
    long capacity = ttp.getCapacity();
    double C = (maxSpeed - minSpeed) / capacity;
    double R = ttp.getRent();

    // initial solution data
    int[] tour = sol.getTour();
    int[] pickingPlan = sol.getPickingPlan();

    // pre-process the knapsack
    // insert and eliminate items
    insertAndEliminate(sol);

    // delta parameters
    double deltaT;
    int deltaP, deltaW;

    // improvement indicator
    boolean improv, improv1, improv2;

    // best solution
    int iBest=0, jBest=0, kBest=0;
    double GBest = sol.ob;
    double ftBest = sol.ft;

    // neighbor solution
    long fp;
    double ft, G;
    long wc;
    int origBF;
    int i, j, k, r, c1, c2, q;
    int nbIter = 0, nbIter1, nbIter2;

    // Delaunay triangulation
    ArrayList<Integer>[] candidates = ttp.delaunay();




    // length of the fitness array
    int lfa=5000;
    // fitness array
    double[] fa = new double[lfa+1];
    // idle steps
    int idlestep = 0;
    int nsteps = 0;
    int ds;


    do {
      tour = sol.getTour();


      improv = false;
      nbIter++;
      nbIter1 = 0;
      for (i=0; i<=lfa; i++) {
        fa[i] = sol.ft;
      }

      /*===================*
       * sub-problem 1:    *
       * TSP with knapsack *
       *===================*/
      do {
        improv1 = false;



        // fast 2-opt
        for (i = 1; i < nbCities - 1; i++) {
          int node1 = tour[i] - 1;
          for (int node2 : candidates[node1]) {
            j = sol.mapCI[node2];

            // calculate final time with partial delta
            ft = sol.ft;
            wc = i - 2 < 0 ? 0 : sol.weightAcc[i - 2]; // fix index...
            deltaT = 0;
            for (q = i - 1; q <= j; q++) {
              wc += TwoOptHelper.get2optValue(q, sol.weightRec, i, j);
              c1 = TwoOptHelper.get2optValue(q, tour, i, j) - 1;
              c2 = TwoOptHelper.get2optValue((q + 1) % nbCities, tour, i, j) - 1;
              deltaT += -sol.timeRec[q] + ttp.distFor(c1,c2) / (maxSpeed - wc * C);
            }
            // retrieve neighbor's final time
            ft = ft + deltaT;



            // employ the LAHC acceptance condition
            int v = nbIter1 % lfa;
            boolean accept = ft < sol.ft || ft < fa[v];
            // when we have an improving move, then this step is not idle
            if (accept && ft < sol.ft)
              idlestep = 0;
            // test acceptance
            if (accept) {
              TwoOptHelper.do2opt(tour, i, j);
              ttp.objective(sol);
              Deb.echo("===> ACCEPTED : "+ft);
            }
            // independently of our acceptance we update the fitness array
            fa[v] = sol.ft;
            // if improvement is made
            if (sol.ft < sBest.ft) {
              sBest = sol.clone(); // update sBest
            }



            nbIter1++;
            idlestep++;

            if (this.debug) {
              //Deb.echo(fa);
              Deb.echo( ">> TSKP: " + nbIter1 +
                //" | ob=" + String.format("%.0f", sol.ob) +
                " | ft=" + String.format("%.0f", sol.ft) );
            }
          } // END FOR j
          //if (improv1) break;
        } // END FOR i


      } while (nbIter1<1000 || idlestep*50<nbIter1);

      if (true) return sBest;

      //ttp.objective(sol); // to compute sol.timeAcc

      if (!improv) break;









      /*=================*
       * sub-problem 2   *
       * KP with routing *
       *=================*/

      nbIter2 = 0;
      double T = 100.0;
      double alpha = .95;

      do {
        nbIter2++;

        for (int u=0; u<nbItems/10; u++) {

          // browse items randomly
          k = RandGen.randInt(0, nbItems - 1);

          /* check if new weight doesn't exceed knapsack capacity */
          if (pickingPlan[k] == 0 && ttp.weightOf(k) > sol.wend) continue;

          /* calculate deltaP and deltaW */
          if (pickingPlan[k] == 0) {
            deltaP = ttp.profitOf(k);
            deltaW = ttp.weightOf(k);
          } else {
            deltaP = -ttp.profitOf(k);
            deltaW = -ttp.weightOf(k);
          }
          fp = sol.fp + deltaP;

          /*
           * handle velocity constraint
           */
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

          // compute objective
          G = fp - ft * R;

          // delta best & current
          double energy_gap = G - GBest;

          // update if improvement or
          // acceptance probability satisfied
          double mu = Math.random();
          boolean acceptance = energy_gap > 0 ? true : Math.exp(energy_gap / T) > mu;
          if (acceptance) {

            if (energy_gap > 0) improv = true;

            kBest = k; // TODO useless here...
            GBest = G;

            // bit-flip
            pickingPlan[kBest] = pickingPlan[kBest] != 0 ? 0 : A[kBest];

            /* ================================================ */
            // recover accumulation vectors
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
            /* ================================================ */

            // debug msg
//            if (this.debug) {
//              Deb.echo(">> KRP: " + nbIter2 + " | ob-best=" + sol.ob + "/" + sol.ft);
//            }
          }
        }

        if (this.debug) {
          Deb.echo(">> KRP: " + nbIter2 + " | ob-best=" + sol.ob + " / " + sol.ft);
        }

        // cool down temperature
        T = T * alpha;

      } while (T > .01);


      // debug msg
      if (this.debug) {
        Deb.echo("Best "+nbIter+":");
        //Deb.echo(sol);
        Deb.echo("ob-best: "+sol.ob);
        Deb.echo("wend   : "+sol.wend);
        Deb.echo("---");
      }

      // to compute all solution params: timeRec, weightRec ...
      // TODO: might be avoided
      ttp.objective(sol);

    } while (false);

    return sol;
  }

}
