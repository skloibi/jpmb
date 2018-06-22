package agent;

import agent.utils.AllocationSite;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AllocationIntrospection {
    private static AllocationIntrospection instance;

    private Map<Integer, AllocationSite> sites;

    private AllocationIntrospection() {
        sites = new ConcurrentHashMap<>();
    }

    public static AllocationIntrospection getInstance() {
        if (instance == null)
            instance = new AllocationIntrospection();

        return instance;
    }

    @SuppressWarnings("unused")
    public void log(String identifier, String alloc, int bci, int line) {
        AllocationSite site = new AllocationSite(identifier, alloc, line, bci);

        AllocationSite old = sites.get(site.hashCode());

        if (old == null)
            sites.put(site.hashCode(), site);
        else
            sites.put(old.hashCode(), old.increase());
    }

    public void printLog(int limit) {
        List<AllocationSite> sites = new ArrayList<>(this.sites.values());

        sites.sort(new Comparator<AllocationSite>() {
            @Override
            public int compare(AllocationSite o1, AllocationSite o2) {
                return Integer.compare(o2.count, o1.count);
            }
        });

        for (int i = 0; i < sites.size() && i < limit; i++) {
            AllocationSite site = sites.get(i);
            System.err.printf(
                    "%3d: %15d %-30s @ %-40s %s\n",
                    (i + 1),
                    site.count,
                    site.className,
                    site.method,
                    site.getLineDescriptor());
        }
    }
}
