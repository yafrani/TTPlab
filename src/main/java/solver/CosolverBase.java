package solver;

import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import utils.Quicksort;

/**
 * Created by kyu on 5/3/15.
 */
public abstract class CosolverBase extends LocalSearch {

  public CosolverBase() {
    super();
  }

  public CosolverBase(TTP1Instance ttp) {
    super(ttp);
  }

  public CosolverBase(TTP1Instance ttp, TTPSolution s0) {
    super(ttp, s0);
  }

  /**
   * KP pre-processing
   * base on the item insertion heuristic
   */
  public void insertAndEliminate(TTPSolution sol) {

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

    // neighbor solution
    int origBF;
    int i, k, itr;

    // distances of all tour cities (city -> end)
    long[] L = new long[nbCities];
    // current weight
    long wCurr;
    // time approximations
    double t1, t2, t3, a, b1, b2;

    // store `distance to end` of each tour city
    L[nbCities-1] = D[tour[nbCities-1] - 1][0];
    for (i=nbCities-2; i >= 0; i--) {
      L[i] = L[i+1] + D[tour[i+1] - 1][tour[i] - 1];
    }

    // sort item according to score
    double[] scores = new double[nbItems];
    int[] sortedItems;
    int[] insertedItems = new int[nbItems];

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

    // loop & insert items
    int nbInserts = 0;
    wCurr = 0;
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
        insertedItems[nbInserts++] = k;
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
          insertedItems[nbInserts++] = k;
        }
        else continue;
      }
    } // END FOR k

    // evaluate solution & update vectors
    ttp.objective(sol);

    // debug msg
    if (this.debug) {
      Deb.echo(">> item insertion: best=" + sol.ob);
      Deb.echo("   wend: "+sol.wend);

      Deb.echo("==> nb t2: "+v2+" | nb t3: "+v3);
      Deb.echo("==> nb inserted: "+nbInserts+"/"+nbItems+"("+
        String.format("%.2f", (nbInserts * 100.0) / nbItems)+"%)");
      Deb.echo("==> w_curr: "+wCurr);
    }

    // improvement indicator
    boolean improv2;

    // best solution
    int kBest=0;
    long GBest = sol.ob;

    // neighbor solution
    long fp;
    long ft, G;
    int nbIter2 = 0;
    long wc;
    int r;



    do {
      improv2 = false;
      nbIter2++;

      // browse items in the new order...
      for (itr = 0; itr < nbInserts; itr++) {
        k = insertedItems[itr];
        // check if picked
        //if (pickingPlan[k] == 0) {
        //  continue;
        //}

        fp = sol.fp - ttp.profitOf(k);

        // index where Bit-Flip happened
        origBF = sol.mapCI[A[k] - 1];

        // starting time
        ft = origBF == 0 ? 0 : sol.timeAcc[origBF - 1];

        // recalculate velocities from bit-flip city
        for (r = origBF; r < nbCities; r++) {
          wc = sol.weightAcc[r] - ttp.weightOf(k);;
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

        // bit-flip
        pickingPlan[kBest] = 0;

        // evaluate & update vectors
        ttp.objective(sol);

        // debug msg
        if (this.debug) {
          Deb.echo(">> item elimination: best=" + sol.ob);
        }
      }

    } while (nbIter2<3);

  }

}
