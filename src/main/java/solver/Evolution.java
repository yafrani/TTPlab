package solver;

import ea.Population;
import ttp.TTP1Instance;
import ttp.TTPSolution;

/**
 * Created by kyu on 10/24/15.
 */
public abstract class Evolution extends SearchHeuristic {

  public Evolution() {
    super();
  }

  public Evolution(TTP1Instance ttp) {
    super(ttp);
  }

  public static final int MAX_GEN = 100000; // basically doesn't matter much...
  public static final int MAX_IDLE_STEPS = 5000;

  // GA params
  public static final double MUTATION_RATE = .1;
  public static final double SELECTION_RATE = .75; // .50 | .80 | .85 | .60
  public static final int POP_SIZE = 20; // 30, 40, ..
  public static final int TOURNAMENT_SIZE = 5;

  // MA params
  public static final double LS_RATE = .2; // .1

  Population pop;

}
