package com.github.dadiyang.httpinvoker.mocker;

import com.github.dadiyang.httpinvoker.requestor.HttpResponse;

import java.util.Map;

/**
 * Mock规则
 *
 * @author huangxuyang
 * @since 2019-05-31
 */
public class MockRule {
    /**
     * url 正则表达式
     */
    private String urlReg;
    /**
     * uri 正则，即忽略协议和域名，如 http://localhost:8080/city/getName，则 uri 为 /city/getName
     */
    private String uriReg;
    /**
     * 请求方法：GET/POST/PUT 等
     */
    private String method;
    private Map<String, String> headers;
    private Map<String, String> cookies;
    private Map<String, Object> data;
    private Object body;
    private HttpResponse response;

    public MockRule() {
    }

    public MockRule(String urlReg) {
        this.urlReg = urlReg;
    }

    public MockRule(String urlReg, HttpResponse response) {
        this.urlReg = urlReg;
        this.response = response;
    }

    public MockRule(String urlReg, String method, HttpResponse response) {
        this.urlReg = urlReg;
        this.method = method;
        this.response = response;
    }

    public MockRule(String urlReg, Map<String, Object> data, HttpResponse response) {
        this.urlReg = urlReg;
        this.data = data;
        this.response = response;
    }

    public String getUrlReg() {
        return urlReg;
    }

    public void setUrlReg(String urlReg) {
        this.urlReg = urlReg;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUriReg() {
        return uriReg;
    }

    public void setUriReg(String uriReg) {
        this.uriReg = uriReg;
    }

    @Override
    public String toString() {
        return "MockRule{" +
                "urlReg='" + urlReg + '\'' +
                ", uriReg='" + uriReg + '\'' +
                ", method='" + method + '\'' +
                ", headers=" + headers +
                ", cookies=" + cookies +
                ", data=" + data +
                ", body=" + body +
                ", response=" + response +
                '}';
    }
}
