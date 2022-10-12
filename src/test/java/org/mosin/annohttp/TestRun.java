package org.mosin.annohttp;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.junit.jupiter.api.*;
import org.mosin.annohttp.annotation.Body;
import org.mosin.annohttp.annotation.ContentTypeTextPlain;
import org.mosin.annohttp.annotation.Request;
import org.mosin.annohttp.http.AnnoHttpClient;
import org.mosin.annohttp.http.HttpMethod;
import org.mosin.annohttp.http.PreparingRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TestRun {

    static HttpServer httpServer;

    @BeforeAll
    static void beforeAll() {

        /*
         * 新建一个HTTP服务，请求什么就响应什么，方便测试
         */

        final var vertx = Vertx.vertx();
        final var router = Router.router(vertx);
        router.route("/test").handler(rctx -> {
            var request = rctx.request();
            var requestHeaders = request.headers();
            var requestMethod = request.method();
            var requestParam = request.params();
            var response = rctx.response();
            requestHeaders.forEach(entry -> response.headers().add(entry.getKey(), entry.getValue()));
            response.putHeader("Request-Method", requestMethod.name());
            requestParam.forEach(entry -> response.putHeader("Request-Param-" + entry.getKey(), entry.getValue()));
            request.body(r -> response.end(r.result()));
        });
        httpServer = vertx.createHttpServer()
                .requestHandler(router)
                .listen(8081).onSuccess(r -> System.out.println("已开启HTTP服务：" + r.actualPort())).result();
    }

    @AfterAll
    static void afterAll() {
        if (httpServer != null) {
            httpServer.close(result -> System.out.println("已关闭HTTP服务器"));
        }
    }

    @Test
    @DisplayName("普通测试 -- 默认GET方式，默认JSON请求类型，默认UTF-8，返回JSON字符串")
    void baseTest() {
        interface Client {
            @Request(url = "http://localhost:8081/test")
            String baseRequest(@Body String jsonBody);
        }

        Client c = AnnoHttpClient.create(Client.class);

        String req =  """
                {
                    "Name": "Mara"
                }
                """;

        String resp = c.baseRequest(req);

        // 请求体 = 响应体
        Assertions.assertEquals(req, resp);
    }

    @Test
    @DisplayName("普通测试 -- 默认GET方式，自定义ContentType")
    void baseTest2() {
        interface Client {
            @Request(url = "http://localhost:8081/test")
            @ContentTypeTextPlain
            HttpResponse baseRequest(@Body String jsonBody);
        }

        Client c = AnnoHttpClient.create(Client.class);

        String req =  """
                {
                    "Name": "Mara"
                }
                """;

        HttpResponse resp = c.baseRequest(req);

        // 请求体 = 响应体
        try {
            Assertions.assertEquals(req, IOUtils.toString(resp.getEntity().getContent(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 请求头Content-Type = PainText
        org.apache.http.Header[] headers = resp.getHeaders("Content-Type");
        Assertions.assertEquals(1, headers.length);
        Assertions.assertEquals("text/plain; charset=ISO-8859-1", headers[0].getValue());
    }

    @Test
    @DisplayName("普通测试 -- 默认GET方式，直接以JSON转换到Bean")
    void baseTest3() {

        record Bean(String name) {

        }

        interface Client {
            @Request(url = "http://localhost:8081/test")
            Bean baseRequest(@Body String jsonBody);
        }

        Client c = AnnoHttpClient.create(Client.class);

        String req =  """
                {
                    "name": "Mara"
                }
                """;

        Bean resp = c.baseRequest(req);

        Assertions.assertEquals("Mara", resp.name);
    }

    @Test
    @DisplayName("普通测试 -- 默认GET方式，间接以JSON转换到Bean")
    void baseTest4() {

        record Bean(String name) {

        }

        interface Client {
            @Request(url = "http://localhost:8081/test")
            PreparingRequest<Bean> baseRequest(@Body String jsonBody);
        }

        Client c = AnnoHttpClient.create(Client.class);

        String s =  """
                {
                    "name": "Mara"
                }
                """;

        PreparingRequest<Bean> req = c.baseRequest(s);
        try (var operableHttpResponse = req.requestOperable()) {
            Assertions.assertEquals("Mara", operableHttpResponse.asJsonConvertible().toBean(Bean.class).name);
        }
    }

    @Test
    @DisplayName("普通测试 -- 默认GET方式，直接转换为Map")
    void baseTest5() {

        interface Client {
            @Request(url = "http://localhost:8081/test")
            Map<String, Object> baseRequest(@Body String jsonBody);
        }

        Client c = AnnoHttpClient.create(Client.class);

        String s =  """
                {
                    "name": "Mara"
                }
                """;

        Map<String, Object> map = c.baseRequest(s);

        Assertions.assertEquals("Mara", map.get("name"));
    }

    @Test
    @DisplayName("普通测试 -- 默认GET方式，自定义请求头，获得所有响应头")
    void baseTest6() {

        interface Client {
            @Request(url = "http://localhost:8081/test", headers = {"Mara: 1", "Mara: 2"})
            Header[] baseRequest(@Body String jsonBody);
        }

        Client c = AnnoHttpClient.create(Client.class);

        String s =  """
                {
                    "name": "Mara"
                }
                """;

        Header[] headers = c.baseRequest(s);

        List<Header> headerList = Arrays.stream(headers).filter(e -> e.getName().equals("Mara")).toList();

        Assertions.assertEquals(2, headerList.size());
    }

    @Test
    @DisplayName("普通测试 -- 默认GET方式，不附加Body，期望不附加ContentType")
    void baseTest7() {

        interface Client {
            @Request(url = "http://localhost:8081/test", headers = {"Mara: 1", "Mara: 2"})
            Header[] baseRequest();
        }

        Client c = AnnoHttpClient.create(Client.class);

        Header[] headers = c.baseRequest();

        List<Header> headerList = Arrays.stream(headers).filter(e -> e.getName().equals("Content-Type")).toList();

        Assertions.assertEquals(0, headerList.size());
    }

    @Test
    @DisplayName("普通测试 -- 默认GET方式，只获取StatusLine")
    void baseTest8() {

        interface Client {
            @Request(url = "http://localhost:8081/test", successCondition = "true")
            StatusLine baseRequest();
        }

        Client c = AnnoHttpClient.create(Client.class);

        StatusLine statusLine = c.baseRequest();

        Assertions.assertEquals(200, statusLine.getStatusCode());
    }

    @Test
    @DisplayName("普通测试 -- 默认GET方式，通过PreparingRequest获取StatusLine")
    void baseTest9() {

        interface Client {
            @Request(url = "http://localhost:8081/test", successCondition = "")
            PreparingRequest<StatusLine> baseRequest();
            // 不推荐将StatusLine、Header[]等特殊类型放入PreparingRequest，因为PreparingRequest是专门处理响应体的
        }

        Client c = AnnoHttpClient.create(Client.class);
        PreparingRequest<StatusLine> preparingRequest = c.baseRequest();

        HttpResponse httpResponse = preparingRequest.requestClassically();
        Assertions.assertEquals(200, httpResponse.getStatusLine().getStatusCode());

        StatusLine statusLine = preparingRequest.request();
        Assertions.assertEquals(200, statusLine.getStatusCode());

        // 断言抛出错误，因为无法将响应体转换为StatusLine
        try (final var o = preparingRequest.requestOperable()) {
            Assertions.assertThrows(Exception.class, () -> o.asJavaSerializedSequenceToObject(StatusLine.class));
        }
    }

    @Test
    @DisplayName("普通测试 -- 默认GET方式，附加baseUrl")
    void baseTest10() {

        interface Client {
            @Request(url = "/test", successCondition = "true")
            StatusLine baseRequest();
        }

        Client c = AnnoHttpClient.create(Client.class, "http://localhost:8081/");
        StatusLine statusLine = c.baseRequest();
        Assertions.assertEquals(200, statusLine.getStatusCode());

        Client c2 = AnnoHttpClient.create(Client.class, (metadata -> {
            if (metadata.getRequestAnnotation().method() == HttpMethod.GET) {
                return "http://localhost:8081/";
            } else {
                return "http://localhost:9081/";
            }
        }));
        StatusLine statusLine2 = c2.baseRequest();
        Assertions.assertEquals(200, statusLine2.getStatusCode());
    }
}
