package com.github.dadiyang.httpinvoker.interfaces;


import com.github.dadiyang.httpinvoker.annotation.HttpApi;
import com.github.dadiyang.httpinvoker.annotation.HttpReq;
import com.github.dadiyang.httpinvoker.annotation.Param;
import com.github.dadiyang.httpinvoker.entity.City;
import com.github.dadiyang.httpinvoker.entity.ResultBean;

import java.util.List;

/**
 * a example interface for testing
 *
 * @author huangxuyang
 * date 2018/11/1
 */
@HttpApi(prefix = "${api.url.city.host}")
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
     * 如果是集合类或数组的参数数据会直接当成请求体直接发送
     */
    @HttpReq(value = "/city/save", method = "POST")
    boolean saveCities(List<City> cities);

    /**
     * 默认使用GET方法，可以通过method指定请求方式
     */
    @HttpReq(value = "/city/saveCity", method = "POST")
    boolean saveCity(@Param("id") Integer id, @Param("name") String name);

    /**
     * 使用完整的路径，不会添加前缀
     */
    @HttpReq(value = "http://localhost:8080/city/getCityByName")
    ResultBean<City> getCityByName(@Param("name") String name);

    /**
     * 支持路径参数
     */
    @HttpReq("/city/getCityRest/{id}")
    City getCityRest(@Param("id") int id);
}
