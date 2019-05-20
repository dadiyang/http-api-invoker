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
    <version>1.1.6</version>
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

注：这里只展示最简单的使用方法，完整功能的示例可查看[单元测试中的CityService接口](./src/test/java/com/github/dadiyang/httpinvoker/interfaces/CityService.java)

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

* 不等时抛出异常，异常信息为 message 的内容
* 相等时解析 data 中的内容

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

## @Cookies

指定方法参数为 Cookies，目前只允许打在类型为 `Map<String, String>` 的参数上，否则会抛出 `IllegalArgumentException`

## @Form

指定方法或类中的所有方法都为 Form 表单形式提交，即 Content-Type 为 application/x-www-form-urlencoded

## @RetryPolicy 重试策略

重试策略。可以打在类和方法上，方法上的策略优先于类上的。

* times 尝试调用次数，默认 3 次
* retryFor 当发生该异常时才重试，默认只在 IOException 时触发重试
* retryForStatus 当服务器返回的状态码为某一类型时触发，默认只要服务器返回非 20x 的状态都进行重试
* fixedBackOffPeriod 退避策略，当需要进行重试时休眠的秒数，默认不休眠