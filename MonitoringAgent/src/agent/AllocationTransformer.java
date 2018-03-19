package agent;

import agent.utils.AllocExprEditor;
import javassist.*;
import javassist.bytecode.*;

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

        System.err.println("TRANSFORMING: " + className + " " + (classBeingRedefined != null ? classBeingRedefined.getSimpleName() : ""));

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
            transform(m);
    }

    private void transform(CtMethod method)
            throws CannotCompileException, NotFoundException {

        System.out.println("checking method: " + method.getDeclaringClass().getSimpleName() + "::" + method.getName());
//        if (MethodUtils.isMain(method)) {
//            System.err.println("MAIN FOUND: " + method.getDeclaringClass().getSimpleName() + "::" + method.getName());
//            try {
//                instrumentMain(method);
//            } catch (RuntimeException e) {
//                System.err.println("FAIL: " + e.getMessage());
//            }
//            System.err.println("AFTER MAIN: " + method.getDeclaringClass().getSimpleName() + "::" + method.getName());
//            System.err.println("MAIN INSTRUMENTED");
//        } else {

//        visitMethodBody(clazz, method);

        CodeConverter conv = new CodeConverter();
        method.instrument(new AllocExprEditor());
//        }
        System.err.println("METHOD INSTRUMENTED: " + method.getDeclaringClass().getSimpleName() + "::" + method.getName());
    }

//    private void instrumentMain(CtMethod main) throws CannotCompileException {
//        main.insertBefore(
//                "{" +
//                    "Runtime.getRuntime().addShutdownHook(new Thread() {" +
//                        "public void run() {" +
////                            AllocationIntrospection.class.getName() + ".getInstance().printLog(" + LIMIT + ");" +
//                        "}" +
//                    "});" +
//                "}"
//        );
//    }

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
        try {
            System.out.println("NEW");
            System.out.println("METHOD: " + method.getName());
            ConstPool cp   = method.getConstPool();
            String    info = cp.getClassInfo(bci);
            System.out.println("info: " + info);
            count(clazz, method, info, it, bci);
            System.err.println("4 NEW");
        } catch (RuntimeException e) {
            System.err.println("RTE!!!! " + e.getMessage());
        } catch (Exception e) {
            System.err.println("E!!!! " + e.getMessage());
        }
    }

    private void handleArray(CtClass clazz, MethodInfo method, CodeIterator it, int bci) throws BadBytecode {
        try {
            System.out.println("ARRAY");
            System.out.println("METHOD: " + method.getName());
            ConstPool cp   = method.getConstPool();
            String    info = cp.getClassInfo(bci);
            System.out.println("info: " + info);

            count(clazz, method, info, it, bci);
//        int a = cp.getIntegerInfo(bci);
//        System.out.println("dims: " + a);
        } catch (RuntimeException e) {
            System.err.println("RTE!!!! " + e.getMessage());
        } catch (Exception e) {
            System.err.println("E!!!! " + e.getMessage());
        }
    }

    private void handleANewArray(CtClass clazz, MethodInfo method, CodeIterator it, int bci) throws BadBytecode {
        try {
            System.out.println("NEW ARRAY");
            System.out.println("METHOD: " + method.getName());
            ConstPool cp   = method.getConstPool();
            String    info = cp.getClassInfo(bci);
            System.out.println("info: " + info);
            count(clazz, method, info, it, bci);
            // TODO
//        System.out.println("dims: " + it.);
        } catch (RuntimeException e) {
            System.err.println("RTE!!!! " + e.getMessage());
        } catch (Exception e) {
            System.err.println("E!!!! " + e.getMessage());
        }
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
