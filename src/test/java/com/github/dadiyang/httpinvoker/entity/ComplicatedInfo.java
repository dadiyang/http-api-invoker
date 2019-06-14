package com.github.dadiyang.httpinvoker.entity;

import java.util.List;
public class ComplicatedInfo {
    private List<City> cities;
    private String msg;
    private City city;

    public ComplicatedInfo(List<City> cities, String msg, City city) {
        this.cities = cities;
        this.msg = msg;
        this.city = city;
    }

    public ComplicatedInfo() {
    }

    public List<City> getCities() {
        return cities;
    }

    public void setCities(List<City> cities) {
        this.cities = cities;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComplicatedInfo that = (ComplicatedInfo) o;

        if (cities != null ? !cities.equals(that.cities) : that.cities != null) return false;
        if (msg != null ? !msg.equals(that.msg) : that.msg != null) return false;
        return city != null ? city.equals(that.city) : that.city == null;

    }

    @Override
    public int hashCode() {
        int result = cities != null ? cities.hashCode() : 0;
        result = 31 * result + (msg != null ? msg.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ComplicatedInfo{" +
                "cities=" + cities +
                ", msg='" + msg + '\'' +
                ", city=" + city +
                '}';
    }
}
