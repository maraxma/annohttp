# annohttp

annohttp 全称是 Annotation HTTP，是一个靠注解驱动的HTTP客户端，类似于retrofit，但基于HttpClient而非OKHttp，
并且提供了更多的更灵活的操作。

## 基本请求

## 为单独的请求附加代理

## 配合 spring-boot-starter-annohttp 实现自动装配

## 结合 SPEL 实现更灵活的操作

**此功能需需要 spring-expression 的支持，你需要引入依赖包**。

## 自定义协议

你可以使用自定义协议来定义你的URI，比如请求“myhttp://xxx”。当annohttp遇到这类协议的时候，
将会寻找合适的 ProtocolHandler 来处理。 本质上自定义的协议将会回归到HTTP请求中来。