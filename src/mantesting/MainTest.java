package mantesting;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import solver.Constructive;
import solver.Joint2optBF;
import solver.LocalSearch;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;

/**
 * testing class
 * 
 * @author kyu
 * 
 */
public class MainTest {
  
  /**
   * test
   */
  public static void main(String[] args) {
    
    String inst[] = {
        "my-ttp/sample-data1.ttp",
        "eil51-ttp/eil51_n50_bounded-strongly-corr_02.ttp",
        "eil51-ttp/eil51_n500_uncorr_10.ttp",
        "berlin52-ttp/berlin52_n510_uncorr-similar-weights_07.ttp",
        "u574-ttp/u574_n2865_bounded-strongly-corr_04.ttp",
        "rl1304-ttp/rl1304_n1303_uncorr_10.ttp",
        "a280-ttp/a280_n279_uncorr_10.ttp",
        "bier127-ttp/bier127_n1260_uncorr-similar-weights_10.ttp",
        "eil51-ttp/eil51_n50_bounded-strongly-corr_01.ttp"
    };
    
    /* instance information */
    final TTP1Instance ttp = new TTP1Instance("./TTP1_data/"+inst[0]);
    Deb.echo(ttp);
    Deb.echo("------------------");
    
    /* initial solution s0 */
    Constructive construct = new Constructive(ttp);
    TTPSolution s0 = construct.generate("sg");
//    s0.setTour(new int[]{1, 3, 2, 5, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 16, 15, 17, 19, 18, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51});
//    s0.setPickingPlan(new int[]{2, 3, 4, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 0, 0, 0, 41, 0, 0, 44, 45, 46, 47, 48, 49, 50, 51});
    ttp.objective(s0);
    Deb.echo("s0: \n"+s0);
    Deb.echo("ob: "+s0.ob);
    Deb.echo("ww: "+s0.wend);
    Deb.echo("==================");
    
    
    /* algorithm */
//    final LocalSearch algo = new JointN1BFOLD(ttp, s0);
    final LocalSearch algo = new Joint2optBF(ttp, s0);
    algo.firstfit();
    algo.noDebug();
    
    
    /* execute */
    ExecutorService executor = Executors.newFixedThreadPool(4);
    Future<?> future = executor.submit(new Runnable() {
      @Override
      public void run() {
        
        long startTime, stopTime;
        long exTime;
        startTime = System.currentTimeMillis();
        TTPSolution sx = algo.solve();
        
        stopTime = System.currentTimeMillis();
        exTime = stopTime - startTime;
        
        
        Deb.echo(sx);
        Deb.echo("objective   : " + sx.ob + "\n");
        Deb.echo("Duration    : " + (exTime/1000.0) + " sec");
        
        // verification
        ttp.objective(sx);
        Deb.echo("\nVerification");
        Deb.echo(sx);
        Deb.echo("objective   : " + sx.ob + "\n");
        
      }
    });
    
    executor.shutdown();  // reject all further submissions
    
    try {
      future.get(600, TimeUnit.SECONDS);  //     <-- wait 5 seconds to finish
        
    } catch (InterruptedException e) {    //     <-- possible error cases
      System.out.println("job was interrupted");
    } catch (ExecutionException e) {
      System.out.println("caught exception: " + e.getCause());
    } catch (TimeoutException e) {
      future.cancel(true);                //     <-- interrupt the job
      System.out.println("timeout");
    }
    
  }
  
}
