package solver;

import ttp.TTP1Instance;

/**
 * base TTP algorithm class
 */
public abstract class TTPHeuristic {
  
  protected TTP1Instance ttp;
  protected boolean debug;
  protected boolean log;
  protected String name;
  
  
  public TTPHeuristic() {
    this.name = this.getClass().getSimpleName();
    
    this.debug = false;
    this.log = false;
  }
  
  public TTPHeuristic(TTP1Instance ttp ) {
    this();
    this.ttp = ttp;
  }
  
  
  /**
   * debugging
   */
  public void debug() {
    this.debug = true;
  }
  
  /**
   * no debugging
   */
  public void noDebug() {
    this.debug = false;
  }
  
  /**
   * save log
   */
  public void log() {
    this.log = true;
  }
  
  /**
   * no log saving
   */
  public void noLog() {
    this.log = false;
  }
  
  
  // heuristic name
  public void setName(String logfile) {
    this.name = logfile;
  }
  public String getName() {
    return name;
  }
  
  
  // TTP access
  public void setTTP(TTP1Instance ttp) {
    this.ttp = ttp;
  }
  public TTP1Instance getTTP() {
    return ttp;
  }
}
