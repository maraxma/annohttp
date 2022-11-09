package org.mosin.annohttp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mosin.annohttp.httpservice.TestClient;
import org.mosin.annohttp.httpservice.TestClientWithoutAnno;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SpringTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
public class CombineWithSpringTest {

    @Autowired(required = false)
    private TestClient testClient;

    @Autowired(required = false)
    private TestClientWithoutAnno testClientWithoutAnno;

    @Test
    void scanTest() {
        Assertions.assertNotEquals(null, testClient);
        Assertions.assertEquals(null, testClientWithoutAnno);
    }

}
