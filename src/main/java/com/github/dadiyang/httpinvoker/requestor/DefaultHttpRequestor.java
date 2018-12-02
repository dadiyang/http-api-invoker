package com.github.dadiyang.httpinvoker.requestor;

import com.alibaba.fastjson.JSON;
import com.github.dadiyang.httpinvoker.annotation.HttpReq;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * The default implementation of {@link Requestor} that send the request using Jsoup which is a elegant http client I've ever use.
 * <p>
 * the parameter will be formatted according to the request method.
 *
 * @author huangxuyang
 * @date 2018/11/1
 */
public class DefaultHttpRequestor implements Requestor {
    private static final Logger log = LoggerFactory.getLogger(Requestor.class);
    public static final int OK_CODE_L = 200;
    public static final int OK_CODE_H = 300;

    /**
     * {@inheritDoc}
     */
    @Override
    public String sendRequest(String url, Map<String, Object> params, Object[] args, HttpReq anno) throws IOException {
        // send request
        Connection.Method m = Connection.Method.valueOf(anno.method().toUpperCase());
        Connection.Response response;
        if (!m.hasBody()) {
            // to those method without body such as GET/DELETE we use QueryString
            String fullUrl = url + queryStringify(params);
            log.debug("send {} request to {}", m, fullUrl);
            response = Jsoup.connect(fullUrl)
                    .method(m)
                    .timeout(anno.timeout())
                    .header("Content-Type", "application/json; charset=utf-8")
                    .ignoreContentType(true)
                    .execute();
        } else {
            Connection conn = Jsoup.connect(url)
                    .method(m)
                    .timeout(anno.timeout())
                    .header("Content-Type", "application/json; charset=utf-8")
                    .ignoreContentType(true);
            if (args == null || args.length <= 0) {
                log.debug("send {} request to {}", m, url);
                response = conn.execute();
            } else {
                String requestBody = JSON.toJSONString(params == null ? args[0] : params);
                log.debug("send {} request to {} with request body {}", m, url, requestBody);
                response = conn.requestBody(requestBody)
                        .execute();
            }
        }
        if (response.statusCode() > OK_CODE_L && response.statusCode() < OK_CODE_H) {
            return response.body();
        } else {
            return null;
        }
    }

    /**
     * query stringify the object
     */
    protected String queryStringify(Map<String, Object> map) {
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
