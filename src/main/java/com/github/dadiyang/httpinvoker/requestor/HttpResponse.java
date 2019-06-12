package com.github.dadiyang.httpinvoker.requestor;

import java.io.BufferedInputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huangxuyang
 * date 2018/12/6
 */
public class HttpResponse {
    /**
     * Get the status code of the response.
     */
    private int statusCode;

    /**
     * Get the status message of the response.
     */
    private String statusMessage;

    /**
     * Get the character set name of the response, derived from the content-type header.
     */
    private String charset;

    /**
     * Get the response content type (e.g. "text/html");
     */
    private String contentType;

    /**
     * Get the body of the response as an array of bytes.
     */
    private byte[] bodyAsBytes;
    /**
     * Get the body of the response as a plain string.
     */
    private String body;
    /**
     * Get the body of the response as a (buffered) InputStream. You should close the input stream when you're done with it.
     * Other body methods (like bufferUp, body, parse, etc) will not work in conjunction with this method.
     * <p>This method is useful for writing large responses to disk, without buffering them completely into memory first.</p>
     */
    private BufferedInputStream bodyStream;

    private Map<String, List<String>> headers;

    /**
     * Retrieve all of the request/response cookies as a map
     *
     * @return cookies
     */
    private Map<String, String> cookies;

    public HttpResponse() {
    }

    public HttpResponse(int statusCode, String statusMessage, String contentType) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.contentType = contentType;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getBodyAsBytes() {
        return bodyAsBytes;
    }

    public void setBodyAsBytes(byte[] bodyAsBytes) {
        this.bodyAsBytes = bodyAsBytes;
    }

    public BufferedInputStream getBodyStream() {
        return bodyStream;
    }

    public void setBodyStream(BufferedInputStream bodyStream) {
        this.bodyStream = bodyStream;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

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

    public Map<String, List<String>> multiHeaders() {
        return headers;
    }

    public List<String> getHeaders(String name) {
        return Arrays.asList(getHeader(name).split(";\\s?"));
    }

    public String getHeader(String name) {
        return getHeaders().get(name);
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public String getCookie(String name) {
        return getCookies().get(name);
    }
}
