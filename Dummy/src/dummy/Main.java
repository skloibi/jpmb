package dummy;

import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {
        System.out.println("START OF MAIN");

        Main m = new Main();

        IntStream.range(0, 100).forEach(i -> m.test());
        System.out.println("END OF MAIN");
    }

    void test() {
        String a = "test";
        Object o = new Object();

        Object[] arr  = new Object[10];
        Object[] list = new String[11];

        String[][] strs = new String[1][1];

        int[]   ints  = new int[9];
        int[][] ints2 = new int[9][10];
    }
}
