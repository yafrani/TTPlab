package ea;

import solver.TTPHeuristic;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.ConfigHelper;
import utils.Deb;
import utils.GraphHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by kyu on 11/2/15.
 */
public class Initialization extends TTPHeuristic {

  protected HashSet<Integer>[] candidates;

  public Initialization() {
    // generate Delaunay triangulation
    candidates = GraphHelper.delaunayKNN(ttp, 5);
  }

  public Initialization(TTP1Instance ttp) {
    this.ttp = ttp;
    // generate Delaunay triangulation
    candidates = GraphHelper.delaunayKNN(ttp, 5);
  }


  // This procedure calculates the objective of solution tour as a sum
  // of distances between cities.
  public long evaluateTSP(int[] tour) {
    long p = 0;
    int c1,c2;
    int nbCities = ttp.getNbCities();

    for (int i=0; i<nbCities-1; i++) {
      c1 = tour[i];
      c2 = tour[i+1];
      p = p + ttp.distFor(c1-1, c2-1);
    }
    p = p + ttp.distFor(tour[nbCities-1]-1, tour[0]-1);

    return p;
  }


  // calculate delta-objective value, the delta value is the diff
  // between the objective of a solution and its modified version
  // (also called neighbor)
  // i1 and i2 represent the indexes of first vertices of exchanged edges
  public long deltaTSP(int[] tour, int i1, int i2) {

    int nbCities = ttp.getNbCities();
    long obj = 0;
    int k1, k2;

    if (i1 == nbCities-1)
      k1 = 0;
    else
      k1 = i1+1;

    if (i2 == nbCities-1)
      k2 = 0;
    else
      k2 = i2+1;

    obj = obj - ttp.distFor(tour[k1]-1,tour[i1]-1) + ttp.distFor(tour[i2]-1,tour[i1]-1);
    obj = obj - ttp.distFor(tour[k2]-1,tour[i2]-1) + ttp.distFor(tour[k1]-1,tour[k2]-1);

    return obj;
  }


  // random initialization procedure
  public int[] randomTour() {

    int nbCities = ttp.getNbCities();
    int[] tour = new int[nbCities];

    for (int i=0; i< nbCities; i++)
      tour[i] = i+1;

    int s1,s2,tmp;
    Random generator = new Random();
    for (int i=1; i<10000; i++) {
      do {
        s1 = generator.nextInt(nbCities);
        s2 = generator.nextInt(nbCities);
      } while (s1==s2 || s1==0 || s2==0);
      // swap cities
      tmp = tour[s1];
      tour[s1] = tour[s2];
      tour[s2] = tmp;
    }
    return tour;
  }


  /**
   * 2-opt search
   */
  public int[] tsp2opt(int[] tour) {

    //tour = new int[]{ 1, 34, 28, 20, 31, 42,  2, 49, 10,  8, 21, 40, 51, 43, 37, 22, 15, 23, 19, 48,  3,  4, 45, 38, 33,  9, 26, 35, 14, 29, 50, 46, 44, 25, 16, 17, 27, 30,  6, 52, 12, 18, 39, 41,  7, 24, 13, 11, 47, 36,  5, 32};
    long obj = evaluateTSP(tour);
//    Deb.echo("obj: "+obj);


    long bestobj = obj;
    boolean improv;
    int nbCities = ttp.getNbCities();
    int nstep = 0;
    int i,j,tmp,di,k;

    int[] itour = new int[nbCities];
    for (int c=0;c<nbCities;c++) {
      itour[tour[c]-1] = c;
    }

    // Delaunay triangulation
    //for (Collection x : candidates)
    //  Deb.echo(x);

    // the search loop is starting here
    do {
      improv = false;

      // we exploit the neighborhood
      for (i = 0; i < nbCities - 1; i++) {
        //for (j = i + 1; j < nbCities; j++) {
        int node1 = tour[i] - 1;
        for (int node2 : candidates[node1]) {
          j = itour[node2];
          if (j<=i) continue;
          //Deb.echo("--> "+i+"/"+j);

          // here we calculate the candidate objective
          long objc = obj + deltaTSP(tour, i, j);
          //Deb.echo(delta(tour,i1,i2)+"/"+i1+"/"+i2);

          // when the acceptance condition is satisfied we do two things:
          if (objc <= obj) {
            // 1. we make the current objective to be equal to the
            // candidate one
            obj = objc;
            // 2. we update the solution tour. Here we exchange two edges:
            // (s1,s1+1) and (s2,s2+1) into (s1,s2) and (s1+1, s2+1).
            di = i + j + 1;
            for (k = i + 1; k <= (i + j) / 2; k++) {
              tmp = tour[k];
              tour[k] = tour[di - k];
              tour[di - k] = tmp;
            }
            //Deb.echo(objc+":"+evaluate(tour));
          }
        }
      }

      // also we check whether or not our solution is the best found yet
      if (obj < bestobj) {
        bestobj = obj;
        improv = true;
      }

      // we increment the number of iterations
      nstep++;

      // debug print
//      Deb.echo(">> "+nstep + ": " + bestobj);

      // continue search while there is improvement
    } while (improv);

    return tour;
  }


  /**
   * random tour
   * + 1 kick using LK
   */
  public int[] rlinkern() {

    int nbCities = ttp.getNbCities();
    int[] tour = new int[nbCities];

    String fileName = ttp.getTspName().replaceAll("-.+", "");

    // number of LK kicks
    int nbKicks = 1;
    if (nbCities>100)
      nbKicks = 10;
    if (nbCities>1000)
      nbKicks = 80;
    if (nbCities>10000)
      nbKicks = 500;
    if (nbCities>30000)
      nbKicks = 800;
    // ...

    //Deb.echo("nb LK kicks: "+nbKicks);
    //if (Thread.currentThread().isInterrupted()) return null;

    try {
      // execute linkern program
      String[] cmd = {"./bins/linkern/rlinkern.sh", fileName, ""+nbKicks};
      Runtime runtime = Runtime.getRuntime();
      Process proc = runtime.exec(cmd);

      proc.waitFor();

      // read output tour
      File tourFile = new File("./bins/linkern/"+fileName+".tour");
      BufferedReader br = new BufferedReader(new FileReader(tourFile));
      String line;
      br.readLine(); // skip first line
      int i = 0;
      while ((line = br.readLine()) != null) {
        String[] parts = line.split("\\s+");
        tour[i++] = 1+Integer.parseInt(parts[0]);
      }
      tourFile.delete();
      br.close();

    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    return tour;
  }








  /**
   * Markus' code for linkern + PackIterative
   */
//  public TTPSolution lkPackIterative() {
//
//    // feed the input solution to approx jar
//    String dataDir = ConfigHelper.getProperty("ttpdata");
//    String instDir = ttp.getDirectory();
//    String instName = ttp.getName();
//    //Deb.echo(fileName+"::"+instFolder);
//
//    // setup and run algorithm
//    int maxRuntime = 100000; // in ms
//    M_TTPInstance instance = new M_TTPInstance(new File(dataDir+"/"+instDir+"/"+instName));
//    M_TTPSolution newSolution = new M_TTPSolution(new int[instance.numberOfNodes+1], new int[instance.numberOfItems]);
//    instance.evaluate(newSolution, false);
//    // execute and get solution
//    newSolution = M_Optimisation.HT(instance, maxRuntime, true);
//    int[] newTour = newSolution.normalTour();
//    int[] newPP = newSolution.normalPP(instance);
////        Deb.echo(newTour);
////        Deb.echo(newPP);
//
//    // read output solution
//    TTPSolution sf = new TTPSolution(newTour, newPP);
//    return sf;
////    problem.evaluate(sf);
////    setWend(sf.wend);
////    setOb(sf.ob);
//
////    // convert to BitSet
////    BitSet bitSet = new BitSet(newPP.length);
////
////    for (int i = 0; i < newPP.length; i++) {
////      bitSet.set(i, newPP[i]!=0);
////    }
////
////    // init tour
////    setVariableValue(0, 1);
////    for (int i = 1; i < this.getNbCities() - 1; i++) {
////      setVariableValue(i, newTour[i]);
////    }
////    setVariableValue(this.getNbCities() - 1, 1);
////
////    // start with empty bit-set
////    setVariableValue(getNbCities(), bitSet);
//  }
}
