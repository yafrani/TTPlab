package ttp;

import java.util.Arrays;

/**
 * a TTP solution
 * 
 * tour: cities references, starts from 1
 * picking plan: 0 if the item is not picked, city reference (i) if picked (from city i)
 * 
 * @author kyu
 *
 */
public class TTPSolution {
  
  private int[] tour;
  private int[] pickingPlan;
  
  public int fp;
  public double ft;
  public double ob;
  public long wend;
  
  /**
   * time accumulator
   */
  public double[] timeAcc;
  public double[] timeRec;
  /**
   * weight accumulator
   */
  public int[] weightAcc;
  
  /**
   * weight record at each iteration
   */
  public int[] weightRecord;
  
  
  public TTPSolution() {
    
  }
  
  public TTPSolution(int[] tour, int[] pickingPlan) {
    
    this.tour = tour;
    this.pickingPlan = pickingPlan;
    
    // records
    timeAcc = new double[tour.length];
    timeRec = new double[tour.length];
    weightAcc = new int[tour.length];
    weightRecord = new int[tour.length];
  }
  
  public TTPSolution(int m, int n) {
    
    this.tour = new int[m];
    this.pickingPlan = new int[n];
    
    // records
    timeAcc = new double[tour.length];
    timeRec = new double[tour.length];
    weightAcc = new int[tour.length];
    weightRecord = new int[tour.length];
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
    this.weightRecord = Arrays.copyOf(s2.weightRecord,s2.weightRecord.length);
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
  
}
