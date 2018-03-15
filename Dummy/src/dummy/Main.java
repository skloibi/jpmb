package dummy;

public class Main {
    public static void main(String[] args) {
        String a = "test";
        Object o = new Object();
        System.out.println("END OF MAIN");

        Object[] arr  = new Object[10];
        Object[] list = new String[11];

        String[][] strs = new String[1][1];

        int[]   ints  = new int[9];
        int[][] ints2 = new int[9][10];
        System.out.println("REAL END OF MAIN");
    }
}
