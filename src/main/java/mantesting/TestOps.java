package mantesting;

import ea.ERX2;
import ea.Initialization;
import ea.MPX2;
import ea.Mutation;
import solver.Constructive;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by kyu on 11/3/15.
 */
public class TestOps {






  final static TTP1Instance ttp = new TTP1Instance("my-ttp/sample10.ttp");
//  final static TTP1Instance ttp = new TTP1Instance("berlin52-ttp/berlin52_n51_bounded-strongly-corr_01.ttp");
//  final static TTP1Instance ttp = new TTP1Instance("u574-ttp/u574_n573_bounded-strongly-corr_01.ttp");
//  final static TTP1Instance ttp = new TTP1Instance("pcb3038-ttp/pcb3038_n3037_bounded-strongly-corr_01.ttp");
//  final static TTP1Instance ttp = new TTP1Instance("a280-ttp/a280_n279_bounded-strongly-corr_01.ttp");


  public static void test_crossover() {

    Deb.echo(ttp);
    Deb.echo("------------------");
    Constructive construct = new Constructive(ttp);

    // parent 1
    TTPSolution p1 = construct.generate("rr");
//    p1.setTour( new int[]{1,  4,  5,  6,  8,  9,  10,  7,  3,  2 });
//    p1.setPickingPlan(new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1});
    ttp.objective(p1);

    // parent 2
    TTPSolution p2 = construct.generate("rr");
//    p2.setTour( new int[]{1,  5,  8,  6,  9,  10,  7,  4,  3,  2 });
//    p2.setPickingPlan(new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0});
    ttp.objective(p2);

    // =============

    Deb.echo(p1.getTour());
    Deb.echo(p1.getPickingPlan());
    Deb.echo(p2.getTour());
    Deb.echo(p2.getPickingPlan());

    TTPSolution c = MPX2.crossover(p1, p2, ttp);
    Deb.echo(c.getTour());
    Deb.echo(c.getPickingPlan());

  }



  // testing
  public static void test_mutation() {

    Deb.echo(ttp);
    Deb.echo("------------------");
    Constructive construct = new Constructive(ttp);
    //Initialization init = new Initialization(ttp);

    // parent 1
    int[] x = construct.randomTour();
    //int[] x=new int[]{1, 4, 13, 5, 6, 8, 12, 9, 10, 7, 11, 14, 15, 16, 3, 2 };
    //int[] x = new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13, 14, 15, 16, 17,18,19};
    Deb.echo(x);
    int[] x2 = Mutation.doubleBridge(x);
    Deb.echo(x2);
  }


  public static void test_runtime_of_ERX_on_large_TTP() {

    Deb.echo(ttp);
    Deb.echo("------------------");
    Constructive construct = new Constructive(ttp);
    Initialization init = new Initialization(ttp);

    Deb.echo("ok 1");
    // parent 1
    final TTPSolution p1 = construct.generate("rz");
    p1.setTour(init.tsp2opt(p1.getTour()));
    ttp.objective(p1);
    Deb.echo("ok 2");

    // parent 2
    final TTPSolution p2 = construct.generate("rz");
    p2.setTour(init.tsp2opt(p2.getTour()));
    ttp.objective(p2);
    Deb.echo("ok 3");



    /* execute */
    ExecutorService executor = Executors.newFixedThreadPool(4);
    Future<?> future = executor.submit(new Runnable() {
      @Override
      public void run() {
        long startTime, stopTime;
        long exTime;
        startTime = System.currentTimeMillis();

        ERX2.crossover(p1, p2);

        stopTime = System.currentTimeMillis();
        exTime = stopTime - startTime;
        Deb.echo("Duration    : " + (exTime/1000.0) + " sec");
      }
    });
    executor.shutdown();  // reject all further submissions
  }


  public static void main(String[] args) {
    test_crossover();
  }



















}
