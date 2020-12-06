package com.github.dadiyang.httpinvoker.requestor;

import com.github.dadiyang.httpinvoker.serializer.JsonSerializerDecider;
import com.github.dadiyang.httpinvoker.util.ObjectUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

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
public class JsoupRequestor implements Requestor {
    private static final Logger log = LoggerFactory.getLogger(JsoupRequestor.class);
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
                    // unlimited size
                    .maxBodySize(0)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true);
            addHeadersAndCookies(request, conn);
            setContentType(request, conn);
            response = conn.execute();
        } else {
            Connection conn = Jsoup.connect(url)
                    .method(m)
                    .timeout(timeout)
                    // unlimited size
                    .maxBodySize(0)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true);
            addHeadersAndCookies(request, conn);
            setContentType(request, conn);
            Map<String, Object> data = request.getData();
            // body first
            if (request.getBody() != null) {
                Object bodyParam = request.getBody();
                // if the body param is MultiPart or InputStream, submit a multipart form
                if (isUploadRequest(bodyParam)) {
                    log.debug("upload file {} request to {} ", m, url);
                    response = uploadFile(request);
                } else {
                    if (useJson(request, bodyParam)) {
                        response = conn.requestBody(JsonSerializerDecider.getJsonSerializer().serialize(bodyParam)).execute();
                    } else {
                        Map<String, String> map = toMapStringString(bodyParam, "");
                        response = conn.data(map).execute();
                    }
                }
            } else if (data == null
                    || data.isEmpty()) {
                log.debug("send {} request to {}", m, url);
                response = conn.execute();
            } else {
                if (useJson(request, data)) {
                    if (m == Method.PATCH) {
                        // use X-HTTP-Method-Override header to send a fake PATCH request
                        conn.method(Method.POST).header("X-HTTP-Method-Override", "PATCH");
                    }
                    response = conn.requestBody(JsonSerializerDecider.getJsonSerializer().serialize(data)).execute();
                } else {
                    Map<String, String> map = toMapStringString(data, "");
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
                || !request.getHeaders().containsKey(CONTENT_TYPE)
                || ObjectUtils.equals(request.getHeaders().get(CONTENT_TYPE), APPLICATION_JSON);
    }

    private void addHeadersAndCookies(HttpRequest request, Connection conn) {
        if (request.getHeaders() != null) {
            conn.headers(request.getHeaders());
        }
        if (request.getCookies() != null) {
            conn.cookies(request.getCookies());
        }
    }

    /**
     * @param request the request
     */
    private Response uploadFile(HttpRequest request) throws IOException {
        Connection conn = Jsoup.connect(request.getUrl());
        conn.method(Method.POST)
                .timeout(request.getTimeout())
                .ignoreHttpErrors(true)
                // unlimited size
                .maxBodySize(0)
                .ignoreContentType(true);
        addHeadersAndCookies(request, conn);
        Object body = request.getBody();
        // handle MultiPart
        if (body instanceof MultiPart) {
            return handleMultiPart(conn, (MultiPart) body);
        } else {
            return handleMultiPart(conn, convertInputStreamAndFile(request));
        }
    }

    private Response handleMultiPart(Connection conn, MultiPart body) throws IOException {
        for (MultiPart.Part part : body.getParts()) {
            if (part.getKey() == null || part.getValue() == null) {
                throw new IllegalArgumentException("both key and value of part must not be null");
            }
            if (part.getInputStream() != null) {
                conn.data(part.getKey(), part.getValue(), part.getInputStream());
            } else {
                conn.data(part.getKey(), part.getValue());
            }
        }
        return conn.execute();
    }
}
