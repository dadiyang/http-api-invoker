package com.github.dadiyang.httpinvoker.requestor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * an http requestor base on HttpClient
 *
 * @author huangxuyang
 * @since 2019-06-13
 */
public class HttpClientRequestor implements Requestor {
    private static final Logger log = LoggerFactory.getLogger(HttpClientRequestor.class);

    @Override
    public HttpResponse sendRequest(HttpRequest request) throws IOException {

        return null;
    }


}
