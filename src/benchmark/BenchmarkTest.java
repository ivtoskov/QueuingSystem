package asl.benchmark;

import java.sql.Connection;
import java.util.Scanner;
import asl.middleware.SocketWrapper;

public abstract class BenchmarkTest extends Thread {
    protected Connection connection;
    protected long duration;
    protected BenchmarkInfo benchmarkInfo;
    protected SocketWrapper sw;

    public BenchmarkTest(Connection connection, int duration, BenchmarkInfo benchmarkInfo) {
        this.connection = connection;
        this.duration = duration * 1000;
        this.benchmarkInfo = benchmarkInfo;
    }

    public BenchmarkTest(SocketWrapper sw, int duration, BenchmarkInfo benchmarkInfo) {
        this.sw = sw;
        this.duration = duration * 1000;
        this.benchmarkInfo = benchmarkInfo;
    }
}