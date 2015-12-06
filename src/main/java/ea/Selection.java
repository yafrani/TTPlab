package ea;

import solver.Evolution;
import ttp.TTPSolution;

/**
 * Created by kyu on 11/4/15.
 */
public class Selection {

  // Selects candidate tour for crossover
  public static TTPSolution[] tournament(Population pop) {

    TTPSolution[] parents = new TTPSolution[2];

    // Create a tournament population
    Population tournament = new Population(Evolution.TOURNAMENT_SIZE);
    // for each place in the tournament get a random
    // candidate tour and add it
    int randomId;
    for (int i = 0; i < Evolution.TOURNAMENT_SIZE; i++) {
      randomId = (int) (Math.random() * Evolution.POP_SIZE);
      tournament.sol[i] = pop.sol[randomId];
    }
    // Get the fittest tour
    //parents[0] = tournament.fittest();
    int fittestIdx = tournament.fittestIndex();
    parents[0] = tournament.sol[fittestIdx];

    // repeat procedure for 2nd parent
    tournament = new Population(Evolution.TOURNAMENT_SIZE);
    for (int i = 0; i < Evolution.TOURNAMENT_SIZE; i++) {
      do {
        randomId = (int) (Math.random() * Evolution.POP_SIZE);
      } while (randomId==fittestIdx);
      tournament.sol[i] = pop.sol[randomId];
    }
    parents[1] = tournament.fittest();

    return parents;
  }

}
