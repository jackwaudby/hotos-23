import org.apache.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.util.*;
import java.util.concurrent.Callable;

public class Main implements Callable<Integer> {

    private final static Logger LOGGER = Logger.getLogger(Main.class.getName());

    @Option(names = {"-k", "--keys"}, description = "Database size")
    private int keys = 1000;

    @Option(names = {"-t", "--transactionSize"}, description = "Transaction size")
    private int transactionSize = 5;

    @Option(names = {"-c", "--communities"}, description = "Number of communities")
    private int communities = 10;

    @Option(names = {"-r", "--ranges"}, description = "Number of ranges")
    private int ranges = 10;

    @Override
    public Integer call() {
        var r = new Random();
        LOGGER.info("----Config----");
        LOGGER.info("Database size (keys): " + keys);
        LOGGER.info("Transaction size: " + transactionSize);
        LOGGER.info("Communities: " + communities);
        LOGGER.info("Ranges: " + ranges);

        // database
        var database = new Database(keys).getKeys();

        // community index
        var communityIndex = Utils.createCommunityIndex(communities);
        Utils.populateCommunityIndex(keys, r, communityIndex);

        // range index
        List<Range> rangeIndex = new ArrayList<>();
        Utils.populateRangeIndex(keys, ranges, rangeIndex);

        Utils.runTransaction(keys, transactionSize, r, database, communityIndex, rangeIndex);

        // Locks acquired
        LOGGER.info("----Results----");
        var rangeLocked = database.stream().filter(Key::isRangeLocked).count();
        LOGGER.info("Range locked keys: " + rangeLocked);
        var communityLocked = database.stream().filter(Key::isCommunityLocked).count();
        LOGGER.info("Community locked keys: " + communityLocked);

        WriteOutResults.writeOutResults(keys,transactionSize,ranges,communities,rangeLocked,communityLocked);

        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}