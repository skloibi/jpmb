package dummy;

import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {
        Main m = new Main();
        IntStream.range(0, 100).forEach(i -> m.test());
    }

    void test() {
        String a = new String("test");
        Object o = new Object();

        Object[] arr  = new Object[10];
        Object[] list = new String[11];

        String[][] strs = new String[1][1];

        int[]   ints  = new int[9];
        int[][] ints2 = new int[9][10];
    }
}
