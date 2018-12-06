# http接口调用器

**让http接口调用跟调用本地方法一样自然优雅**

将http api和接口绑定，然后由框架生成接口的代理类，无需自己发送 http 请求，直接调用接口的方法就会自动构建请求参数并发送请求，然后处理请求响应转换为接口方法的返回值返回（**支持泛型**）。

若与 **Spring 集成（可选）**，更能使用 @Autowired 进行自动注入接口的代理实现。

# 特色

1. 像 MyBatis 一样，只写接口，由框架提供实现
2. 轻量级，不要求依赖Spring，只使用少量注解和
3. 若使用 Spring ，则可以使用 Autowired 自动注入接口的实现
4. 完善的文档用例和单元测试

# 原理
 
**技术：动态代理 + 反射 + 注解 + 自动包扫描**
 
* 使用@HttpReq **注解** 使接口方法与http服务地址绑定
* 使用 **动态代理**，生成绑定了http请求的接口的代理实现类
* 通过 **反射** 获取方法参数和返回值信息，根据这些信息处理请求
* 利用 **包扫描**，注入所有HttpApi注解标注的接口到Spring容器中
 
当调用接口的方法的时候框架会完成以下三个步骤
 
1. 将方法参数根据规则序列化为所需的请求参数，并且填充路径参数（如果有的话）
2. 发送请求（使用Jsoup发送）
3. 将请求响应序列化为方法的返回值（使用fastJson反序列化） 

# 使用
 
## 一、添加maven依赖

```xml
 <dependency>
    <groupId>com.github.dadiyang</groupId>
    <artifactId>http-api-invoker</artifactId>
    <version>1.0.2</version>
 </dependency>
```

## 二、定义接口
 
将请求url与接口方法绑定（支持路径参数和配置项）
 
示例：

```java
@HttpApi(prefix="${api.url.city}")
public interface CityService {
    /**
     * 使用URI，会自动添加prefix指定的前缀
     */
    @HttpReq("/city/allCities")
    List<City> getAllCities();
    /**
    * 使用Param注解指定方法参数对应的请求参数名称
    */
    @HttpReq("/city/getById")
    City getCity(@Param("id") int id);
    /**
    * 默认使用GET方法，可以通过method指定请求方式
    * 如果是集合类或数组的参数数据会直接当成请求体直接发送
    */
    @HttpReq(value = "/city/save", method = "POST")
    boolean saveCities(List<City> cities);
    /**
    * 使用完整的路径，不会添加前缀
    */
    @HttpReq(value = "http://localhost:8080/city/saveCity", method = "POST")
    boolean saveCity(@Param("id") String id, @Param("name") String name, @Param("wubaId") int wubaId);
    /**
     * 支持路径参数
     */
    @HttpReq("/city/getCityRest/{id}")
    City getCityRest(@Param("id") int id);
}
```
 
## 三、获取代理类
 
 你唯一需要做的就是定义上面的接口，然后在使用的时候就可以通过工厂类获取接口的代理实现类了。
 
 如果你使用Spring，那简单地配置一下就可以使用@Autowired注解直接注入你的接口实现了
 
### 工厂方法示例：
 
 使用 HttpApiProxyFactory.getProxy(接口.class) 获取接口代理类
 
 ```java
 Properties properties = new Properties();
 properties.load(getClass().getClassLoader().getResourceAsStream("conf.properties"));
 // properties 是可选的，若不提供则默认使用 System.getProperties() 提供的配置
 CityService service = new HttpApiProxyFactory(properties).getProxy(CityService.class);
 ```
  

### Spring 集成

#### 添加两个配置
 
 1. 注册 HttpApiConfigurer 配置器到Spring容器，有两种方式（任选一种即可）：
    
    1. 启用包扫描
    
        `@ComponentScan(basePackages = {"com.github.dadiyang.httpinvoker.spring"})`
    
    2. 通过@Bean注解方法
    
        ```java
            @Bean
            public HttpApiConfigurer httpApiConfigurer(){
                return new HttpApiConfigurer();
            }
        ```
 2. 像@ComponentScan注解一样，在有 @Configuration 注解的类上加上 @HttpApiScan 注解开启服务接口扫描
    
    注： configPaths是可选的，用于填充url中使用的配置占位符，如 ${api.url} 对应 api.url 配置项

    ```java
    @Configuration
    // 在包扫描中加上配置器所在的包
    @ComponentScan(basePackages = {"com.github.dadiyang.httpinvoker.spring"})
    // 启动服务接口扫描，configPaths是可选的，用于填充url中使用的配置项
    @HttpApiScan(configPaths = "classpath:conf.properties")
    public class TestApplication {
    //    或者使用@Bean注解的方法生成配置器
    //    @Bean
    //    public HttpApiConfigurer httpApiConfigurer() {
    //        return new HttpApiConfigurer();
    //    }
    }
    ```
 
#### 使用 @Autowired 注入

因为是动态代理生成并注册到Spring容器中的，所以IDE可能会警告 "Could not autowired. no beans of type 'xxx' type found." 忽略即可。

```java
@Autowired
private CityService cityService;
```

## 四、使用示例

可查看项目单元测试HttpApiInvokerTest和HttpApiInvokerSpringTest两个类

### Spring集成使用

```java
@Autowired
private CityService cityService;

public void test() {
    List<City> cities = cityService.getAllCities();
    for (City city : cities) {
        System.out.println(city);
    }
}

```

### 工厂方式使用

```java
public void getProxyTest() throws Exception {
     CityService service = new HttpApiProxyFactory().getProxy(CityService.class);
     List<City> cities = service.getAllCities();
     for (City city : cities) {
         System.out.println(city);
     }
}
```

# 核心注解

## @HttpReq

标注方法对应的url

## @Param

指定方法参数名对应的请求参数名称

## @HttpApi

标注一个类是与Http接口绑定的，需要被包扫描的接口。类似Spring中的@Component注解

## @HttpApiScan

启动包扫描，类似@ComponentScan。
* value属性设定扫包的 basePackage，如果没有设置则使用被标注的类所在的包为基包
* configPaths属性指定配置文件
