package solver;

import ttp.TTP1Instance;
import ttp.TTPSolution;

/**
 * local search algorithms
 * 
 * @author kyu
 * 
 */
public abstract class LocalSearch extends SingleSolution {
  
  /**
   * first fit or best fit
   */
  public boolean firstfit;
  
  
  public LocalSearch() {
    super();
  }
  
  public LocalSearch(TTP1Instance ttp) {
    super(ttp);
  }
  
  public LocalSearch(TTP1Instance ttp, TTPSolution s0) {
    super(ttp, s0);
  }
  
  
  /**
   * use first fit strategy
   */
  public void firstfit() {
    firstfit = true;
  }
  
  
  /**
   * use best fit strategy
   */
  public void bestfit() {
    firstfit = false;
  }
  
  @Override
  public String getName() {
    String suf = this.firstfit ? "-FF" : "-BF";
    return this.name + suf;
  }
}
