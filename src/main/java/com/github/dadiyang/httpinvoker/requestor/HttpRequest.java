package com.github.dadiyang.httpinvoker.requestor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author huangxuyang
 * date 2018/12/7
 */
public class HttpRequest {
    private String method = "GET";
    private int timeout = 5000;
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
        this.headers = new HashMap<String, String>(headers);
    }

    public void addHeader(String key, String value) {
        if (headers == null) {
            headers = new HashMap<String, String>(8);
        }
        headers.put(key, value);
    }

    public void addCookie(String key, String value) {
        if (cookies == null) {
            cookies = new HashMap<String, String>(8);
        }
        cookies.put(key, value);
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = new HashMap<String, String>(cookies);
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public void addParam(String key, String value) {
        if (data == null) {
            data = new HashMap<String, Object>(8);
        }
        data.put(key, value);
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

}
