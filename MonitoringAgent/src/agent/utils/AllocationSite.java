package agent.utils;

public class AllocationSite {
    public final  String method;
    public final  String className;
    private final int    line;
    private final int    bci;
    public final  int    count;

    private AllocationSite(String method, String className, int line, int bci, int count) {
        this.method = method;
        this.className = className;
        this.line = line;
        this.bci = bci;
        this.count = count;
    }

    public AllocationSite(String method, String className, int line, int bci) {
        this(method, className, line, bci, 1);
    }

    public AllocationSite(AllocationSite site, int count) {
        this(site.method, site.className, site.line, site.bci, count);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AllocationSite that = (AllocationSite) o;

        if (bci != that.bci) return false;
        if (!method.equals(that.method)) return false;
        return className.equals(that.className);
    }

    @Override
    public int hashCode() {
        int result = method.hashCode();
        result = 31 * result + className.hashCode();
        result = 31 * result + bci;
        return result;
    }

    @Override
    public String toString() {
        return "AllocationSite{" +
                "method='" + method + '\'' +
                ", className='" + className + '\'' +
                ", line=" + line +
                ", bci=" + bci +
                ", count=" + count +
                '}';
    }

    public AllocationSite increase() {
        return new AllocationSite(this, count + 1);
    }

    public String getLineDescriptor() {
        if (line != -1)
            return "(Line " + line + ")";

        return "(BCI " + bci + ")";
    }
}
