package mantesting;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import solver.Constructive;
import oldsolver.JointN1BF;
import solver.LocalSearch;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.ConfigHelper;
import utils.Deb;
import utils.Log;
import utils.RandGen;

public class TestAll {
  
  /**
   * max execution time in seconds
   */
  static final int maxTime = 600;
  
  
  /**
   * number of repetitions for random init algos
   */
  static final int nbRep = 5;
  
  
  /**
   * initial tour generation
   * l: Lin-Kernighan
   * o: optimal
   * r: random
   * g: greedy
   * s: simple
   */
  static final char tourAlgo = 'l';
  
  
  /**
   * initial picking plan generation
   * z: zeros
   * r: random
   * g: greedy
   */
  static final char ppAlgo = 'r';
  
  
  /**
   * instances folder
   */
  static final String instFolder = "bier127-ttp";
  
  
  /**
   * instances files
   */
  static final String instFiles[] = {
    "eil51-ttp/eil51_n50_bounded-strongly-corr_01.ttp",
    "eil51-ttp/eil51_n50_bounded-strongly-corr_02.ttp",
    "eil51-ttp/eil51_n50_bounded-strongly-corr_03.ttp"
  };
  
  
  
  /**
   * test function
   */
  public static void main(String[] args) throws FileNotFoundException {
    
    // list of instances file names
    TreeSet<String> instances = new TreeSet<String>();
    if (instFolder.equals("")) {
      instances.addAll(Arrays.asList(instFiles));
    }
    else {
      // get TTP file names from folder
      // get datasets folder path
      Properties prop = new Properties();
      InputStream input;
      String ttpData = ConfigHelper.getProperty("ttpdata");
      final File instFolderRef = new File(ttpData+instFolder);
      
      for (final File fileEntry : instFolderRef.listFiles()) {
        String fileName = fileEntry.getName();
        int fileLen = fileName.length()-1;
        if ( ! fileEntry.isDirectory() && fileName.substring(fileLen-3).equals(".ttp")) {
          instances.add(instFolder+"/"+fileName);
        }
      }
    }
    
    
    /* algorithm settings */
    final LocalSearch algo = new JointN1BF();
    algo.firstfit();
    algo.noDebug();
    algo.noLog();
    // constructive algorithm code
    final String codeS0 = tourAlgo+""+ppAlgo;
    
    // save result
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    String filename = "./results/"+instFolder+"-"+algo.getName()+"."+codeS0+"."+df.format(new Date())+".csv";
    final PrintWriter out = new PrintWriter(filename);
    
    // number of repetitions for random-based algos
    final int nbIt = ppAlgo == 'r' ? nbRep : 1;
    
    int cmp = 1;
    do {
      
      /* test for all instances */
      for (final String ttpi : instances) { // instances
        
        // TTP instance
        TTP1Instance ttp = new TTP1Instance(ttpi);
        
        /* initial solution s0 */
        Constructive construct = new Constructive(ttp);
        TTPSolution s0 = construct.generate(codeS0);
        ttp.objective(s0);
        
        /* algorithm setting */
        algo.setS0(s0);
        algo.setTTP(ttp);
        
        
        /*---------------------------------*
         * execute: interruption sensitive *
         *---------------------------------*/
        ExecutorService executor = Executors.newFixedThreadPool(4);
        Future<?> future = executor.submit(new Runnable() {
            
          @Override
          public void run() {
            /* execute */
            long startTime = System.currentTimeMillis();
            TTPSolution sx = algo.search();
            long stopTime = System.currentTimeMillis();
            long exTime = stopTime - startTime;
            double exTimeSec = exTime/1000.0;
            String time = String.format("%.2f", exTimeSec);
            
            /* output */
            long ob = Math.round(sx.ob);
            
            Deb.echo(codeS0 + " " + ttpi + ":\n"+
                     "Objective: " + ob + "\n"+
                     "Duration : " + time + " sec\n");
            
            /* save */
            out.println(ttpi+"; "+ob+"; "+time);
            
            /* log results */
            String namePrefix = "RES."+ttpi.replace('/', '#')+"."+codeS0+"-"+algo.getName()+"."+ RandGen.randStr(nbIt / 2);
            Log log = new Log(namePrefix);
            log.print(sx+"\n"+
                      "Objective: "+sx.ob+"\n"+
                      "Duration : "+exTimeSec+" sec");
            log.close();
            
          }
        });
        
        executor.shutdown();  // reject all further submissions
        
        try {
          // wait 1 seconds to finish
          future.get(maxTime, TimeUnit.SECONDS);  
        } catch (InterruptedException e) {
          // possible error cases
          System.out.println("job was interrupted");
        } catch (ExecutionException e) {
          // possible error cases
          System.out.println("caught exception: " + e.getCause());
        } catch (TimeoutException e) {
          // interrupt the job
          future.cancel(true);
          System.out.println("timeout");
        }
        /* end execution */
        
    
    
        //out.println();
        
      } // END for instances
    } while (cmp++ < nbIt);
    
    out.close();
  }
}
