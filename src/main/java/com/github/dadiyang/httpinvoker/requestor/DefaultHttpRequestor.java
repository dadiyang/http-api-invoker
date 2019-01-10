package com.github.dadiyang.httpinvoker.requestor;

import com.alibaba.fastjson.JSON;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.jsoup.Connection.Method;
import static org.jsoup.Connection.Response;

/**
 * The default implementation of {@link Requestor} that send the request using Jsoup which is a elegant http client I've ever use.
 * <p>
 * the parameter will be formatted according to the request method.
 *
 * @author huangxuyang
 * date 2018/11/1
 */
public class DefaultHttpRequestor implements Requestor {
    private static final Logger log = LoggerFactory.getLogger(Requestor.class);
    private static final String FILE_NAME = "fileName";
    private static final String DEFAULT_UPLOAD_FORM_KEY = "media";
    private static final String FORM_KEY = "formKey";

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpResponse sendRequest(HttpRequest request) throws IOException {
        // send request
        Method m = Method.valueOf(request.getMethod().toUpperCase());
        Response response;
        String url = request.getUrl();
        int timeout = request.getTimeout();
        if (!m.hasBody()) {
            // to those method without body such as GET/DELETE we use QueryString
            String fullUrl = request.getUrl() + queryStringify(request.getData());
            log.debug("send {} request to {}", m, fullUrl);
            Connection conn = Jsoup.connect(fullUrl)
                    .method(m)
                    .timeout(timeout)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true);
            addHeadersAndCookies(request, conn);
            response = conn.execute();
        } else {
            Connection conn = Jsoup.connect(url)
                    .method(m)
                    .timeout(timeout)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true);
            addHeadersAndCookies(request, conn);
            Map<String, Object> data = request.getData();
            // body first
            if (request.getBody() != null) {
                Object bodyParam = request.getBody();
                // if the body param is InputStream, upload it
                if (isUploadRequest(request, bodyParam)) {
                    log.debug("upload file {} request to {} ", m, url);
                    response = uploadFile(request);
                } else {
                    // else the serialize the bodyParam as requestBody
                    String requestBody = JSON.toJSONString(bodyParam);
                    log.debug("send {} request to {} with request body {}", m, url, requestBody);
                    response = conn.requestBody(requestBody)
                            .execute();
                }
            } else if (data == null
                    || data.isEmpty()) {
                log.debug("send {} request to {}", m, url);
                response = conn.execute();
            } else {
                String requestBody = JSON.toJSONString(data);
                log.debug("send {} request to {} with request body {}", m, url, requestBody);
                response = conn.requestBody(requestBody)
                        .execute();
            }
        }
        return new JsoupHttpResponse(response);
    }

    private void addHeadersAndCookies(HttpRequest request, Connection conn) {
        if (request.getHeaders() != null) {
            conn.headers(request.getHeaders());
        }
        if (request.getCookies() != null) {
            conn.cookies(request.getCookies());
        }
    }

    private boolean isUploadRequest(HttpRequest request, Object bodyParam) {
        return bodyParam != null && (InputStream.class.isAssignableFrom(bodyParam.getClass())
                || File.class.isAssignableFrom(request.getBody().getClass()));
    }

    /**
     * @param request the request
     */
    private Response uploadFile(HttpRequest request) throws IOException {
        Map<String, Object> paramMap = request.getData();
        String formKey = DEFAULT_UPLOAD_FORM_KEY;
        if (request.getFileFormKey() != null
                && !request.getFileFormKey().isEmpty()) {
            formKey = request.getFileFormKey();
        } else if (paramMap != null && paramMap.containsKey(FORM_KEY)) {
            formKey = paramMap.get(FORM_KEY).toString();
        }
        Map<String, String> params = new HashMap<>();
        if (paramMap != null) {
            for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    params.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
        }
        Connection conn = Jsoup.connect(request.getUrl());
        conn.method(Method.POST)
                .timeout(request.getTimeout())
                .ignoreHttpErrors(true)
                .ignoreContentType(true);
        addHeadersAndCookies(request, conn);
        String fileName = "fileName";
        InputStream in;
        if (File.class.isAssignableFrom(request.getBody().getClass())) {
            File file = (File) request.getBody();
            in = new FileInputStream(file);
            fileName = file.getName();
        } else {
            in = (InputStream) request.getBody();
        }
        if (!params.isEmpty()) {
            conn.data(params);
            if (params.containsKey(FILE_NAME)) {
                fileName = params.get(FILE_NAME);
            }
        }
        conn.data(formKey, fileName, in);
        return conn.execute();
    }

    /**
     * query stringify the object
     *
     * @param map the param map
     * @return a query string represent the map
     */
    private String queryStringify(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder("?");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            builder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        return builder.toString().substring(0, builder.length() - 1);
    }
}
