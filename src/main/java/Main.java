public class Main {
    static final String URI = "bolt://localhost:7687";
    static final String USER = "neo4j";
    static final String PASSWORD = "admin";
    static final int PERSONS = 100;
    static final int TRANSACTIONS = 100000;

    public static void main(String[] args) {

        try (var coordinator = new BoltCoordinator(URI, USER, PASSWORD, PERSONS)) {
            System.out.printf("Initialising database with %s persons...\n", PERSONS);
            var startLoadTime = System.nanoTime();
            coordinator.initialiseDatabase();
            var endLoadTime = System.nanoTime();
            var loadDuration = (double) (endLoadTime - startLoadTime) / 1000000.0 / 1000.0;
            System.out.printf("Database initialised in %.2f seconds\n", loadDuration);

            System.out.printf("Starting experiment for %s transactions...\n", TRANSACTIONS);
            try (var client = new BoltClient(URI, USER, PASSWORD, PERSONS)) {
                var startTime = System.nanoTime();
                client.run(TRANSACTIONS);
                var endTime = System.nanoTime();
                var duration = (double) (endTime - startTime) / 1000000.0 / 1000.0;
                var throughput = (double) TRANSACTIONS / duration;
                System.out.printf("Throughput: %.2f (transactions/s)\n", throughput);
                System.out.printf("Experiment finished in %.2f seconds\n", duration );
            }

            System.out.println("Tearing down database...");
            coordinator.tearDownDatabase();
            System.out.println("Database destroyed");
        }
    }
}
