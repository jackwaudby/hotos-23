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

    public Metrics(BlockingQueue<Integer> requests, BlockingQueue<Integer> shutdown) {
        this.in = requests;
        this.out = shutdown;
        this.currentCommitted = 0;
        this.committed = new ArrayList<>();
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
                for (Integer re : getCommitted()) {
                    writer.append(String.valueOf(re));
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
            System.out.println("committed: " + getCount());
            committed.add(getCount());
            currentCommitted = 0;
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

    public List<Integer> getCommitted() {
        synchronized (this) {
            return committed;
        }
    }
}