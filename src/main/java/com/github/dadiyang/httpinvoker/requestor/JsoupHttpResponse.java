package com.github.dadiyang.httpinvoker.requestor;

import org.jsoup.Connection;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author huangxuyang
 * date 2018/12/6
 */
public class JsoupHttpResponse implements HttpResponse {
    private final Connection.Response response;

    public JsoupHttpResponse(Connection.Response response) {
        this.response = response;
    }

    @Override
    public int getStatusCode() {
        return response.statusCode();
    }

    @Override
    public String getStatusMessage() {
        return response.statusMessage();
    }

    @Override
    public String getCharset() {
        return response.charset();
    }

    @Override
    public String getContentType() {
        return response.contentType();
    }

    @Override
    public byte[] getBodyAsBytes() {
        return response.bodyAsBytes();
    }

    @Override
    public InputStream getBodyStream() {
        return response.bodyStream();
    }

    @Override
    public String getBody() {
        return response.body();
    }

    @Override
    public Map<String, String> getHeaders() {
        return response.headers();
    }

    @Override
    public String getHeader(String name) {
        return response.header(name);
    }

    @Override
    public Map<String, String> getCookies() {
        return response.cookies();
    }

    @Override
    public String getCookie(String name) {
        return response.cookie(name);
    }

    @Override
    public Map<String, List<String>> multiHeaders() {
        return response.multiHeaders();
    }

    @Override
    public List<String> getHeaders(String name) {
        return response.headers(name);
    }
}
