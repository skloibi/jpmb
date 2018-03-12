package agent;

import agent.utils.AllocationSite;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
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

    public void log(Class<?> clazz, Method method, String[] args, String alloc, long bci, long line) {
        AllocationSite site = new AllocationSite(
                clazz.getSimpleName() + "::" + method.getName() + "(" + Arrays.toString(method.getParameterTypes()) + ")",
                alloc,
                line,
                bci);

        sites.compute(site.hashCode(), (k, v) -> v == null ? site : v.increase());
    }

    public void printLog(int limit) {
        sites.values().stream()
                .sorted(Comparator.comparingLong(AllocationSite::getCount))
                .limit(limit)
                .forEach(a -> System.err.println(
                        String.format("%d %s @ %s (Line %d)",
                                a.getCount(), a.className, a.method, a.line)
                        // TODO check if line exists
                ));
    }
}
