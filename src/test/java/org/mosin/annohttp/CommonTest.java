package org.mosin.annohttp;

public class CommonTest {
    public static void main(String[] args) {


        long start2 = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
        }
        long end2 = System.nanoTime();
        System.out.println("Cost: " + (end2 - start2));

        long start = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            Thread.onSpinWait();
        }
        long end = System.nanoTime();
        System.out.println("Cost: " + (end - start));
    }
}
