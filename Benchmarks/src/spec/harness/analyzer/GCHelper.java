package spec.harness.analyzer;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

final class GCHelper {
    static long gcCount() {
        long sum = 0;
        for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = b.getCollectionCount();
            if (count != -1)
                sum += count;
        }
        return sum;
    }

    static long gcTime() {
        long sum = 0;
        for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = b.getCollectionTime();
            if (count != -1)
                sum += count;
        }
        return sum;
    }

    static long currentlyUsedMemory() throws InterruptedException {
        long m;
        long m2 = currentlyUsedMemorySnapshot();
        do {
            Thread.sleep(500);
            m = m2;
            m2 = currentlyUsedMemorySnapshot();
        } while (m2 < currentlyUsedMemorySnapshot());
        return m;
    }

    static long forceGC() {
        final long before = gcCount();
        System.gc();
        while (gcCount() == before) ;
        return gcTime();
    }

    static long totalMemory() throws InterruptedException {
        long m;
        long m2 = totalMemorySnapshot();
        do {
            Thread.sleep(500);
            m = m2;
            m2 = totalMemorySnapshot();
        } while (m2 < totalMemorySnapshot());
        return m;
    }

    private static long currentlyUsedMemorySnapshot() {
        forceGC();
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() +
                ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
    }

    private static long totalMemorySnapshot() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getCommitted() +
                ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getCommitted();
    }
}
