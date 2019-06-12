package com.github.dadiyang.httpinvoker.entity;

public class ResultBeanWithStatusAsCode<T> {
    private int status;
    private T data;
    private String msg;

    public ResultBeanWithStatusAsCode() {
    }

    public ResultBeanWithStatusAsCode(int status, T data) {
        this.status = status;
        this.data = data;
    }

    public ResultBeanWithStatusAsCode(String msg, int status) {
        this.status = status;
        this.msg = msg;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResultBeanWithStatusAsCode<?> that = (ResultBeanWithStatusAsCode<?>) o;

        if (status != that.status) return false;
        if (data != null ? !data.equals(that.data) : that.data != null) return false;
        return msg != null ? msg.equals(that.msg) : that.msg == null;

    }

    @Override
    public int hashCode() {
        int result = status;
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (msg != null ? msg.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ResultBeanWithStatusAsCode{" +
                "status=" + status +
                ", data=" + data +
                ", msg='" + msg + '\'' +
                '}';
    }
}
