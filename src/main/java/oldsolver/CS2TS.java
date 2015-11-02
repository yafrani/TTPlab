package oldsolver;

import solver.LocalSearch;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import utils.TwoOptHelper;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by kyu on 4/7/15.
 */
public class CS2TS extends LocalSearch {

  public CS2TS() {
    super();
  }

  public CS2TS(TTP1Instance ttp) {
    super(ttp);
  }

  public CS2TS(TTP1Instance ttp, TTPSolution s0) {
    super(ttp, s0);
  }


  @Override
  public TTPSolution search() {

    // calculate initial objective value
    ttp.objective(s0);

    // copy initial solution into improved solution
    TTPSolution sol = s0.clone(), sBest = s0.clone();

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
    boolean improv, improv1;

    // best solution
    int iBest=-1, jBest=-1, kBest=-1;
    double GBest = sol.ob;
    double ftBest = sol.ft;

    // neighbor solution
    long fp;
    double ft, G;
    long wc;
    int origBF;
    int i, j, k, r;
    int nbIter = 0, nbIter1, nbIter2;

    // Delaunay triangulation
    ArrayList<Integer>[] candidates = ttp.delaunay();

    // tabu search params
    LinkedList<Integer> tabuList = new LinkedList<>();
    int maxTabuSize = 100, tabuTenure = 200, tabuCount;
    double GBestCand;
    int kBestCand = -1;



    do {

      improv = false;
      nbIter++;



      /*===================*
       * sub-problem 1:    *
       * TSP with knapsack *
       *===================*/
      nbIter1 = 0;
      tour = sol.getTour();

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

              deltaT += -sol.timeRec[q] + ttp.distFor(c1, c2) / (maxSpeed - wc * C);
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
          sol.setTour(tour);
          // evaluate & update vectors
          ttp.objective(sol);

          // debug msg
          if (this.debug) {
            Deb.echo(ftBest+">> TSKP: " + nbIter1 + " | ob-best=" + sol.ob);
          }
        }

      } while (improv1);

      //if (!improv) break;
      sBest = sol.clone();







      /*=================*
       * sub-problem 2   *
       * KP with routing *
       *=================*/
      nbIter2 = 0;
      pickingPlan = sol.getPickingPlan();
      maxTabuSize = nbItems/10;
      tabuTenure = 200;
      tabuList = new LinkedList<>();
      tabuCount = 0;

      do {
        nbIter2++;
        GBestCand = -Double.MAX_VALUE;

        // TODO: reduce nb of candidates (insert/eliminate, score sorting, etc.)
        for (k = 0; k < nbItems; k++) {

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
          origBF = sol.mapCI[A[k] - 1];

          // starting time
          ft = origBF == 0 ? 0 : sol.timeAcc[origBF - 1];

          // recalculate velocities from bit-flip city
          for (r = origBF; r < nbCities; r++) {
            wc = sol.weightAcc[r] + deltaW;
            ft += ttp.distFor(tour[r] - 1, tour[(r + 1) % nbCities] - 1) / (maxSpeed - wc * C);
          }

          G = fp - ft * R;

          //Deb.echo(">>>>>>>>>>>>>>"+G);
          /* update best candidate */
          // TODO: tabu test could be used before evaluation ?
          //Deb.echo(">>> "+G+" / "+GBestCand);
          if ( G > GBestCand && !tabuList.contains(k) ) {
            //Deb.echo("OKAAAAAAAAY");
            kBestCand = k;
            GBestCand = G;
            //if (firstfit) break;
          }

        } // END FOR k

        /* update current solution */
        // bit-flip
        pickingPlan[kBestCand] = pickingPlan[kBestCand] != 0 ? 0 : A[kBestCand];
        sol.setPickingPlan(pickingPlan);
        // re-evaluate & update vectors
        ttp.objective(sol);

        /* update best solution if improvement */
        Deb.echo(GBestCand +"///"+sol.ob);
        if ( GBestCand > GBest ) {
          Deb.echo("***************");
          improv = true;
          GBest = GBestCand;
          sBest = sol.clone();
        }
        else { // no improvement made
          tabuCount++;
        }

        /* update tabu list */
        if (tabuList.isEmpty() || kBestCand != tabuList.getFirst()) {
          tabuList.addFirst(kBestCand);
          if (tabuList.size() > maxTabuSize) {
            tabuList.removeLast();
          }
        }

        // debug msg
        if (this.debug) {
          Deb.echo(">> KRP: " + nbIter2 + " | ob-best=" + sol.ob);
          //Deb.echo(tabuCount + " ~ " + tabuList.size() + " ~ " + tabuList);
        }

      } while (tabuCount < tabuTenure);

      //Deb.echo("===================");
      //if (true) return sBest;



      sol = sBest.clone(); // TODO no clone needed...



      // debug msg
      if (this.debug) {
        Deb.echo("Best "+nbIter+":");
        Deb.echo(sol);
        Deb.echo("ob-best: "+sol.ob);
        Deb.echo("wend   : "+sol.wend);
        Deb.echo("---");
      }

    } while (improv);

    return sBest;
  }

}
