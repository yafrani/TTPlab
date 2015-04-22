package solver;

import ttp.TTP1Instance;

import ttp.TTPSolution;
import utils.Deb;
import utils.Quicksort;
import utils.TwoOptHelper;

import java.util.ArrayList;

/**
 * Created by kyu on 4/7/15.
 */
public class CosolverLarge extends LocalSearch {

  public CosolverLarge() {
    super();
  }

  public CosolverLarge(TTP1Instance ttp) {
    super(ttp);
  }

  public CosolverLarge(TTP1Instance ttp, TTPSolution s0) {
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

    // initial solution data
    int[] tour = sol.getTour();
    int[] pickingPlan = sol.getPickingPlan();

    // delta parameters
    double deltaT;

    // improvement indicator
    boolean improv, improv1, improv2;

    // best solution
    int iBest=0, jBest=0, kBest=0;
    double GBest = sol.ob;
    double ftBest = sol.ft;

    // neighbor solution
    int fp;
    double ft, G;
    int nbIter = 0, nbIter1, nbIter2;
    int wc, origBF;
    int i, j, k, r, itr;

    // distances of all tour cities (city -> end)
    long[] L = new long[nbCities];
    // current weight
    double wCurr = .0;
    // time approximations
    double t1, t2, t3, a, b1, b2;

    // Delaunay triangulation
    ArrayList<Integer>[] candidates = ttp.delaunay();

    do {
      improv = false;
      nbIter++;
      nbIter1 = 0;
      /*===================*
       * sub-problem 1:    *
       * TSP with knapsack *
       *===================*/
      do {
        improv1 = false;
        nbIter1++;

        // fast 2-opt
        for (i = 1; i < nbCities - 1; i++) {

          int node1 = tour[i] - 1;

          for (int node2 : candidates[node1]) {

            j = sol.mapCI[node2];

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

      /*=================*
       * pre-processing  *
       *=================*/
      // store `distance to end` of each tour city
      L[nbCities-1] = D[tour[nbCities-1] - 1][0];
      for (i=nbCities-2; i >= 0; i--) {
        L[i] = L[i+1] + D[tour[i+1] - 1][tour[i] - 1];
      }

      // sort item according to score
      double[] scores = new double[nbItems];
      int[] sortedItems;
      int[] selectedItems = new int[nbItems];

      for (k = 0; k < nbItems; k++) {
        // index where Bit-Flip happened
        origBF = sol.mapCI[A[k] - 1];
        // calculate time approximations
        t1 = L[origBF]*(1/(maxSpeed-C*ttp.weightOf(k)) - 1/maxSpeed);
        // affect score to item
        scores[k] = (ttp.profitOf(k)-R*t1) / ttp.weightOf(k);
        // empty the knapsack
        pickingPlan[k] = 0;
      }

      // evaluate solution after emptying knapsack
      ttp.objective(sol);

      // sort items according to score
      Quicksort qs = new Quicksort(scores);
      qs.sort();
      sortedItems = qs.getIndices();

      /* loop & insert items */
      int nbInserts = 0;
      wCurr = .0;
      int v2=0,v3=0;
      for (itr = 0; itr < nbItems; itr++) {

        k = sortedItems[itr];

        // check if new weight doesn't exceed knapsack capacity
        if (wCurr + ttp.weightOf(k) > capacity) {
          continue;
        }

        // index where Bit-Flip happened
        origBF = sol.mapCI[A[k] - 1];

        /* insert item if it has a potential gain */
        // time approximations t2 (worst-case time)
        t2 = L[origBF] * (1/(maxSpeed-C*(wCurr+ttp.weightOf(k))) - 1/(maxSpeed-C*wCurr));
        if (ttp.profitOf(k) > R*t2) {v2++;
          pickingPlan[k] = A[k];
          wCurr += ttp.weightOf(k);
        }
        else {
          // time approximations t3 (expected time)
          a = wCurr / L[0];
          b1 = maxSpeed - C * (wCurr + ttp.weightOf(k));
          b2 = maxSpeed - C * wCurr;
          t3 = (1 / a) * Math.log(
            ( (a * L[0] + b1) * (a * (L[0] - L[origBF]) + b2) ) /
            ( (a * (L[0] - L[origBF]) + b1) * (a * L[0] + b2) )
          );
          if (ttp.profitOf(k) > R*t3) {v3++;
            pickingPlan[k] = A[k];
            wCurr += ttp.weightOf(k);
          }
          else continue;
        }
        nbInserts++;
      } // END FOR k
      Deb.echo("=> nb t2: "+v2+" | nb t3: "+v3);
      Deb.echo("=> nb inserted: "+nbInserts+"/"+nbItems+"("+
        String.format("%.2f", (nbInserts * 100.0) / nbItems)+"%)");
      Deb.echo("=> w_curr: "+wCurr);

      // evaluate solution & update vectors
      ttp.objective(sol);

      // debug msg
      if (this.debug) {
        Deb.echo(">> KRP pre-processing: ob-best=" + sol.ob);
        Deb.echo("   wend: "+sol.wend);
      }

      //if (true) return null;


      /*======================*
       * eliminating bit-flip *
       *======================*/
      nbIter2 = 0;
      do {
        improv2 = false;
        nbIter2++;

        // browse items in the new order...
        for (k = 0; k < nbItems; k++) {

          // check if picked
          if (pickingPlan[k] == 0) {
            continue;
          }

          fp = sol.fp - ttp.profitOf(k);

          // index where Bit-Flip happened
          origBF = sol.mapCI[A[k] - 1];

          // starting time
          ft = origBF == 0 ? .0 : sol.timeAcc[origBF - 1];

          // recalculate velocities from bit-flip city
          for (r = origBF; r < nbCities; r++) {
            wc = sol.weightAcc[r] - ttp.weightOf(k);;
            ft += D[tour[r] - 1][tour[(r + 1) % nbCities] - 1] / (maxSpeed - wc * C);
          }

          G = fp - ft * R;

          // update best
          if (G > GBest) {

            kBest = k;
            GBest = G;
            improv2 = true;
            if (firstfit) break;
            //break;
          }

        } // END FOR k

        /* update if improvement */
        if (improv2) {

          improv = true;

          // bit-flip
          pickingPlan[kBest] = 0;

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

    } while (improv);

    return sol;
  }

}
