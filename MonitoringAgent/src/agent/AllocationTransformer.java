package agent;

import agent.utils.MethodUtils;
import javassist.*;
import javassist.bytecode.MethodInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Tracking bytecodes:
 * anewarray
 * multianewarray
 * new
 * newarray
 */
public class AllocationTransformer implements ClassFileTransformer {
    public static final int LIMIT = 10;

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer)
            throws IllegalClassFormatException {

        try {
            ClassPool pool  = ClassPool.getDefault();
            CtClass   clazz = pool.makeClass(new ByteArrayInputStream(classfileBuffer));

            visitMethods(clazz);

            return clazz.toBytecode();
        } catch (IOException | CannotCompileException | NotFoundException e) {
            e.printStackTrace();
        }

        return classfileBuffer;
    }

    private void visitMethods(CtClass clazz) throws CannotCompileException, NotFoundException {
        for (CtMethod m : clazz.getDeclaredMethods())
            transform(clazz, m);
    }

    private void transform(CtClass clazz, CtMethod method)
            throws CannotCompileException, NotFoundException {

        if (MethodUtils.isMain(method))
            method.insertAfter(AllocationIntrospection.class.getName() + ".printLog(" + LIMIT + ");", true);

        MethodInfo info = method.getMethodInfo();

        visitMethodBody(clazz, info);
    }

    private void visitMethodBody(CtClass clazz, MethodInfo info) {
        // TODO
    }
}
