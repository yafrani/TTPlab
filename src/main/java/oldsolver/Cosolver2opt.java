package oldsolver;

import solver.LocalSearch;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import utils.Quicksort;
import utils.TwoOptHelper;

/**
 * Created by kyu on 2/26/15.
 */
public class Cosolver2opt extends LocalSearch {

  public Cosolver2opt() {
    super();
  }

  public Cosolver2opt(TTP1Instance ttp) {
    super(ttp);
  }

  public Cosolver2opt(TTP1Instance ttp, TTPSolution s0) {
    super(ttp, s0);
  }


  @Override
  public TTPSolution solve() {

    // calculate initial objective value
    ttp.objective(s0);

    // copy initial solution into improved solution
    TTPSolution sol = s0.clone();//, sBest = s0.clone();

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
    int[] w = ttp.getWeights();

    // initial solution data
    int[] tour = sol.getTour();
    int[] pickingPlan = sol.getPickingPlan();

    // delta parameters
    int deltaP, deltaW;
    double deltaT;

    // improvement indicator
    boolean improv, improv1,improv2;

    // best solution
    int iBest=0, jBest=0, kBest=0;
    double GBest = sol.ob;
    double ftBest = sol.ft;

    // neighbor solution
    long fp;
    double ft, G;
    int nbIter = 0, nbIter1 = 0, nbIter2 = 0;
    long wc;
    int i, j, k;

    // KP step
    Double[] scores = new Double[nbItems];
    int itr;
    int[] sortedItems;


    do {
      improv = false;
      nbIter++;

      /*===================*
       * sub-problem 1:    *
       * TSP with knapsack *
       *===================*/
      do {
        improv1 = false;
        nbIter1++;

        // slow 2-opt
        for (i = 1; i < nbCities - 1; i++) {
          for (j = i + 1; j < nbCities; j++) {

            /* calculate final time with partial delta */
            ft = sol.ft;
            wc = i - 2 < 0 ? 0 : sol.weightAcc[i - 2]; // fix index...
            deltaT = 0;
            for (int q = i - 1; q <= j; q++) {

              wc += TwoOptHelper.get2optValue(q, sol.weightRec, i, j);
              int c1 = TwoOptHelper.get2optValue(q, tour, i, j) - 1;
              int c2 = TwoOptHelper.get2optValue((q + 1) % nbCities, tour, i, j) - 1;

              deltaT += -sol.timeRec[q] + D[c1][c2] / (maxSpeed - wc * C);
            }

            // retrieve neighbor's final time
            ft += deltaT;


            // update best
            if (ft < ftBest) {
              iBest = i;
              jBest = j;
              ftBest = ft;
              improv1 = true;

              if (firstfit) break;
            }

            if (firstfit && improv1) break;
          } // END FOR j
          if (firstfit && improv1) break;
        } // END FOR i


        /* update if improvement */
        if (improv1) {

          improv = true;

          // 2opt invert
          TwoOptHelper.do2opt(tour, iBest, jBest);

          // evaluate & update vectors
          ttp.objective(sol);

          // debug msg
          if (this.debug) {
            Deb.echo(">> TSKP: " + nbIter1 + " | ob-best=" + sol.ob);
          }
        }

      } while (improv1);

      if (!improv) break;


      /*=================*
       * sub-problem 2   *
       * KP with routing *
       *=================*/
      for (k = 0; k < nbItems; k++) {
        scores[k] = sol.mapCI[A[k] - 1] * ttp.profitOf(k) / (ttp.weightOf(k)+.0);
      }
      Quicksort<Double> qs = new Quicksort<>(scores);
      qs.sort();
      sortedItems = qs.getIndices();

      /* fast bit-flip */
      do {
        improv2 = false;
        nbIter2++;

        // browse items in the new order...
        for (itr = 0; itr < nbItems; itr++) {

          k = sortedItems[itr];

          /* check if new weight doesn't exceed knapsack capacity */
          if (pickingPlan[k] == 0 && ttp.weightOf(k) > sol.wend) {
            continue;
          }

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
           * velocity-TSP
           * TSP constrained with knapsack weight
           */
          // index where Bit-Flip happened
          int origBF = sol.mapCI[A[k] - 1];

          // starting time
          ft = origBF == 0 ? 0 : sol.timeAcc[origBF - 1];

          // recalculate velocities from bit-flip city
          for (int r = origBF; r < nbCities; r++) {
            wc = sol.weightAcc[r] + deltaW;
            ft += D[tour[r] - 1][tour[(r + 1) % nbCities] - 1] / (maxSpeed - wc * C);
          }

          G = Math.round(fp - ft * R);

          // update best
          if (G > GBest) {

            kBest = k;
            GBest = G;
            improv2 = true;
            if (firstfit) break;
          }

        } // END FOR k


        /* update if improvement */
        if (improv2) {

          improv = true;

          // bit-flip
          pickingPlan[kBest] = pickingPlan[kBest] != 0 ? 0 : A[kBest];

          // evaluate & update vectors
          ttp.objective(sol);

          // debug msg
          if (this.debug) {
            Deb.echo(">> KRP: " + nbIter2 + " | ob-best=" + sol.ob);
          }
        }

      } while (improv2);



      // debug msg
      if (this.debug) {
        Deb.echo("Best "+nbIter+":");
        Deb.echo(sol);
        Deb.echo("ob-best: "+sol.ob);
        Deb.echo("wend   : "+sol.wend);
        Deb.echo("---");
      }
    } while(improv);


    return sol;
  }

}
