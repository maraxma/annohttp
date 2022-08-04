# annohttp

annohttp 全称是 Annotation HTTP，是一个靠注解驱动的HTTP客户端，类似于retrofit，但基于HttpClient而非OKHttp，
并且提供了更多的更灵活的操作。

## 基本请求

```java
// ItemService.java
public interface ItemService {
    @Request(url = "http://yourhost:8080/item/get/{id}")
    ItemInfo getItemInfo(@PathVar("id") String id, @Headers Map<String, String> headers);
}

// main
public static void main(String[] args]) {
    ItemService itemService = AnnoHttpClient.create(ItemService.class);
    ItemInfo itemInfo = itemService.getItemInfo("99", Map.of("JWT", "xxxxxx"));
}
```

## 为单独的请求附加代理

```java
import org.mosin.annohttp.http.proxy.RequestProxy;

public interface ItemService {
    // 以注解方式添加代理（SPEL）
    @Request(url = "http://yourhost:8080/item/get/{id}", proxy = "T(org.mosin.annohttp.http.proxy.RequestProxy).create('localhost', 8090, T(org.mosin.annohttp.http.proxy.RequestProxy.ProxyType).HTTP, false, T(org.mosin.annohttp" +
            ".http.proxy.RequestProxy.ProxyCredentialType).NONE, null, null, null, null)")
    ItemInfo getItemInfo(@PathVar("id") String id, @Headers Map<String, String> headers);

    // 以参数方式添加代理
    @Request(url = "http://yourhost:8080/item/get/{id}")
    ItemInfo getItemInfoWithProxy(@PathVar("id") String id, @Headers Map<String, String> headers, @Proxy RequestProxy RequestProxy);
}
```

## 配合 spring-boot-starter-annohttp 实现自动装配
```yaml
# application.yml
annohttp:
  http-client:
    connect-timeout-in-seconds: 10 
    socket-timeout-in-seconds: 10
    read-timeout-in-seconds: 20
    connection-idle-timeout-in-seconds: 20
    connection-timeout-in-seconds: 20
    max-connections: 80
    max-connections-per-route: 40
    keep-alive: false
    keep-alive-time-in-seconds: 30
    flow-redirect: true
  service-base-packages: [com.xx.xx.http.service, com.xx.xx.http.request]
```
```java
// 注入，然后直接使用
@Autowired
private ItemService itemService;
```

## 结合 SPEL 实现更灵活的操作

**此功能需需要 spring-expression 的支持，你需要引入依赖包**。

@Request注解上很多参数都支持SPEL表达式，它们的名称都以“Spel”结尾，如bodySpel、headerSpel等。请参见Request的javadoc获得更多信息。

所有的SPEL表达式上下文中都提供了如下的信息，可以直接在SPEL表达式中取用：

- #arg{id}: 对应请求方法上的参数列表，{id}需要替换为整数，从0开始，如#arg0代表第一个参数

Requst.successCondition接受一个字符串，该字符串必须是SPEL并返回boolean，以确定请求成功的条件。特殊的是，在这个SPEL的上下文中额外提供了：

- #httpResponse：此次请求的响应对象，可以通过其获得各种信息，如响应码、HTTP版本、响应头等等

```java
// 
public interface ItemService {
    // 以注解方式添加代理（SPEL）
    @Request(url = "http://yourhost:8080/item/get/{id}", successCondition = "#httpResponse.statusLine.statusCode==201",
    bodySpel = "Map.of(\"name\": \"mara\")")
    ItemInfo getItemInfo(@PathVar("id") String id, @Headers Map<String, String> headers);
}
```

## 自定义协议

你可以使用自定义协议来定义你的URI，比如请求“myhttp://xxx”。当annohttp遇到这类协议的时候，
将会寻找合适的 ProtocolHandler 来处理。 本质上自定义的协议将会回归到HTTP请求中来。

```java
import org.mosin.annohttp.annotation.Method;
import org.mosin.annohttp.annotation.PathVar;
import org.mosin.annohttp.annotation.Request;
import org.mosin.annohttp.http.AnnoHttpClient;

/********************************
 * 
 * 如下的例子描述了一个自定义的协议“httpx”
 * 该协议需要通过路径中的服务名称从远端查询获得真正的地址，然后通过此地址附加ItemNumber查询商品的价格
 * 这只是个例子，实际情况需要灵活运用
 * 通过自定义的ProtocolHandler，你几乎可以实现任何基于HTTP的其他和公司具体流程相关的请求动作
 * 
 *******************************/


// HttpxProtocolHandler.java
public class HttpxProtocolHandler implements ProtocolHandler {
    @Override
    public String protocol() {
        return "httpx";
    }

    @Override
    public void handle(AnnoHttpClientMetadata metadata, PreparingRequest<?> preparingRequest) {
        preparingRequest.customRequestUrl(oldUrl -> {
            String serviceName = oldUrl.replace("httpx").split(":");
            String serviceUrl = serviceReg.getServiceUrl(serviceName);
            String itemNumber = (String) metadata.getRequestMethodArguments()[0];
            return new URIBuilder(serviceUrl).addParameter("itemNumber", itemNumber).toString();
        });
    }
}

// ItemService.java
public interface ItemService {

    @Request(url = "httpx://service:item:{itemNumber}")
    BigDecimal getPrice(@PathVar("itemNumber") String itemNumber);
}

// UserCode
public class Main {
    public static void main(String[] args) {
        AnnoHttpClient.registerProtocolHandlers(new HttpxProtocolHandler());
        ItemService itemService = AnnoHttpClient.create(ItemService.class);
        System.out.println(itemService.getPrice("SK0001"));
    }
}
```


COPYRIGHT @ Mara.X.Ma 2022