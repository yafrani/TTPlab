package mantesting;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import oldsolver.Handoff2optBF;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import solver.*;

public class TestOne {
  
  public static void main(String[] args) {
    
    String inst[] = {
        "my-ttp/sample-data1.ttp",
        "berlin52-ttp/berlin52_n153_bounded-strongly-corr_01.ttp",
        "eil51-ttp/eil51_n50_uncorr_01.ttp",
        "eil51-ttp/eil51_n50_bounded-strongly-corr_01.ttp",
        "a280-ttp/a280_n279_uncorr_01.ttp",
        "eil51-ttp/eil51_n500_uncorr_10.ttp",
        "berlin52-ttp/berlin52_n510_uncorr-similar-weights_07.ttp",
        "u574-ttp/u574_n2865_bounded-strongly-corr_04.ttp",
        "rl1304-ttp/rl1304_n1303_uncorr_10.ttp",
        "bier127-ttp/bier127_n1260_uncorr-similar-weights_10.ttp",
    };
    
    /* instance information */
    final TTP1Instance ttp = new TTP1Instance("./TTP1_data/"+inst[1]);
    Deb.echo(ttp);
    Deb.echo("------------------");
    
    /* initial solution s0 */
    Constructive construct = new Constructive(ttp);
    TTPSolution s0 = construct.generate("lg");
    ttp.objective(s0);
    Deb.echo("s0: \n"+s0);
    Deb.echo("ob: "+s0.ob);
    Deb.echo("ww: "+s0.wend);
    Deb.echo("==================");
    
    
    /* algorithm */
    final LocalSearch algo = new solver.Handoff2optSH(ttp, s0);
    algo.firstfit();
    algo.debug();
    
    
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
        
      }
    });
    
    executor.shutdown();  // reject all further submissions
    
    try {
      future.get(600000, TimeUnit.SECONDS);  //     <-- wait 5 seconds to finish
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
