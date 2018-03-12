package agent;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class Agent {

	// test.Fib::fib
	public static void premain(String args, Instrumentation instrumentation) throws UnmodifiableClassException {
		String classname = args.substring(0, args.indexOf("::")).replace('.', '/');
		String methodname = args.substring(args.indexOf("::") + 2);
		
		//instrumentation.addTransformer(new MyHighLevelTransformer(classname, methodname), true);
		instrumentation.addTransformer(new MyLowLevelTransformer(classname, methodname), true);
		Class<?>[] classes = instrumentation.getAllLoadedClasses();

		for(Class<?> clazz : classes) {
			if(!instrumentation.isModifiableClass(clazz)) {
				continue;
			}
			instrumentation.retransformClasses(clazz);
		}		
	}
	
}
