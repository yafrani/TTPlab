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

      //===========================================
      // ITEM FACTOR = 1
      //===========================================
//      "eil76-ttp/eil76_n75_uncorr_01.ttp",
      "kroA100-ttp/kroA100_n99_uncorr_01.ttp",
//      "ch130-ttp/ch130_n129_bounded-strongly-corr_01.ttp",
//      "u159-ttp/u159_n158_bounded-strongly-corr_01.ttp",
//      "a280-ttp/a280_n279_bounded-strongly-corr_01.ttp",
//      "u574-ttp/u574_n573_bounded-strongly-corr_01.ttp",
//      "u724-ttp/u724_n723_bounded-strongly-corr_01.ttp",
//      "dsj1000-ttp/dsj1000_n999_bounded-strongly-corr_01.ttp",
//      "rl1304-ttp/rl1304_n1303_bounded-strongly-corr_01.ttp",
//      "fl1577-ttp/fl1577_n1576_bounded-strongly-corr_01.ttp",
//      "d2103-ttp/d2103_n2102_bounded-strongly-corr_01.ttp",
//      "pcb3038-ttp/pcb3038_n3037_bounded-strongly-corr_01.ttp",
//      "fnl4461-ttp/fnl4461_n4460_bounded-strongly-corr_01.ttp",
//      "pla7397-ttp/pla7397_n7396_bounded-strongly-corr_01.ttp",
//      "rl11849-ttp/rl11849_n11848_bounded-strongly-corr_01.ttp",
//      "usa13509-ttp/usa13509_n13508_bounded-strongly-corr_01.ttp",
//      "brd14051-ttp/brd14051_n14050_bounded-strongly-corr_01.ttp",
//      "d15112-ttp/d15112_n15111_bounded-strongly-corr_01.ttp",
//      "d18512-ttp/d18512_n18511_bounded-strongly-corr_01.ttp",
//      "pla33810-ttp/pla33810_n33809_bounded-strongly-corr_01.ttp",

      //===========================================
      // ITEM FACTOR = 5
      //===========================================
//      "eil76-ttp/eil76_n375_uncorr-similar-weights_05.ttp",
//      "kroA100-ttp/kroA100_n495_uncorr-similar-weights_05.ttp",
//      "ch130-ttp/ch130_n645_uncorr-similar-weights_05.ttp",
//      "u159-ttp/u159_n790_uncorr-similar-weights_05.ttp",
//      "a280-ttp/a280_n1395_uncorr-similar-weights_05.ttp",
//      "u574-ttp/u574_n2865_uncorr-similar-weights_05.ttp",
//      "u724-ttp/u724_n3615_uncorr-similar-weights_05.ttp",
//      "dsj1000-ttp/dsj1000_n4995_uncorr-similar-weights_05.ttp",
//      "rl1304-ttp/rl1304_n6515_uncorr-similar-weights_05.ttp",
//      "fl1577-ttp/fl1577_n7880_uncorr-similar-weights_05.ttp",
//      "d2103-ttp/d2103_n10510_uncorr-similar-weights_05.ttp",
//      "pcb3038-ttp/pcb3038_n15185_uncorr-similar-weights_05.ttp",
//      "fnl4461-ttp/fnl4461_n22300_uncorr-similar-weights_05.ttp",
//      "pla7397-ttp/pla7397_n36980_uncorr-similar-weights_05.ttp",
//      "rl11849-ttp/rl11849_n59240_uncorr-similar-weights_05.ttp",
//      "usa13509-ttp/usa13509_n67540_uncorr-similar-weights_05.ttp",
//      "brd14051-ttp/brd14051_n70250_uncorr-similar-weights_05.ttp",
//      "d15112-ttp/d15112_n75555_uncorr-similar-weights_05.ttp",
//      "d18512-ttp/d18512_n92555_uncorr-similar-weights_05.ttp",
//      "pla33810-ttp/pla33810_n169045_uncorr-similar-weights_05.ttp",

      //===========================================
      // ITEM FACTOR = 10
      //===========================================
//      "eil76-ttp/eil76_n750_uncorr_10.ttp",
//      "kroA100-ttp/kroA100_n990_uncorr_10.ttp",
//      "ch130-ttp/ch130_n1290_uncorr_10.ttp",
//      "u159-ttp/u159_n1580_uncorr_10.ttp",
//      "a280-ttp/a280_n2790_uncorr_10.ttp",
//      "u574-ttp/u574_n5730_uncorr_10.ttp",
//      "u724-ttp/u724_n7230_uncorr_10.ttp",
      "dsj1000-ttp/dsj1000_n9990_uncorr_10.ttp",
//      "rl1304-ttp/rl1304_n13030_uncorr_10.ttp",
//      "fl1577-ttp/fl1577_n15760_uncorr_10.ttp",
//      "d2103-ttp/d2103_n21020_uncorr_10.ttp",
//      "pcb3038-ttp/pcb3038_n30370_uncorr_10.ttp",
//      "fnl4461-ttp/fnl4461_n44600_uncorr_10.ttp",
//      "pla7397-ttp/pla7397_n73960_uncorr_10.ttp",
//      "rl11849-ttp/rl11849_n118480_uncorr_10.ttp",
//      "usa13509-ttp/usa13509_n135080_uncorr_10.ttp",
//      "brd14051-ttp/brd14051_n140500_uncorr_10.ttp",
//      "d15112-ttp/d15112_n151110_uncorr_10.ttp",
//      "d18512-ttp/d18512_n185110_uncorr_10.ttp",
//      "pla33810-ttp/pla33810_n338090_uncorr_10.ttp",

    };

    /* instance information */
    final TTP1Instance ttp = new TTP1Instance(inst[0]);
    Deb.echo(ttp);
    Deb.echo("------------------");


    /* algorithm */
//    final SearchHeuristic evalgo  = new MA2B(ttp);
//    final SearchHeuristic evalgo = new CS2B(ttp);
//    final SearchHeuristic evalgo = new HybridMASA(ttp);
    final SearchHeuristic evalgo = new CS2SA(ttp);

    evalgo.debug();

    /* execute */
    ExecutorService executor = Executors.newFixedThreadPool(4);
    Future<?> future = executor.submit(() -> {
      long startTime, stopTime;
      long exTime;
      startTime = System.currentTimeMillis();
      TTPSolution sx = evalgo.search();
      stopTime = System.currentTimeMillis();
      exTime = stopTime - startTime;

      Deb.echo("objective   : " + sx.ob);
      Deb.echo("Duration    : " + (exTime/1000.0) + " sec");
      sx.printStats();
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
