package mantesting;

import ea.EdgeRecombination;
import ea.Initialization;
import ea.PartitionCrossover;
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
public class TestEvo {

  public static void main4(String[] args) {

    //final TTP1Instance ttp = new TTP1Instance("my-ttp/sample-data-10.ttp");
    final TTP1Instance ttp = new TTP1Instance("berlin52-ttp/berlin52_n51_bounded-strongly-corr_01.ttp");
    Deb.echo(ttp);
    Deb.echo("------------------");
    Constructive construct = new Constructive(ttp);
    Initialization init = new Initialization(ttp);

    // parent 1
    TTPSolution p1 = construct.generate("rz");
    //p1.setTour( new int[]{1,  4,  5,  6,  8,  9,  10,  7,  3,  2 });
    p1.setTour(init.tsp2opt(p1.getTour()));
    ttp.objective(p1);

    // parent 2
    TTPSolution p2 = construct.generate("rz");
    //p2.setTour( new int[]{1,  5,  8,  6,  9,  10,  7,  4,  3,  2 });
    p2.setTour(init.tsp2opt(p2.getTour()));
    ttp.objective(p2);

    // =============

    Deb.echoz(p1.getTour());
    Deb.echo("OBJ: " + p1.ob);
    Deb.echoz(p2.getTour());
    Deb.echo("OBJ: " + p2.ob);

    TTPSolution c = EdgeRecombination.ERX(p1, p2);
    ttp.objective(c);
    Deb.echo("OBJ: " + c.ob);
    c.setTour(init.tsp2opt(c.getTour()));
    ttp.objective(c);
    Deb.echo("OBJ: " + c.ob);

  }



  // testing
  public static void main3(String[] args) {

    final TTP1Instance ttp = new TTP1Instance("berlin52-ttp/berlin52_n51_bounded-strongly-corr_01.ttp");
//    final TTP1Instance ttp = new TTP1Instance("u574-ttp/u574_n573_bounded-strongly-corr_01.ttp");
//    final TTP1Instance ttp = new TTP1Instance("pcb3038-ttp/pcb3038_n3037_bounded-strongly-corr_01.ttp");
//    final TTP1Instance ttp = new TTP1Instance("a280-ttp/a280_n279_bounded-strongly-corr_01.ttp");


    Deb.echo(ttp);
    Deb.echo("------------------");

    Initialization in = new Initialization(ttp);

    int[] rt = in.randomTour();
    //Deb.echo(rt);
    int[] lo = in.tsp2opt(rt);
    Deb.echo(lo);
  }



  // testing PX
  public static void main5() {

//    final TTP1Instance ttp = new TTP1Instance("berlin52-ttp/berlin52_n51_bounded-strongly-corr_01.ttp");
//    final TTP1Instance ttp = new TTP1Instance("u574-ttp/u574_n573_bounded-strongly-corr_01.ttp");
//    final TTP1Instance ttp = new TTP1Instance("rl11849-ttp/rl11849_n11848_uncorr_07.ttp");
    final TTP1Instance ttp = new TTP1Instance("a280-ttp/a280_n279_bounded-strongly-corr_01.ttp");

    Deb.echo(ttp);
    Deb.echo("------------------");
    Constructive construct = new Constructive(ttp);
    Initialization init = new Initialization(ttp);

    Deb.echo("ok 1");
    // parent 1
    TTPSolution p1 = construct.generate("rz");
    //p1.setTour(init.tsp2opt(p1.getTour()));
    p1.setTour(init.qBoruvka());
    ttp.objective(p1);
    Deb.echo("ok 2");

    // parent 2
    TTPSolution p2 = construct.generate("rz");
    p2.setTour(init.tsp2opt(p2.getTour()));
    ttp.objective(p2);
    Deb.echo("ok 3");

    // resulting child
    //Deb.echo(p1.getTour());
    Deb.echo("OBJ: " + p1.ob);
    //Deb.echo(p2.getTour());
    Deb.echo("OBJ: " + p2.ob);
    Deb.echo("ok 4");

    TTPSolution c1 = EdgeRecombination.ERX(p1, p2);
    Deb.echo("ok 5");
    c1.setTour(init.tsp2opt(c1.getTour()));
    ttp.objective(c1);
    Deb.echo("OBJ: " + c1.ob);
    Deb.echo("ok 6");
    //PartitionCrossover.PX(p1, p2);

  }




  public static void test_runtime_of_ERX_on_large_TTP() {

//    final TTP1Instance ttp = new TTP1Instance("berlin52-ttp/berlin52_n51_bounded-strongly-corr_01.ttp");
    final TTP1Instance ttp = new TTP1Instance("u574-ttp/u574_n573_bounded-strongly-corr_01.ttp");
//    final TTP1Instance ttp = new TTP1Instance("rl11849-ttp/rl11849_n11848_uncorr_07.ttp");
//    final TTP1Instance ttp = new TTP1Instance("a280-ttp/a280_n279_bounded-strongly-corr_01.ttp");

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

        TTPSolution c1 = EdgeRecombination.ERX(p1, p2);

        stopTime = System.currentTimeMillis();
        exTime = stopTime - startTime;
        Deb.echo("Duration    : " + (exTime/1000.0) + " sec");
      }
    });
    executor.shutdown();  // reject all further submissions
  }



  public static void main(String[] args) {
    //test_runtime_of_ERX_on_large_TTP();
    main5();
  }
}
