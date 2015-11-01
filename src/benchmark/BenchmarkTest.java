package asl.benchmark;

import java.sql.Connection;
import java.util.Scanner;

public abstract class BenchmarkTest extends Thread {
    protected Connection connection;
    protected long duration;

    public BenchmarkTest(Connection connection, int duration) {
        this.connection = connection;
        this.duration = duration * 1000;
    }
}