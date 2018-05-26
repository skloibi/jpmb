package agent;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Agent {

    public static final int    DEFAULT_LIMIT = 10;
    public static final String DEBUG_PREFIX  = "-d";
    public static final String LIMIT_PREFIX  = "-l";
    public static final String ARG_SPLIT     = ";";

    public static void premain(String args, Instrumentation instrumentation) throws UnmodifiableClassException {

        int     l;
        boolean debug = false;
        List<String> argList =
                args == null ?
                        new ArrayList<>() :
                        Arrays.asList(args.split(ARG_SPLIT));

        // check if debug mode
        if (argList.contains(DEBUG_PREFIX))
            debug = true;

        // get limit
        try {
            String limitStr = "";
            for (String a : argList) {
                if (a.startsWith(LIMIT_PREFIX)) {
                    limitStr = a;
                    break;
                }
            }

            if (limitStr.length() >= LIMIT_PREFIX.length())
                limitStr = limitStr.substring(LIMIT_PREFIX.length());

            l = Integer.parseInt(limitStr);
        } catch (NumberFormatException e) {
            // do nothing - simply apply default limit
            l = DEFAULT_LIMIT;
        }

        List<String> exclusions = new ArrayList<>();
        for (String a : argList) {
            if (!a.startsWith(LIMIT_PREFIX) && !a.startsWith(DEBUG_PREFIX))
                exclusions.add(a);
        }

        final int limit = l > 0 ? l : DEFAULT_LIMIT;

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                AllocationIntrospection.getInstance().printLog(limit);
            }
        }));

        instrumentation.addTransformer(new AllocationTransformer(debug, exclusions), true);
        Class<?>[] classes = instrumentation.getAllLoadedClasses();

        for (Class<?> clazz : classes) {
            boolean fail = false;

            for (String prefix : exclusions) {
                if (clazz.getName().startsWith(prefix))
                    fail = true;
            }

            if (fail || !instrumentation.isModifiableClass(clazz) || clazz.getName().startsWith("java.lang.invoke.LambdaForm"))
                continue;

            instrumentation.retransformClasses(clazz);
        }
    }
}
