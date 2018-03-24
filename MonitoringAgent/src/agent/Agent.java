package agent;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Agent {

    public static final int    DEFAULT_LIMIT = 10;
    public static final String DEBUG_PREFIX  = "-d";
    public static final String LIMIT_PREFIX  = "-l";
    public static final String ARG_SPLIT     = ";";

    public static void premain(String args, Instrumentation instrumentation) {

        int          l;
        boolean      debug = false;
        List<String> argList;

        if (args == null)
            argList = new ArrayList<>();
        else
            argList = Arrays.asList(args.split(ARG_SPLIT));

        // check if debug mode
        if (argList.contains(DEBUG_PREFIX))
            debug = true;

        // get limit
        try {
            String limitStr = argList.stream().filter(a -> a.startsWith(LIMIT_PREFIX))
                    .findFirst()
                    .map(a -> a.substring(LIMIT_PREFIX.length()))
                    .orElse("");

            l = Integer.parseInt(limitStr);
        } catch (NumberFormatException e) {
            // do nothing - simply apply default limit
            l = DEFAULT_LIMIT;
        }

        List<String> exclusions = argList.stream()
                .filter(a -> !a.startsWith(LIMIT_PREFIX))
                .filter(a -> !a.startsWith(DEBUG_PREFIX))
                .collect(Collectors.toList());

        final int limit = l > 0 ? l : DEFAULT_LIMIT;

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                AllocationIntrospection.getInstance().printLog(limit);
            }
        }));

        instrumentation.addTransformer(new AllocationTransformer(debug, exclusions), true);
        Class<?>[] classes = instrumentation.getAllLoadedClasses();

        Arrays.stream(classes)
                .filter(instrumentation::isModifiableClass)
                .filter(c -> exclusions.stream()
                        .noneMatch(e -> c.getName().startsWith(e)))
                .forEach(c -> {
                    try {
                        instrumentation.retransformClasses(c);
                    } catch (UnmodifiableClassException e) {
                        e.printStackTrace();
                    }
                });
    }
}
