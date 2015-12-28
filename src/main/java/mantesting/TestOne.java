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
//      "my-ttp/sample10.ttp",

//      "eil76-ttp/eil76_n75_bounded-strongly-corr_01.ttp",
//      "kroA100-ttp/kroA100_n99_bounded-strongly-corr_01.ttp",
//      "ch130-ttp/ch130_n129_bounded-strongly-corr_01.ttp",
//      "u159-ttp/u159_n158_bounded-strongly-corr_01.ttp",
//      "a280-ttp/a280_n279_bounded-strongly-corr_01.ttp",
//      "u574-ttp/u574_n573_bounded-strongly-corr_01.ttp",
//      "u724-ttp/u724_n723_bounded-strongly-corr_01.ttp",
//      "dsj1000-ttp/dsj1000_n999_bounded-strongly-corr_01.ttp",
//      "rl1304-ttp/rl1304_n1303_bounded-strongly-corr_01.ttp",
//      "fl1577-ttp/fl1577_n1576_bounded-strongly-corr_01.ttp",
      "d2103-ttp/d2103_n2102_bounded-strongly-corr_01.ttp",
//      "pcb3038-ttp/pcb3038_n3037_bounded-strongly-corr_01.ttp",
//      "fnl4461-ttp/fnl4461_n4460_bounded-strongly-corr_01.ttp",
//      "pla7397-ttp/pla7397_n7396_bounded-strongly-corr_01.ttp",
//      "d15112-ttp/d15112_n15111_bounded-strongly-corr_01.ttp",



      "a280-ttp/a280_n1395_uncorr-similar-weights_05.ttp",
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
    final Evolution evalgo = new EvoMPUX(ttp);
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
