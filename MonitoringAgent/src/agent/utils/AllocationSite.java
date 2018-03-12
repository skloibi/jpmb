package agent.utils;

import java.util.Objects;

public class AllocationSite {
    public final String method;
    public final String className;
    public final long   line;
    public final long   bci;
    private      long   count;

    public AllocationSite(String method, String className, long line, long bci) {
        this.method = method;
        this.className = className;
        this.line = line;
        this.bci = bci;
        this.count = 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AllocationSite that = (AllocationSite) o;
        return bci == that.bci &&
                Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, bci);
    }

    public AllocationSite increase() {
        count++;
        return this;
    }

    public long getCount() {
        return count;
    }
}
