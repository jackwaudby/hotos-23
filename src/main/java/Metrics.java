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
    private int currentMammoth;
    private final List<Integer> mammoth;

    public Metrics(BlockingQueue<Integer> in, BlockingQueue<Integer> out) {
        this.in = in;
        this.out = out;
        this.currentCommitted = 0;
        this.currentMammoth = 0;
        this.committed = new ArrayList<>();
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
                writer.close();
                break;
            }
        }
        out.put(0);
    }

    private void collect() {
        synchronized (this) {
            System.out.print("committed: " + getCount() + "\r");
            committed.add(getCount());
            mammoth.add(getMammoth());
            currentMammoth = 0;
            currentCommitted = 0;
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

    void increment() {
        synchronized (this) {
            currentCommitted++;
        }
    }

    public void markMammoth() {
        synchronized (this) {
            currentMammoth++;
        }
    }
}