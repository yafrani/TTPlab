package utils;

/**
 * Created by kyu on 12/9/15.
 */
public class kOptMoves {

  void Perturb4Opt(int Strength, int pbsize) {
    // Purpose    : Perturb the current TSP tour using 4-Opt double bridge move
    //              This 4-Opt move can't be reversed easily by 2-Opt/3-Opt.
    // Complexity : O(n)

//    int pos1,pos2,pos3;
//    int[] newSolution = new int[pbsize];
//
//    Prev_OV = Cur_OV;
//    for (int i=0; i<n; i++) { // backup...
//      tempSolution[i] = solution[i];
//      previousSolutionMap[i] = solutionMap[i];
//    }
//
//    for (l=0; l<Strength; l++) {
//      // 4-Opt double bridge move... pick 3 split points randomly...
//      pos1 = 1 + rand() % (n / 4);
//      pos2 = pos1 + 1 + rand() % (n / 4);
//      pos3 = pos2 + 1 + rand() % (n / 4);
//
//      // Perturb from current solution, in ILS, current solution is the LO...
//      for (i=j=0 ; i<pos1; i++,j++)
//        newSolution[j] = solution[i]; // Part A
//      for (i=pos3; i<n   ; i++,j++)
//        newSolution[j] = solution[i]; // Part D
//      for (i=pos2; i<pos3; i++,j++)
//        newSolution[j] = solution[i]; // Part C
//      for (i=pos1; i<pos2; i++,j++)
//        newSolution[j] = solution[i]; // Part B
//
//      for (i=0; i<n; i++) {
//        solution[i] = newSolution[i]; // put back
//        solutionMap[solution[i]] = i;
//      }
//    }
//
//    assert(IsValidPermutation());
//    Cur_OV = Evaluate();
  }
}
