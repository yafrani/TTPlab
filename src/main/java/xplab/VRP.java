package xplab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

//FW is short for Floyd-Warshall
public class VRP {

  Node[] node;	// nodes
  int n;			// number of nodes
  Queue queue;	// the queue

  int nc;			// number of customers
  int[] customer;	// customers
  int[] demand;	// demand of the customers;
  int[][] dist;	// distance between any pair of nodes
  int[][] cost;	// the distance between customers and depot
  int capacity;	// capacity of the vehicle

  // construct the FW with input arc file and customer file
  public VRP(String arcfile, String customerfile){
    this.readArc(arcfile);
    this.ReadCustomer(customerfile);
  }


  // this method reads the information of the nodes and arcs
  public void readArc(String filename){
    try{

      File input = new File(filename);
      BufferedReader br =
        new BufferedReader(
          new InputStreamReader(
            new FileInputStream(input)));

      String line;
      String token;
      line = br.readLine();	// read the first line of the input file

      //parse the first line of the input file
      StringTokenizer st = new StringTokenizer(line);
      token = st.nextToken();

      this.n = Integer.parseInt(token); // get the number of the nodes, n
      this.node = new Node[n]; // create n Nodes
      for(int i=0; i<n; i++){
        node[i] = new Node();
        node[i].num = i;
      }

      while((line = br.readLine())!= null){//read each rest lines in the file which contains arc information
        int from;
        int to;
        int cost;

        st = new StringTokenizer(line);
        token = st.nextToken();
        from = Integer.parseInt(token);
        token = st.nextToken();
        to = Integer.parseInt(token);
        token = st.nextToken();
        cost = Integer.parseInt(token);

        Arc arc = new Arc();
        arc.from_node = node[from];
        arc.to_node = node[to];
        arc.cost = cost;

        // if the node contains no arcs, this is the first out arc
        if (node[from].first_out_arc == null){
          node[from].first_out_arc = arc;
          node[from].last_out_arc = arc;
        }
        // else we should find the last arc
        else{
          (node[from].last_out_arc).next_out_arc = arc;
          node[from].last_out_arc = arc;
        }
      }

      br.close();

    } catch (Exception e) {
      e.printStackTrace();
    }


  }

  // read the customers and demands
  public void ReadCustomer(String filename){
    try{
      File customerFile = new File(filename);
      BufferedReader br = new BufferedReader(
        new InputStreamReader(
          new FileInputStream(customerFile)));

      String temp = br.readLine();
      this.capacity = Integer.parseInt(temp);
      temp = br.readLine();
      StringTokenizer st = new StringTokenizer(temp);
      this.nc = Integer.parseInt(st.nextToken());
      nc++;	// there is a depot
      this.customer = new int[nc];
      this.demand = new int[nc];
      customer[0] = Integer.parseInt(st.nextToken());

      for(int i=1; i<nc; i++){
        temp = br.readLine();
        st = new StringTokenizer(temp);
        customer[i] = Integer.parseInt(st.nextToken());
        demand[i] = Integer.parseInt(st.nextToken());
      }

    }catch(Exception e){
      e.printStackTrace();
    }
  }

  // print the distance between each pair of customers
  public void printDist(){
    for(int i=0; i<nc; i++){
      for(int j=i; j<nc; j++){
        System.out.println(
          "The distance from customer " + i + "(node " + customer[i] + ")" +
            " to customer " + j + "(node " + customer[j] + ")" +
            " is " + dist[customer[i]][customer[j]]);
      }
    }
  }


  // Floyd-Warshall algorithm
  public void floyd_Warshall(){
    this.dist = new int[n][n];

    for(int i=0; i<n; i++){
      for(int j=0; j<n; j++){
        if (i==j) dist[i][j] = 0;
        else dist[i][j] = 10000;
      }
    }

    for(int i=0; i<n; i++){
      Arc arc = node[i].first_out_arc;
      while(arc != null){
        dist[i][arc.to_node.num] = arc.cost;
        // here change the graph to undirected
        dist[arc.to_node.num][i] = arc.cost;
        arc = arc.next_out_arc;
      }
    }


    for(int k=0; k<n; k++){
      for(int i=0; i<n; i++){
        for(int j=0; j<n; j++){
          if (dist[i][j] > dist[i][k] + dist[k][j]){
            dist[i][j] = dist[i][k] + dist[k][j];
          }
        }
      }
    }
  }

  // Clarke-Wright Savings Method
  // add you own codes here:
  public void clarke_Wright(){


















  }



  public static void main(String[] args) {
    VRP vrp = new VRP("a1_data.txt", "customers.txt");
    vrp.floyd_Warshall();
    //vrp.printDist();
    vrp.clarke_Wright();



  }
}



class Node{				/* node structure */
  int num;			/* node id */
  Arc first_out_arc;	/* first outbound arc from the node */
  Arc last_out_arc;	/* last outbound arc from the node, you may or may not use this */
  Node pred;	// for shortest path
  Node next;	// for the queue
  int dist;

  public Node(){		// construciton
    this.num = -1;
    this.dist = -1;
    this.pred = null;
    this.first_out_arc = null;
    this.last_out_arc = null;
  }
}

class Arc{				/* arc structure */
  Node from_node;		/* from node of the arc	*/
  Node to_node;		/* to node of the arc */
  int cost;			/* some type of weight */
  Arc next_out_arc;	/* next arc on the list of outbound arcs of the same from node */

  public Arc(){
    this.from_node = null;
    this.to_node = null;
    this.cost = -1;
    this.next_out_arc = null;
  }
}

class Queue{
  Node top;		// top of the queue
  Node bottom;	// bottom of the queue
  int length;		//length of the queue

  public Queue(){
    this.top = null;
    this.bottom = null;
    length = 0;
  }

  // to check whether the queue is empty
  public boolean isQueueEmpty(){
    if (length==0) return true;
    else return false;
  }

  // to check whether a node is in the queue
  public boolean is_in_Q(Node node){
    if (node==null) return false;
    if (this.top == null || this.length == 0) return false;
    Node t = this.top;
    while(t != null){
      if (t == node) return true;
      t = t.next;
    }
    return false;
  }

  // add a node to the top of the queue
  public void addToTop(Node node){
    if (node == null) return;
    if (this.length == 0){
      this.top = node;
      this.bottom = node;
      this.length ++;
    }
    else{
      node.next = top;
      this.top = node;
      this.length ++;
    }
  }

  // add a node to the bottom of the queue
  public void addToBottom(Node node){
    if (node == null) return;
    if (this.length == 0){
      this.top = node;
      this.bottom = node;
      this.length ++;
    }
    else{//length>0
      this.bottom.next = node;
      this.bottom = node;
      node.next = null;
      this.length ++;
    }
  }

  // get the top of the queue, and remove it from the queue
  public Node getAndRemoveTop(){
    if (this.top == null || this.length == 0) return null;
    else {
      Node t = this.top;
      if (this.length > 1){
        this.top = this.top.next;
      }
      else{// length == 1
        this.top = null;
        this.bottom = null;
      }
      t.next = null;
      this.length --;
      return t;
    }
  }


}

