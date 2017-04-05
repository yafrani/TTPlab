package utils;

import ttp.TTP1Instance;
import ttp.TTPInstance;

import java.io.*;
import java.util.HashSet;

/**
 * Created by kyu on 11/3/15.
 */
public class GraphHelper {

  /**
   * get delaunay candidates
   */
  public static HashSet<Integer>[] delaunay(TTPInstance ttp) {

    int nbCities = ttp.getNbCities();

    try {
      // write coordinates
      String[] tspBase = ttp.getName().split("_",2);
      String name = tspBase[0];
      String fileNameCoord = "bins/delaunay/" + name + ".coord";
      File fileCoord = new File(fileNameCoord);
      PrintWriter coordWriter = new PrintWriter(fileCoord);
      coordWriter.println(ttp.getNbCities());
      for (CityCoordinates node : ttp.getCoordinates()) {
        coordWriter.println(node.getX() + " " + node.getY());
      }
      coordWriter.close();

      // execute delaunay program
      String[] cmd = {"./bins/delaunay/dct.sh", fileNameCoord};
      Runtime runtime = Runtime.getRuntime();
      Process proc = runtime.exec(cmd);

      // read output from Delaunay program
      BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));

      // store candidates in a hash table
      // and instantiate all sub lists
      HashSet<Integer>[] arcTable = new HashSet[nbCities];
      for (int i = 0; i < nbCities; i++) {
        arcTable[i] = new HashSet<>();
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
   * delaunay triangulation
   * k'th level
   */
  public static HashSet<Integer>[] delaunayKNN(TTPInstance ttp, int k) {

    int nbCities = ttp.getNbCities();
    HashSet<Integer>[] tri = delaunay(ttp);
    HashSet<Integer>[] tri2 = new HashSet[nbCities];

    // fill tri2 with tri ==> level 1
    for (int i = 0; i < nbCities; i++) {
      tri2[i] = new HashSet(tri[i]);
    }

    for (int i = 0; i < nbCities; i++) {
      //Deb.echo(i+"-"+tri[i]);
      for (int j=1; j <= k-1; j++) {
        HashSet<Integer> currentSet = new HashSet(tri2[i]);
        for (int x : currentSet) {
          //Deb.echo(">"+x+":"+tri[x]);
          tri2[i].addAll(tri[x]);
        }
        //Deb.echo(i + "-" + tri2[i]);
      }
    }

    return tri2;
  }



  // testing...
  public static void main(String[] args) {

//    final TTP1Instance ttp = new TTP1Instance("my-ttp/sample-data-10.ttp");
//    final TTP1Instance ttp = new TTP1Instance("berlin52-ttp/berlin52_n51_bounded-strongly-corr_01.ttp");
    final TTP1Instance ttp = new TTP1Instance("d15112-ttp/d15112_n15111_bounded-strongly-corr_01.ttp");
    Deb.echo(ttp);

    HashSet<Integer>[] delaunay = GraphHelper.delaunayKNN(ttp, 10);

    int sum = 0;
    for (HashSet<Integer> x : delaunay) {
      sum += x.size();
      //Deb.echo(x);
    }

    Deb.echo(sum/ttp.getNbCities());
  }
}
