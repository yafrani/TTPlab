package xplab;

// This is the simplified code used for experiments presented in paper:
// E.K. Burke, Y.Bykov "The Late Acceptance Hill Climbing heuristic".
// *** Copyright Yuri Bykov, April 2012 (C)
// The entire code (or its parts) can be used or modified by everyone
// for non-commercial scientific experiments. The commercial use of
// this code can be done only with the written author's permission.
// Any written or oral public presentation of the details of this code
// (even modified) can be done only with the reference to the author.

import utils.ConfigHelper;

import java.io.*;
import java.util.*;

public class LAHC_TSPlib {
  // external .tsp file name with dataset. Change this string if
  // you would like to search another instance
  // private static String tspfilename="d:\\jwork01\\tsp001\\rat783.tsp";
  private static String tspfilename = ConfigHelper.getProperty("tspdata") + "eil51.tsp";
  // the number of cities in the problem
  private static int ncit;
  // Euclidian coordinates of cities
  private static float[] xnod,ynod;
  // matrix of distances between cities
  private static long[][] cdist;
  // solution for the problem: permutation of ncit numbers
  private static int[] sol;
  // different penalties: current, candidate, control (for reference only)
  // and the best found
  private static long pen,pen1,pen2,bestpen;
  // numbers of iterations: actual and idle ones and the output interval
  private static long nstep,idlestep,dstep=2000000;
  // length of the fitness array
  private static int lfa=5000;
  // fitness array
  private static long[] fa = new long [lfa+1];

  public static void main(String[] args)
  {
    readtspfile();
    System.out.println("N cities= "+ncit);
    if (ncit!=0)
    {
      init();
      search();
    }
  }

  // This is a supplement procedure for reading an external file
  // It extracts 3 values (nr,xr and yr) from a string stk
  // It returns false when numeric format of the string is incorrect.
  private static int nr;
  private static float xr,yr;
  private static boolean string_to_3val(String stk)
  {
    String[] sar=new String[50];
    sar=stk.split(" ");
    int ib=0;
    while (sar[ib].length()==0)
    {ib++;}

    try {nr=Integer.parseInt(sar[ib]);}
    catch (NumberFormatException nfe)
    {return(false);}

    try {xr=Float.parseFloat(sar[ib+1]);}
    catch (NumberFormatException nfe)
    {return(false);}

    try {yr=Float.parseFloat(sar[ib+2]);}
    catch (NumberFormatException nfe)
    {return(false);}

    return(true);
  }

  // This procedure reads the euclidian .tsp file. All not-numerical
  // strings are ignored. Numerical values are read as euclidian
  // coordinates of cities. Then all distances are calculated.
  // As this is a simplified procedure, then no guarantee that
  // all .tsp files will be read correctly.
  private static void readtspfile()
  {
    BufferedReader fi;
    String st;
    ncit=0;
    try
    {
      // we read the external file in two passes. At first pass we find the
      // number of cities
      fi = new BufferedReader (new FileReader(tspfilename));
      do
      {
        st=fi.readLine();
        if (st==null)
          break;
        if (! string_to_3val(st))
          continue;
        ncit++;
      } while (true);
      fi.close();

      xnod=new float[ncit+1];
      ynod=new float[ncit+1];
      cdist=new long[ncit+1][ncit+1];
      sol=new int[ncit+1];
      int i=0;

      // In the second pass we read the actual data
      fi = new BufferedReader (new FileReader(tspfilename));
      do
      {
        st=fi.readLine();
        if (st==null)
          break;
        if (! string_to_3val(st))
          continue;
        i++;
        xnod[i]=xr;
        ynod[i]=yr;
      } while (true);
      fi.close();
    }
    catch (IOException ioe)
    {System.out.println("file read error");return;}

    float dx;
    float dy;
    // here we calculate all distances and fill the matrix cdist
    for (int i=1; i<=ncit;i++)
    {
      for (int j=1; j<=ncit;j++)
      {
        dx=xnod[i]-xnod[j];
        dy=ynod[i]-ynod[j];
        cdist[i][j]=(long) Math.round(Math.sqrt(dx*dx+dy*dy));
      }
    }
  }

  // This is a random initialization procedure. In reality,
  // we first take an ascending order solution (1,2,3,4....)
  // and then 10000 times exchange two randomly chosen elements.
  // Hope, this produces a really random order.
  private static void init()
  {
    for (int i=1; i<=ncit; i++)
      sol[i]=i;

    int s1=0,s2=0,sk=0;
    Random generator = new Random(0);
    for (int i=1; i<=10000; i++)
    {
      do
      {
        s1=generator.nextInt(ncit)+1;
        s2=generator.nextInt(ncit)+1;
      } while (s1==s2);
      sk=sol[s1];
      sol[s1]=sol[s2];
      sol[s2]=sk;
    }
  }

  // This procedure calculates the penalty of solution sol as a sum
  // of distances between cities.
  private static long penalty()
  {
    long p=0;
    int s1=0,s2=0;
    for (int i=1; i<=ncit-1;i++)
    {
      s1=sol[i];
      s2=sol[i+1];
      p=p+cdist[s1][s2];
    }
    p=p+cdist[sol[ncit]][sol[1]];
    return(p);
  }

  // This procedure calculates delta-penalty, when evaluating a candidate
  // solution. This procedure is used in this algorithm, because it is
  // much less time consuming than the calculation of complete penalty.
  // Here s1,s2 represent the indexes of first vertices of exchanged edges.
  private static long deltapenalty(int s1,int s2)
  {
    long p=0;
    int k1,k2;
    if (s1==ncit)
      k1=1;
    else k1=s1+1;
    if (s2==ncit)
      k2=1;
    else k2=s2+1;
    p=p-cdist[sol[k1]][sol[s1]]+cdist[sol[s2]][sol[s1]];
    p=p-cdist[sol[k2]][sol[s2]]+cdist[sol[k1]][sol[k2]];
    return(p);
  }

  // This is an actual search procedure
  private static void search()
  {
    // before the search we calculate initial penalty and fill with that
    // the fitness array
    Random generator = new Random();
    pen=penalty();
    bestpen=pen;
    for (int i=0; i<=lfa; i++)
      fa[i]=pen;
    nstep=0;
    idlestep=0;
    int s1,s2,sk,ds,v,i;
    boolean accept;
    // the search loop is starting here
    do
    {
      // firstly we select two random values, which will serve as the
      // indexes of first vertices of exchanged edges
      do
      {
        s1=generator.nextInt(ncit)+1;
        s2=generator.nextInt(ncit)+1;
        ds=Math.abs(s2-s1);
      } while((s2<=s1)||(ds<=1)||(ds>=ncit-1));
      // here we calculate the candidate penalty
      pen1=pen+deltapenalty(s1,s2);


      // here we employ the LAHC acceptance condition
      v=(int) nstep % lfa;
      accept=(pen1<=pen) || (pen1<=fa[v]);
      // when we have an improving move, then this step is not idle
      if (accept && (pen1<pen))
        idlestep=0;
      // when the acceptance condition is satisfied we do two things:
      if (accept)
      {
        // 1. we make the current penalty to be equal to the
        // candidate one
        pen=pen1;
        // 2. we update the solution sol. Here we exchange two edges:
        // (s1,s1+1) and (s2,s2+1) into (s1,s2) and (s1+1, s2+1).
        ds=s1+s2+1;
        for (i=s1+1; i<=(s1+s2)/2; i++)
        {
          sk=sol[i];
          sol[i]=sol[ds-i];
          sol[ds-i]=sk;
        }
      }
      // independently of our acceptance we update the fitness array
      fa[v] = pen;
      // also we check, whether or not our solution is the best found one
      if (pen<bestpen)
        bestpen=pen;
      // here we output on the screen intermediate results at every dstep
      // iterations. This is for reference only.
      if (nstep%dstep==0)
      {
        System.out.printf("%11d",nstep);
        System.out.print("      ");
        System.out.printf("%16d",pen);

        pen2=penalty();
        System.out.print("      ");
        System.out.printf("%16d",pen2);

        System.out.print("      ");
        System.out.printf("%16d",bestpen);

        System.out.println();
      }
      // finally, we increment the number of iterations and the number
      // of idle iterations
      nstep++;
      idlestep++;
      // main loop is ended here
    } while ((nstep<200000)||(idlestep*50<nstep));
    System.out.print("Experiment finished. Best penaly= "+bestpen);
  }

}
