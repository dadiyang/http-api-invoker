package com.github.dadiyang.httpinvoker.requestor;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author huangxuyang
 * date 2018/12/6
 */
public interface HttpResponse {

    int getStatusCode();

    String getStatusMessage();

    String getCharset();

    String getContentType();

    byte[] getBodyAsBytes();

    InputStream getBodyStream();

    String getBody();

    Map<String, String> getHeaders();

    Map<String, List<String>> multiHeaders();

    List<String> getHeaders(String name);

    String getHeader(String name);

    Map<String, String> getCookies();

    String getCookie(String name);
}
