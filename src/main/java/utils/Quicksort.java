package utils;

/**
 * Quicksort implementation
 * sort in descending order
 * 
 * @author kyu
 *
 */
public class Quicksort {
  
  private double[] data;
  private int[] indices;
  
  public Quicksort(double[] values) {
    this.data = values;
    this.indices = new int[this.data.length];
    
    for (int i=0;i<this.data.length;i++) {
      this.indices[i] = i;
    }
  }
  
  public int[] getIndices() {
    return indices;
  }
  
  /**
   * apply quicksort
   */
  public void sort() {
    sort(0, this.data.length-1);
  }
  
  /**
   * apply quicksort
   * 
   * @param low
   * @param high
   */
  private void sort(int low, int high) {
    
    int i = low, j = high;
    
    // Get the pivot element from the middle of the list
    double pivot = data[low + (high-low)/2];
    
    // Divide into two lists
    while (i <= j) {
  	  while (data[i] > pivot) {
  	    i++;
  	  }
  	  while (data[j] < pivot) {
  	    j--;
  	  }
  	  
  	  if (i <= j) {
  	    swap(i, j);
  	    i++;
  	    j--;
  	  }
    }
    
    // Recursion
    if (low < j)
      sort(low, j);
    if (i < high)
      sort(i, high);
  }
  
  /**
   * swap two elements
   * 
   * @param i
   * @param j
   */
  private void swap(int i, int j) {
    
    double tmpd = data[i];
    data[i] = data[j];
    data[j] = tmpd;
    
    int tmpi = indices[i];
    indices[i] = indices[j];
    indices[j] = tmpi;
  }
}
