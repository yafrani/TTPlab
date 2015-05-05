package solver;

import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import utils.TwoOptHelper;

/**
 * iterative algorithm
 * uses solve1 for the initial solution
 * merges TSP's 2-opt and KP's bit-flip
 * 
 * @author kyu
 *
 */
public class Handoff2optBF extends LocalSearch {
  
  public Handoff2optBF() {
    super();
  }
  
  public Handoff2optBF(TTP1Instance ttp) {
    super(ttp);
  }
  
  public Handoff2optBF(TTP1Instance ttp, TTPSolution s0) {
    super(ttp, s0);
  }
  
  @Override
  public TTPSolution solve() {
    
    // TTP data
    long[][] D = ttp.getDist();
    int[] A = ttp.getAvailability();
    double maxSpeed = ttp.getMaxSpeed();
    double minSpeed = ttp.getMinSpeed();
    long capacity = ttp.getCapacity();
    double C = (maxSpeed - minSpeed) / capacity;
    double R = ttp.getRent();
    int nbCities = ttp.getNbCities(),
        nbItems = ttp.getNbItems();
    
    
    
    // initial solution
    TTPSolution sol = s0;
    
    // initial solution data
    int[] tour = sol.getTour();
    int[] pickingPlan = sol.getPickingPlan();
    
    long fp2 = sol.fp;
    long ft2, ob2 = 0;
    int nbIter = 0;
    long wc;
    int start;
    int i=3, j=7;
    
    // best sol params
    int iBest=0, jBest=0, kBest=0;
    long obBest = sol.ob;
    TTPSolution sBest = sol;
    
    boolean improv = false;
    
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
          ft2 = start==0 ? 0 : sol.timeAcc[start-1];
          wc = start==0 ? 0 : sol.weightAcc[start-1];
          
          long[] wcR = new long[nbCities];
          
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
          
          ob2 = Math.round(fp2 - ft2*R);
          
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
      
      
      
      /**
       * Disturb packing plan
       */
      for (int k=0; k<nbItems; k++) {
        
      }
      
      
      /**
       * test improvement
       */
      if (improv) {
        
        // 2opt invert
        TwoOptHelper.do2opt(tour, iBest, jBest);
        ttp.objective(sol);
        
        sBest = sol;
        
        // debug print
        Deb.echo("---");
        Deb.echo("Best "+nbIter+":");
        Deb.echo(sol);
        Deb.echo("ob-best: "+sol.ob);
        Deb.echo("---");
      }
      
      
    } while(improv);
    
    
    Deb.echo(nbIter+">>"+ob2);
    
    return sol;
  }

}
