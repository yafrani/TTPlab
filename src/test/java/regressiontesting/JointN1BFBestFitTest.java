package regressiontesting;

import org.junit.Before;
import org.junit.BeforeClass;

import utils.Deb;

public class JointN1BFBestFitTest extends LocalSearchTest {

  @Before
  public void init() {
    algoCls = "oldsolver.JointN1BF";
    firstfit = false;
  }
  
  @BeforeClass
  public static void msg() {
    Deb.echo("=============================");
    Deb.echo("Testing JointN1BF - best fit");
    Deb.echo("=============================");
  }
}
