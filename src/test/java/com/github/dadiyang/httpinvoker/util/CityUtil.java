package com.github.dadiyang.httpinvoker.util;

import com.github.dadiyang.httpinvoker.entity.City;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CityUtil {
    public static List<City> createCities() {
        List<City> cityList = new ArrayList<>();
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
        return null;
    }

    public static City createCity(String name) {
        List<City> cities = createCities();
        for (City city : cities) {
            if (Objects.equals(city.getName(), name)) {
                return city;
            }
        }
        return null;
    }
}
