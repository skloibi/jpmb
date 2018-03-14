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
            System.err.println("FAIL");
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
                    handleArray(clazz, info, it, bci);
                    break;
                case Opcode.NEW:
                    handleNew(clazz, info, it, bci);
                    break;
                case Opcode.NEWARRAY:
                    handleArray(clazz, info, it, bci);
                    break;
                default:
                    break;
            }
        }
    }

    private void handleNew(CtClass clazz, MethodInfo method, CodeIterator it, int bci) throws BadBytecode {
        System.out.println("NEW");
        System.out.println("METHOD: " + method.getName());
        System.err.println("1");
        ConstPool cp   = method.getConstPool();
        String    info = cp.getClassInfo(bci);
        System.err.println("2");
        System.out.println("info: " + info);
        System.err.println("3");
        count(clazz, method, info, it, bci);
        System.err.println("4");
    }

    private void handleArray(CtClass clazz, MethodInfo method, CodeIterator it, int bci) throws BadBytecode {
        System.out.println("ARRAY");
        System.out.println("METHOD: " + method.getName());
        ConstPool cp   = method.getConstPool();
        String    info = cp.getClassInfo(bci);
        System.out.println("info: " + info);

        count(clazz, method, info, it, bci);
//        int a = cp.getIntegerInfo(bci);
//        System.out.println("dims: " + a);
    }

    private void handleANewArray(CtClass clazz, MethodInfo method, CodeIterator it, int bci) throws BadBytecode {
        System.out.println("NEW ARRAY");
        System.out.println("METHOD: " + method.getName());
        ConstPool cp   = method.getConstPool();
        String    info = cp.getClassInfo(bci);
        System.out.println("info: " + info);
        count(clazz, method, info, it, bci);
        // TODO
//        System.out.println("dims: " + it.);
    }

    private void count(CtClass clazz, MethodInfo method, String instance, CodeIterator it, int bci) throws BadBytecode {
//        ConstPool cp = method.getConstPool();
//        int ci = cp.addClassInfo(AllocationIntrospection.class.getName());

        Bytecode code = new Bytecode(method.getConstPool(), 0, 0);

        // [
        code.addInvokestatic(AllocationIntrospection.class.getName(), "getInstance", "()L" + AllocationIntrospection.class.getName().replace('.', '/') + ";");
        // [ singleton
        code.addLdc(clazz.getSimpleName());
        // [ singleton class_name
        code.addLdc(method.getName());
        // [ singleton class_name method_name
        code.addLdc(method.getDescriptor());
        // [ singleton class_name method_name descriptor
        code.addLdc(instance);
        // [ singleton class_name method_name instance_name
        code.addLdc2w(bci);
        // [ singleton class_name method_name instance_name bci
        code.addLdc2w(method.getLineNumber(bci));
        // [ singleton class_name method_name instance_name bci line_num
        code.addInvokevirtual(AllocationIntrospection.class.getName(), "log", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JJ)V");
        // [
        it.insert(code.get());
    }
}
