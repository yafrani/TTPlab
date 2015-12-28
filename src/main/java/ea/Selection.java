package ea;

import solver.Evolution;
import ttp.TTPSolution;
import utils.Deb;

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
    parents[0] = tournament.fittest();

    // repeat procedure for 2nd parent
    tournament = new Population(Evolution.TOURNAMENT_SIZE);
    for (int i = 0; i < Evolution.TOURNAMENT_SIZE; i++) {
      do {
        randomId = (int) (Math.random() * Evolution.POP_SIZE);
      } while (pop.sol[randomId].ob==parents[0].ob);
      tournament.sol[i] = pop.sol[randomId];
    }
    parents[1] = tournament.fittest();

    return parents;
  }


  // TODO implement & test simple random selection
  public static TTPSolution[] random(Population pop) {
    return null;
  }
}
