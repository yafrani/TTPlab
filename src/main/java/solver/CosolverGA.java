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
public class CosolverGA extends LocalSearch {

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
    insertItems(sol);

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
    int MAX_ITR = 100;
    int popSize = 100, selectSize = 50, mutationSize = 10;







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

      // create initial population
      TTPSolution[] pop = new TTPSolution[popSize];
      pop[0] = new TTPSolution(tour, pickingPlan.clone());
      ttp.objective(pop[0]);
      // diversify the initial population
      for (i=1; i<popSize; i++) {
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
      for (int u=1;u<popSize;u++) {
        ttp.objective(pop[u]);
        //Deb.echo(u+": "+pop[u].wend);
      }

      // debug
//      for (i=0; i< popSize; i++) {
//        Deb.echo(pop[i].getPickingPlan());
//      }

      //if(true) return null;

      do {

        improv2 = false;
        nbIter2++;

        /* evaluate */
        Deb.echo("## EVALUATION ##");
        double min = Double.MAX_VALUE;
        for (i=0; i < popSize; i++) {
          ttp.objective(pop[i]);
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
          spop[i] = pop[sortIdx[i]];
        }

        // accumulated fitness
        double accFit[] = new double[popSize];
        accFit[0] = normFit[0];
        for (i=1; i < popSize; i++) {
          accFit[i] = accFit[i-1] + normFit[i];
        }

        //for (i=0; i<popSize; i++) {
        //  Deb.echo(i+": "+spop[i].ob+" / "+accFit[i]+" / "+normFit[i]);
        //}
        //if (true) return null;

//        Deb.echol("SORT: "); Deb.echo(sortInd);
//        Deb.echol("NORM: "); Deb.echo(normFit);
//        Deb.echol("ACCF: "); Deb.echo(accFit);

        /* simple roulette selection */
        Deb.echo("## SELECTION ##");
        int[] selectIdx = new int[selectSize];
        for (i=0; i < selectSize; i++) {
          double RR = Math.random();
          //Deb.echo("R: "+RR);
          selectIdx[i] = -1;
          for (j=0; j < popSize; j++) {
            // check if already selected
            boolean ok = true;
            for (k=0; k < i+1; k++) {
              if (selectIdx[k]==j) {
                ok = false;
                break;
              }
            }
            if (!ok) {
              RR = Math.random();
              Deb.echo(i+" >> "+j);
              //i--;
              //continue;
              //continue;
            }
            // select
            if (ok && accFit[j] > RR) {
              selectIdx[i] = j;
              break;
              //Deb.echo(sortInd[j]+" is selected");
            }
          }
        }
        Deb.echol("selected: "); Deb.echo(selectIdx);

        /* crossover */
        Deb.echo("## CROSSOVER ##");
        int[][] childrenPP = new int[selectSize][nbItems];
        TTPSolution[] children = new TTPSolution[selectSize];
        int nbChildren = 0, n;
        for (i=0; i < selectSize/2; i++) {
          do {
            Deb.echo(">>>>>>>>>>>>>>>>>>>>>>>==OK");
            n = RandGen.randInt(0, selectSize - 1);
          } while(n==i);

          //n = selectSize-1-i;
          double CR = Math.random();
          Deb.echo(">>>> "+selectIdx[i]+" & "+selectIdx[n]+" "+CR);

          // reproduction
          int v1 = new Double(nbItems * CR).intValue(); // cross point

          // make a one point crossover
          for (j=0; j < v1; j++) {
            childrenPP[i][j] = spop[selectIdx[n]].getPickingPlan()[j];
            childrenPP[n][j] = spop[selectIdx[i]].getPickingPlan()[j];
          }
          for (j=v1; j < nbItems; j++) {
            childrenPP[n][j] = spop[selectIdx[i]].getPickingPlan()[j];
            childrenPP[i][j] = spop[selectIdx[n]].getPickingPlan()[j];
          }
          TTPSolution child1 = new TTPSolution(tour, childrenPP[i]);
          TTPSolution child2 = new TTPSolution(tour, childrenPP[n]);
          ttp.objective(child1); // TODO not necessary... replace with simple
          ttp.objective(child2); // weight calculation

          if (child1.wend >= 0) {
            children[nbChildren++] = child1;
          } else {
            Deb.echo("===========");
            children[nbChildren++] = pop[selectIdx[i]];
          }

          if (child2.wend >= 0) {
            children[nbChildren++] = child2;
          } else {
            Deb.echo("===========");
            children[nbChildren++] = pop[selectIdx[n]];
          }

//          if (child1.wend < 0 && child2.wend < 0) {
//            Deb.echo(">>"+i+" > "+child1.wend+"/"+child2.wend);
//            i--;
//          }
        }

        Deb.echo("nbCh: "+nbChildren);

        /* mutation */
        Deb.echo("## MUTATION ##");
        for (int u=0; u<mutationSize; u++) {
          int randIdx = RandGen.randInt(0, nbChildren-1);
          int[] pp = children[randIdx].getPickingPlan();
          // bit-flip
          int randGene = RandGen.randInt(0, nbItems-1);
          if (pp[randGene]==0 && ttp.weightOf(randGene) > children[randIdx].wend) {
            u--;
            continue;
          }
          pp[randGene] = pp[randGene]!=0 ? 0:A[randGene];
          // recompute objective
          ttp.objective(children[randIdx]);
          //Deb.echo("OK"+u);
        }

        /* new generation */
        int nbSelects = 0;
        for (int u = 0; u < selectSize; u++) {
          if (selectIdx[u]==-1) {
            continue;
          }
          pop[nbSelects++] = pop[selectIdx[u]];
        }

        for (int u = nbSelects; u < nbSelects + nbChildren; u++) {
          pop[u] = children[u-nbSelects];
        }
        // best solution
        TTPSolution bestGen = pop[0];
        for (int u=1;u<popSize;u++) {
          if (pop[u].ob > bestGen.ob) {
            bestGen = pop[u];
          }
        }

        for (i=0; i<popSize; i++) {
          //Deb.echo(i+": "+spop[i].ob+" / "+accFit[i]+" / "+normFit[i]);
          Deb.echol(">>>"); Deb.echo(pop[i].getPickingPlan());
        }
        //if (true) return null;




        // update best if improvment
        if (bestGen.ob > GBest) {

          improv = true;

          // evaluate & update vectors
          ttp.objective(sol);

          sol = bestGen;
          GBest = bestGen.ob;

          // debug msg
          if (this.debug) {
            Deb.echo(">> KRP: " + nbIter2 + " | ob-best=" + sol.ob);
          }
        }

        nbIter2++;


        // debug msg
        if (this.debug) {
          Deb.echo(">> GA KRP: " + nbIter2 + " | ob-best=" + sol.ob);
        }

      } while(nbIter2 < MAX_ITR);





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


  /**
   * KP pre-processing
   * base on the item insertion heuristic
   */
  public void insertItems(TTPSolution sol) {

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
    double wCurr;
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

    eliminateItems(sol, insertedItems, nbInserts);
    Deb.echo("==============================================");
  }

  /**
   * search heuristic to eliminate some items
   */
  public void eliminateItems(TTPSolution sol, int[] insertedItems, int nbInserts) {

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

    // improvement indicator
    boolean improv2;

    // best solution
    int kBest=0;
    double GBest = sol.ob;

    // neighbor solution
    int fp;
    double ft, G;
    int nbIter2 = 0;
    int wc, origBF;
    int k, r, itr;



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

    } while (improv2);

  }


  public void oneBitFlip(TTPSolution sol) {

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
    int i, j, k, r, itr;

    // Delaunay triangulation
    ArrayList<Integer>[] candidates = ttp.delaunay();


    nbIter2 = 0;
    do {
      improv2 = false;
      nbIter2++;

      // browse items in the new order...
      for (k = 0; k < nbItems; k++) {

        // check if picked
          /*if (nbIter==1) {
            if(pickingPlan[k] == 0) {
              //Deb.echo("OK11");
              continue;
            }
          }
          else
          */
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

        // index where Bit-Flip happened
        origBF = sol.mapCI[A[k] - 1];

        // starting time
        ft = origBF == 0 ? .0 : sol.timeAcc[origBF - 1];

        // recalculate velocities from bit-flip city
        for (r = origBF; r < nbCities; r++) {
          wc = sol.weightAcc[r] + deltaW;
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
        //pickingPlan[kBest] = 0;
        pickingPlan[kBest] = pickingPlan[kBest] != 0 ? 0 : A[kBest];

        // evaluate & update vectors
        ttp.objective(sol);

        // debug msg
        if (this.debug) {
          Deb.echo(">> KRP: " + nbIter2 + " | ob-best=" + sol.ob);
        }
      }

    } while (improv2);

  }
}
