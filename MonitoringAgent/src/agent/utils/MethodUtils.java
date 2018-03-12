package agent.utils;

import javassist.CtMethod;
import javassist.NotFoundException;

public class MethodUtils {
    public static boolean isMain(CtMethod m) throws NotFoundException {
        return m.getName().equals("main") &&
                m.getParameterTypes().length == 1 &&
                m.getParameterTypes()[0].getSimpleName()
                        .equals("[Ljava/lang/String");
    }
}
