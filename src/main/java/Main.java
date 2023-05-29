import bolt.BoltDriverUtils;
import bolt.BoltMammothDriver;
import bolt.BoltOltpDriver;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import postgres.PostgresMammothDriver;
import postgres.PostgresOltpDriver;
import utils.Metrics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class Main implements Callable<Integer> {
    @Option(names = {"-u", "--uri"}, description = "Neo4j URI")
    private String URI = "bolt://localhost:7687";

    @Option(names = {"-m", "--mammothDelay"}, description = "Delay before starting mammoth transaction (secs)")
    private int MAMMOTH_DELAY = 10;

    @Option(names = {"-b", "--balanced"}, description = "Balanced or unbalanced workload")
    private String BALANCED = "true";

    @Option(names = {"-r", "--readClients"}, description = "Read clients")
    private int READERS = 5;

    @Option(names = {"-w", "--writeClients"}, description = "Write clients")
    private int WRITERS = 1;

    @Option(names = {"-d", "--duration"}, description = "Experiment duration (secs)")
    private long EXPERIMENT_DURATION = 10;

    @Option(names = {"-s", "--system"}, description = "neo4j or postgres")
    private String SYSTEM = "neo4j";

    @Override
    public Integer call() throws InterruptedException {
        var exp = "unbalanced";
        var params = "u-params.csv";
        if (Boolean.parseBoolean(BALANCED)) {
            exp = "balanced";
            params = "b-params.csv";
        }
        System.out.printf("Starting %s experiment using %s for %s secs...\n", exp, SYSTEM, EXPERIMENT_DURATION);
        System.out.println("Readers: " + READERS);
        System.out.println("Writers: " + WRITERS);
        System.out.printf("Mammoth delay %s secs\n", MAMMOTH_DELAY);

        var startTime = System.nanoTime();

//        List<Long> personIds;
//        if (SYSTEM.equals("postgres")) {
//            personIds = List.of(1L);
//        } else {
//            try (var coordinator = new BoltDriverUtils(URI)) {
//                personIds = coordinator.getAllPersonIds(Boolean.parseBoolean(BALANCED));
//                var writer = new FileWriter("./u-params.csv");
//                var collect = personIds.stream().map(Object::toString).collect(Collectors.joining(","));
//                writer.write(collect);
//                writer.close();
//
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        System.exit(0);

        // load parameters in
        List<Long> personIds = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(params))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                personIds = Arrays.stream(values).map(Long::parseLong).collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BlockingQueue<Integer> requests = new LinkedBlockingQueue<>();
        BlockingQueue<Integer> shutdownNotification = new LinkedBlockingQueue<>();

        var m = new Metrics(requests, shutdownNotification);
        initMetrics(m).start();

        List<Thread> clients = new ArrayList<>();
        List<BlockingQueue<Integer>> notifyShutdown = new ArrayList<>();

        createOltpReadClients(URI, personIds, shutdownNotification, m, clients, notifyShutdown, READERS, SYSTEM);
        createOltpReadWriteClients(URI, personIds, shutdownNotification, m, clients, notifyShutdown, WRITERS, SYSTEM);
        clients.forEach(Thread::start);
        createMammothClient(URI, shutdownNotification, MAMMOTH_DELAY * 1000, Boolean.parseBoolean(BALANCED), m, SYSTEM).start();

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
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    private static void createOltpReadWriteClients(String URI, List<Long> personIds, BlockingQueue<Integer> out,
                                                   Metrics m, List<Thread> clients, List<BlockingQueue<Integer>> notifyShutdown,
                                                   int READERS, String system) {
        for (int i = 0; i < READERS; i++) {
            BlockingQueue<Integer> channel = new LinkedBlockingQueue<>();
            notifyShutdown.add(channel);
            var client = createOltpClient(URI, personIds, channel, out, m, true, system);
            clients.add(client);
        }
    }

    private static void createOltpReadClients(String URI, List<Long> personIds, BlockingQueue<Integer> out, Metrics m,
                                              List<Thread> clients, List<BlockingQueue<Integer>> notifyShutdown,
                                              int WRITERS, String system) {
        for (int i = 0; i < WRITERS; i++) {
            BlockingQueue<Integer> in = new LinkedBlockingQueue<>();
            notifyShutdown.add(in);
            var oltpClient = createOltpClient(URI, personIds, in, out, m, false, system);
            clients.add(oltpClient);
        }
    }

    private static Thread createMammothClient(String URI, BlockingQueue<Integer> out, int delayInMillis, boolean balanced, Metrics metrics, String system) {
        return new Thread(() -> {
            if (system.equals("postgres")) {
                var client = new PostgresMammothDriver(metrics, out, delayInMillis, balanced);
                try {
                    client.run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try (var client = new BoltMammothDriver(URI, metrics, out, delayInMillis, balanced)) {
                    try {
                        client.run();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private static Thread createOltpClient(String URI, List<Long> personIds, BlockingQueue<Integer> in, BlockingQueue<Integer> out, Metrics metrics, boolean rw, String system) {
        return new Thread(() -> {
            if (system.equals("postgres")) {
                var client = new PostgresOltpDriver(metrics, in, out, personIds, rw);
                try {
                    client.run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try (var client = new BoltOltpDriver(URI, metrics, in, out, personIds, rw)) {
                    try {
                        client.run();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
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
