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

  public static final int MAX_ITER = 20000;
  public static final int MAX_IDLE_STEPS = 200;

  // GA params
  public static final double MUTATION_RATE = .01;
  public static final double SELECTION_RATE = .95;
  public static final int POP_SIZE = 50;
  public static final int TOURNAMENT_SIZE = 40;

  Population pop;


}
