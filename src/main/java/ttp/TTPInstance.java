package ttp;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;

import utils.CityCoordinates;
import utils.Deb;
import utils.Log;

/**
 * TTP instance
 * 
 * @author kyu
 */
public abstract class TTPInstance {
  
  protected String name;
  protected String knapsackDataType;
  protected int nbCities;
  protected int nbItems;
  protected long capacity;
  protected double minSpeed;
  protected double maxSpeed;
  protected String edgeWeightType;
  protected CityCoordinates[] coordinates;
  protected long[][] dist;
  protected int[] availability;
  protected int[] profits;
  protected int[] weights;
  
  protected File ttpFile;

  // item clusters per city
  protected ArrayList<Integer>[] clusters;

  @Override
  public String toString() {
    
    String s = "";
    if (nbCities < 20 && nbItems < 20) {
      // coordinates
      s += "cities | coordinates:\n";
      for (int i=0; i<this.nbCities; i++) {
        s += String.format("%6d | ", i+1) + this.coordinates[i] + "\n";
      }
      s+="\n";
      // distance matrix
      s += "distance matrix:\n";
      for (int i=0; i<this.nbCities; i++) {
        for (int j=0; j<this.nbCities; j++) {
          s += String.format("%5d", this.getDist()[i][j]);
        }
        s += "\n";
      }
      s+="\n";
      
      // items
      s += "items   : ";
      for (int i=0; i<this.nbItems; i++) {
          s +=  String.format("%5d", i+1);
      }
      s += "\n";
      s += "values  : ";
      for (int i=0; i<this.nbItems; i++) {
          s +=  String.format("%5d", this.profits[i]);
      }
      s += "\n";
      s += "weights : ";
      for (int i=0; i<this.nbItems; i++) {
          s +=  String.format("%5d", this.weights[i]);
      }
      s += "\n";
      s += "city ref: ";
      for (int i=0; i<this.nbItems; i++) {
          s +=  String.format("%5d", this.availability[i]);
      }
      s+="\n\n";
      
    }
    
    s += "name     : " + this.name + "\n";
    s += "#cities  : " + this.nbCities + "\n";
    s += "#items   : " + this.nbItems + "\n";
    s += "capacity : " + this.capacity + "\n";
    s += "min speed: " + this.getMinSpeed() + "\n";
    s += "max speed: " + this.getMaxSpeed() + "\n";
    
    return s;
  }
  
  public void setDist(long[][] dist) {
    this.dist = dist;
  }
  
  public String getName() {
    return name;
  }
  public long[][] getDist() {
    return dist;
  }
  public int[] getAvailability() {
    return availability;
  }
  public int[] getWeights() {
    return weights;
  }
  public int[] getProfits() {
    return profits;
  }
  public int getNbCities() {
    return nbCities;
  }
  public int getNbItems() {
    return nbItems;
  }
  public double getMaxSpeed() {
    return maxSpeed;
  }
  public double getMinSpeed() {
    return minSpeed;
  }
  public long getCapacity() {
    return capacity;
  }
  public ArrayList<Integer>[] getClusters() {
    return clusters;
  }

  
  public int profitOf(int i) {
    return this.profits[i];
  }
  public int weightOf(int i) {
    return this.weights[i];
  }


  /**
   * get delaunay candidates
   */
  public ArrayList<Integer>[] delaunay() {

    try {
      // write coordinates
      String fileNameCoord = "bins/" + getName() + ".coord";
      File fileCoord = new File(fileNameCoord);
      PrintWriter coordWriter = new PrintWriter(fileCoord);
      coordWriter.println(getNbCities());
      for (CityCoordinates node : coordinates) {
        coordWriter.println(node.getX() + " " + node.getY());
      }
      coordWriter.close();

      // execute delaunay program
      String[] cmd = {"./bins/dct.sh", fileNameCoord};
      Runtime runtime = Runtime.getRuntime();
      Process proc = runtime.exec(cmd);

      // read output from Delaunay program
      BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));

      // store candidates in a hash table
      // and instantiate all sub lists
      ArrayList<Integer>[] arcTable = new ArrayList[nbCities];
      for (int i=0;i<nbCities;i++) {
        arcTable[i] = new ArrayList<>();
      }

      int node1, node2;
      String line;
      while ((line = br.readLine()) != null) {
        String[] parts = line.split("\\s+");

        node1 = Integer.parseInt(parts[0]);
        node2 = Integer.parseInt(parts[1]);

        arcTable[node1].add(node2);
        arcTable[node2].add(node1);
      }
      br.close();

      // delete coordinates file
      fileCoord.delete();

      return arcTable;

    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }


  /**
   * organize items per city
   */
  public void clusterItems() {

    clusters = new ArrayList[nbCities];
    int i;

    // init cluster arrays
    for (i=0; i<nbCities; i++) {
      clusters[i] = new ArrayList<>();
    }
    // fill clusters
    for (i=0; i<nbItems; i++) {
      clusters[ availability[i]-1 ].add(i);
    }
  }

}
