package com.github.dadiyang.httpinvoker.mocker;

import com.github.dadiyang.httpinvoker.requestor.HttpResponse;

/**
 * @author huangxuyang
 * @since 2019-05-31
 */
public class MockResponse extends HttpResponse {
    private static final int STATUS_CODE_SUC = 200;

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
        super(statusCode, statusMessage, contentType);
    }

    @Override
    public byte[] getBodyAsBytes() {
        byte[] bytes = super.getBodyAsBytes();
        String body = getBody();
        return bytes == null && body != null ? getBody().getBytes() : bytes;
    }
}
