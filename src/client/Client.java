package asl.client;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

public class Client {
  private static Logger logger = Logger.getLogger(Client.class);

  public static void main(String[] args) {
    logger.info("Hello ASL");

    assert(args.length >= 2);
    logger.info("Arg0: " + args[0]);
    logger.info("Arg1: " + args[1]);

    if(args.length >= 3)
      System.out.println("Arg2: " + args[2]);

    try {
      Class.forName("org.postgresql.Driver");
        System.out.println("JDBC Driver found");
    } catch(ClassNotFoundException e) {
      System.out.println("JDBC Driver not found!!!");
    }
  }

}
