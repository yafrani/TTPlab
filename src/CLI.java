import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class CLI {
  static char randChar(){
    char min = 'a';
    char max = 'z'+1;
    Random random = new Random();
    int r=random.nextInt(max - min) + min;
    return (char)r;
  }
  
  public static void main(String[] args) {
    
    String a="";
    a+=randChar();
    System.out.println(a);
    System.exit(0);
    
    ExecutorService executor = Executors.newFixedThreadPool(4);
    Future<?> future = executor.submit(new Runnable() {
      
      public void run() {
          int a = writeToDb();            //        <-- your job
          System.out.println("A: "+a);
      }
    });
    
    executor.shutdown();            //        <-- reject all further submissions
    
    try {
      future.get(2, TimeUnit.SECONDS);  //     <-- wait 5 seconds to finish
        
    } catch (InterruptedException e) {    //     <-- possible error cases
      System.out.println("job was interrupted");
    } catch (ExecutionException e) {
      System.out.println("caught exception: " + e.getCause());
    } catch (TimeoutException e) {
      future.cancel(true);              //     <-- interrupt the job
      System.out.println("timeout");
    }
    
    System.out.println("next thing...");
  }
  
  public static int writeToDb(){
    // writes to the database
//    Scanner x = new Scanner(System.in);
//    
//    System.out.print("gimme a byte: ");
//    byte a = x.nextByte();
//    System.out.print("OKAY: "+a);
//    x.close();
    for (int i=1; i<100000;i++) {
      for (int j=1; j<100000;j++) {
        
        for (int k=1;k<100000;k++) {
          if (Thread.currentThread().isInterrupted()) {
            // cleanup and stop execution
            // for example a break in a loop
            System.out.println("interrupted...");
            return k;
          }
        }
      }
    }
    System.out.print("OKAY");
    return -1;
  }

}
