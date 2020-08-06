package com.github.dadiyang.httpinvoker.requestor;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author huangxuyang
 * @since 2019-06-13
 */
public class HttpClientResponse implements HttpResponse {
    public static final String SET_COOKIE = "set-cookie";
    private final CloseableHttpResponse response;

    public HttpClientResponse(CloseableHttpResponse response) {
        this.response = response;
    }

    @Override
    public int getStatusCode() {
        return response.getStatusLine().getStatusCode();
    }

    @Override
    public String getStatusMessage() {
        return response.getStatusLine().getReasonPhrase();
    }

    @Override
    public String getCharset() {
        return response.getEntity().getContentEncoding().getValue();
    }

    @Override
    public String getContentType() {
        return response.getEntity().getContentType().getValue();
    }

    @Override
    public byte[] getBodyAsBytes() {
        try {
            return EntityUtils.toByteArray(response.getEntity());
        } catch (IOException e) {
            throw new IllegalStateException("cannot read bytes from response!", e);
        }
    }

    @Override
    public InputStream getBodyStream() {
        try {
            return response.getEntity().getContent();
        } catch (IOException e) {
            throw new IllegalStateException("cannot read stream from response!", e);
        }
    }

    @Override
    public String getBody() {
        try {
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (IOException e) {
            throw new IllegalStateException("cannot read String from response!", e);
        }
    }

    @Override
    public Map<String, String> getHeaders() {
        Header[] headers = response.getAllHeaders();
        if (headers == null || headers.length == 0) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new HashMap<String, String>(headers.length);
        for (Header header : headers) {
            map.put(header.getName(), header.getValue());
        }
        return map;
    }

    @Override
    public String getHeader(String name) {
        return response.getFirstHeader(name).getValue();
    }

    @Override
    public Map<String, String> getCookies() {
        Header[] headers = response.getAllHeaders();
        if (headers == null || headers.length == 0) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new HashMap<String, String>();
        for (Header header : headers) {
            if (SET_COOKIE.equalsIgnoreCase(header.getName())) {
                for (HeaderElement element : header.getElements()) {
                    map.put(element.getName(), element.getValue());
                }
            }
        }
        return map;
    }

    @Override
    public String getCookie(String name) {
        return getCookies().get(name);
    }

    @Override
    public Map<String, List<String>> multiHeaders() {
        Header[] headers = response.getAllHeaders();
        if (headers == null || headers.length == 0) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> map = new HashMap<String, List<String>>(headers.length);
        for (Header header : headers) {
            List<String> values = map.containsKey(header.getName()) ? map.get(header.getName()) : new LinkedList<String>();
            values.add(header.getValue());
            map.put(header.getName(), values);
        }
        return map;
    }

    @Override
    public List<String> getHeaders(String name) {
        Header[] headers = response.getHeaders(name);
        if (headers == null || headers.length == 0) {
            return Collections.emptyList();
        }
        List<String> values = new LinkedList<String>();
        for (Header header : headers) {
            values.add(header.getValue());
        }
        return values;
    }
}
