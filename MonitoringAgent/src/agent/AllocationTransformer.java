package agent;

import agent.utils.AllocExprEditor;
import javassist.*;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class AllocationTransformer implements ClassFileTransformer {
    public static final int LIMIT = 10;

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {

        if (className.startsWith("agent") || className.startsWith("java.util")) {
            System.err.println("TRANSFORMER skipping: " + className);
            return classfileBuffer;
        }

        try {
            ClassPool pool  = ClassPool.getDefault();
            CtClass   clazz = pool.makeClass(new ByteArrayInputStream(classfileBuffer));

            visitMethods(clazz);

            byte[] data = clazz.toBytecode();
            if (className.endsWith("Main")) {
                String file = className.replace('/', '.') + ".class";
                System.err.println("WRITING : " + file);
                try (FileOutputStream out = new FileOutputStream(file)) {
                    out.write(data);
                }
            }

            return data;
        } catch (IOException | RuntimeException | CannotCompileException | NotFoundException e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        return classfileBuffer;
    }

    private void visitMethods(CtClass clazz) throws CannotCompileException, NotFoundException {
        for (CtMethod m : clazz.getDeclaredMethods())
            m.instrument(new AllocExprEditor());
    }

}
