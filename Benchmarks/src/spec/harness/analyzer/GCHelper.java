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

    static long forceGC() {
        final long before = gcCount();
        System.gc();
        while (gcCount() == before) ;
        return gcTime();
    }

    static long currentlyUsedMemory() {
        forceGC();
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() +
                ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
    }
}
