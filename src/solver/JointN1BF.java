package solver;

import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;

/**
 * local search algorithms 
 * based on TSP's N1 and KP's Bit-flip
 * 
 * @author kyu
 *
 */
public class JointN1BF extends LocalSearch {
  
  
  public JointN1BF() {
    super();
  }
  
  public JointN1BF(TTP1Instance ttp) {
    super(ttp);
  }
  
  public JointN1BF(TTP1Instance ttp, TTPSolution s0) {
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
    long[][] dist = ttp.getDist();
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
    int deltaP, deltaW,
        tmp,
        oldWR_i;
    
    // improvement indicator
    boolean improv;
    
    // best solution
    int iBest=0, kBest=0;
    double obBest = sol.ob;
    
    // neighbor solution
    int fp2;
    double ft2, ob2;
    int nbIter = 0;
    int wc, start;
    
    do {
      improv = false;
      nbIter++;
      // find all neighbors
      for (int i=1; i<nbCities-1; i++) { // TSP swap
        
        // swap city i with i+1
        tmp = tour[i];
        tour[i] = tour[i+1];
        tour[i+1] = tmp;
        oldWR_i = sol.weightAcc[i];
        
        sol.weightAcc[i] = sol.weightAcc[i-1]+sol.weightAcc[i+1]-sol.weightAcc[i];
        
        for (int k=0; k<nbItems; k++) {  // KP bit-flip
          
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
          
          /**
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
          
          /**
           * velocity-TSP
           * TSP constrained with knapsack weight
           */
          // tour index where change happened (from which the item is added/left)
          int refBF;
          for (refBF=0; refBF<nbCities; refBF++) { // necessary ?
            if (A[k]==tour[refBF]) break;
          }
          
          // tour index from which start recalculation
          start = refBF<i-1 ? refBF : i-1;
          
          // time to start with
          ft2 = start==0 ? .0 : sol.timeAcc[start-1];
          
          // recalculate velocities from start
          for (int r=start; r<nbCities; r++) {
            
            wc = sol.weightAcc[r];
            
            // add delta stating from refBF
            if (r>=refBF) {
              wc += deltaW;
            }
            
            ft2 += dist[tour[r]-1][tour[(r+1)%nbCities]-1] / (maxSpeed-wc*C);
            
            // debugging...
            int c1 = tour[r], c2 = tour[(r+1)%nbCities];
            Deb.echo(r+": ("+String.format("%2d", c1)+","+String.format("%2d", c2)+") | wc "+wc+" vs "+sol.weightAcc[r]+
                " ft "+String.format("%.2f", ft2));
          }
          
          ob2 = fp2 - ft2*R;
          
          //pickingPlan[k] = pickingPlan[k]!=0 ? 0 : A[k];
          //TTPSolution cl = sol.clone();
          //P.echo((i+1)+"x"+(i+2)+":"+(k+1)+"\n"+sol);
          //pickingPlan[k] = pickingPlan[k]!=0 ? 0 : A[k];
          
          //P.echo2(i+"~"+(i+1)+"/"+k+"  "); P.echo2("ob2: "+ob2);
          //ttp.objective(cl);
          //P.echo(" :: "+cl.ob);
          
          
          if (ob2 > obBest) {
            iBest = i;
            kBest = k;
            obBest = ob2;
            improv = true;
            
            // echos
            //pickingPlan[jBest] = pickingPlan[jBest]!=0 ? 0 : A[jBest];
            //P.echo(sol+"\n");
            //pickingPlan[jBest] = pickingPlan[jBest]!=0 ? 0 : A[jBest];
            if (firstfit) break;
          }
          
        } // END FOR k
        
        // retrieve initial solution
        tmp = tour[i];
        tour[i] = tour[i+1];
        tour[i+1] = tmp;
        sol.weightAcc[i] = oldWR_i;
        
        if (firstfit && improv) break;
      } // END FOR i
      
      
      if (improv) {
        
        // swap
        tmp = tour[iBest];
        tour[iBest] = tour[iBest+1];
        tour[iBest+1] = tmp;
        
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
      improv=false;
    } while(improv);
    
    
    return sBest;
  }
  
}
