package mantesting;

import solver.*;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import utils.Quicksort;
import utils.TwoOptHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.*;

/**
 * junk testing
 */
public class Experiment1 {

  public static void main(String[] args) {

    if (args.length < 2) {
      args = new String[]{"eil76_n75_bounded-strongly-corr_01.ttp", "cs2sa"};
    }

    String[] spl = args[0].split("_",2);

    // TTP instance name
    final String inst = args[0];

    // algorithm name
    final String algoName = args[1];

    // output file
    final String outputFile = "./output/CS2SA-experiment.csv";

    // runtime limit
    long runtimeLimit = 600;

    // TTP instance
    final TTP1Instance ttp = new TTP1Instance(spl[0]+"-ttp/"+inst);

    /* algorithm to run */
    final SearchHeuristic algo;
    switch (algoName) {
      case "cs2b":
        algo = new CS2B(ttp);
        break;
      case "cs2sa":
        algo = new CS2SA(ttp);
        break;
      case "ma2b":
        algo = new MA2B(ttp);
        break;
      default:
        algo = new CS2SA(ttp);
    }


    // runnable class
    class TTPRunnable implements Runnable {

      String resultLine;

      @Override
      public void run() {


        /* start search & measure runtime */
        long startTime, stopTime;
        long exTime;
        startTime = System.currentTimeMillis();

        TTPSolution sx = algo.search();

        stopTime = System.currentTimeMillis();
        exTime = stopTime - startTime;

        /* print result */
        resultLine = inst + " " + Math.round(sx.ob) + " " + (exTime/1000.0);

      }
    };

    // my TTP runnable
    TTPRunnable ttprun = new TTPRunnable();
    ExecutorService executor = Executors.newFixedThreadPool(4);
    Future<?> future = executor.submit(ttprun);
    executor.shutdown();  // reject all further submissions

    // limit execution time to 600 seconds
    try {
      future.get(runtimeLimit, TimeUnit.SECONDS);  // wait X seconds to finish
    } catch (InterruptedException e) {
      System.out.println("job was interrupted");
    } catch (ExecutionException e) {
      System.out.println("caught exception: " + e.getCause());
    } catch (TimeoutException e) {
      future.cancel(true);
      System.out.println("/!\\ Timeout");
    }

    // wait for execution to be done
    try {
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // print results
    Deb.echo(ttprun.resultLine);

    // log results into text file
    try {
      File file = new File(outputFile);
      if (!file.exists()) file.createNewFile();
      Files.write(Paths.get(outputFile), (ttprun.resultLine + "\n").getBytes(), StandardOpenOption.APPEND);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

}
