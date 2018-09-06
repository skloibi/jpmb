package spec.harness.analyzer;

public class GarbageCollectionAnalyzer extends AnalyzerBase {
    private static final String NAME = "Total time of garbage collection";
    private static final String UNIT = "milliseconds";

    private long gcTime;

    @Override
    public void execute(long __) {
    }

    @Override
    public void startMeasurementInterval() {
    }

    @Override
    public void endMeasurementInterval() {
        gcTime = GCHelper.gcTime();
    }

    @Override
    public void tearDown() {
        GCHelper.forceGC();
        report(new GarbageCollectionResult(gcTime));
    }

    public static class GarbageCollectionResult extends AnalyzerResult {
        public GarbageCollectionResult(long value) {
            super(value);
        }

        public String getName() {
            return GarbageCollectionAnalyzer.NAME;
        }

        public String getUnit() {
            return GarbageCollectionAnalyzer.UNIT;
        }
    }
}
