package com.github.dadiyang.httpinvoker.util;

import com.github.dadiyang.httpinvoker.entity.City;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CityUtil {
    public static List<City> createCities() {
        List<City> cityList = new ArrayList<City>();
        cityList.add(new City(1, "北京"));
        cityList.add(new City(2, "上海"));
        cityList.add(new City(3, "广州"));
        cityList.add(new City(4, "深圳"));
        return cityList;
    }

    public static City createCity(int id) {
        List<City> cities = createCities();
        for (City city : cities) {
            if (city.getId() == id) {
                return city;
            }
        }
        throw new IllegalArgumentException("city not exists, id:" + id);
    }

    public static List<City> getCities(List<Integer> ids) {
        List<City> cities = createCities();
        List<City> rs = new LinkedList<City>();
        for (City city : cities) {
            if (ids.contains(city.getId())) {
                rs.add(city);
            }
        }
        return rs;
    }

    public static City createCity(String name) {
        List<City> cities = createCities();
        for (City city : cities) {
            if (ObjectUtils.equals(city.getName(), name)) {
                return city;
            }
        }
        return null;
    }
}
