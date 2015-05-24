package regressiontesting;

import org.junit.Before;
import org.junit.BeforeClass;

import utils.Deb;

public class Joint2optBFFirstFitTest extends LocalSearchTest {

  @Before
  public void init() {
    algoCls = "oldsolver.Joint2optBF";
    firstfit = false;
  }
  
  @BeforeClass
  public static void msg() {
    Deb.echo("===============================");
    Deb.echo("Testing Joint2optBF - first fit");
    Deb.echo("===============================");
  }
}
