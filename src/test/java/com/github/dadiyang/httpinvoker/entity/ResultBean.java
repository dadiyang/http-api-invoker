package com.github.dadiyang.httpinvoker.entity;

public class ResultBean<T> {
    private int code;
    private T data;
    private String msg;

    public ResultBean() {
    }

    public ResultBean(int code, T data) {
        this.code = code;
        this.data = data;
    }

    public ResultBean(String msg, int code) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "ResultBean{" +
                "code=" + code +
                ", data=" + data +
                ", msg='" + msg + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResultBean<?> that = (ResultBean<?>) o;

        if (code != that.code) return false;
        if (data != null ? !data.equals(that.data) : that.data != null) return false;
        return msg != null ? msg.equals(that.msg) : that.msg == null;

    }

    @Override
    public int hashCode() {
        int result = code;
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (msg != null ? msg.hashCode() : 0);
        return result;
    }

}
