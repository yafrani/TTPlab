package solver;

import ttp.TTP1Instance;
import ttp.TTPSolution;

/**
 * single solution based
 *
 * @author kyu
 *
 */
public abstract class SearchHeuristic extends TTPHeuristic {

  protected int maxIterTSKP = Integer.MAX_VALUE;
  protected int maxIterKRP = Integer.MAX_VALUE;

  public SearchHeuristic() {
    super();
  }
  
  public SearchHeuristic(TTP1Instance ttp) {
    super(ttp);
  }
  
  public abstract TTPSolution search();
}
