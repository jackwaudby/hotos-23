import java.util.*;

public class Utils {

    public static void runTransaction(int keys, int transactionSize, Random r, List<Key> database, Map<Integer, Community> communityIndex, List<Range> rangeIndex) {
        int lastKey = -1;
        for (int i = 0; i < transactionSize; i++) {
            var key = getKey(keys, r, true, lastKey, communityIndex);
            // Range lock
            var range = rangeIndex.stream().filter(ra -> ra.contains(key)).findFirst().orElseThrow();
            database.forEach(e -> {
                if (e.getId() >= range.start() && e.getId() <= range.end()) {
                    e.setRangeLocked();
                }
            });

            // Community lock
            var community = communityIndex.values().stream().filter(c -> c.getKeys().contains(key.getId())).findFirst().orElseThrow();
            database.forEach(e -> {
                if (community.getKeys().contains(e.getId())) {
                    e.setCommunityLocked();
                }
            });
            lastKey = key.getId();
        }
    }

    public static void populateRangeIndex(int keys, int ranges, List<Range> rangeIndex) {
        var rangeSize = keys / ranges;

        for (int j = 0; j < keys; j++) {
            var start = j;
            var end = j + rangeSize - 1;
            if (end > keys) {
                end = keys - 1;
            }

            var range = new Range(start, end);
            rangeIndex.add(range);
            j += rangeSize - 1;
        }
    }

    public static void populateCommunityIndex(int keys, Random r, Map<Integer, Community> communityIndex) {
        for (int i = 0; i < keys; i++) {
            var community = r.nextInt(communityIndex.size());
            communityIndex.get(community).getKeys().add(i);
        }
    }

    public static Map<Integer, Community> createCommunityIndex(int communities) {
        Map<Integer, Community> communityIndex = new HashMap<>();
        for (int k = 0; k < communities; k++) {
            communityIndex.put(k, new Community(k));
        }
        return communityIndex;
    }

    public static Key getKey(int keys, Random r, boolean aware, int lastKey, Map<Integer, Community> communityIndex) {
        if (aware && lastKey != -1) {
            if (r.nextDouble() < .85) {
                var community = communityIndex.values().stream().filter(c -> c.getKeys().contains(lastKey)).findFirst().orElseThrow();
                var temp = new ArrayList<>(community.getKeys());
                Collections.shuffle(temp);
                return new Key(temp.stream().findFirst().orElseThrow());
            } else {
                var next = r.nextInt(keys);
                return new Key(next);
            }
        } else {
            return new Key(r.nextInt(keys));
        }
    }
}
