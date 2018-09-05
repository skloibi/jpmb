package spec.harness.analyzer;

public class RuntimeAnalyzer extends AnalyzerBase {
    private static final String NAME = "Total runtime of an iteration";
    private static final String UNIT = "milliseconds";

    private long start;
    private long end;

    @Override
    public void execute(long __) {
    }

    @Override
    public void startMeasurementInterval() {
        start = System.currentTimeMillis();
    }

    @Override
    public void endMeasurementInterval() {
        end = System.currentTimeMillis();
    }

    @Override
    public void tearDown() {
        report(new RuntimeResult(end - start));
    }

    public static class RuntimeResult extends AnalyzerResult {
        public RuntimeResult(long value) {
            super(value);
        }

        public String getName() {
            return RuntimeAnalyzer.NAME;
        }

        public String getUnit() {
            return RuntimeAnalyzer.UNIT;
        }
    }
}
