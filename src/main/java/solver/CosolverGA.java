package solver;

import ttp.TTP1Instance;

import ttp.TTPSolution;
import utils.Deb;
import utils.Quicksort;
import utils.RandGen;
import utils.TwoOptHelper;

import java.util.ArrayList;


/**
 * Created by kyu on 4/7/15.
 */
public class CosolverGA extends CosolverBase {

  public CosolverGA() {
    super();
  }

  public CosolverGA(TTP1Instance ttp) {
    super(ttp);
  }

  public CosolverGA(TTP1Instance ttp, TTPSolution s0) {
    super(ttp, s0);
  }


  @Override
  public TTPSolution solve() {

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

    // calculate initial objective value
    ttp.objective(s0);
    TTPSolution sol = s0.clone();//, sBest = s0.clone();

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
    int fp;
    double ft, G;
    int nbIter = 0, nbIter1, nbIter2;
    int wc, origBF;
    int i, j, k, q, r, itr;

    // Delaunay triangulation
    ArrayList<Integer>[] candidates = ttp.delaunay();

    // GA params
    int MAX_ITR = 200;
    double mutationRate = .001, selectionRate = .75;
    int popSize = 200,
        selectSize = (int) Math.round(selectionRate * popSize);
    selectSize = selectSize%2==0 ? selectSize:selectSize-1;






    do {
      improv = false;
      nbIter++;


      /*===================*
       * sub-problem 1:    *
       * TSP with knapsack *
       *===================*/
      nbIter1 = 0;
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
            for (q = i - 1; q <= j; q++) {

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














      /*===================*
       * sub-problem 2:    *
       * KP with routing   *
       *===================*/
      nbIter2 = 0;
      int noImprovCounter=0;

      /* create initial population */
      TTPSolution[] pop = new TTPSolution[popSize];
      pop[0] = new TTPSolution(tour, pickingPlan.clone());
      ttp.objective(pop[0]);

      // 1/4 of the population is a mutation of the best found
      // picking plan so far
      for (i=1; i<popSize/4; i++) {
        int[] pp = pickingPlan.clone();
        TTPSolution x = new TTPSolution(tour, pp);
        long w2 = capacity - pop[0].wend;
        for (j=0; j<nbItems*.18; j++) {

          r = RandGen.randInt(0, nbItems-1);
          if (pp[r] != 0 ) {
            // bit-flip (remove an item)
            pp[r] = 0;
            w2 -= ttp.weightOf(r);
          }
          else if (ttp.weightOf(r) + w2 < capacity) {
            // bit-flip (insert an item)
            pp[r] = A[r];
            w2 += ttp.weightOf(r);
          }
        }
        pop[i] = x;
      }

      // 3/4 of the population are random individuals
      Constructive construct = new Constructive(ttp);
      for (i=popSize/4; i<popSize; i++) {
        int[] pp = construct.randomPickingPlan();
        pop[i] = new TTPSolution(tour, pp);
      }

      // calculate objective
      for (i=1; i<popSize; i++) {
        ttp.objective(pop[i]);
      }


      do {

        improv2 = false;
        nbIter2++;

        /*
         * Evaluate
         */
        //Deb.echo("## EVALUATION ##");

        // compute fitness
        double min = Double.MAX_VALUE;
        for (i=0; i < popSize; i++) {
          //ttp.objective(pop[i]); // TODO remove this...
          if (pop[i].ob < min) min = pop[i].ob;
        }
        min = min < 0 ? -min : 0;
        double sumFit = .0;
        double fitList[] = new double[popSize];
        for (i=0; i < popSize; i++) {
          fitList[i] = pop[i].ob + min;
          sumFit += fitList[i];
        }

        // normalize fitness
        double normFit[] = new double[popSize];
        for (i=0; i < popSize; i++) {
          normFit[i] = fitList[i] / sumFit;
          //Deb.echo(u+" >> "+fitList[u] +" | "+normFit[u]);
        }

        // sort individuals
        Quicksort qs = new Quicksort(normFit);
        qs.sort();
        int[] sortIdx = qs.getIndices();

        // sorted population
        TTPSolution spop[] = new TTPSolution[popSize];
        for (i=0; i < popSize; i++) {
          spop[i] = pop[sortIdx[i]]; // TODO remove `spop` later...
        }

        // accumulated fitness
        double accFit[] = new double[popSize];
        accFit[0] = normFit[0];
        for (i=1; i < popSize; i++) {
          accFit[i] = accFit[i-1] + normFit[i];
        }


        /*
         * Roulette/group selection
         */
        //Deb.echo("## SELECTION ##");
        int pi1=0, pi2=0;
        double RR;
        int[][] childrenPP = new int[selectSize][nbItems];
        TTPSolution[] children = new TTPSolution[selectSize];

        for (i=0; i < selectSize-1; i+=2) {

          /*
           * Crossover
           */
          // first parent
          RR = Math.random();
          for (j = 0; j < popSize; j++) {
            if (accFit[j] > RR) {
              pi1 = j;
              break;
            }
          }

          // second parent
          RR = Math.random();
          for (j = 0; j < popSize && j != pi1; j++) {
            if (accFit[j] > RR) {
              pi2 = j;
              break;
            }
          }

          // cross point
          int cp = RandGen.randInt(0,nbItems-1);
          int[] ppp1 = spop[pi1].getPickingPlan();
          int[] ppp2 = spop[pi2].getPickingPlan();

          long childW1 = 0, childW2 = 0;
          // make a one point crossover
          for (j=0; j < cp; j++) {
            childrenPP[i][j] = ppp1[j];
            childrenPP[i+1][j] = ppp2[j];
            // recover weights
            childW1 += ppp1[j]!=0 ? ttp.weightOf(j):0;
            childW2 += ppp2[j]!=0 ? ttp.weightOf(j):0;
          }
          for (j=cp; j < nbItems; j++) {
            childrenPP[i][j] = ppp2[j];
            childrenPP[i+1][j] = ppp1[j];
            // recover weights
            childW1 += ppp2[j]!=0 ? ttp.weightOf(j):0;
            childW2 += ppp1[j]!=0 ? ttp.weightOf(j):0;
          }
          TTPSolution child1 = new TTPSolution(tour, childrenPP[i]);
          TTPSolution child2 = new TTPSolution(tour, childrenPP[i+1]);

          //Deb.echo("===> ch1 "+childW1);
          //Deb.echo("===> ch2 "+childW2);

          //ttp.objective(child1); // TODO| not necessary... replace with
          //ttp.objective(child2); // TODO| partial delta

          //Deb.echo("***> ch1 "+(capacity-child1.wend));
          //Deb.echo("***> ch2 "+(capacity-child2.wend));

          //if (true) return null;

          if (childW1 <= capacity) {
            children[i] = child1;
          } else {
            Deb.echo("===========");
            children[i] = spop[pi1];
          }
          if (childW2 <= capacity) {
            children[i+1] = child2;
          } else {
            Deb.echo("===========");
            children[i+1] = spop[pi2];
          }

          /*
           * Mutation
           */
          int[] pp = children[i].getPickingPlan();
          for (j=0; j<nbItems; j++) {
            if (Math.random() < mutationRate) {
              if (pp[j] == 0 && ttp.weightOf(j) > children[i].wend) break;
              pp[j] = pp[j] != 0 ? 0 : A[j];
            }
          }
          pp = children[i+1].getPickingPlan();
          for (j=0; j<nbItems; j++) {
            if (Math.random() < mutationRate) {
              // bit-flip
              if (pp[j] == 0 && ttp.weightOf(j) > children[i+1].wend) break;
              pp[j] = pp[j] != 0 ? 0 : A[j];
            }
          }

          // recompute objective
          ttp.objective(children[i]); // TODO: replace with partial delta
          // recompute objective
          ttp.objective(children[i+1]); // TODO: replace with partial delta

        }

        /*
         * new generation
         */
        for (i = 0; i < selectSize; i++) {
          pop[i] = children[i];
        }
        //for (i = selectSize; i < popSize; i++) {
          //pop[i] = children[i-selectSize];
        //}

        // get best solution
        TTPSolution bestGen = pop[0];
        for (i=1; i<popSize; i++) {
          if (pop[i].ob > bestGen.ob) {
            bestGen = pop[i];
          }
        }


        // update best if improvement
        if (bestGen.ob > GBest) {
          Deb.echo(nbIter2+" >>> "+noImprovCounter);
          noImprovCounter = 0;
          improv = true;

          // evaluate & update vectors
          sol = bestGen.clone();
          //ttp.objective(sol);

          GBest = bestGen.ob;

          // debug msg
          if (this.debug) {
            //Deb.echo(">> IMPROVEMENT MADE!");
            Deb.echo(">> GA KRP: " + nbIter2 + " | ob-best=" + sol.ob);
          }
        }
        else {
          noImprovCounter++;
        }

        // debug msg
        if (this.debug) {
          //Deb.echo(">> GA KRP: " + nbIter2 + " | ob-best=" + sol.ob);
        }

        nbIter2++;



      } while (noImprovCounter<MAX_ITR);





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
