package test;

public class Fib {
	
	public static void main(String[] args) {
		foo(new Object(), "Hello", 'C', false, (byte) 1, (short) 2, 3, 4L, 5F, 6D, new boolean[] { false, true, false });
		System.out.println(fib(20));
	}

	private static int fib(int n) {
		if (n <= 1) {
			return n;
		} else {
			return fib(n - 1) + fib(n - 2);
		}
	}
	
	private static void foo(Object obj, String str, char c, boolean z, byte b, short s, int i, long j, float f, double d, boolean[] array) {
		
	}
	
}











