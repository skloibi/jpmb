package dummy;

public class Main {
    public static void main(String[] args) {
        System.out.println("START MAIN");
        Main m = new Main();
        for (int i = 0; i < 2000; i++) {
            m.test();
        }
        System.out.println("END MAIN");
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
