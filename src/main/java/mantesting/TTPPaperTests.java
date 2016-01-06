package mantesting;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import solver.*;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import utils.Log;
import utils.RandGen;

public class TTPPaperTests {
  
  /**
   * max execution time in seconds
   */
  static final int maxTime = 600;
  
  
  /**
   * initial tour generation
   */
  static final char tourAlgo = 'l';
  
  
  /**
   * initial picking plan generation
   */
  static final char ppAlgo = 'z';
  
  
  
  static final String[] instFolders = {
//    // small
//    "eil76-ttp",
//    "kroA100-ttp",
//    "ch130-ttp",
//    "u159-ttp",
//    "a280-ttp",
//    "u574-ttp",
//    "u724-ttp",
//    // mid-size
//    "dsj1000-ttp",
//    "rl1304-ttp",
//    "fl1577-ttp",
//    "d2103-ttp",
    "pcb3038-ttp",
    "fnl4461-ttp",
    "pla7397-ttp",
    // large
    "rl11849-ttp",
    "usa13509-ttp",
    "brd14051-ttp",
    "d15112-ttp",
    "d18512-ttp",
    "pla33810-ttp",
//    "pla85900-ttp",
  };


  static final String[] KPTypes = {
//    "uncorr",
//    "uncorr-similar-weights",
    "bounded-strongly-corr",
  };
  
  
  static final int[] knapsackCategories = {
    1,
//    5,
//    10,
  };
  
  
  static final int[] itemFactor = {
    1,
//    5,
//    10,
  };
  
  
  static final int nbRep = 1;

  static final String[] exclude = {
    
  };
  
  /**
   * test function
   */
  public static void main(String[] args) throws FileNotFoundException, InterruptedException {
    
    
    /* algorithm settings */
//    final LocalSearch algo = new CS2SA();
    final Evolution algo = new EvoMPUX();

    //algo.firstfit();
//    algo.noDebug();
    algo.noLog();
    
    // constructive algorithm code
    final String codeS0 = tourAlgo + "" + ppAlgo;

    // save result
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    String filename = "./results/"+algo.getName()+"."+codeS0+"."+df.format(new Date())+".csv";
    final PrintWriter out = new PrintWriter(filename);
    
    /* test for all instances */
    for (final int ifact : itemFactor) {
      for (final int kcat : knapsackCategories) {
        for (final String kptype : KPTypes) {
          for (final String fold : instFolders) {
            for (int u=0;u<nbRep;u++) { // instances

              String tspi = fold.substring(0, fold.length()-4);
              String dimstr = "";
              for (int i=0;i<tspi.length();i++) {
                char c = tspi.charAt(i);
                if (c>='0' && c<='9') dimstr += c;
              }
              
              int dim = Integer.parseInt(dimstr);
              final String name = tspi+"_n"+
                  (dim-1)*ifact+"_"+
                  kptype+"_"+
                  (kcat<10?"0":"")+kcat+".ttp";
              final String ttpi = fold+"/"+name;
              Deb.echo(name);
              
              if (Arrays.asList(exclude).contains(name)) {
                Deb.echo(">> skipped");
                continue;
              }
              //if (true) continue;
              
              // TTP instance
              TTP1Instance ttp = new TTP1Instance(ttpi);
              
              /* initial solution s0 */
              Constructive construct = new Constructive(ttp);
              TTPSolution s0 = construct.generate(codeS0);
              ttp.objective(s0);
              
              /* algorithm setting */
              //algo.setS0(s0);
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
                  //String time = String.format("%.4f", exTimeSec);
                  
                  /* console output */
                  long ob = Math.round(sx.ob);
                  
                  Deb.echo(codeS0 + " " + ttpi + ":\n"+
                           "Objective: " + ob + "\n"+
                           "Duration : " + String.format("%.4f", exTimeSec) + " sec\n");
                  
                  /* save in result file */
                  out.println(name + " " + ob + " " + 
                              String.format("%.4f", exTimeSec));
                  
                  /* log results (solution, objective value, and runtime) */
                  String namePrefix = "RES."+ttpi.replace('/', '#')+"."+codeS0+"-"+algo.getName()+"."+ RandGen.randStr(2);
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




              // delay execution
              int sleepTime;
              if (dim < 300) sleepTime = 2000;
              else if (dim < 1000) sleepTime = 5000;
              else if (dim < 10000) sleepTime = 20000;
              else sleepTime = 60000;
              Thread.sleep(sleepTime);
            }
          }
        }
      }
    } // END for instances
    
    out.close();
  }

}
