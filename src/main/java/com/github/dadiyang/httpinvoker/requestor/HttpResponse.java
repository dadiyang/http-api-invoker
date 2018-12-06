package com.github.dadiyang.httpinvoker.requestor;

import java.io.BufferedInputStream;

/**
 * @author huangxuyang
 * date 2018/12/6
 */
public class HttpResponse {
    /**
     * Get the status code of the response.
     *
     * @return status code
     */
    int statusCode;

    /**
     * Get the status message of the response.
     *
     * @return status message
     */
    String statusMessage;

    /**
     * Get the character set name of the response, derived from the content-type header.
     *
     * @return character set name
     */
    String charset;

    /**
     * Get the response content type (e.g. "text/html");
     *
     * @return the response content type
     */
    String contentType;

    /**
     * Get the body of the response as an array of bytes.
     *
     * @return body bytes
     */
    byte[] bodyAsBytes;
    /**
     * Get the body of the response as a plain string.
     *
     * @return body
     */
    String body;
    /**
     * Get the body of the response as a (buffered) InputStream. You should close the input stream when you're done with it.
     * Other body methods (like bufferUp, body, parse, etc) will not work in conjunction with this method.
     * <p>This method is useful for writing large responses to disk, without buffering them completely into memory first.</p>
     *
     * @return the response body input stream
     */
    BufferedInputStream bodyStream;

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
}
