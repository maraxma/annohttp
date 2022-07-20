package org.mosin.annohttp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mosin.annohttp.annotation.ContentTypeApplicationJson;
import org.mosin.annohttp.annotation.Request;
import org.mosin.annohttp.http.AnnoHttpClient;
import org.mosin.annohttp.http.HttpMethod;
import org.mosin.annohttp.http.PreparingRequest;

public class TestRun {

    @Test
    void run() throws IOException {
        R r = AnnoHttpClient.create(R.class);
        System.out.println(r.getTime1().requestOperable().asSequenceToString());
        // System.out.println(r.getTime2());

        BufferedInputStream bufferedInputStream = new BufferedInputStream(r.getTime3());
        byte[] bytes = bufferedInputStream.readAllBytes();
        System.out.println(new String(bytes, "UTF-8"));

        // System.out.println(test());
    }

    int test() {
        try {
            int number = getNumber();
            return number;
        } finally {
            System.out.println(2);
        }
    }

    int getNumber() {
        return 1 + -99;
    }

    interface R {

        /**
         * 如果以 {@link PreparingRequest} 来接受返回，那么可以在后面再实施真正请求。
         *
         * @return
         */
        @Request(method = HttpMethod.POST,
                url = "http://localhost:8080/test",
                bodyString = "{\"id\": 2, \"name\": \"mara\", \"age\": 31, \"score\": 59}"
        )
        @ContentTypeApplicationJson
        PreparingRequest<Map<String, Object>> getTime1();

        @Request(method = HttpMethod.POST,
                url = "http://localhost:8080/test",
                bodyString = "{\"id\": 2, \"name\": \"mara\", \"age\": 31, \"score\": 59}"
        )
        @ContentTypeApplicationJson
        Map<String, Object> getTime2();

        @Request(method = HttpMethod.POST,
                url = "http://localhost:8080/test",
                bodyString = "{\"id\": 2, \"name\": \"mara\", \"age\": 31, \"score\": 59}"
        )
        @ContentTypeApplicationJson
        InputStream getTime3();
    }
}
