package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

public class Metrics {
    private final BlockingQueue<Integer> in;
    private final BlockingQueue<Integer> out;
    private int currentCommitted;
    private final List<Integer> committed;

    private int currentAborted;
    private final List<Integer> aborted;

    private int currentMammoth;
    private final List<Integer> mammoth;

    public Metrics(BlockingQueue<Integer> in, BlockingQueue<Integer> out) {
        this.in = in;
        this.out = out;
        this.currentCommitted = 0;
        this.currentAborted = 0;
        this.currentMammoth = 0;
        this.committed = new ArrayList<>();
        this.aborted = new ArrayList<>();
        this.mammoth = new ArrayList<>();
    }

    public void run() throws InterruptedException, IOException {
        while (true) {
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    collect();
                }
            }, 0, 1000);

            int x = in.take();
            if (x == 0) {
                System.out.println("Shutdown metrics");
                t.cancel();

                FileWriter writer = new FileWriter("test.csv");
                for (int i = 0; i < committed.size(); i++) {
                    writer.append(String.valueOf(committed.get(i)));
                    writer.append(",");
                    writer.append(String.valueOf(mammoth.get(i)));
                    writer.append("\n");

                }

                writer.flush();
                writer.close();
                break;
            }
        }
        out.put(0);
    }

    private void collect() {
        synchronized (this) {
            System.out.print("Committed: " + getCount() + " Aborted: " + getAbortedCount() + "\r");
            committed.add(getCount());
            mammoth.add(getMammoth());
            currentMammoth = 0;
            currentCommitted = 0;
            currentAborted = 0;
        }
    }

    private int getMammoth() {
        synchronized (this) {
            return currentMammoth;
        }
    }

    private int getCount() {
        synchronized (this) {
            return currentCommitted;
        }
    }

    private int getAbortedCount() {
        synchronized (this) {
            return currentAborted;
        }
    }


    public void increment() {
        synchronized (this) {
            currentCommitted++;
        }
    }

    public void incrementAborted() {
        synchronized (this) {
            currentAborted++;
        }
    }

    public void markMammoth() {
        synchronized (this) {
            currentMammoth++;
        }
    }
}