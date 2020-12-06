[**English**](./README-en.md)

# HTTP接口调用框架

**让 HTTP 接口调用跟调用本地方法一样自然优雅**

将 HTTP 请求和接口绑定，然后由框架生成接口的代理类，直接调用接口的方法就会自动构建请求参数并发送请求，然后处理请求响应转换为接口方法的返回值返回（**支持泛型**）。

若与 **Spring 集成（可选）**，更能使用 @Autowired 进行自动注入接口的代理实现。

# 特色

1. 像 MyBatis 一样，只写接口，由框架提供实现
2. 轻量级，不要求依赖Spring，只使用少量注解
3. 支持上传和下载文件
4. 若使用 Spring ，则可以使用 Autowired 自动注入接口的实现
5. 完善的文档用例和单元测试
6. 支持 Mock
7. JDK6+（注：1.2.0版本后支持JDK6，之前的版本必须JDK8）

# 技术栈
 
* 动态代理
* 反射
* 注解
* 自动包扫描

# 快速开始
 
## 一、添加maven依赖

```xml
 <dependency>
    <groupId>com.github.dadiyang</groupId>
    <artifactId>http-api-invoker</artifactId>
    <version>1.2.4</version>
 </dependency>
<!-- fastjson 或 gson 二选一即可 -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.75</version>
</dependency>
```

## 二、定义接口

假设有一个 GET 请求 `http://localhost:8080/city/allCities` 会响应: 

```json
[
    {
        "id": 1,
        "name": "beijing"
    },
    {
        "id": 2,
        "name": "shanghai"
    }
]
```

我们定义一个接口来调用这个请求: 

```java
@HttpApi
public interface CityService {
    @HttpReq("http://localhost:8080/city/allCities")
    List<City> getAllCities();
}
```

注：
  * 如果有参数，需要添加 `@Param("参数名")`
  * 这里只展示最简单的使用方法，完整功能的示例可查看[单元测试中的CityService接口](./src/test/java/com/github/dadiyang/httpinvoker/interfaces/CityService.java)

## 三、获取代理

获取代理有两种方式，一种是直接通过工厂方法获取，一种是集成Spring通过 @Autowired 注入

### HttpApiProxyFactory

通过调用 `HttpApiProxyFactory.getProxy` 方法获取，如：

```java
CityService cityService = HttpApiProxyFactory.getProxy(CityService.class);
List<City> cities = cityService.getAllCities()
System.out.println(cities);
```

### Spring 集成

#### 配置开启 HTTP API 扫描

只需添加`@HttpApiScan`到任意一个 `@Configuration` 的类上即可：

```java
@Configuration
@HttpApiScan
public class TestApplication {
}
```
**注**：加上 @HttpApiScan 后会自动扫描这个 Configuration 类所在的包及其子包中所有带有 @HttpApi 注解的接口并生成代理类注册到Spring容器中。你也可以通过设置 `@HttpApiScan` 中的 value 值来指定要扫描的包。

#### @Autowired 注入接口代理

```java
@Autowired
private CityService cityService;

public void test() {
    List<City> cities = cityService.getAllCities();
    System.out.println(cities);
}
```

**注**：因为是动态代理生成并注册到Spring容器中的，所以IDE可能会警告 "Could not autowired. no beans of type 'xxx' type found." 忽略即可。

## 四、占位符

在 `@HttpApi` 注解的 prefix 和 `@HttpReq` 注解的 url 中都支持配置和路径参数占位符

* 配置占位符：${}，如 ${api.url.city}
* 路径参数占位符：{}，如 {cityId}
* 保留到请求参数中的路径参数占位符：#{}，如 #{cityId}
* 可以通过 : 分隔来指定默认值，如 ${api.url.city:北京} {cityId:1} #{cityId:}

配置占位符中的配置项将会从以下几个来源中获取：

* 在 `@HttpApiScan` 中设置的 **configPaths** 对应的配置文件中
* **系统配置**，即 System.getProperty("property")
* 与Spring集成时，也会从**Spring Environment**中获取

## 五、重试策略

当调用接口失败时，可能是网络不通或者接口返回的状态码不是2xx时，我们可能需要重试几次。这种情况下，我们可以使用`@RetryPolicy`注解。这个注解可以打在类和方法上，方法上的策略优先于类上的。支持的参数如下：

* times 尝试调用次数，默认 3 次
* retryFor 当发生该异常时才重试，默认只在 IOException 时触发重试
* retryForStatus 当服务器返回的状态码为某一类型时触发，默认只要服务器返回非 20x 的状态都进行重试
* fixedBackOffPeriod 退避策略，当需要进行重试时休眠的秒数，默认不休眠

## 六、扩展

### 请求前置处理器

有些情况下，我们需要给所有的请求添加一个请求头、Cookie或者固定的参数，这时候如果我们在接口里添加这些参数会很冗余

此时，我们可以实现 RequestPreprocessor 接口，并在初始化代理工厂时使用该接口，此时所有的请求都会通过这个接口进行预处理

我们可以在框架发送请求之前对请求体做任何修改

```java
public void preprocessorTest() {
    HttpApiProxyFactory factory = new HttpApiProxyFactory(request -> {
        // 我们为所有的请求都加上 cookie 和 header
        request.addCookie("authCookies", authKey);
        request.addHeader("authHeaders", authKey);
        // 可以通过 CURRENT_METHOD_THREAD_LOCAL 获取到当前的被代理的方法
        Method method = CURRENT_METHOD_THREAD_LOCAL.get();
    });
    CityService cityService = factory.getProxy(CityService.class);
    City city = cityService.getCity(id);
}
```

### 响应处理器

接管响应结果的处理逻辑。通过实现 `ResponseProcessor` 接口并在初始化代理工厂时使用，可以拿到响应结果，并根据自己的需求对响应结果进行反序列化等操作

```java
ResponseProcessor cityResultProcessor = (response, method) -> {
    ResultBean<City> cityResultBean = JSON.parseObject(response.getBody(), 
            new TypeReference<ResultBean<City>>() {
    });
    return cityResultBean.getData();
};
HttpApiProxyFactory factory = new HttpApiProxyFactory(cityResultProcessor);
CityService cityServiceWithResponseProcessor = factory.getProxy(CityService.class);
City city = cityServiceWithResponseProcessor.getCity(id);
```

整合 Spring 时，可以使用 @Import(ResultBeanResponseProcessor.class) 加入全局的结果处理器

可以通过 @NotResultBean 注解声明此接口无需 ResultBeanResponseProcessor 来处理

实践中，很多 HTTP 接口通常使用具有 code、msg或message、data 三个字段的类作为返回值，即 ResultBean 的形式: 

```json
{
    "code": 0, 
    "data": {
        "name": "Hello"
    },
    "msg或message": "xx"
}
```

因此，我们提供了 ResultBeanResponseProcessor 来处理这种请求。

配合 @ExpectedCode 注解，来表明期望的 code 值，默认为 0。

若响应体返回的 code 值与期望值：

* 不等时抛出 UnexpectedResultException 异常，异常信息为 message 的内容
* 相等时解析 data 中的内容

### JSON序列化器

本项目JSON序列化/反序列化使用 FastJson，但是出现一些用户反馈由于公司规定等客观原因，他们无法引入 FastJson 的情况，所以我们对 序列化器 进行了解耦，**默认仍然采用 FastJson 实现**，如果有特殊需求，可以通过以下方法指定具体的实现：

```java
JsonSerializerDecider.registerJsonSerializer("Gson", GsonJsonSerializer.getInstance());
JsonSerializerDecider.setJsonInstanceKey("Gson");
```

我们提供了 FastJson 和 Gson 两种实现，并通过个性化的配置使这两种实现的行为最大限度地保持一致，以让更换 JSON 实现不会影响到原有的代码。若你需要其他的实现，可以通过实现 `com.github.dadiyang.httpinvoker.serializer.JsonSerializer` 接口编写自己的实现，然后根据上面的 方式更换为自己的实现。

## 七、文件上传

只要方法参数是 MultiPart 
示例：

```java
/**
 * 提交 multipart/form-data 表单，实现多文件上传
 *
 * @param multiPart 表单
 */
@HttpReq(value = "/files/upload", method = "POST")
String multiPartForm(MultiPart multiPart);
```

调用示例：

```java
String fileName1 = "conf.properties";
String fileName2 = "conf2.properties";
try (InputStream in1 = new FileInputStream(fileName1);
     InputStream in2 = new FileInputStream(fileName2);) {
    // 支持一个或多个文件
    MultiPart.Part part1 = new MultiPart.Part("conf1", fileName1, in1);
    MultiPart.Part part2 = new MultiPart.Part("conf2", fileName2, in2);
    MultiPart multiPart = new MultiPart();
    multiPart.addPart(part1);
    multiPart.addPart(part2);
    cityService.multiPartForm(multiPart);
} catch (IOException e) {
    e.printStackTrace();
}
```

## 八、Mock

在实际开发过程中，我们依赖的接口可能由于尚未开发完成、服务不稳定或者没有需要的测试数据等原因，导致我们在**开发过程中浪费掉很多时间**

如果服务接口能在我们开发调试的时候**随心所欲地返回我们设定好的响应**，等到开发完再进行真实接口的联调，就会**大大提高我们的开发效率**

因此，本项目提供 Mock 的功能，**根据给定的规则匹配请求，若与给定的规则能匹配上，则使用给定的 Response 做为请求响应直接返回，没有匹配到则发送真实请求**

其原理就是实现 Requestor 接口以**接管发送请求的方法**

### 直接使用
```java
// 生成 MockRequestor 对象
MockRequestor requestor = new MockRequestor();
HttpApiProxyFactory factory = new HttpApiProxyFactory.Builder()
        // 注册到 代理工厂 中，以接管请求发送过程
        .setRequestor(requestor)
        // 配置文件
        .addProperties(in)
        .build();
// 获取代理对象
CityService cityService = factory.getProxy(CityService.class);
Map<String, String> params = new HashMap<>();
params.put("id", 1);
// 返回模拟的请求响应：MockResponse
MockResponse response = new MockResponse(200, "北京");
// 添加匹配规则
MockRule rule = new MockRule("http://localhost:18888/city/getCityName", params, response);
requestor.addRule(rule);
String name = cityService.getCityName(1);
System.out.println(name);
```

### 整合Spring

在有 @Configuration 注解的类中添加方法：

* 注意，**千万不要在生产环境中使用**，可以通过 @Profile 或者 @Conditional 注解来有条件地添加，或者只在单元测试中使用

```java
    @Bean
    // 注意，千万不要在生产环境中使用，可以使用 @Profile("dev") 注解声明只在开发环境中自动扫包
    @Profile("dev")
    public MockRequestor requestor() {
        MockRequestor requestor = new MockRequestor();
        MockRule rule = ...
        // 添加 mock 规则
        requestor.addRule(rule);
        return requestor;
    }
```

Mock请求器会在每次发送请求的时候打印警告

# 核心注解

## @HttpApiScan

启动包扫描，类似@ComponentScan。
* value属性设定扫包的 basePackage，如果没有设置则使用被标注的类所在的包为基包
* configPaths属性指定配置文件

## @HttpApi

标注一个类是与Http接口绑定的，需要被包扫描的接口。类似Spring中的@Component注解

## @HttpReq

标注方法对应的url

## @Param

value: 指定方法参数名对应的请求参数名称
isBody: 指定是否将该参数的所有字段都做为单独的参数
这两个参数不能同时为空

## @Headers

指定方法参数为 Headers，目前只允许打在类型为 `Map<String, String>` 的参数上，否则会抛出 `IllegalArgumentException`

打在方法上和类上则 keys 和 values 来指定请求头，keys 和 values 数组元素必须一一对应

## @Cookies

指定方法参数为 Cookies，目前只允许打在类型为 `Map<String, String>` 的参数上，否则会抛出 `IllegalArgumentException`

打在方法上和类上则 keys 和 values 来指定 cookie，keys 和 values 数组元素必须一一对应

## @Form

指定方法或类中的所有方法都为 Form 表单形式提交，即 Content-Type 为 application/x-www-form-urlencoded

## @RetryPolicy 重试策略

重试策略。可以打在类和方法上，方法上的策略优先于类上的。

* times 尝试调用次数，默认 3 次
* retryFor 当发生该异常时才重试，默认只在 IOException 时触发重试
* retryForStatus 当服务器返回的状态码为某一类型时触发，默认只要服务器返回非 20x 的状态都进行重试
* fixedBackOffPeriod 退避策略，当需要进行重试时休眠的秒数，默认不休眠