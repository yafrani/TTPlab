package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by kyu on 12/5/15.
 */
public class ConfigHelper {

  public static String getProperty(String name) {
    // get datasets folder path
    Properties prop = new Properties();
    InputStream input;
    String ttpData = null;
    try {
      input = new FileInputStream("config.properties");

      // load a properties file
      prop.load(input);

      // get the property value and print it out
      ttpData = prop.getProperty(name);

    } catch (IOException ex) {
      ex.printStackTrace();
    }
    return ttpData;
  }
}
