package mantesting;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import solver.*;

public class TestOne {
  
  public static void main(String[] args) {

    String[] inst = {
      "my-ttp/sample-data1.ttp",
      "u724-ttp/u724_n723_uncorr_06.ttp",
      "eil51-ttp/eil51_n50_uncorr_10.ttp",
      "u574-ttp/u574_n2865_uncorr-similar-weights_04.ttp",
      "dsj1000-ttp/dsj1000_n999_bounded-strongly-corr_01.ttp",
      "fl1577-ttp/fl1577_n1576_bounded-strongly-corr_10.ttp",
      "a280-ttp/a280_n2790_uncorr-similar-weights_10.ttp",
      "a280-ttp/a280_n279_uncorr_01.ttp",
      "a280-ttp/a280_n1395_uncorr-similar-weights_05.ttp",
      "rl1304-ttp/rl1304_n1303_uncorr_06.ttp",
      "fnl4461-ttp/fnl4461_n4460_bounded-strongly-corr_01.ttp",
      "dsj1000-ttp/dsj1000_n999_uncorr_06.ttp",
      "u724-ttp/u724_n723_uncorr_06.ttp",
      "u574-ttp/u574_n573_uncorr_06.ttp",
      "lin318-ttp/lin318_n317_uncorr_06.ttp",
      "ts225-ttp/ts225_n224_uncorr_06.ttp",
      "kroA200-ttp/kroA200_n199_uncorr_06.ttp",
      "ch150-ttp/ch150_n149_uncorr-similar-weights_06.ttp",
      "bier127-ttp/bier127_n1260_uncorr-similar-weights_10.ttp",
      "berlin52-ttp/berlin52_n153_bounded-strongly-corr_01.ttp",
      "berlin52-ttp/berlin52_n510_uncorr-similar-weights_07.ttp",
      "eil51-ttp/eil51_n50_uncorr_01.ttp",
      "eil51-ttp/eil51_n50_bounded-strongly-corr_01.ttp",
      "u574-ttp/u574_n2865_bounded-strongly-corr_04.ttp",
      "u574-ttp/u574_n2865_uncorr-similar-weights_04.ttp",
      "a280-ttp/a280_n279_uncorr_01.ttp",
      "berlin52-ttp/berlin52_n510_uncorr-similar-weights_07.ttp",
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
    final LocalSearch algo = new solver.Cosolver2opt(ttp, s0);
    //algo.firstfit();
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
        Deb.echo("objective   : " + sx.ob);
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
