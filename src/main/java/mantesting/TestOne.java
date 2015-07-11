package mantesting;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import oldsolver.Joint2optBF;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import solver.*;

public class TestOne {

  public static void main(String[] args) {

    String[] inst = {
      "pla85900-ttp/pla85900_n85899_bounded-strongly-corr_01.ttp",
//      "pla85900-ttp/pla85900_n429495_uncorr-similar-weights_05.ttp",
      "pla85900-ttp/pla85900_n858990_uncorr_10.ttp",

      "a280-ttp/a280_n279_bounded-strongly-corr_01.ttp",
    };

    /* instance information */
    final TTP1Instance ttp = new TTP1Instance("./TTP1_data/"+inst[0]);
    Deb.echo(ttp);
    Deb.echo("------------------");

    /* algorithm */
    final LocalSearch algo = new Cosolver2SA(ttp);
//    algo.firstfit();
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

        Deb.echo("objective   : " + sx.ob);
        Deb.echo("Duration    : " + (exTime/1000.0) + " sec");
      }
    });
    
    executor.shutdown();  // reject all further submissions
    
    try {
      future.get(1200, TimeUnit.SECONDS);  //     <-- wait 5 seconds to finish
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
