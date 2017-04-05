package ttp;

import java.io.*;

import utils.CityCoordinates;
import utils.ConfigHelper;
import utils.Deb;

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
    //this.name = fileName;
    String[] sp = fileName.split("/", 2);
    this.directory = sp[0];
    this.name = sp[1];
    String[] sp2 = directory.split("-", 2);
    this.tspName = sp2[0];

    String ttpData = ConfigHelper.getProperty("ttpdata");

    this.ttpFile = new File(ttpData+fileName);
    BufferedReader br = null;
    
    try {
      br = new BufferedReader(new FileReader(this.ttpFile));
      String line;
      
      while ((line = br.readLine()) != null) {
        
        // instance name
        if (line.startsWith("PROBLEM NAME")) {
          line = line.substring(line.indexOf(":")+1);
          line = line.replaceAll("\\s+","");
          //this.name = line;
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
          if (nbCities < 10000) {
            this.setDist(new long[this.nbCities][this.nbCities]);
            for (int i = 0; i < nbCities; i++) {
              for (int j = 0; j < nbCities; j++) {
//                getDist()[i][j] = Math.round(this.coordinates[i].distanceEuclid(this.coordinates[j]));
                getDist()[i][j] = (long)Math.ceil(this.coordinates[i].distanceEuclid(this.coordinates[j]));
                //System.out.println(this.coord[i] + "&" + this.coord[j] + "->" + dist[i][j]);
              }
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
    clusterItems();
  }


  public double getRent() {
    return rent;
  }


  /**
   * objective function
   *
   * @param s the TTP solution
   */
  public void objective(TTPSolution s) {

    int[] x = s.getTour();
    int[] z = s.getPickingPlan();

    long[][] D = getDist();
    double C = (maxSpeed-minSpeed)/capacity; // velocity const
    double velocity;

    long acc;       // iteration weight accumulator
    long wc = 0;    // current weight
    long fp = 0;    // final profit
    double ft = 0;  // tour time
    double ob;      // objective value


    // visit all cities
    for (int i=0; i<this.nbCities; i++) {
      acc = 0;
      // check only items contained in current city
      for (int j : clusters[ x[i]-1 ]) {
        if (z[j]!=0) {
          fp += profits[j];
          acc += weights[j];
        }
      }

      wc += acc;
      velocity = maxSpeed - wc*C;

      int h = (i+1)%nbCities;
      ft += distFor(x[i]-1,x[h]-1) / velocity;

      // record important data for future use
      s.timeAcc[i] = ft;
      s.timeRec[i] = distFor(x[i]-1, x[h]-1) / velocity;
      s.weightAcc[i] = wc;
      s.weightRec[i] = acc;

      // map indices to their associated cities
      s.mapCI[x[i]-1] = i;
    }

    ob = fp - ft*rent;

    // solution properties
    s.fp = fp;
    s.ft = ft;
    s.wend = capacity-wc;
    s.ob = ob;
  }




}
