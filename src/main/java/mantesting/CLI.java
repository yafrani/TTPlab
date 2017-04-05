package mantesting;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import solver.J2B;
import solver.JNB;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import solver.*;

public class CLI {

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
    final String outputFile;
    if (args.length >= 3)
      outputFile = "./output/"+args[2];
    else
      outputFile = "./output/"+algoName+".csv";

    // runtime limit
    long runtimeLimit = 600;
    if (args.length >= 4)
      runtimeLimit = Long.parseLong(args[3]);

    // TTP instance
    final TTP1Instance ttp = new TTP1Instance(spl[0]+"-ttp/"+inst);
    int nbItems = ttp.getNbItems();

    /* algorithm to run */
    final SearchHeuristic algo;
    switch (algoName) {

      case "cs2b":
        algo = new CS2B(ttp);
      break;

      case "cs2sa":
        algo = new CS2SA(ttp);

        // tuned using irace package
//        ((CS2SA)algo).trialFactor = CS2SA.generateTFLinFit(nbItems);
//        ((CS2SA)algo).alpha = .9484;
//        ((CS2SA)algo).T0 = 3587;
//        Deb.echo(((CS2SA)algo).trialFactor + " // ");
      break;

      case "cs2sar":
        algo = new CS2SAR(ttp);
      break;


      case "ma2b":
        algo = new MA2B(ttp);
      break;

      case "j2b":
        algo = new J2B(ttp);
      break;

      case "jnb":
        algo = new JNB(ttp);
      break;

      default:
        algo = new CS2SA(ttp);
      break;
    }


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
      Files.write(Paths.get(outputFile), (ttprun.resultLine+"\n").getBytes(), StandardOpenOption.APPEND);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // save solution in a file
    try {
      String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
      PrintWriter pw = new PrintWriter("./output/solutions/"+inst+"-"+algoName+"-"+currentTime+".txt");
      pw.println(ttprun.sx);
      pw.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

  }
}
