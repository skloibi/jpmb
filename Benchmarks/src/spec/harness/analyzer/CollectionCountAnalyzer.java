package spec.harness.analyzer;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

public class CollectionCountAnalyzer extends AnalyzerBase {
    private static final String NAME = "Total number of Garbage collections";
    private static final String UNIT = "";

    @Override
    public void execute(long l) {
        long collectionCount = 0L;
        for (GarbageCollectorMXBean garbageCollectorMXBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = garbageCollectorMXBean.getCollectionCount();
            collectionCount += count;
        }

        report(new CollectionCountAnalyzer.CollectionCount(l, collectionCount));
    }

    public static class CollectionCount extends TYInfo {
        public CollectionCount(long time, long value) {
            super(time, value);
        }

        public String getName() {
            return CollectionCountAnalyzer.NAME;
        }

        public String getUnit() {
            return CollectionCountAnalyzer.UNIT;
        }
    }
}
