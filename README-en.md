# HTTP API INVOKER

**Make HTTP api invokes as natural and elegant as calling local methods.** 

Binding HTTP url to interface, the framework will generate a proxy class which send HTTP request and handle response when we call the interface's method. Generic is supported.

The only thing we need to do is defining the interface.

# FEATURE

1. Just define interface, and the framework provide the proxy implement like MyBatis.
2. Light-weigh.
3. Upload and download file is supported.
4. Autowired annotation is supported if integrate with Spring.
5. Well documented and unit tested.

# TECHNOLOGY STACK
 
* dynamic proxy
* reflection
* annotation
* auto package scanning

# GET STARTED
 
## I. Add maven dependency

```xml
 <dependency>
    <groupId>com.github.dadiyang</groupId>
    <artifactId>http-api-invoker</artifactId>
    <version>1.1.0</version>
 </dependency>
```

## II. Define interface

Provided `http://localhost:8080/city/allCities` response: 
```json
[
    {
        "id":1,
        "name":"beijing"
    },
    {
        "id":2,
        "name":"shanghai"
    }
]
```

Define an interface like this: 
```java
@HttpApi
public interface CityService {
    @HttpReq("http://localhost:8080/city/allCities")
    List<City> getAllCities();
}
```

## III. Get proxy
 
### HttpApiProxyFactory

Now we can get a proxy implement of the interface by calling

`CityService cityService = HttpApiProxyFactory.getProxy(CityService.class)`

And just use this instance to send a request to the HTTP api:

`List<City> cities = cityService.getAllCities()`

### Spring Integration

#### Configuration

Add @HttpApiScan to a @Configuration class for enabling package scanning.

```java
@Configuration
// enable HttpApiScan
@HttpApiScan
public class TestApplication {
}
```
 
#### Autowired the interface

```java
@Autowired
private CityService cityService;

public void test() {
    List<City> cities = cityService.getAllCities();
    for (City city : cities) {
        System.out.println(city.getId() + ", " + city.getName());
    }
}
```

Note: if your IDE complain "Could not autowired. no beans of type 'xxx' type found", just ignore that message.

## VI. PLACEHOLDER

Placeholder is supported for read config (using **${}**, like ${api.url.city}) and path variables (using **{}**, like {cityName}). 

We can use placeholder in **@HttpApi's prefix and @HttpReq's url**.

Note：path variable using **`{}`** and config using **`${}`**

The framework will get the config property from: 

* **property file** set by **configPaths** in @HttpApiScan,
* **System property**: System.getProperty("property"),
* and **Spring Environment** in Spring integration scenario.

## V. EXTENSION

### RequestPreprocessor

Sometimes, we need to get all request added some specific headers, cookies or params.

It would be redundant to add these stuff.

Now we can register a RequestPreprocessor. RequestPreprocessor's process() method will be call when a request is prepared but not yet send.

We are provided a chance to access the request and set anything we need. 

```java
public void preprocessorTest() {
    HttpApiProxyFactory factory = new HttpApiProxyFactory(request -> {
        // we add cookie and header for all request invoked by the proxy get from this factory
        request.addCookie("authCookies", authKey);
        request.addHeader("authHeaders", authKey);
    });
    CityService cityService = factory.getProxy(CityService.class);
    City city = cityService.getCity(id);
}
```

or in Spring scenario, register a RequestPreprocessor Bean.


### ResponseProcessor

Similar to RequestPreprocessor, ResponseProcessor enable us to get access to take over the response by implementing the ResponseProcessor interface.

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

or in Spring scenario, register a RequestPreprocessor Bean.


# CORE ANNOTATION

## @HttpApiScan

Enable package scanning similar to @ComponentScan

* value: to set basePackage，the annotated class's package by default
* configPaths: to specify config files

## @HttpApi

Declare a class it HTTP api binding class which need to be scanned. Similar to Spring's @Component

## @HttpReq

Set the url and request method (GET/POST/DELETE etc.) binding to the method.

## @Param

value: the key of request param
isBody: mark that the argument is the request body, if the argument is an non-primary object, all the field-value will be a part of request params.

value and isBody should not both be empty/false, otherwise the param will be ignored

## @Headers

Headers of the request, must be `Map<String, String>` otherwise an `IllegalArgumentException` will be thrown.

## @Cookies

Cookies of the request, must be `Map<String, String>` otherwise an `IllegalArgumentException` will be thrown.

## @RetryPolicy

Retry policy can be annotated to both class and method.

* times: try times, 3 by default;
* retryFor: what exception to retry, IOException by default;
* retryForStatus: what status code would retry, other than 20x by default;
* fixedBackOffPeriod: back off strategy, the number of seconds to sleep when retry is required, not to sleep by default.