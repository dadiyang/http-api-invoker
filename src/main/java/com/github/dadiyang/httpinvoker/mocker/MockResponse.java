package com.github.dadiyang.httpinvoker.mocker;

import com.github.dadiyang.httpinvoker.requestor.HttpResponse;

import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huangxuyang
 * date 2018/12/6
 */
public class MockResponse implements HttpResponse {
    private static final int STATUS_CODE_SUC = 200;
    private int statusCode = STATUS_CODE_SUC;
    private String statusMessage;
    private String charset;
    private String contentType;
    private byte[] bodyAsBytes;
    private String body;
    private InputStream bodyStream;
    private Map<String, List<String>> headers;
    private Map<String, String> cookies;

    public MockResponse() {
    }

    public MockResponse(int statusCode, String body) {
        setStatusCode(statusCode);
        setBody(body);
    }

    public MockResponse(String statusMessage, int statusCode) {
        setStatusCode(statusCode);
        setStatusMessage(statusMessage);
    }

    public MockResponse(String body) {
        setStatusCode(STATUS_CODE_SUC);
        setBody(body);
    }

    public MockResponse(int statusCode, String statusMessage, String contentType) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.contentType = contentType;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    @Override
    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public byte[] getBodyAsBytes() {
        return bodyAsBytes;
    }

    public void setBodyAsBytes(byte[] bodyAsBytes) {
        this.bodyAsBytes = bodyAsBytes;
    }

    @Override
    public InputStream getBodyStream() {
        return bodyStream;
    }

    public void setBodyStream(InputStream bodyStream) {
        this.bodyStream = bodyStream;
    }

    @Override
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public Map<String, String> getHeaders() {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(headers.size());
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String header = entry.getKey();
            List<String> values = entry.getValue();
            if (values.size() > 0) {
                map.put(header, values.get(0));
            }
        }
        return map;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    @Override
    public Map<String, List<String>> multiHeaders() {
        return headers;
    }

    @Override
    public List<String> getHeaders(String name) {
        return Arrays.asList(getHeader(name).split(";\\s?"));
    }

    @Override
    public String getHeader(String name) {
        return getHeaders().get(name);
    }

    @Override
    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    @Override
    public String getCookie(String name) {
        return getCookies().get(name);
    }
}
