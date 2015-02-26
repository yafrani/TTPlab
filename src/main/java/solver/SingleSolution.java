package solver;

import ttp.TTP1Instance;
import ttp.TTPSolution;

public abstract class SingleSolution extends TTPSolver {
  
  protected TTPSolution s0;
  
  public SingleSolution() {
    super();
  }
  
  public SingleSolution(TTP1Instance ttp) {
    super(ttp);
  }
  
  public SingleSolution(TTP1Instance ttp, TTPSolution s0) {
    super(ttp);
    this.s0 = s0;
  }
  
  // initial solution
  public void setS0(TTPSolution s0) {
    this.s0 = s0;
  }
  public TTPSolution getS0() {
    return s0;
  }
}
