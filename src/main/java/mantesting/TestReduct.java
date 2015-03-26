package mantesting;

import solver.Constructive;
import solver.LocalSearch;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.*;

/**
 * Created by kyu on 3/1/15.
 */
public class TestReduct {

  static String[] inst = {
    "eil51-ttp/eil51_n50_bounded-strongly-corr_01.ttp",
    "berlin52-ttp/berlin52_n51_bounded-strongly-corr_01.ttp",
    "eil76-ttp/eil76_n75_bounded-strongly-corr_01.ttp",
    "kroA100-ttp/kroA100_n99_bounded-strongly-corr_01.ttp",
    "ch150-ttp/ch150_n149_bounded-strongly-corr_01.ttp",
    "u159-ttp/u159_n158_bounded-strongly-corr_01.ttp",
    "kroA200-ttp/kroA200_n199_bounded-strongly-corr_01.ttp",
    "ts225-ttp/ts225_n224_bounded-strongly-corr_01.ttp",
    "a280-ttp/a280_n279_bounded-strongly-corr_01.ttp",
    "lin318-ttp/lin318_n317_bounded-strongly-corr_01.ttp",
    "u574-ttp/u574_n573_bounded-strongly-corr_01.ttp",
    "u724-ttp/u724_n723_bounded-strongly-corr_01.ttp",
    "dsj1000-ttp/dsj1000_n999_bounded-strongly-corr_01.ttp",
    "rl1304-ttp/rl1304_n1303_bounded-strongly-corr_01.ttp",
    "fl1577-ttp/fl1577_n1576_bounded-strongly-corr_01.ttp",
    "d2103-ttp/d2103_n2102_bounded-strongly-corr_01.ttp",
    "pcb3038-ttp/pcb3038_n3037_bounded-strongly-corr_01.ttp",
    "fnl4461-ttp/fnl4461_n4460_bounded-strongly-corr_01.ttp",
  };

  public static double RRMIN = .08;
  public static double RRMAX = .09;
  public static double RRGAP = 11;

  public static void main(String[] args) throws FileNotFoundException {

    final PrintWriter output = new PrintWriter("results/2-opt_reduct_4.log");

    for (int i=12; i<14; i++) {

      for (RRMIN = 0.16; RRMIN < 1; RRMIN += .01) {
        for (RRMAX = RRMIN + .01; RRMAX <= 1; RRMAX += .01) {

          /* instance name */
          final String ttpname = inst[i];
          Deb.echo("[" + String.format("%.2f", RRMIN) + " ; " + String.format("%.2f", RRMAX) + "] " + inst[i]);

          /* instance information */
          final TTP1Instance ttp = new TTP1Instance("./TTP1_data/" + inst[i]);
          Deb.echo(ttp);
          Deb.echo("------------------");

          /* initial solution s0 */
          Constructive construct = new Constructive(ttp);
          TTPSolution s0 = construct.generate("lg");
          ttp.objective(s0);
          Deb.echo("s0: \n" + s0);
          Deb.echo("ob: " + s0.ob);
          Deb.echo("ww: " + s0.wend);
          Deb.echo("==================");

          /* algorithm */
          final LocalSearch algo = new solver.Cosolver2opt(ttp, s0);
//          algo.firstfit();
//          algo.debug();


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
              Deb.echo("Duration    : " + (exTime / 1000.0) + " sec");

              output.println(ttpname + " " +
                String.format("%.2f", RRMIN) + " " +
                String.format("%.2f", RRMAX) + " " +
                sx.ob + " " + (exTime / 1000.0));
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
    }

    output.close();
  }

}
