package solver;

import ttp.TTP1Instance;
import ttp.TTPSolution;

public abstract class TTPSolver extends TTPHeuristic {

  public TTPSolver() {
    super();
  }
  
  public TTPSolver(TTP1Instance ttp) {
    super(ttp);
  }
  
  public abstract TTPSolution solve();
  
}
