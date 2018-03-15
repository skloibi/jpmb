package agent;

import agent.utils.AllocationSite;

import java.util.HashMap;
import java.util.Map;

public class AllocationIntrospection {
    private static AllocationIntrospection instance;

    private Map<Integer, AllocationSite> sites;

    private AllocationIntrospection() {
        sites = new HashMap<>();
        sites.put(1, new AllocationSite("m", "c", 1, 10));
    }

    public static AllocationIntrospection getInstance() {
        if (instance == null)
            instance = new AllocationIntrospection();

        return instance;
    }

    public void log(String clazz, String method, String params, String alloc, int bci, int line) {
        AllocationSite site = new AllocationSite(
                clazz + "::" + method + "(" + params + ")",
                alloc,
                line,
                bci);

        sites.compute(site.hashCode(), (k, v) -> v == null ? site : v.increase());
    }

    public void printLog(int limit) {
        System.err.println("Printing log ");
        sites.values().stream()
                .sorted((s1, s2) -> Integer.compare(s2.getCount(), s1.getCount()))
                .limit(limit)
                .forEach(a -> System.err.println(
                        String.format("%d %s @ %s (Line %d)",
                                a.getCount(), a.className, a.method, a.line)
                        // TODO check if line exists
                ));
    }
}
