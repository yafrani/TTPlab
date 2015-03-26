package utils;

/**
 * swap two nodes
 * 
 * @author kyu
 *
 */
public class SwapHelper {

  /**
   * apply a swap between i and j
   * 
   * @param tour TSP tour
   * @param i swap index
   * @param j swap 2nd index
   */
  public static void doSwap(int[] tour, int i, int j) {
    
    int tmp = tour[i];
    tour[i] = tour[j];
    tour[j] = tmp;
  }
  
  /**
   * apply a N1 swap
   * 
   * @param tour TSP tour
   * @param i swap index
   */
  public static void doSwap(int[] tour, int i) {
    doSwap(tour, i, i+1);
  }
}
