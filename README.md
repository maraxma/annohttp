# annohttp

annohttp 全称是 Annotation HTTP，是一个靠注解驱动的HTTP客户端，类似于retrofit，但基于HttpClient而非OKHttp，
并且提供了更多的更灵活的操作。

annohttp采用全注解的方式驱动，在常规模式下无需其他任何额外的代码。



## 基本请求

```java
// ItemService.java
public interface ItemService {
    @Request(url = "http://yourhost:8080/item/get/{id}")
    ItemInfo getItemInfo(@PathVar("id") String id, @Headers Map<String, String> headers);
}

// main
public static void main(String[] args]) {
    ItemService itemService = AnnoHttpClients.create(ItemService.class);
    ItemInfo itemInfo = itemService.getItemInfo("99", Map.of("JWT", "xxxxxx"));
}
```



## 灵活请求

灵活请求实在基本请求的基础上，将返回值使用PreparingRequest包裹。此时，你在完成此方法调用后会得到PreparingRequest对象，该对象提供了很多灵活的请求方式，这当中也包含了对返回体的灵活处理。

注意：使用PreparingRequest包裹意味着请求操作会被滞后，真正发起请求需要调用PreparingRequest.request()方法。

```java
// ItemService.java
public interface ItemService {
    @Request(url = "http://yourhost:8080/item/get/{id}")
    PreparingRequest<ItemInfo> getItemInfo(@PathVar("id") String id, @Headers Map<String, String> headers);
}

// main
public static void main(String[] args]) {
    ItemService itemService = AnnoHttpClients.create(ItemService.class);
    PreparingRequest<ItemInfo> itemInfoReq = itemService.getItemInfo("99", Map.of("JWT", "xxxxxx"));
    // 常规请求
    ItemInfo itemInfo = itemInfoReq.request();
    // 异步请求
    ItemInfo itemInfo2 = itemInfoReq.requestAsync(new MyExecutor());
    // 经典请求（返回HttpResponse对象，适合于需要自行处理响应的情况）
    HttpResponse response = itemInfoReq.requestClassically();
    // 在请求前修改请求头或请求参数(XXX代表具体的内容，参见源码)
    itemInfoReq.customXXX();
    // 将请求后的返回视为可操作性对象（可操作性对象提供了很多便捷方法，参见源码）
    itemInfoReq.requestOperable();
}
```



## 只关注输入流的请求

只关注输入流的请求只需要将返回类型改为InputStream即可。

```
// ItemService.java
public interface ItemService {
    @Request(url = "http://yourhost:8080/item/get/{id}")
    InputStream getItemInfo(@PathVar("id") String id, @Headers Map<String, String> headers);
}
```



## 特别章节：OperableHttpResponse对象

OperableHttpResponse对象由PreparingRequest.requestOperable()方法生成，它提供了对响应体的灵活处理。这些处理包括：

- 视响应体为字符串
- 视响应体为JSON
- 视响应体为XML
- 视响应体为YAML
- 视响应体为InputStream
- 视响应体为Java序列化后的序列
- 视响应体为字节数组

OperableHttpResponse是HttpResponse的子类，包含HttpResponse提供的所有方法，因此，既可以获得响应体也可以获得响应中的其他部分（如响应头、状态行等）。
但是asXX()方法只针对响应体。

推荐使用PreparingRequest.requestOperable()方法来获得OperableHttpResponse响应对象，这样你几乎可以做到任何事情。

OperableHttpResponse对象实现了Closable接口，因此需要注意在使用完成后手动关闭OperableHttpResponse对象。推荐使用try-with-resources来书写。

annohttp可以让用户通过方法的选择直接处理如上的情况，将其转换为具体的Java对象。

```
// 获得JSON反序列化后的JavaBean（XML、YAML同理）
MyBean bean = itemService.getItemInfo().requestOperable().asJsonConvertible().toBean(MyBean.class);
// 获得Java序列化后的反序列化对象
MyBean bean = itemService.getItemInfo().requestOperable().asJavaSerializedSequenceToObject(MyBean.class);
// 直接获得字符串
String respString = itemService.getItemInfo().requestOperable().asSequenceToString("UTF-8");
// 直接获得字节数组
byte[] bytes = itemService.getItemInfo().requestOperable().asSequenceToBytes();

// ...更多方法请自行探索
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

import org.mosin.annohttp.annotation.PathVar;
import org.mosin.annohttp.annotation.Request;
import org.mosin.annohttp.http.AnnoHttpClients;

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
        AnnoHttpClients.registerProtocolHandlers(new HttpxProtocolHandler());
        ItemService itemService = AnnoHttpClients.create(ItemService.class);
        System.out.println(itemService.getPrice("SK0001"));
    }
}
```



## 自定义转换器

annohttp提供了两类的转换器接口：

- RequestBodyConverter：请求体转换器，专门负责转换请求体（如将Map转换为urlencoded形式的请求体）
- ResponseConverter：响应转换器，专门负责将响应转换为用户需要的样子（如只提取响应头，只提取StatusLine，将响应体转换为Object等）

需要注意的是ResponseBodyConverter是ResponseConverter的子类，只负责响应体的转换。

annohttp内置的各种转换器足以应付大部分开发需求。

但是，我们可以自行实现上述的接口以扩展我们的处理范围或实现其他的需求。

且，内置的转换器的优先级是低于用户自定义的转换器的。意即，当同时出现符合逻辑的转换器，将使用用户定义的转换器。

转换器在编写完毕后，使用如下的方式告诉annohttp：

```java
AnnoHttpClients.registerRequestBodyConverter(new MyRequestBodyConverter());
AnnoHttpClients.registerResponseBodyConverter(new MyResponseConverter());
```


## 注解一览表

可供使用的注解全部位于org.mosin.http.annotation包下。

| 名称     | 目标                             | 作用                           |
| -------- | ------------------------------- | ------------------------------ |
| @Request | 标注于接口的抽象方法上           | 声明这是一个HTTP请求方法       |
| @Body    | 标注于@Request修饰的方法的参数上 | 声明此参数是该HTTP请求的请求体 |
| ... 不想写了，请参见源码 |                                 |                                |



COPYRIGHT @ Mara.X.Ma 2022