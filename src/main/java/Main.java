import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    static final String URI = "bolt://localhost:7687";
    static final int TRANSACTIONS = 1000;
    static final int MAMMOTH_DELAY = 5000;
    static final boolean BALANCED = true;

    public static void main(String[] args) throws InterruptedException {
        System.out.printf("Starting experiment for %s transactions...\n", TRANSACTIONS);
        var startTime = System.nanoTime();

        List<Long> personIds;
        try (var coordinator = new BoltDriverUtils(URI)) {
            personIds = coordinator.getAllPersonIds(BALANCED);
        }

        BlockingQueue<Integer> requests = new LinkedBlockingQueue<>();
        BlockingQueue<Integer> shutdown = new LinkedBlockingQueue<>();

        var m = new Metrics(requests, shutdown);
        initMetrics(m).start();

        var oltp = createOltpClient(personIds, shutdown, m);
        var mammoth = createMammothClient(shutdown, MAMMOTH_DELAY, BALANCED);
        var threads = List.of(oltp, mammoth);
        threads.forEach(Thread::start);


        var finished = 0;
        while (true) {
            if (shutdown.size() > 0) {
                shutdown.take();
                finished++;
            }

            if (finished == 2) {
                System.out.println("Clients finished");
                break;
            }
        }

        // shutdown metrics
        requests.put(0);

        while (true) {
            if (shutdown.size() > 0) {
                shutdown.take();
                break;
            }
        }
        var endTime = System.nanoTime();
        var duration = (double) (endTime - startTime) / 1000000.0 / 1000.0;
        System.out.printf("Experiment finished in %.2f seconds\n", duration);
    }

    private static Thread createMammothClient(BlockingQueue<Integer> shutdown, int delayInMillis, boolean balanced) {
        return new Thread(() -> {
            try (var client = new BoltMammothDriver(URI, shutdown, delayInMillis, balanced)) {
                try {
                    client.run(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static Thread createOltpClient(List<Long> personIds, BlockingQueue<Integer> shutdown, Metrics m) {
        return new Thread(() -> {
            try (var client = new BoltOltpDriver(URI, personIds, m, shutdown)) {
                try {
                    client.run(TRANSACTIONS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static Thread initMetrics(Metrics m) {
        return new Thread(() -> {
            try {
                m.run();
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
