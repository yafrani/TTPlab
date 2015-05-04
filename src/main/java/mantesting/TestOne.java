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
      "eil51-ttp/eil51_n50_uncorr_10.ttp",
      "a280-ttp/a280_n279_bounded-strongly-corr_01.ttp",
      "a280-ttp/a280_n1395_uncorr-similar-weights_05.ttp",
      "a280-ttp/a280_n2790_uncorr_10.ttp",
      "fnl4461-ttp/fnl4461_n4460_bounded-strongly-corr_01.ttp",
      "fnl4461-ttp/fnl4461_n22300_uncorr-similar-weights_05.ttp",
      "fnl4461-ttp/fnl4461_n44600_uncorr_10.ttp",

      "d2103-ttp/d2103_n21020_uncorr-similar-weights_07.ttp",
      "dsj1000-ttp/dsj1000_n999_bounded-strongly-corr_01.ttp",

    };

    /* instance information */
    final TTP1Instance ttp = new TTP1Instance("./TTP1_data/"+inst[7]);
    Deb.echo(ttp);
    Deb.echo("------------------");
    
    /* initial solution s0 */
    Constructive construct = new Constructive(ttp);
    TTPSolution s0 = construct.generate("lz");
    ttp.objective(s0);
    //Deb.echo("s0  : \n"+s0);
    Deb.echo("ob  : "+s0.ob);
    Deb.echo("wend: "+s0.wend);
    Deb.echo("==================");

    /* algorithm */
    final LocalSearch algo = new CosolverBitFlip(ttp, s0);
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
