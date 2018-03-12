package agent;

import agent.utils.MethodUtils;
import javassist.*;
import javassist.bytecode.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

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
        } catch (IOException | CannotCompileException | NotFoundException | BadBytecode e) {
            e.printStackTrace();
        }

        return classfileBuffer;
    }

    private void visitMethods(CtClass clazz) throws CannotCompileException, NotFoundException, BadBytecode {
        for (CtMethod m : clazz.getDeclaredMethods())
            transform(clazz, m);
    }

    private void transform(CtClass clazz, CtMethod method)
            throws CannotCompileException, NotFoundException, BadBytecode {

        if (MethodUtils.isMain(method))
            method.insertAfter(AllocationIntrospection.class.getName() + ".getInstance().printLog(" + LIMIT + ");", true);

        visitMethodBody(clazz, method);
    }

    private void visitMethodBody(CtClass clazz, CtMethod method) throws BadBytecode {

        MethodInfo    info = method.getMethodInfo();
        CodeAttribute code = info.getCodeAttribute();
        CodeIterator  it   = code.iterator();

        while (it.hasNext()) {
            int bci    = it.next();
            int opcode = it.byteAt(bci);

            switch (opcode) {
                case Opcode.ANEWARRAY:
                    handleANewArray(clazz, info, it, bci);
                    break;
                case Opcode.MULTIANEWARRAY:

                    break;
                case Opcode.NEW:

                    break;
                case Opcode.NEWARRAY:

                    break;
                default:
                    break;
            }
        }
    }

    private void handleANewArray(CtClass clazz, MethodInfo method, CodeIterator it, int bci) {
        System.out.println("METHOD: " + method.getName());
        ConstPool cp   = method.getConstPool();
        String    info = cp.getClassInfo(bci);
        System.out.println("info: " + info);


    }
}
