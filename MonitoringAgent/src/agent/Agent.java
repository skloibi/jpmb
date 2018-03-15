package agent;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

import static agent.AllocationTransformer.LIMIT;

public class Agent {

    public static void premain(String args, Instrumentation instrumentation)
            throws UnmodifiableClassException {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            AllocationIntrospection.getInstance().printLog(LIMIT);
        }));
        instrumentation.addTransformer(new AllocationTransformer(), true);
        Class<?>[] classes = instrumentation.getAllLoadedClasses();

        for (Class<?> clazz : classes) {
            if (!instrumentation.isModifiableClass(clazz))
                continue;

            instrumentation.retransformClasses(clazz);
        }
    }
}
