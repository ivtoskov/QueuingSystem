package asl.client;

public class Client {
  public static void main(String[] args) {
    System.out.println("HelloASL");

    assert(args.length >= 2);
    System.out.println("Arg0: " + args[0]);
    System.out.println("Arg1: " + args[1]);

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
