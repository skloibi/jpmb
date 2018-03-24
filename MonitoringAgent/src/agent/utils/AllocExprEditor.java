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

public class AllocExprEditor extends ExprEditor {

    @Override
    public void edit(NewExpr e) throws CannotCompileException {
        try {

            insertBefore(e, getStatement(e));
        } catch (RuntimeException | NotFoundException ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void edit(NewArray a) throws CannotCompileException {
        try {

            insertBefore(a, getStatement(a));
        } catch (RuntimeException | NotFoundException ex) {
            System.err.println("Received exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void insertBefore(Expr e, String expr) throws CannotCompileException {
        e.replace("{ " + expr + " $_ = $proceed($$); }");
    }

    private String getStatement(NewExpr e) throws NotFoundException {
        return getStatement(e, e.getClassName(), 0);
    }

    private String getStatement(NewArray a) throws NotFoundException {
        return getStatement(a, a.getComponentType().getName(), a.getDimension());
    }

    private String getStatement(Expr e, String instance, int dims) throws NotFoundException {
        int        bci    = e.indexOfBytecode();
        int        line   = e.getLineNumber();
        CtBehavior behav  = e.where();
        String     clazz  = behav.getDeclaringClass().getName(); // Foo
        String     method = behav.getName(); // bar
        String args = Arrays.stream(behav.getParameterTypes())
                .map(CtClass::getSimpleName)
                .reduce((a, b) -> String.join(", ", a, b))
                .orElse("");

        StringBuilder bracks = new StringBuilder();
        for (int i = 0; i < dims; i++) bracks.append("[]");

        return AllocationIntrospection.class.getName() +
                "#getInstance().log(" +
                "\"" + clazz + "::" + method + "(" + args + ")\"," +
                "\"" + instance + bracks + "\", " +
                "" + bci + ", " +
                "" + line +
                ");";
    }
}
