package mantesting;

import solver.*;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

/**
 * tune CS2SA with irace
 */
public class CLIiraceCS2SA {

  public static void main(String[] args) {


    // for testing in IDE
    if (args.length < 2) {
      args = new String[]{"eil51_n50_uncorr_01.ttp", "alpha=0.9345", "T0=310"};
    }

    String[] tspBase = args[0].split("_",2);

    // TTP instance name
    final String inst = args[0];

    // runtime limit
    final long runtimeLimit = 600;

    if (args.length < 1) {
      Deb.echo("ERROR: Not enough params");
      return;
    }

    // get algo params
    double alpha = -1.0;
    double T0 = -1;
    double trialsFactor = -1;
    for (int k=1; k<args.length; k++) {
      if (args[k].split("=", 2)[0].contains("alpha"))
        alpha = Double.parseDouble(args[k].split("=", 2)[1]);
      if (args[k].split("=", 2)[0].contains("T0"))
        T0 = Double.parseDouble(args[k].split("=", 2)[1]);
      if (args[k].split("=", 2)[0].contains("TF"))
        trialsFactor = Double.parseDouble(args[k].split("=", 2)[1]);
    }

    // TTP instance
    final TTP1Instance ttp = new TTP1Instance(tspBase[0]+"-ttp/"+inst);


    // algorithm to run
    final CS2SA algo = new CS2SA(ttp);
    if (alpha>0.0)
      algo.alpha = alpha;
    if (T0>0.0)
      algo.T0 = T0;
    if (trialsFactor>0.0)
      algo.trialFactor = trialsFactor;
    //Deb.echo(">>"+algo.trialFactor );


    // runnable class
    class TTPRunnable implements Runnable {

      String resultLine;
      TTPSolution sx;

      @Override
      public void run() {

        /* start search & measure runtime */
        long startTime, stopTime;
        long exTime;
        startTime = System.currentTimeMillis();

        sx = algo.search();

        stopTime = System.currentTimeMillis();
        exTime = stopTime - startTime;

        /* print result */
        resultLine = Math.round(sx.ob)+"";
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
      //System.out.println("/!\\ Timeout");
    }

    // wait for execution to be done
    try {
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // print results
    Deb.echo(-Integer.parseInt(ttprun.resultLine));

  }
}
