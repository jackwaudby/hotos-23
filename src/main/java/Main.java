import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    @Option(names = {"-u", "--uri"}, description = "Neo4j URI")
    private static String URI = "bolt://localhost:7687";

    @Option(names = {"-m", "--mammothDelay"}, description = "Delay before starting mammoth transaction (secs)")
    private static int MAMMOTH_DELAY = 10;

    @Option(names = {"-b", "--balanced"}, description = "Balanced or unbalanced workload")
    private static String BALANCED = "false";

    @Option(names = {"-r", "--readClients"}, description = "Read clients")
    private static int READERS = 5;

    @Option(names = {"-w", "--writeClients"}, description = "Write clients")
    private static int WRITERS = 1;

    @Option(names = {"-d", "--duration"}, description = "Experiment duration (secs)")
    private static long EXPERIMENT_DURATION = 10;

    public static void main(String[] args) throws InterruptedException {
        System.out.printf("Starting %s experiment for %s secs...\n", BALANCED, EXPERIMENT_DURATION);
        System.out.println("Readers: " + READERS);
        System.out.println("Writers: " + WRITERS);
        System.out.println("Mammoth delay: " + MAMMOTH_DELAY);

        var startTime = System.nanoTime();

        List<Long> personIds;
        try (var coordinator = new BoltDriverUtils(URI)) {
            personIds = coordinator.getAllPersonIds(Boolean.parseBoolean(BALANCED));
        }

        BlockingQueue<Integer> requests = new LinkedBlockingQueue<>();
        BlockingQueue<Integer> shutdownNotification = new LinkedBlockingQueue<>();

        var m = new Metrics(requests, shutdownNotification);
        initMetrics(m).start();

        List<Thread> clients = new ArrayList<>();
        List<BlockingQueue<Integer>> notifyShutdown = new ArrayList<>();

        createOltpReadClients(personIds, shutdownNotification, m, clients, notifyShutdown);
        createOltpReadWriteClients(personIds, shutdownNotification, m, clients, notifyShutdown);
        clients.forEach(Thread::start);
        createMammothClient(shutdownNotification, MAMMOTH_DELAY * 1000, Boolean.parseBoolean(BALANCED), m).start();

        Thread.sleep(EXPERIMENT_DURATION * 1000);

        notifyShutdown.forEach(ch -> {
            try {
                ch.put(0);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        var finished = 0;
        do {
            if (shutdownNotification.size() > 0) {
                shutdownNotification.take();
                finished++;
            }

        } while (finished != clients.size());

        // shutdown metrics
        requests.put(0);

        while (true) {
            if (shutdownNotification.size() > 0) {
                shutdownNotification.take();
                break;
            }
        }
        var endTime = System.nanoTime();
        var duration = (double) (endTime - startTime) / 1000000.0 / 1000.0;
        System.out.printf("Experiment finished in %.2f seconds\n", duration);
    }

    private static void createOltpReadWriteClients(List<Long> personIds, BlockingQueue<Integer> out, Metrics m, List<Thread> clients, List<BlockingQueue<Integer>> notifyShutdown) {
        for (int i = 0; i < READERS; i++) {
            BlockingQueue<Integer> channel = new LinkedBlockingQueue<>();
            notifyShutdown.add(channel);
            var client = createOltpClient(personIds, channel, out, m, true);
            clients.add(client);
        }
    }

    private static void createOltpReadClients(List<Long> personIds, BlockingQueue<Integer> out, Metrics m, List<Thread> clients, List<BlockingQueue<Integer>> notifyShutdown) {
        for (int i = 0; i < WRITERS; i++) {
            BlockingQueue<Integer> in = new LinkedBlockingQueue<>();
            notifyShutdown.add(in);
            var oltpClient = createOltpClient(personIds, in, out, m, false);
            clients.add(oltpClient);
        }
    }

    private static Thread createMammothClient(BlockingQueue<Integer> out, int delayInMillis, boolean balanced, Metrics metrics) {
        return new Thread(() -> {
            try (var client = new BoltMammothDriver(URI, metrics, out, delayInMillis, balanced)) {
                try {
                    client.run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static Thread createOltpClient(List<Long> personIds, BlockingQueue<Integer> in, BlockingQueue<Integer> out, Metrics metrics, boolean rw) {
        return new Thread(() -> {
            try (var client = new BoltOltpDriver(URI, metrics, in, out, personIds, rw)) {
                try {
                    client.run();
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
