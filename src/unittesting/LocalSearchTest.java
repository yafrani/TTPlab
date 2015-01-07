package unittesting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import solver.Constructive;
import solver.LocalSearch;
import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;


@RunWith(Theories.class)
public class LocalSearchTest {
  
  /**
   * data
   */
  @DataPoints public static String instances[] = {
    "eil51-ttp/eil51_n50_bounded-strongly-corr_02.ttp",
    "eil51-ttp/eil51_n500_uncorr_10.ttp",
    "berlin52-ttp/berlin52_n510_uncorr-similar-weights_07.ttp",
    "bier127-ttp/bier127_n1260_uncorr-similar-weights_10.ttp",
    "a280-ttp/a280_n279_uncorr_10.ttp",
  };
  
  TTP1Instance ttp;
  TTPSolution s0;
  
  
  /** algorithm to test */
  LocalSearch algo;
  
  
  /** precision needed */
  double epsilon = .001;
  
  
  // algo params
  String algoCls = "solver.JointN1BF"; // default algo is N1BF
  boolean firstfit = true;
  
  
  /**
   * initialize data
   * 
   * @param instance
   */
  private void prepareData(String instance) {
    
    // instance
    ttp = new TTP1Instance("./TTP1_data/"+instance);
    
    // initial solution s0
    Constructive construct = new Constructive(ttp);
    s0 = construct.generate("lg");
    
    // config algorithm
    try {
      Class<?> cl = Class.forName(algoCls);
      Constructor<?> cons = cl.getConstructor(new Class[]{TTP1Instance.class, TTPSolution.class});
      algo = (LocalSearch)cons.newInstance(ttp, s0);
      
    } catch (ClassNotFoundException|InstantiationException|
             IllegalAccessException|NoSuchMethodException|
             SecurityException|IllegalArgumentException|
             InvocationTargetException ex) {
      ex.printStackTrace();
    }
    algo.firstfit = this.firstfit;
    
  }
  
  
  @Theory
  public void shouldCalculateObjectiveCorrectly(String instance) {
    
    Deb.echo("sould calculate objective correctly");
    
    prepareData(instance);
    
    TTPSolution sx = algo.solve();

    double obtained = sx.ob;
    ttp.objective(sx);
    double expected = sx.ob;
    Deb.echo(ttp.getName()+" "+ttp.getNbCities()+"x"+ttp.getNbItems()+":");
    Deb.echo(obtained + " vs " + expected);
    assertTrue("recovered objective value is false", Math.abs(obtained - expected) < epsilon);
  }
  
  
  @Theory
  public void shouldReturnSameSolutionAsOldSolver(String instance) {
    
    Deb.echo("sould return same solution as old solver");
    
    prepareData(instance);
    
    TTPSolution sn = algo.solve();
    
    try {
      String smplName = algo.getClass().getSimpleName();
      Class<?> cl = Class.forName("oldsolver."+smplName+"OLD");// x
      Constructor<?> cons = cl.getConstructor(new Class[]{TTP1Instance.class, TTPSolution.class});
      LocalSearch alg = (LocalSearch)cons.newInstance(ttp, s0);
      if (firstfit) alg.firstfit();
      else alg.bestfit();
      TTPSolution so = alg.solve();
      
      Deb.echo(ttp.getName()+" "+ttp.getNbCities()+"x"+ttp.getNbItems()+":");
      Deb.echo(" old: "+so.ob + " vs new:" + sn.ob);
      assertEquals("new solution is false", sn, so);
      
    } catch (ClassNotFoundException|InstantiationException|
             IllegalAccessException|NoSuchMethodException|
             SecurityException|IllegalArgumentException|
             InvocationTargetException ex) {
      ex.printStackTrace();
    }
  }
}
