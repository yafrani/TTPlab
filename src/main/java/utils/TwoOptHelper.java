package utils;

/**
 * 2-OPT operations
 * 
 * @author kyu
 *
 */
public class TwoOptHelper {

  /**
   * get tour value using 2opt technique
   * 
   * @param k element index
   * @param tour TSP tour
   * @param i beginning
   * @param j end
   * @return the k'th element
   */
  public static int get2optValue(int k, int[] tour, int i, int j) {
    
    if (k>=i && k<=j) {
      return tour[j-k+i];
    }
    return tour[k];
  }

  /**
   * get tour value using 2opt technique
   */
  public static long get2optValue(int k, long[] vect, int i, int j) {

    if (k>=i && k<=j) {
      return vect[j-k+i];
    }
    return vect[k];
  }

  /**
   * get tour index after 2opt is applied
   * 
   * @param k element index
   * @param tour TSP tour
   * @param i beginning
   * @param j end
   * @return the k'th element index
   */
  public static int get2optIndex(int k, int[] tour, int i, int j) {
    
    if (k>=i && k<=j) {
      return j-k+i;
    }
    return k;
  }
  
  
  /**
   * do 2opt arcs exchange
   * 
   * @param tour TSP tour
   * @param i beginning
   * @param j end
   */
  public static void do2opt(int[] tour, int i, int j) {
    
    int tmp, N = (j-i+1)/2;
    for (int k=i; k<i+N; k++) {
      tmp = tour[k];
      tour[k] = tour[j-k+i];
      tour[j-k+i] = tmp;
    }
  }




  /**
   * do random 2opt arcs exchange
   */
  public static void doRand2opt(int[] tour) {

    int i, j, n = tour.length;

    i = RandGen.randInt(1, n-11);
    j = i + RandGen.randInt(2,10);

    int tmp, N = (j-i+1)/2;
    for (int k=i; k<i+N; k++) {
      tmp = tour[k];
      tour[k] = tour[j-k+i];
      tour[j-k+i] = tmp;
    }
  }


  public static void doRandExchange(int[] tour) {
    int pos1,pos2,pos3,
      n=tour.length;

    pos1 = RandGen.randInt(1, n-11);
    pos2 = pos1 + RandGen.randInt(2,10);

    int tmp = tour[pos1];
    tour[pos1] = tour[pos2];
    tour[pos2] = tmp;
  }

  public static void doRandDoubleBridge(int[] tour, int Strength) {
    int pos1,pos2,pos3,
      l,i,j, n = tour.length;
    int[] solution=tour, newSolution = new int[n];

    for (l=0; l<Strength; l++) {
      // 4-Opt double bridge move... pick 3 split points randomly...
      pos1 = RandGen.randInt(1, n/4);
      pos2 = pos1 + RandGen.randInt(1, n/4);
      pos3 = pos2 + RandGen.randInt(1, n/4);

      // Perturb from current solution, in ILS, current solution is the LO...
      for (i=j=0 ; i<pos1; i++,j++)
        newSolution[j] = solution[i]; // Part A
      for (i=pos3; i<n   ; i++,j++)
        newSolution[j] = solution[i]; // Part D
      for (i=pos2; i<pos3; i++,j++)
        newSolution[j] = solution[i]; // Part C
      for (i=pos1; i<pos2; i++,j++)
        newSolution[j] = solution[i]; // Part B

      for (i=0; i<n; i++) {
        solution[i] = newSolution[i]; // put back
        //solutionMap[solution[i]] = i;
      }
    }

  }
}
