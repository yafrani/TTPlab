package oldsolver;

import solver.LocalSearch;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import utils.TwoOptHelper;


/**
 * local search algorithms 
 * based on TSP's 2-opt and KP's Bit-flip
 * 
 * @author kyu
 *
 */
public class Joint2optBFOLD extends LocalSearch {
  
  
  public Joint2optBFOLD() {
    super();
  }
  
  public Joint2optBFOLD(TTP1Instance ttp) {
    super(ttp);
  }
  
  public Joint2optBFOLD(TTP1Instance ttp, TTPSolution s0) {
    super(ttp, s0);
  }
  
  
  @Override
  public TTPSolution solve() {
    
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
    
    // delta parameters
    int deltaP, deltaW;
    
    // improvement indicator
    boolean improv;
    
    // best solution
    int iBest=0, jBest=0, kBest=0;
    double obBest = sol.ob;
    
    // neighbor solution
    int fp2;
    double ft2, ob2;
    int nbIter = 0;
    int wc, start;
    int i=3, j=7, k=5;
    
    
    do {
      improv = false;
      nbIter++;
      
      // TSP 2opt exchange
      for (i=1; i<nbCities-1; i++) {
        for (j=i+1; j<nbCities; j++) {
          
          // KP bit-flip
          for (k=0; k<nbItems; k++) {
            
            /*
             * cleanup and stop execution
             */
            if (Thread.currentThread().isInterrupted()) {
              return sBest;
            }
            
            
            // check if new weight doesn't exceed knapsack capacity
            if (pickingPlan[k]==0 && ttp.weightOf(k)>sol.wend) {
              continue;
            }
            
            /*
             * KP
             */
            // calculate delta-profit and delta-weight
            if (pickingPlan[k]==0) {
              deltaP = ttp.profitOf(k);
              deltaW = ttp.weightOf(k);
            }
            else {
              deltaP = -ttp.profitOf(k);
              deltaW = -ttp.weightOf(k);
            }
            
            fp2 = sol.fp + deltaP;
            
            /*
             * velocity-TSP
             * TSP constrained with knapsack weight
             */
            // tour index where change happened (from which the item is picked/leaved)
            int refBF;
            for (refBF=0; refBF<nbCities; refBF++) {
              if (A[k]==tour[refBF]) break;
            }
            
            // tour index from which start recalculation
            start = refBF<i-1 ? refBF : i-1;
            
            // time to start with
            ft2 = start==0 ? .0 : sol.timeAcc[start-1];
            wc = start==0 ? 0 : sol.weightAcc[start-1];
            //P.echo(refBF+"--"+wc+"--"+deltaW+"--"+ft2);
            
            int[] wcR = new int[nbCities];
            
            // recalculate velocities from start
            for (int r=start; r<nbCities; r++) {
              
              /** @Todo: replace weightRecord with weightAcc ? */
              wc += TwoOptHelper.get2optValue(r, sol.weightRec, i, j);
              
              // add delta stating from refBF
              if (TwoOptHelper.get2optIndex(r, tour, i, j)==refBF) {
                wc += deltaW;
              }
              
              int c1 = TwoOptHelper.get2optValue(r, tour, i, j)-1;
              int c2 = TwoOptHelper.get2optValue((r+1)%nbCities, tour, i, j)-1; // todo: avoid using the % operator...
              
              ft2 += D[c1][c2] / (maxSpeed-wc*C);
              
              wcR[r] = wc;
              //P.echo(r+": ("+(c1+1)+","+(c2+1)+") wc "+wc+" ft "+String.format("%.2f", ft2));
            }
            
            ob2 = fp2 - ft2*R;
            
//            Deb.echo("("+i+","+j+","+k+") iBF:"+refBF+" || "+"ft: "+ ft2 + " | G: " + ob2 + " | fp: "+fp2);

            // update best
            if (ob2 > obBest) {
              
              iBest = i;
              jBest = j;
              kBest = k;
              obBest = ob2;
              improv = true;
              
              if (firstfit) break;
            }
            
          } // END FOR k
          if (firstfit && improv) break;
        } // END FOR j
        if (firstfit && improv) break;
      } // END FOR i
      
      
      /*
       * test improvement
       */
      if (improv) {
        
        // 2opt invert
        TwoOptHelper.do2opt(tour, iBest, jBest);
        
        // bit-flip
        pickingPlan[kBest] = pickingPlan[kBest]!=0 ? 0 : A[kBest];
        
        ttp.objective(sol);
        sBest = sol.clone();
        
        // debug print
        if (this.debug) {
          Deb.echo("Best "+nbIter+":");
          Deb.echo(sol);
          Deb.echo("ob-best: "+sol.ob);
          Deb.echo("wend   : "+sol.wend);
          Deb.echo("---");
        }
      }
      
    } while(improv);
    
    return sBest;
  }
  

}
