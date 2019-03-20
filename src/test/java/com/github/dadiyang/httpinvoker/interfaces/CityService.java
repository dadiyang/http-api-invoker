package com.github.dadiyang.httpinvoker.interfaces;


import com.github.dadiyang.httpinvoker.annotation.*;
import com.github.dadiyang.httpinvoker.entity.City;
import com.github.dadiyang.httpinvoker.entity.ResultBean;
import com.github.dadiyang.httpinvoker.requestor.HttpResponse;
import com.github.dadiyang.httpinvoker.requestor.Status;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * a example interface for testing
 *
 * @author huangxuyang
 * date 2018/11/1
 */
@HttpApi("${api.url.city.host}/city")
@RetryPolicy
public interface CityService {
    /**
     * 使用URI，会自动添加prefix指定的前缀
     */
    @HttpReq("/allCities")
    @RetryPolicy(times = 2, fixedBackOffPeriod = 3000)
    List<City> getAllCities();

    /**
     * 使用Param注解指定方法参数对应的请求参数名称
     */
    @HttpReq("/getById")
    City getCity(@Param("id") int id);

    /**
     * 如果是集合类或数组的参数数据会直接当成请求体直接发送
     */
    @HttpReq(value = "/save", method = "POST")
    @RetryPolicy(times = 2, retryForStatus = Status.SERVER_ERROR)
    boolean saveCities(List<City> cities);

    /**
     * 默认使用GET方法，可以通过method指定请求方式
     */
    @HttpReq(value = "/saveCity", method = "POST")
    boolean saveCity(@Param("id") Integer id, @Param("name") String name);

    /**
     * 使用完整的路径，不会添加前缀
     */
    @HttpReq(value = "${api.url.city.host}/city/getCityByName")
    ResultBean<City> getCityByName(@Param("name") String name);

    /**
     * 支持路径参数
     */
    @HttpReq("/getCityRest/{id}")
    City getCityRest(@Param("id") Integer id);

    /**
     * 获取请求体，可以拿到请求头和cookie等信息
     */
    @HttpReq("/listCity")
    HttpResponse listCity();

    /**
     * 带错误请求头的方法
     */
    @HttpReq("/getCityRest/{id}")
    City getCityWithErrHeaders(@Param("id") int id, @Headers String headers);

    /**
     * 带正确请求头的方法
     */
    @HttpReq("/getCityRest/{id}")
    City getCityWithHeaders(@Param("id") int id, @Headers Map<String, String> headers);

    /**
     * 带cookie的方法
     */
    @HttpReq("/getCityRest/{id}")
    City getCityWithCookies(@Param("id") int id, @Cookies Map<String, String> cookies);

    /**
     * 判断给定的城市是否存在
     * <p>
     * 用于测试复杂对象做为参数是否可以被解析
     */
    @HttpReq("/getCity")
    boolean hasCity(City city);

    /**
     * 上传输入流
     *
     * @param fileName 文件名
     * @param in       输入流
     */
    @HttpReq(value = "/picture/upload", method = "POST")
    String upload(@Param("fileName") String fileName,
                  @Param(value = "media") InputStream in);

    /**
     * #{variable} 表示支持路径参数，且该路径参数不会在填充后被移除，而是在消息体中也带上该参数
     */
    @HttpReq(value = "/#{id}", method = "PUT")
    boolean updateCity(@Param("id") int id, @Param("name") String name);

    /**
     * 模拟表单提交 application/x-www-form-urlencoded
     */
    @Form
    @HttpReq(value = "/saveCity", method = "POST")
    boolean saveCityForm(City city);

    /**
     * 使用Param注解指定方法参数对应的请求参数名称
     */
    @HttpReq("/getByIds")
    List<City> getCities(@Param("ids") List<Integer> ids);
}
