package agent;

public class LoggerIntrospection {
	
	public static void logTest(String classname, String methodname) {
		System.err.println(classname + " " + methodname);
	}

	public static void logEnter(Object[] args, String classname, String methodname) {
		System.err.println(" > " + classname + "::" + methodname
				+ "(" + getArgumentString(args) + ")");
	}

	public static void logExit(Object returnValue, Object[] args, String classname, String methodname, long time) {
		System.err.println(" < " + classname + "::" + methodname
				+ "(" + getArgumentString(args) + ") -> " + returnValue + "(" + time + "ms)");
	}

	private static String getArgumentString(Object[] args) {
		StringBuilder string = new StringBuilder();
		for(int i = 0; i < args.length; i++) {
			if(i > 0) {
				string.append(", ");
			}
			string.append(args[i]);
		}
		return string.toString();
	}
	
}
