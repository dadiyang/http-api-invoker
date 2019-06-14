package com.github.dadiyang.httpinvoker.requestor;

import com.alibaba.fastjson.JSON;
import com.github.dadiyang.httpinvoker.util.ObjectUtils;
import com.github.dadiyang.httpinvoker.util.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.dadiyang.httpinvoker.enumeration.ReqMethod.*;
import static com.github.dadiyang.httpinvoker.util.ParamUtils.toMapStringString;
import static com.github.dadiyang.httpinvoker.util.ParamUtils.toQueryString;

/**
 * an http requestor base on HttpClient
 *
 * @author huangxuyang
 * @since 2019-06-13
 */
public class HttpClientRequestor implements Requestor {
    private static final Logger log = LoggerFactory.getLogger(HttpClientRequestor.class);
    private static final String FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final String APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE = "Content-Type";
    private CloseableHttpClient httpClient;

    public HttpClientRequestor(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpClientRequestor() {
        httpClient = HttpClients.createDefault();
    }

    @Override
    public HttpResponse sendRequest(HttpRequest request) throws IOException {
        String method = StringUtils.upperCase(request.getMethod());
        if (ObjectUtils.equals(method, GET)) {
            return sendGet(request);
        } else if (ObjectUtils.equals(method, POST)) {
            return sendPost(request);
        } else if (ObjectUtils.equals(method, PUT)) {
            return sendPut(request);
        } else if (ObjectUtils.equals(method, DELETE)) {
            return sendDelete(request);
        } else if (ObjectUtils.equals(method, PATCH)) {
            return sendPatch(request);
        } else if (ObjectUtils.equals(method, HEAD)) {
            return sendHead(request);
        } else if (ObjectUtils.equals(method, OPTIONS)) {
            return sendOptions(request);
        } else if (ObjectUtils.equals(method, TRACE)) {
            return sendTrace(request);
        } else {
            throw new IllegalArgumentException("Unsupported http method: " + method);
        }
    }

    private HttpResponse sendTrace(HttpRequest request) throws IOException {
        String fullUrl = request.getUrl() + toQueryString(request.getData());
        HttpTrace httpTrace = new HttpTrace(fullUrl);
        return sendRequest(request, httpTrace);
    }

    private HttpResponse sendOptions(HttpRequest request) throws IOException {
        String fullUrl = request.getUrl() + toQueryString(request.getData());
        HttpOptions httpOptions = new HttpOptions(fullUrl);
        return sendRequest(request, httpOptions);
    }

    private HttpResponse sendHead(HttpRequest request) throws IOException {
        String fullUrl = request.getUrl() + toQueryString(request.getData());
        HttpHead httpHead = new HttpHead(fullUrl);
        return sendRequest(request, httpHead);
    }

    private HttpResponse sendPatch(HttpRequest request) throws IOException {
        HttpEntity entity = createHttpEntity(request);
        HttpPatch httpPatch = new HttpPatch(request.getUrl());
        httpPatch.setEntity(entity);
        return sendRequest(request, httpPatch);
    }

    private HttpResponse sendDelete(HttpRequest request) throws IOException {
        String fullUrl = request.getUrl() + toQueryString(request.getData());
        HttpDelete httpDelete = new HttpDelete(fullUrl);
        return sendRequest(request, httpDelete);
    }

    private HttpResponse sendPut(HttpRequest request) throws IOException {
        HttpEntity entity = createHttpEntity(request);
        HttpPut httpPut = new HttpPut(request.getUrl());
        httpPut.setEntity(entity);
        return sendRequest(request, httpPut);
    }

    private HttpResponse sendPost(HttpRequest request) throws IOException {
        HttpEntity entity = createHttpEntity(request);
        HttpPost httpPost = new HttpPost(request.getUrl());
        httpPost.setEntity(entity);
        return sendRequest(request, httpPost);
    }

    private HttpEntity createHttpEntity(HttpRequest request) throws UnsupportedEncodingException {
        HttpEntity entity;
        // handle MultiPart
        if (request.getBody() instanceof MultiPart) {
            MultiPart multiPart = (MultiPart) request.getBody();
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            for (MultiPart.Part part : multiPart.getParts()) {
                builder.addPart(part.getKey(), new InputStreamBody(part.getInputStream(), part.getValue()));
            }
            return builder.build();
        }
        if (request.getHeaders() != null
                && ObjectUtils.equals(FORM_URLENCODED, request.getHeaders().get(CONTENT_TYPE))) {
            List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
            Map<String, String> map = toMapStringString(request.getData());
            for (Map.Entry<String, String> entry : map.entrySet()) {
                parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            entity = new UrlEncodedFormEntity(parameters, "UTF-8");
        } else {
            if (request.getBody() != null) {
                entity = new ByteArrayEntity(JSON.toJSONBytes(request.getBody()),
                        ContentType.create(APPLICATION_JSON, "UTF-8"));
            } else if (request.getData() != null) {
                entity = new ByteArrayEntity(JSON.toJSONBytes(request.getData()),
                        ContentType.create(APPLICATION_JSON, "UTF-8"));
            } else {
                BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
                basicHttpEntity.setContentLength(0);
                entity = basicHttpEntity;
            }
        }
        return entity;
    }

    private HttpResponse sendGet(HttpRequest request) throws IOException {
        String fullUrl = request.getUrl() + toQueryString(request.getData());
        HttpGet httpGet = new HttpGet(fullUrl);
        return sendRequest(request, httpGet);
    }

    private HttpResponse sendRequest(HttpRequest request, HttpRequestBase httpRequestBase) throws IOException {
        addHeaders(request, httpRequestBase);
        addCookies(request, httpRequestBase);
        CloseableHttpResponse response = httpClient.execute(httpRequestBase);
        response.setEntity(new BufferedHttpEntity(response.getEntity()));
        return new HttpClientResponse(response);
    }

    private void addCookies(HttpRequest request, HttpMessage msg) {
        Map<String, String> cookies = request.getCookies();
        if (cookies == null || cookies.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
        }
        msg.addHeader("Cookie", sb.substring(0, sb.length()));
    }

    private void addHeaders(HttpRequest request, HttpMessage msg) {
        Map<String, String> headers = request.getHeaders();
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                msg.addHeader(entry.getKey(), entry.getValue());
            }
        }


    }
}
