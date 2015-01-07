package solver;

import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import utils.Quicksort;
import utils.TwoOptHelper;

/**
 * iterative algorithm
 * uses solve1 for the initial solution
 * merges TSP's 2-opt and KP's greedy algorithm from SH
 * 
 * @author kyu
 *
 */
public class Handoff2optSH extends LocalSearch {
  
  public Handoff2optSH() {
    super();
  }
  
  public Handoff2optSH(TTP1Instance ttp) {
    super(ttp);
  }
  
  public Handoff2optSH(TTP1Instance ttp, TTPSolution s0) {
    super(ttp, s0);
  }
  
  @Override
  public TTPSolution solve() {
    
    /* TTP data */
    long[][] D = ttp.getDist();
    int[] A = ttp.getAvailability();
    double maxSpeed = ttp.getMaxSpeed();
    double minSpeed = ttp.getMinSpeed();
    long capacity = ttp.getCapacity();
    double C = (maxSpeed - minSpeed) / capacity;
    double R = ttp.getRent();
    int nbCities = ttp.getNbCities(),
        nbItems = ttp.getNbItems();
    
    
    /* initial solution */
    TTPSolution sol = this.s0;
    // initial solution data
    int[] tour = sol.getTour();
    int[] pickingPlan = sol.getPickingPlan();
    ttp.objective(sol);
    
    int fp2 = sol.fp;
    double ft2, ob2 = .0;
    int nbIter = 0;
    int wc, start;
    int i=3, j=7;
    
    
    /* best solution params */
    int iBest = 0, jBest = 0;
    double obBest = sol.ob;
    TTPSolution sBest = sol;
    boolean improv = false;
    
    
    /* Simple heuristic params */
    // partial distance from city x_i
    long di;
    // partial time with item k collected from city x_i
    double tik;
    // item scores
    double[] score = new double[nbItems];
    // total time with no items collected
    double t_ = sol.ft;
    // total time with only item k collected
    double tik_;
    // fitness value
    double u[] = new double[nbItems];
    
    
    
    do {
      improv = false;
      nbIter++;
      
      
      /*
       * Find a suitable tour in a 2opt neighborhood
       * TSP 2opt exchange
       */
      for (i=1; i<nbCities-1; i++) {
        
        for (j=i+1; j<nbCities; j++) {
          
          // tour index from which start recalculation
          start = i-1;
          
          // time and weight to start with
          ft2 = start==0 ? .0 : sol.timeAcc[start-1];
          wc = start==0 ? 0 : sol.weightAcc[start-1];
          
          int[] wcR = new int[nbCities];
          
          // recalculate velocities from start
          for (int r=start; r<nbCities; r++) {
            
            wc += TwoOptHelper.get2optValue(r, sol.weightRec, i, j);
            
            int c1 = TwoOptHelper.get2optValue(r, tour, i, j)-1;
            int c2 = TwoOptHelper.get2optValue((r+1)%nbCities, tour, i, j)-1; // todo: avoid using the % operator...
            
            ft2 += D[c1][c2] / (maxSpeed-wc*C);
            
            wcR[r] = wc;
            //P.echo(r+": ("+(c1+1)+","+(c2+1)+") wc "+wc+
            //    " ft "+String.format("%.2f", ft2));
          }
          
          ob2 = fp2 - ft2*R;
          
          // update best
          if (ob2 > obBest) {
            iBest = i;
            jBest = j;
            obBest = ob2;
            improv = true;
            
            if (firstfit) break;
          }
          
        } // end FOR j
        
        if (firstfit && improv) break;
      } // end FOR i
      
      
      
      /*
       * Find a suitable picking plan
       * Based on Simple Heuristic
       */
      for (int k=0; k<nbItems; k++) {
        
        for (i=0; i<nbCities; i++) {
          if (A[k]==tour[i]) break;
        }
        //P.echo2("["+k+"]"+(i+1)+"~");
        
        // time to start with
        tik = i==0 ? .0 : sol.timeAcc[i-1];
        int iw = ttp.weightOf(k),
            ip = ttp.profitOf(k);
        
        // recalculate velocities from start
        di = 0;
        for (int r=i; r<nbCities; r++) {
          
          int c1 = tour[r]-1;
          int c2 = tour[(r+1)%nbCities]-1;
          
          di += D[c1][c2];
          tik += D[c1][c2] / (maxSpeed-iw*C);
        }
        
        score[k] = ip - R*tik;
        tik_ = t_ - di + tik;
        u[k] = R*t_ + ttp.profitOf(k) - R*tik_;
      }
      
      Quicksort qs = new Quicksort(score);
      qs.sort();
      int[] si = qs.getIndices();
      wc = 0;
      for (int k=0; k<nbItems; k++) {
        pickingPlan[k] = 0;
      }
      for (int k=0; k<nbItems; k++) {
        i = si[k];
        int wi = ttp.weightOf(i);
        
        // eliminate useless items
        if (wi+wc <= capacity && u[i] > 0) {
          pickingPlan[i] = A[i];
          wc += wi;
        }
      }
      
      
      /*
       * test improvement
       */
      if (improv) {
        
        // 2opt invert
        TwoOptHelper.do2opt(tour, iBest, jBest);
        ttp.objective(sol);
        
        sBest = sol;
        
        // debug print
        if (this.debug) {
          Deb.echo("Best "+nbIter+":");
          Deb.echo(sol);
          Deb.echo("ob-best: "+sol.ob);
          Deb.echo("wend   : "+sol.wend);
          Deb.echo("---");
        }
      }
      
      
      /*
       * cleanup and stop execution
       */
      if (Thread.currentThread().isInterrupted()) {
        return sBest;
      }
      
    } while(improv);
    
    
    
    /* save log file */
//    if (this.log) {
//      Log log = new Log(this.logfile);
//      log.print("Best "+nbIter+":");
//      log.print(sol);
//      log.print("ob-best: "+sol.ob);
//      log.print("---");
//      log.close();
//    }
    
    
    return sBest;
  }

}
