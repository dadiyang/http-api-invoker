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
import java.util.Objects;

import static com.github.dadiyang.httpinvoker.util.ParamUtils.*;
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
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

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
            String qs = toQueryString(request.getData());
            String fullUrl = request.getUrl() + qs;
            log.debug("send {} request to {}", m, fullUrl);
            Connection conn = Jsoup.connect(fullUrl)
                    .method(m)
                    .timeout(timeout)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true);
            addHeadersAndCookies(request, conn);
            setContentType(request, conn);
            response = conn.execute();
        } else {
            Connection conn = Jsoup.connect(url)
                    .method(m)
                    .timeout(timeout)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true);
            addHeadersAndCookies(request, conn);
            setContentType(request, conn);
            Map<String, Object> data = request.getData();
            // body first
            if (request.getBody() != null) {
                Object bodyParam = request.getBody();
                // if the body param is InputStream, upload it
                if (isUploadRequest(request, bodyParam)) {
                    log.debug("upload file {} request to {} ", m, url);
                    response = uploadFile(request);
                } else {
                    if (useJson(request, bodyParam)) {
                        response = conn.requestBody(JSON.toJSONString(bodyParam)).execute();
                    } else {
                        Map<String, String> map = toMapStringString(bodyParam);
                        response = conn.data(map).execute();
                    }
                }
            } else if (data == null
                    || data.isEmpty()) {
                log.debug("send {} request to {}", m, url);
                response = conn.execute();
            } else {
                if (useJson(request, data)) {
                    response = conn.requestBody(JSON.toJSONString(data)).execute();
                } else {
                    Map<String, String> map = toMapStringString(data);
                    response = conn.data(map).execute();
                }
            }
        }
        return new JsoupHttpResponse(response);
    }

    private void setContentType(HttpRequest request, Connection conn) {
        // set a default Content-Type if not provided
        if (request.getHeaders() == null || !request.getHeaders().containsKey(CONTENT_TYPE)) {
            conn.header(CONTENT_TYPE, APPLICATION_JSON);
        }
    }

    /**
     * either param is a collection or Content-Type absence or equals to APPLICATION_JSON
     */
    private boolean useJson(HttpRequest request, Object param) {
        // collection can only be send by json currently
        return isCollection(param) || request.getHeaders() == null
                || Objects.equals(request.getHeaders().get(CONTENT_TYPE), APPLICATION_JSON);
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
}
