package agent;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/*
 * test.Fib::fib
 * 
 * > test.Fib::fib(2)
 * > test.Fib::fib(1)
 * < test.Fib::fib(1) -> 1
 * > test.Fib::fib(0)
 * < test.Fib::fib(0) -> 0
 * < test.Fib::fib(2) -> 1
 */
public class MyHighLevelTransformer implements ClassFileTransformer {

	private final String classname;
	private final String methodname;

	public MyHighLevelTransformer(String classname, String methodname) {
		this.classname = classname;
		this.methodname = methodname;
	}

	@Override
	public byte[] transform(ClassLoader loader, String name, Class<?> ___, ProtectionDomain __, byte[] classfile) throws IllegalClassFormatException {
		if (!name.equals(classname)) {
			return null;
		}
		
		try {
			ClassPool pool = ClassPool.getDefault(); //TODO
			CtClass clazz = pool.makeClass(new ByteArrayInputStream(classfile));
			
			for(CtMethod method : clazz.getDeclaredMethods(methodname)) {
				transform(method);
			}
			
			byte[] data = clazz.toBytecode();
			try (FileOutputStream out = new FileOutputStream(classname.replace('/', '.') + ".class")) {
				out.write(data);
			}
			return data;
		} catch (IOException | RuntimeException | CannotCompileException | NotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * test.Fib::fib
	 * 
	 * > test.Fib::fib(2)
	 * > test.Fib::fib(1)
	 * < test.Fib::fib(1) -> 1
	 * > test.Fib::fib(0)
	 * < test.Fib::fib(0) -> 0
	 * < test.Fib::fib(2) -> 1
	 */
	private void transform(CtMethod method) throws CannotCompileException {
		method.addLocalVariable("___time___", CtClass.longType);
		method.insertBefore(
			LoggerIntrospection.class.getName() + ".logEnter($args, \"" + classname + "\", \"" + methodname + "\");"
			+ "___time___ = System.currentTimeMillis();"
		);
		method.insertAfter(
			"___time___ = System.currentTimeMillis() - ___time___;"
			+ LoggerIntrospection.class.getName() + ".logExit(($w) $_, $args, \"" + classname + "\", \"" + methodname + "\", ___time___);"
		);
		//TODO handle exceptions
	}

}













