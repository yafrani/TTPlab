package ttp;

import utils.Deb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

/**
 * a TTP solution
 * 
 * tour: cities references, starts from 1
 * picking plan: '0' if the item is not picked,
 *               'city reference (i)' if picked (from city i)
 * 
 * @author kyu
 *
 */
public class TTPSolution {
  
  private int[] tour;
  private int[] pickingPlan;
  
  public long fp;
  public double ft;
  public double ob;
  public long wend;

  // time accumulator
  public double[] timeAcc;
  // time record
  public double[] timeRec;
  // weight accumulator
  public long[] weightAcc;
  // weight record at each iteration
  public long[] weightRec;
  // tour mapper
  public int[] mapCI;


  private void initSolution(int[] tour, int[] pickingPlan) {
    this.tour = tour;
    this.pickingPlan = pickingPlan;

    // records
    this.timeAcc = new double[this.tour.length];
    this.timeRec = new double[this.tour.length];
    this.weightAcc = new long[this.tour.length];
    this.weightRec = new long[this.tour.length];
    this.mapCI = new int[this.tour.length];
  }

  public TTPSolution() {

  }
  
  public TTPSolution(int[] tour, int[] pickingPlan) {
    initSolution(tour, pickingPlan);
  }
  
  public TTPSolution(int m, int n) {
    
    this.tour = new int[m];
    this.pickingPlan = new int[n];

    // records
    timeAcc = new double[tour.length];
    timeRec = new double[tour.length];
    weightAcc = new long[tour.length];
    weightRec = new long[tour.length];
    mapCI = new int[tour.length];
  }
  
  public TTPSolution(TTPSolution s2) {

    this.tour = Arrays.copyOf(s2.tour,s2.tour.length);
    this.pickingPlan = Arrays.copyOf(s2.pickingPlan,s2.pickingPlan.length);
    
    this.fp = s2.fp;
    this.ft = s2.ft;
    this.ob = s2.ob;
    this.wend = s2.wend;
    
    this.timeAcc= Arrays.copyOf(s2.timeAcc,s2.timeAcc.length);
    this.timeRec= Arrays.copyOf(s2.timeRec,s2.timeRec.length);
    this.weightAcc = Arrays.copyOf(s2.weightAcc,s2.weightAcc.length);
    this.weightRec = Arrays.copyOf(s2.weightRec,s2.weightRec.length);
    this.mapCI = Arrays.copyOf(s2.mapCI,s2.mapCI.length);
  }
  
  public TTPSolution(String filePath) {
    File solFile = new File(filePath);
    BufferedReader br = null;

    int nbCities = 0, nbItems = 0;

    try {
      br = new BufferedReader(new FileReader(solFile));
      String line;

      // scan tour
      while ((line = br.readLine()) != null) {

        // number of cities
        if (line.startsWith("DIMENSION")) {
          line = line.substring(line.indexOf(":")+1);
          line = line.replaceAll("\\s+","");
          nbCities = Integer.parseInt(line);
        }

        // number of items
        if (line.startsWith("NUMBER OF ITEMS")) {
          line = line.substring(line.indexOf(":")+1);
          line = line.replaceAll("\\s+","");
          nbItems = Integer.parseInt(line);
        }

        if (line.startsWith("TOUR_SECTION")) {
          this.tour = new int[nbCities];
          for (int j=0; j<nbCities; j++) {
            line = br.readLine();
            tour[j] = Integer.parseInt(line);
          }
        }

        if (line.startsWith("PP_SECTION")) {
          this.pickingPlan = new int[nbItems];
          for (int j=0; j<nbItems; j++) {
            line = br.readLine();
            pickingPlan[j] = Integer.parseInt(line);
          }
        }
      } // end while

      br.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    this.initSolution(tour, pickingPlan);
  }

  @Override
  public String toString() {
    // the tour
    String s = "tsp tour    : (";
    for (int i=0;i<tour.length; i++) {
      s += tour[i] + " ";
    }
    s = s.substring(0, s.length()-1) + ")\n";
    
    // the picking plan
    s += "picking plan: (";
    for (int i=0;i<pickingPlan.length; i++) {
      int pp = pickingPlan[i];
      s += pp + " ";
    }
    s = s.substring(0, s.length()-1) + ")";
    
    return s;
  }
  
  @Override
  public TTPSolution clone() {
    return new TTPSolution(this);
  }
  
  @Override
  public boolean equals(Object o2) {
    
    TTPSolution s2 = (TTPSolution) o2;
    
    for (int i=0; i<this.tour.length; i++) {
      if (this.tour[i]!=s2.tour[i]) return false;
    }
    for (int i=0; i<this.pickingPlan.length; i++) {
      if (this.pickingPlan[i]!=s2.pickingPlan[i]) return false;
    }
    
    return true;
  }
  
  // getters
  public int[] getTour() {
    return tour;
  }
  public int[] getPickingPlan() {
    return pickingPlan;
  }
  
  // setters
  public void setTour(int[] tour) {
    this.tour = tour;
  }
  public void setPickingPlan(int[] pickingPlan) {
    this.pickingPlan = pickingPlan;
  }


  public String output() {
    String s=
      "DIMENSION : "+tour.length+"\n" +
      "NUMBER OF ITEMS : "+pickingPlan.length+"\n" +
      "\n";

    s +=  "TOUR_SECTION\n";
    for (int x : tour) {
      s += x+"\n";
    }
    s += "\n";

    s += "PP_SECTION\n";
    for (int x : pickingPlan) {
      s += x+"\n";
    }

    s += "EOF";

    return s;
  }

  public void printStats() {

    Deb.echo("============");
    Deb.echo(" STATISTICS ");
    Deb.echo("============");
    Deb.echo("objective   : " + this.ob);
    Deb.echo("final time  : " + this.fp);
    Deb.echo("final weight: " + this.wend);
    Deb.echo("final profit: " + this.fp);

    int cmpItems = 0;
    int nbItems = this.pickingPlan.length;
    for(int x:this.pickingPlan) if(x!=0) cmpItems++;
    Deb.echo("percent inserted: " + cmpItems + "/" + nbItems + "(" +
      String.format("%.2f", (cmpItems * 100.0) / nbItems) + "%)");
    Deb.echo("============");
  }
}
