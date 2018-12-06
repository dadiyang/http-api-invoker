package com.github.dadiyang.httpinvoker.requestor;

import org.jsoup.Connection;

import java.io.BufferedInputStream;

/**
 * @author huangxuyang
 * date 2018/12/6
 */
public class JsoupHttpResponse extends HttpResponse {
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
    public BufferedInputStream getBodyStream() {
        return response.bodyStream();
    }

    @Override
    public String getBody() {
        return response.body();
    }
}
