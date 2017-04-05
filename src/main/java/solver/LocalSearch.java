package solver;

import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.*;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * local search algorithms
 * 
 * @author kyu
 * 
 */
public abstract class LocalSearch extends SearchHeuristic {

  protected TTPSolution s0;
  protected HashSet<Integer>[] candidates;

  // initial solution
  public void setS0(TTPSolution s0) {
    this.s0 = s0;
  }
  public TTPSolution getS0() {
    return s0;
  }

  /**
   * first fit or best fit
   */
  public boolean firstfit;
  
  
  public LocalSearch() {
    super();
  }

  public LocalSearch(TTP1Instance ttp) {
    super(ttp);
    // generate Delaunay triangulation
    if (! System.getProperty("os.name").contains("Windows")) {
//      candidates = GraphHelper.delaunayKNN(ttp,10);
      candidates = GraphHelper.delaunay(ttp);

      if (debug) Deb.echo("Delaunay triangulation: OK");
    }
  }
  

  @Override
  public void setTTP(TTP1Instance ttp) {
    super.setTTP(ttp);
    // generate Delaunay triangulation
    if (! System.getProperty("os.name").contains("Windows")) {
//    candidates = GraphHelper.delaunayKNN(ttp,5);
      candidates = GraphHelper.delaunay(ttp);
    }
  }

  public void setDelaunayLevel(int L) {
    // generate Delaunay triangulation
    if (! System.getProperty("os.name").contains("Windows")) {
      candidates = GraphHelper.delaunayKNN(ttp, L);
    }
  }


  /**
   * use first fit strategy
   */
  public void firstfit() {
    firstfit = true;
  }
  
  
  /**
   * use best fit strategy
   */
  public void bestfit() {
    firstfit = false;
  }
  
  @Override
  public String getName() {
    String suf = this.firstfit ? "-FF" : "-BF";
    return this.name + suf;
  }




  /**
   * KP pre-processing
   *
   * base on the item insertion heuristic in MATLS
   * described in "improving efficiency of heuristics..."
   *
   * uses only delta_1 and delta_2 approximations
   * in order to limit the number of inserted items
   */
  public TTPSolution insertT2(TTPSolution sol) {

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
    L[nbCities - 1] = ttp.distFor(tour[nbCities - 1] - 1, 0);
    for (i = nbCities - 2; i >= 0; i--) {
      L[i] = L[i + 1] + ttp.distFor(tour[i + 1] - 1, tour[i] - 1);
    }

    // sort item according to score
    Double[] scores = new Double[nbItems];
    int[] insertedItems = new int[nbItems];

    for (k = 0; k < nbItems; k++) {
      // index where Bit-Flip happened
      origBF = sol.mapCI[A[k] - 1];
      // calculate time approximations
      t1 = L[origBF] * (1 / (maxSpeed - C * ttp.weightOf(k)) - 1 / maxSpeed);
      // affect score to item
      scores[k] = (ttp.profitOf(k) - R * t1) / ttp.weightOf(k);
      // empty the knapsack
      pickingPlan[k] = 0;
    }

    // evaluate solution after emptying knapsack
    ttp.objective(sol);

    // sort items according to score
    Quicksort<Double> qs = new Quicksort<>(scores);
    qs.sort();
    int[] sortedItems = qs.getIndices();

    // loop & insert items
    int nbInserts = 0;
    wCurr = 0;
    int v2 = 0, v3 = 0;
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
      t2 = L[origBF] * (1 / (maxSpeed - C * (wCurr + ttp.weightOf(k))) - 1 / (maxSpeed - C * wCurr));
      if (ttp.profitOf(k) > R * t2) {
        v2++;
        pickingPlan[k] = A[k];
        wCurr += ttp.weightOf(k);
        insertedItems[nbInserts++] = k;
      }
    } // END FOR k

    // evaluate solution & update vectors
    ttp.objective(sol);

    // debug msg
    if (this.debug) {
      Deb.echo(">> item insertion: best=" + sol.ob);
      Deb.echo("   wend: " + sol.wend);

      Deb.echo("==> nb t2: " + v2 + " | nb t3: " + v3);
      Deb.echo("==> nb inserted: " + nbInserts + "/" + nbItems + "(" +
        String.format("%.2f", (nbInserts * 100.0) / nbItems) + "%)");
      Deb.echo("==> w_curr: " + wCurr);
    }

    return sol;
  }


  /**
   * KP pre-processing
   *
   * base on the item insertion heuristic in MATLS
   * described in "improving efficiency of heuristics..."
   */
  public TTPSolution insertAndEliminate(TTPSolution sol) {

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
    L[nbCities-1] = ttp.distFor(tour[nbCities-1] - 1,0);
    for (i=nbCities-2; i >= 0; i--) {
      L[i] = L[i+1] + ttp.distFor(tour[i+1]-1, tour[i]-1);
    }

    // sort item according to score
    Double[] scores = new Double[nbItems];
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
    Quicksort<Double> qs = new Quicksort<>(scores);
    qs.sort();
    int[] sortedItems = qs.getIndices();

    // loop & insert items
    int nbInserts = 0;
    wCurr = 0;
    int v2 = 0, v3 = 0;
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
      if (ttp.profitOf(k) > R*t2) {
        v2++;
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

    if (nbItems > 100000 || nbCities > 50000) return sol;



    //===========================
    // elimination
    //===========================
    // best solution
    int kBest = 0;
    double GBest = sol.ob;

    // neighbor solution
    long fp;
    double ft, G;
    long wc;
    int r;



    // improvement indicator
    boolean improved = false;

    // browse items in the new order...
    for (itr = 0; itr < nbInserts; itr++) {
      k = insertedItems[itr];

      fp = sol.fp - ttp.profitOf(k);

      // index where Bit-Flip happened
      origBF = sol.mapCI[A[k] - 1];

      // starting time
      ft = origBF == 0 ? 0 : sol.timeAcc[origBF - 1];

      // recalculate velocities from bit-flip city
      for (r = origBF; r < nbCities; r++) {
        wc = sol.weightAcc[r] - ttp.weightOf(k);;
        ft += ttp.distFor(tour[r]-1, tour[(r + 1) % nbCities]-1) / (maxSpeed - wc * C);
      }

      G = Math.round(fp - ft * R);

      // update best
      if (G > GBest) {

        kBest = k;
        GBest = G;
        improved = true;
        if (firstfit) break;
      }

    } // END FOR k

      /* update if improvement */
    if (improved) {

      // bit-flip
      pickingPlan[kBest] = 0;

      // evaluate & update vectors
      ttp.objective(sol);

      // debug msg
      if (this.debug) {
        Deb.echo(">> item elimination: best=" + sol.ob);
      }
    }

    return sol;
  }


  /**
   * 2-opt search
   *
   * deal with the TSKP sub-problem
   * 2-opt heuristic with Delaunay candidate generator
   */
  public TTPSolution fast2opt(TTPSolution sol) {

    // TTP data
    int nbCities = ttp.getNbCities();
    int nbItems = ttp.getNbItems();
    double maxSpeed = ttp.getMaxSpeed();
    double minSpeed = ttp.getMinSpeed();
    long capacity = ttp.getCapacity();
    double C = (maxSpeed - minSpeed) / capacity;

    // initial solution data
    int[] tour;

    // delta parameters
    double deltaT;

    // improvement indicator
    boolean improved;

    // best solution
    ttp.objective(sol);
    int iBest=0, jBest=0;
    double ftBest = sol.ft;

    // neighbor solution
    double ft;
    long wc;
    int i, j, c1, c2, q;
    int nbIter = 0;

    // current tour
    tour = sol.getTour();

    // search params
    double threshold = -0.1;
    if (nbItems >= 100000) {
      threshold = -10;
    }
    if (nbCities >= 50000) { // ex. pla85000 based instances
      threshold = -1000;
    }

    // search
    do {
      improved = false;
      nbIter++;

      // cleanup and stop execution if interrupted
      if (Thread.currentThread().isInterrupted()) break;

      // fast 2-opt
      for (i = 1; i < nbCities - 1; i++) {
        int node1 = tour[i] - 1;
        for (int node2 : candidates[node1]) {
          j = sol.mapCI[node2];
          //if (j<=i) continue;

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

          // update best
          if (ft - ftBest < threshold) { // soft condition
            iBest = i;
            jBest = j;
            ftBest = ft;
            improved = true;

            if (firstfit) break;
          }

          //if (firstfit && improved) break;
        } // END FOR j
        if (firstfit && improved) break;
      } // END FOR i


      //===================================
      // update if improvement
      //===================================
      if (improved) {

        // apply 2-opt move
        TwoOptHelper.do2opt(tour, iBest, jBest);

        // evaluate & update vectors
        ttp.objective(sol);
      }

      // debug msg
      if (this.debug) {
        Deb.echo(">> TSKP " + nbIter +
          ": ob=" + String.format("%.0f", sol.ob) +
          " | ft=" + String.format("%.0f", sol.ft));
      }

    } while (improved && nbIter<maxIterTSKP);

    if (debug) Deb.echo("==> 2-opt :" + nbIter + " iterations");

    // in order to compute sol.timeAcc
    // we need to use objective function
    ttp.objective(sol);

    return sol;
  }


  /**
   * 2-opt search
   *
   * deal with the TSKP sub-problem
   * 2-opt heuristic without neighborhood reduction techniques
   */
  public TTPSolution slow2opt(TTPSolution sol) {

    // TTP data
    int nbCities = ttp.getNbCities();
    int nbItems = ttp.getNbItems();
    double maxSpeed = ttp.getMaxSpeed();
    double minSpeed = ttp.getMinSpeed();
    long capacity = ttp.getCapacity();
    double C = (maxSpeed - minSpeed) / capacity;

    // initial solution data
    int[] tour;

    // delta parameters
    double deltaT;

    // improvement indicator
    boolean improved;

    // best solution
    ttp.objective(sol);
    int iBest=0, jBest=0;
    double ftBest = sol.ft;

    // neighbor solution
    double ft;
    long wc;
    int i, j, c1, c2, q;
    int nbIter = 0;

    // Delaunay triangulation
    //ArrayList<Integer>[] candidates = ttp.delaunay();

    // current tour
    tour = sol.getTour();

    // search params
    double threshold = -0.1;
    if (nbItems >= 100000) {
      threshold = -10;
    }
    if (nbCities >= 50000) { // ex. pla85000 based instances
      threshold = -1000;
    }

    // search
    do {
      improved = false;
      nbIter++;

      // cleanup and stop execution if interrupted
      if (Thread.currentThread().isInterrupted()) break;

      // fast 2-opt
      for (i = 1; i < nbCities - 1; i++) {
        for (j=i+1; j < nbCities; j++) {

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

          // update best
          if (ft - ftBest < threshold) { // soft condition
            iBest = i;
            jBest = j;
            ftBest = ft;
            improved = true;

            if (firstfit) break;
          }

          //if (firstfit && improved) break;
        } // END FOR j
        if (firstfit && improved) break;
      } // END FOR i


      //===================================
      // update if improvement
      //===================================
      if (improved) {

        // apply 2-opt move
        TwoOptHelper.do2opt(tour, iBest, jBest);

        // evaluate & update vectors
        ttp.objective(sol);
      }

      // debug msg
      if (this.debug) {
        Deb.echo(">> TSKP " + nbIter +
          ": ob=" + String.format("%.0f", sol.ob) +
          " | ft=" + String.format("%.0f", sol.ft));
      }

    } while (improved && nbIter<maxIterTSKP);


    // in order to compute sol.timeAcc
    // we need to use objective function
    ttp.objective(sol);
    return sol;
  }


  /**
   * bit-flip search
   *
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

    } while (improved && nbIter<maxIterKRP);

    if (debug) Deb.echo("==> bitflip :" + nbIter + " iterations");

    // in order to recover all history vectors
    ttp.objective(sol);

    return sol;
  }


}
