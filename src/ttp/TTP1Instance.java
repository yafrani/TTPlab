package ttp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import utils.CityCoordinates;

/**
 * TTP1 instance
 * 
 * in this particular case of TTP1
 * there is no repetition on items, and
 * the first city does'nt contain any items
 * 
 * @author kyu
 * 
 */
public class TTP1Instance extends TTPInstance {
  
  /**
   * knapsack renting ratio per time unit
   */
  protected double rent;
  
  
  /**
   * instance to string
   */
  @Override
  public String toString() {
    return super.toString() + "rent     : $" + this.rent;
  }
  
  
  /**
   * reads the instance from a .ttp file using file's name
   * 
   * @param fileName
   */
  public TTP1Instance(String fileName) {
    this(new File(fileName));
  }
  
  /**
   * reads the instance from a .ttp file
   * 
   * @param file
   */
  public TTP1Instance(File file) {
    
    this.ttpFile = file;
    BufferedReader br = null;
    
    try {
      br = new BufferedReader(new FileReader(file));
      String line;
      
      while ((line = br.readLine()) != null) {
        
        // instance name
        if (line.startsWith("PROBLEM NAME")) {
          line = line.substring(line.indexOf(":")+1);
          line = line.replaceAll("\\s+","");
          this.name = line;
        }
        
        // KP data type
        if (line.startsWith("KNAPSACK DATA TYPE")) {
          line = line.substring(line.indexOf(":")+1);
          line = line.replaceAll("\\s+","");
          this.knapsackDataType = line;
        }
        
        // number of cities
        if (line.startsWith("DIMENSION")) {
          // if (line.startsWith("NUMBER OF NODES")) {
          line = line.substring(line.indexOf(":")+1);
          line = line.replaceAll("\\s+","");
          this.nbCities = Integer.parseInt(line);
        }
        
        // number of items
        if (line.startsWith("NUMBER OF ITEMS")) {
          line = line.substring(line.indexOf(":")+1);
          line = line.replaceAll("\\s+","");
          this.nbItems = Integer.parseInt(line);
        }
        
        // knapsack capacity
        if (line.startsWith("CAPACITY OF KNAPSACK")) {
          line = line.substring(line.indexOf(":")+1);
          line = line.replaceAll("\\s+","");
          this.capacity = Long.parseLong(line);
        }
        
        // minimum velocity
        if (line.startsWith("MIN SPEED")) {
          line = line.substring(line.indexOf(":")+1);
          line = line.replaceAll("\\s+","");
          this.minSpeed = Double.parseDouble(line);
        }
        
        // maximum velocity
        if (line.startsWith("MAX SPEED")) {
          line = line.substring(line.indexOf(":")+1);
          line = line.replaceAll("\\s+","");
          this.maxSpeed = Double.parseDouble(line);
        }
        
        // rent
        if (line.startsWith("RENTING RATIO")) {
          line = line.substring(line.indexOf(":")+1);
          line = line.replaceAll("\\s+","");
          this.rent = Double.parseDouble(line);
        }
        
        // edge weight
        if (line.startsWith("EDGE_WEIGHT_TYPE")) {
          line = line.substring(line.indexOf(":")+1);
          line = line.replaceAll("\\s+","");
          this.edgeWeightType = line;
        }
        
        // nodes
        if (line.startsWith("NODE_COORD_SECTION")) {
          // coordinates
          this.coordinates = new CityCoordinates[this.nbCities];
          for (int i=0; i<this.nbCities; i++) {
            line = br.readLine();
            String[] parts = line.split("\\s+");
            this.coordinates[i] = new CityCoordinates(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
          }
          
          // distance matrix
          this.setDist(new long[this.nbCities][this.nbCities]);
          for (int i=0; i<nbCities; i++) {
            for (int j=0; j<nbCities; j++) {
              getDist()[i][j] = Math.round(this.coordinates[i].distanceEuclid(this.coordinates[j]));
              //System.out.println(this.coord[i] + "&" + this.coord[j] + "->" + dist[i][j]);
            }
          }
        }
        
        // items
        if (line.startsWith("ITEMS SECTION")) {
          this.profits = new int[this.nbItems];
          this.weights = new int[this.nbItems];
          this.availability = new int[this.nbItems];
          
          for (int i=0; i<this.nbItems; i++) {
            line = br.readLine();
            String[] splittedLine = line.split("\\s+");
            
            this.profits[i] = Integer.parseInt(splittedLine[1]);
            this.weights[i] = Integer.parseInt(splittedLine[2]);
            this.availability[i] = Integer.parseInt(splittedLine[3]);
          }
        }
        
      } // end while
      
      br.close();
      
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
  
  
  /**
   * objective function
   * 
   * @param s the TTP solution
   * @return the objective value
   */
  public void objective(TTPSolution s) {
    
    int[] x = s.getTour();
    int[] z = s.getPickingPlan();
    
    long[][] D = getDist();
    double C = (maxSpeed-minSpeed)/capacity; // velocity const
    
    int wc = 0;  // current weight
    int fp = 0;  // final profit
    double ft = .0; // tour time
    double ob = .0; // objective value
    
    int[] selectedItems = new int[this.nbItems]; // remove this... (memory abuse)
    
    double velocity = 1.0;
    
    // visit all cities
    for (int i=0; i<this.nbCities; i++) {
      int k = 0;
      for (int j=0; j<this.nbItems; j++) {
        if (z[j]==x[i]) {
          selectedItems[k++] = j;
        }
      }
      int acc=0;
      if (k > 0) {
        for (int j=0;j<k;j++) {
          //wc += weights[selectedItems[j]];
          fp += profits[selectedItems[j]];
          acc += weights[selectedItems[j]];
        }
        wc += acc;
        velocity = maxSpeed - wc*C;
      }
      
      int h = (i+1)%nbCities;
      ft += D[x[i]-1][x[h]-1] / velocity;
      
      // record important data for future use
      s.timeAcc[i] = ft;
      s.timeRec[i] = D[x[i]-1][x[h]-1] / velocity;
      s.weightAcc[i] = wc;
      s.weightRec[i] = acc;
    }
    ob = fp - ft*rent;
    
    // solution properties
    s.fp = fp;
    s.ft = ft;
    s.wend = capacity-wc;
    s.ob = ob;
  }
  
  public double getRent() {
    return rent;
  }
}
