package com.github.dadiyang.httpinvoker.requestor;

import com.github.dadiyang.httpinvoker.serializer.JsonSerializerDecider;
import com.github.dadiyang.httpinvoker.util.ObjectUtils;
import com.github.dadiyang.httpinvoker.util.ParamUtils;
import com.github.dadiyang.httpinvoker.util.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.dadiyang.httpinvoker.enumeration.ReqMethod.*;
import static com.github.dadiyang.httpinvoker.util.ParamUtils.*;

/**
 * an http requestor base on HttpClient
 *
 * @author huangxuyang
 * @since 2019-06-13
 */
public class HttpClientRequestor implements Requestor {
    private static final String FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final String APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE = "Content-Type";
    private CloseableHttpClient httpClient;

    /**
     * 使用默认的 httpClient 实现和配置
     */
    public HttpClientRequestor() {
        httpClient = createHttpClient();
    }

    /**
     * 自定义配置 httpClient
     */
    public HttpClientRequestor(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    private CloseableHttpClient createHttpClient() {
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
        poolingHttpClientConnectionManager.setMaxTotal(32);
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(16);
        return HttpClients.custom()
                .setConnectionManager(poolingHttpClientConnectionManager)
                .build();
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
        // handle MultiPart
        if (isUploadRequest(request.getBody())) {
            MultiPart multiPart;
            if (!(request.getBody() instanceof MultiPart)) {
                multiPart = ParamUtils.convertInputStreamAndFile(request);
            } else {
                multiPart = (MultiPart) request.getBody();
            }
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setLaxMode();
            for (MultiPart.Part part : multiPart.getParts()) {
                if (part.getInputStream() != null) {
                    builder.addBinaryBody(part.getKey(), part.getInputStream(), ContentType.DEFAULT_BINARY, part.getValue());
                } else {
                    ContentType contentType = ContentType.create("text/plain", "UTF-8");
                    builder.addTextBody(part.getKey(), part.getValue(), contentType);
                }
            }
            HttpEntity entity = builder.build();
            HttpPost httpPost = new HttpPost(request.getUrl());
            httpPost.setEntity(entity);
            return sendMultiPartRequest(request, httpPost);
        }
        HttpEntity entity = createHttpEntity(request);
        HttpPost httpPost = new HttpPost(request.getUrl());
        httpPost.setEntity(entity);
        return sendRequest(request, httpPost);
    }

    private HttpEntity createHttpEntity(HttpRequest request) throws IOException {
        HttpEntity entity;
        // handle x-www-form-urlencoded
        if (request.getHeaders() != null
                && ObjectUtils.equals(FORM_URLENCODED, request.getHeaders().get(CONTENT_TYPE))) {
            List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
            Map<String, String> map = toMapStringString(request.getData(), "");
            for (Map.Entry<String, String> entry : map.entrySet()) {
                parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            entity = new UrlEncodedFormEntity(parameters, "UTF-8");
        } else {
            if (request.getBody() != null) {
                entity = new ByteArrayEntity(JsonSerializerDecider.getJsonSerializer().serialize(request.getBody()).getBytes(Charset.forName("UTF-8")),
                        ContentType.create(APPLICATION_JSON, "UTF-8"));
            } else if (request.getData() != null) {
                entity = new ByteArrayEntity(JsonSerializerDecider.getJsonSerializer().serialize(request.getData()).getBytes(Charset.forName("UTF-8")),
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
        prepare(request, httpRequestBase);
        CloseableHttpResponse response = httpClient.execute(httpRequestBase);
        if (response.getEntity() != null) {
            response.setEntity(new BufferedHttpEntity(response.getEntity()));
            EntityUtils.consume(response.getEntity());
        }
        return new HttpClientResponse(response);
    }

    private HttpResponse sendMultiPartRequest(HttpRequest request, HttpRequestBase httpRequestBase) throws IOException {
        prepare(request, httpRequestBase);
        CloseableHttpClient httpClient = null;
        try {
            httpClient = createHttpClient();
            CloseableHttpResponse response = httpClient.execute(httpRequestBase);
            response.setEntity(new BufferedHttpEntity(response.getEntity()));
            EntityUtils.consume(response.getEntity());
            return new HttpClientResponse(response);
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }
    }

    private void prepare(HttpRequest request, HttpRequestBase httpRequestBase) {
        addHeaders(request, httpRequestBase);
        addCookies(request, httpRequestBase);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(request.getTimeout())
                .build();
        httpRequestBase.setConfig(requestConfig);
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

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }
}
