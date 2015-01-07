package unittesting;

import org.junit.Before;
import org.junit.BeforeClass;

import utils.Deb;

public class JointN1BFFirstFitTest extends LocalSearchTest {
  
  @Before
  public void init() {
    algoCls = "solver.JointN1BF";
    firstfit = true;
    
  }
  
  @BeforeClass
  public static void msg() {
    Deb.echo("=============================");
    Deb.echo("Testing JointN1BF - first fit");
    Deb.echo("=============================");
  }
}
