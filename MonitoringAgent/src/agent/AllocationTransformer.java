package agent;

import agent.utils.AllocExprEditor;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

public class AllocationTransformer implements ClassFileTransformer {

    public static final String[] DEFAULT_PACKAGES = {
            AllocationTransformer.class.getPackageName(),
            "java/util",
            "java.util",
            "jdk",
//            "java/lang",
//            "java.lang"
            // TODO add more / remove java.util?
    };

    private final boolean      debug;
    private final List<String> excludedPackages;

    public AllocationTransformer(boolean debug, List<String> excludePackages) {
        this.debug = debug;

        if (excludePackages.isEmpty())
            this.excludedPackages = Arrays.asList(DEFAULT_PACKAGES);
        else
            this.excludedPackages = excludePackages;
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {

        // skip excluded classes
        if (excludedPackages.stream().anyMatch(className::startsWith))
            return classfileBuffer;

        try {
            ClassPool pool  = ClassPool.getDefault();
            CtClass   clazz = pool.makeClass(new ByteArrayInputStream(classfileBuffer));

            instrumentMethods(clazz, new AllocExprEditor());

            byte[] data = clazz.toBytecode();

            if (debug && className.endsWith("Main")) {
                String file = className.replace('/', '.') + ".class";
                try (FileOutputStream out = new FileOutputStream(file)) {
                    out.write(data);
                }
            }

            return data;
        } catch (IOException | RuntimeException | CannotCompileException e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        return classfileBuffer;
    }

    /**
     * Instrument the class' methods with the given {@link ExprEditor}.
     *
     * @param clazz The class whose methods have to be instrumented
     * @throws CannotCompileException if the instrumentation fails
     */
    private void instrumentMethods(CtClass clazz, ExprEditor editor) throws CannotCompileException {
        for (CtMethod m : clazz.getDeclaredMethods())
            m.instrument(editor);
    }

}
