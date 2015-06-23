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
      "ts225-ttp/ts225_n1120_uncorr-similar-weights_05.ttp",
      "eil51-ttp/eil51_n150_uncorr-similar-weights_01.ttp",

      "lin318-ttp/lin318_n1585_uncorr-similar-weights_05.ttp",
      "u574-ttp/u574_n2865_uncorr-similar-weights_05.ttp",
      "dsj1000-ttp/dsj1000_n4995_uncorr-similar-weights_05.ttp",
      "rl1304-ttp/rl1304_n6515_uncorr-similar-weights_05.ttp",
      "fl1577-ttp/fl1577_n7880_uncorr-similar-weights_05.ttp",
      "d2103-ttp/d2103_n10510_uncorr-similar-weights_05.ttp",
      "pcb3038-ttp/pcb3038_n15185_uncorr-similar-weights_05.ttp",
      "usa13509-ttp/usa13509_n67540_uncorr-similar-weights_05.ttp",
      "d18512-ttp/d18512_n92555_uncorr-similar-weights_05.ttp",
      "pcb3038-ttp/pcb3038_n15185_uncorr-similar-weights_05.ttp",
      "fnl4461-ttp/fnl4461_n22300_uncorr-similar-weights_05.ttp",
      "d15112-ttp/d15112_n75555_uncorr-similar-weights_05.ttp",
      "rl11849-ttp/rl11849_n59240_uncorr-similar-weights_05.ttp",
      "pla33810-ttp/pla33810_n169045_uncorr-similar-weights_05.ttp",
      "ts225-ttp/ts225_n1120_uncorr-similar-weights_05.ttp",
      "ch150-ttp/ch150_n745_uncorr-similar-weights_05.ttp",
      "kroA100-ttp/kroA100_n495_uncorr-similar-weights_05.ttp",
      "berlin52-ttp/berlin52_n255_uncorr-similar-weights_05.ttp",
      "pla33810-ttp/pla33810_n169045_uncorr-similar-weights_05.ttp",
    };

    /* instance information */
    final TTP1Instance ttp = new TTP1Instance("./TTP1_data/"+inst[0]);
    Deb.echo(ttp);
    Deb.echo("------------------");

    /* initial solution s0 */
    Constructive construct = new Constructive(ttp);
    TTPSolution s0 = construct.generate("lg");
    ttp.objective(s0);
    //Deb.echo("s0  : \n"+s0);
    Deb.echo("ob  : "+s0.ob);
    Deb.echo("wend: "+s0.wend);
    Deb.echo("==================");

    /* algorithm */
    final LocalSearch algo = new CosolverLASA(ttp, s0);
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

        Deb.echo("objective   : " + sx.ob);
        Deb.echo("Duration    : " + (exTime/1000.0) + " sec");

      }
    });
    
    executor.shutdown();  // reject all further submissions
    
    try {
      future.get(900, TimeUnit.SECONDS);  //     <-- wait 5 seconds to finish
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
