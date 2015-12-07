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

      //"berlin52-ttp/berlin52_n51_bounded-strongly-corr_01.ttp",
      "a280-ttp/a280_n1395_uncorr-similar-weights_05.ttp",
//      "a280-ttp/a280_n2790_uncorr_10.ttp",
      "d15112-ttp/d15112_n15111_bounded-strongly-corr_01.ttp",
      "pla85900-ttp/pla85900_n85899_bounded-strongly-corr_01.ttp",
      "pla85900-ttp/pla85900_n429495_uncorr-similar-weights_05.ttp",
      "pla85900-ttp/pla85900_n858990_uncorr_10.ttp",
    };

    /* instance information */
    final TTP1Instance ttp = new TTP1Instance(inst[0]);
    Deb.echo(ttp);
    Deb.echo("------------------");

    /* algorithm */
//    final LocalSearch algo = new CS2B();
//    algo.setTTP(ttp);
//    algo.firstfit();
//    algo.debug();
    final Evolution evalgo = new EvoERSP(ttp);
    evalgo.debug();

    /* execute */
    ExecutorService executor = Executors.newFixedThreadPool(4);
    Future<?> future = executor.submit(new Runnable() {
      @Override
      public void run() {
        long startTime, stopTime;
        long exTime;
        startTime = System.currentTimeMillis();
        TTPSolution sx = evalgo.search();
//        TTPSolution sx = algo.search();
        stopTime = System.currentTimeMillis();
        exTime = stopTime - startTime;

        Deb.echo("objective   : " + sx.ob);
        Deb.echo("Duration    : " + (exTime/1000.0) + " sec");
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
      System.out.println("Ding Ding! TIMEOUT!");
    }

  }
}
