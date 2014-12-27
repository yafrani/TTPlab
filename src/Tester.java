import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import solver.Constructive;
import solver.JointN1BF;
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
public class Tester {
  
  /**
   * test
   */
  public static void main(String[] args) {
    
    String inst[] = {
        "sample-data.ttp",
        "eil51-ttp/eil51_n500_uncorr_10.ttp",
        "eil51-ttp/eil51_n50_bounded-strongly-corr_01.ttp",
        "eil51-ttp/eil51_n150_bounded-strongly-corr_01.ttp",
        "eil51-ttp/eil51_n150_bounded-strongly-corr_09.ttp",
        "berlin52-ttp/berlin52_n51_uncorr-similar-weights_03.ttp",
        "berlin52-ttp/berlin52_n153_uncorr-similar-weights_04.ttp",
        "a280-ttp/a280_n279_bounded-strongly-corr_01.ttp",
        "a280_n1395_uncorr-similar-weights_05.ttp",
        "a280_n2790_uncorr_10.ttp",
        "fnl4461_n4460_bounded-strongly-corr_01.ttp",
        "fnl4461_n22300_uncorr-similar-weights_05.ttp",
        "fnl4461_n44600_uncorr_10.ttp",
        "pla33810_n33809_bounded-strongly-corr_01.ttp",
        "pla33810_n169045_uncorr-similar-weights_05.ttp",
        "pla33810_n338090_uncorr_10.ttp",
    };
    
    
    /* instance information */
    TTP1Instance ttp = new TTP1Instance("./TTP1_data/"+inst[0]);
    Deb.echo(ttp);
    Deb.echo("------------------");
    
    
    /* initial solution s0 */
    Constructive construct = new Constructive(ttp);
    TTPSolution s0 = construct.generate("sg");
    ttp.objective(s0);
    Deb.echo("s0: \n"+s0);
    Deb.echo("ob: "+s0.ob);
    Deb.echo("ww: "+s0.wend);
    Deb.echo("==================");
    
    
    /* algorithm */
    final LocalSearch algo = new JointN1BF(ttp, s0);
    algo.firstfit();
    algo.debug();
    
    
    /* execute */
    ExecutorService executor = Executors.newFixedThreadPool(4);
    Future<?> future = executor.submit(new Runnable() {
      public void run() {
        
        long startTime, stopTime;
        long exTime;
        startTime = System.currentTimeMillis();
        TTPSolution sx = algo.solve();
        
        stopTime = System.currentTimeMillis();
        exTime = stopTime - startTime;
        
        Deb.echo(sx+"\n"+
            "obj  "+sx.ob+"\n");
        Deb.echo("Duration: "+(exTime/1000.0)+" sec");
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
      future.cancel(true);              //     <-- interrupt the job
      System.out.println("timeout");
    }
    
    
    
    
    
    
    
    
    
    
    
    
    // output
//    Deb.echo(sx+"\n"+
//             "obj  "+sx.ob+"\n"+
//             "wend "+sx.wend+"\n");
//    Deb.echo("Duration: "+(exTime/1000.0)+" sec");
  }
  
  
  
}
