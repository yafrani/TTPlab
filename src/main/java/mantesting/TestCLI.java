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

public class TestCLI {

  public static void main(String[] args) {

    Deb.echo(">>"+args[0]);
    //if (true) return;

    String inst = args[0];

    /* instance information */
    final TTP1Instance ttp = new TTP1Instance(inst);
    Deb.echo(ttp);
    Deb.echo("------------------");

    /* algorithm */
    final LocalSearch algo = new CS2B(ttp);
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

        TTPSolution sx = algo.search();

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
