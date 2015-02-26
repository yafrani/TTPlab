package utils;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
  
  private PrintWriter log;
  
  public Log() {
    this("");
  }
  
  public Log(String prefix) {
    
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");
    
    String filename = "./logs/" + prefix + "." +df.format(new Date());
    
    try {
      this.log = new PrintWriter(filename);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
  
  public void print(Object msg) {
    log.println(msg);
  }
  
  public void close() {
    log.close();
  }
  public static void main(String[] args) {
    double x=12333.23443973;
    long a=Math.round(x);
    Deb.echo(x+" ---> "+a);
//    Log x = new Log("rr_algo1");
//    for (int i=1;i<10;i++) {
//      x.print("hello..."+i);
//    }
//    x.close();
  }
}
