package org.mosin.annohttp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mosin.annohttp.annotation.Body;
import org.mosin.annohttp.annotation.ContentTypeApplicationJson;
import org.mosin.annohttp.annotation.Header;
import org.mosin.annohttp.annotation.Request;
import org.mosin.annohttp.http.AnnoHttpClient;
import org.mosin.annohttp.http.HttpMethod;
import org.mosin.annohttp.http.PreparingRequest;

public class TestRun {

    @Test
    void run() throws IOException {
        R r = AnnoHttpClient.create(R.class);
//        System.out.println(r.test4("application/json", Map.of("id", 21, "name", "mara", "age", 31, "score", 59)));
        System.out.println(r.test5(Map.of("id", 21, "name", "mara", "age", 31, "score", 59)));
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
        PreparingRequest<Map<String, Object>> test1();

        @Request(method = HttpMethod.POST,
                url = "http://localhost:8080/test",
                bodyString = "{\"id\": 2, \"name\": \"mara\", \"age\": 31, \"score\": 59}"
        )
        @ContentTypeApplicationJson
        Map<String, Object> test2();

        @Request(method = HttpMethod.POST,
                url = "http://localhost:8080/test",
                bodyString = "{\"id\": 2, \"name\": \"mara\", \"age\": 31, \"score\": 59}"
        )
        @ContentTypeApplicationJson
        InputStream test3();
        
        @Request(method = HttpMethod.POST,
                url = "http://localhost:8080/test"
        )
        Map<String, Object> test4(@Header("Content-Type") String contentType, @Body Map<String, Object> body);
        
        @Request(method = HttpMethod.POST,
                url = "http://localhost:8080/test",
                proxy = "T(org.mosin.annohttp.http.proxy.RequestProxy).create('localhost', 8090, T(org.mosin.annohttp.http.proxy.RequestProxy.ProxyType).HTTP, false, T(org.mosin.annohttp" +
                        ".http.proxy" +
                        ".RequestProxy.ProxyCredentialType).NONE, null, null, null, null)"
        )
        Map<String, Object> test5(@Body Map<String, Object> body);
    }
}
