package spec.harness.analyzer;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

public class CollectionTimeAnalyzer extends AnalyzerBase {
    private static final String NAME = "Total time of Garbage collection";
    private static final String UNIT = "milliseconds";

    @Override
    public void execute(long l) {
        long collectionCount = 0L;
        for (GarbageCollectorMXBean garbageCollectorMXBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            long collectionTime = garbageCollectorMXBean.getCollectionTime();
            collectionCount += collectionTime;
        }

        report(new CollectionTimeAnalyzer.CollectionTime(l, collectionCount));
    }

    public static class CollectionTime extends TYInfo {
        public CollectionTime(long time, long value) {
            super(time, value);
        }

        public String getName() {
            return CollectionTimeAnalyzer.NAME;
        }

        public String getUnit() {
            return CollectionTimeAnalyzer.UNIT;
        }
    }
}
