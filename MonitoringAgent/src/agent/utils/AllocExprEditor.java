package agent.utils;

import agent.AllocationIntrospection;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.Expr;
import javassist.expr.ExprEditor;
import javassist.expr.NewArray;
import javassist.expr.NewExpr;

import java.util.Arrays;
import java.util.stream.IntStream;

public class AllocExprEditor extends ExprEditor {



    @Override
    public void edit(NewExpr e) throws CannotCompileException {
        try {
            System.err.println("FOUND NEW EXPR: " + e.getClassName());

            int        bci      = e.indexOfBytecode();
            int        line     = e.getLineNumber();
            CtBehavior behav    = e.where();
            String     clazz    = behav.getDeclaringClass().getSimpleName(); // Foo
            String     method   = behav.getName(); // bar
            String     instance = e.getClassName(); // java.lang.String
            String args = Arrays.stream(behav.getParameterTypes())
                    .map(CtClass::getSimpleName)
                    .reduce((a, b) -> String.join(", ", a, b))
                    .orElse("");

            // log(String clazz, String method, String params, String alloc, long bci, long line)

            insertBefore(
                    e,
                    AllocationIntrospection.class.getName() +
                            ".getInstance().log(" +
                            "\"" + clazz + "\", " +
                            "\"" + method + "\", " +
                            "\"" + args + "\", " +
                            "\"" + instance + "\", " +
                            "" + bci + ", " +
                            "" + line +
                            ");"
            );
        } catch (RuntimeException ex) {
            System.err.println(ex.getMessage());
        } catch (NotFoundException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void edit(NewArray a) throws CannotCompileException {
        try {
            super.edit(a);
            System.err.println("FOUND NEW ARRAY: " + a.getComponentType().getName());

            int        bci      = a.indexOfBytecode();
            int        line  = a.getLineNumber();
            int        dim      = a.getDimension();
            CtBehavior behav    = a.where();
            String     clazz    = behav.getDeclaringClass().getSimpleName(); // Foo
            String     method   = behav.getName(); // bar
            String     instance = a.getComponentType().getName(); // java.lang.String
            String args = Arrays.stream(behav.getParameterTypes())
                    .map(CtClass::getSimpleName)
                    .reduce((x, y) -> String.join(", ", x, y))
                    .orElse("");

            insertBefore(
                    a,
                    AllocationIntrospection.class.getName() +
                            ".getInstance().log(" +
                            "\"" + clazz + "\", " +
                            "\"" + method + "\", " +
                            "\"" + args + "\", " +
                            "\"" + instance + IntStream.range(0, dim).mapToObj(i -> "[]").reduce(String::join) + "\", " +
                            "" + bci + ", " +
                            "" + line +
                            ");"
            );
        } catch (RuntimeException ex) {
            System.err.println(ex.getMessage());
        } catch (NotFoundException e1) {
            e1.printStackTrace();
        }
    }

    private void insertBefore(Expr e, String expr) throws CannotCompileException {
        e.replace("{" + expr + "$_ = $proceed($$);}");
    }
}