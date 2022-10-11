package org.mosin.annohttp;

import org.junit.jupiter.api.Test;

public class MethodTest {
    @Test
    void methodTest() {
        Integer i1 = 127;
        Integer i2 = 127;
        System.out.println(i1 == i2);

        String s1 = new String("100");
        String s2 = "100";
        System.out.println(s1 == s2);
        System.out.println(s1.equals(s2));
        String s3 = s1.intern();
        System.out.println(s1 == s3);
        System.out.println(s2 == s3);
    }
}
