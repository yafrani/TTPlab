package unittesting;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  JointN1BFFirstFitTest.class,
  JointN1BFBestFitTest.class,
})
public class AllTests {

} 
