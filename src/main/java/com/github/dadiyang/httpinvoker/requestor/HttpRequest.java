package com.github.dadiyang.httpinvoker.requestor;

import java.util.Map;
import java.util.Objects;

/**
 * @author huangxuyang
 * date 2018/12/7
 */
public class HttpRequest {
    private String method = "GET";
    private int timeout = 30000;
    private String url;
    private Map<String, String> headers;
    private Map<String, String> cookies;
    private Map<String, Object> data;
    private Object body;
    private String fileFormKey;

    public HttpRequest(String url) {
        this.url = url;
    }

    public HttpRequest(String url, String method, int timeout) {
        this.method = method;
        this.timeout = timeout;
        this.url = url;
    }

    public HttpRequest(int timeout, String method) {
        this.method = method;
        this.timeout = timeout;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileFormKey() {
        return fileFormKey;
    }

    public void setFileFormKey(String fileFormKey) {
        this.fileFormKey = fileFormKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpRequest request = (HttpRequest) o;
        return timeout == request.timeout &&
                Objects.equals(method, request.method) &&
                Objects.equals(url, request.url) &&
                Objects.equals(headers, request.headers) &&
                Objects.equals(cookies, request.cookies) &&
                Objects.equals(data, request.data) &&
                Objects.equals(body, request.body) &&
                Objects.equals(fileFormKey, request.fileFormKey);
    }

    @Override
    public int hashCode() {

        return Objects.hash(method, timeout, url, headers, cookies, data, body, fileFormKey);
    }
}
