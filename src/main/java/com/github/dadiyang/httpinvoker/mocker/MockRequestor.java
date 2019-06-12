package com.github.dadiyang.httpinvoker.mocker;

import com.github.dadiyang.httpinvoker.requestor.DefaultHttpRequestor;
import com.github.dadiyang.httpinvoker.requestor.HttpRequest;
import com.github.dadiyang.httpinvoker.requestor.HttpResponse;
import com.github.dadiyang.httpinvoker.requestor.Requestor;
import com.github.dadiyang.httpinvoker.util.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Mock 请求器，使用这个请求器可以配置一些规则，当发起的请求符合这个规则时，直接返回给定的结果而不发起真实请求
 * <p>
 * 没有匹配的规则才发起真实请求
 * <p>
 * 注：只用于开发环境使用，生产环境千万不要使用此请求器！！
 *
 * @author dadiyang
 * @since 2019-05-31
 */
public class MockRequestor implements Requestor {
    private static final Logger log = LoggerFactory.getLogger(MockRequestor.class);
    /**
     * 是否忽略环境警告信息，默认每次使用本请求器时都会打印警告
     */
    private boolean ignoreWarning;
    private final List<MockRule> mockRules;
    private final Requestor realRequestor;

    public MockRequestor() {
        this(new ArrayList<MockRule>(), new DefaultHttpRequestor());
    }

    public MockRequestor(List<MockRule> mockRules, Requestor realRequestor) {
        if (realRequestor == null) {
            throw new IllegalArgumentException("必须配置一个真实请求的请求器");
        }
        this.mockRules = mockRules;
        this.realRequestor = realRequestor;
        log.info("初始化 MOCK 请求器，注意：一般只用于开发环境使用，生产环境千万不要使用此请求器！！");
    }

    public MockRequestor(List<MockRule> mockRules) {
        this(new ArrayList<MockRule>(mockRules), new DefaultHttpRequestor());
    }

    public void addRule(MockRule rule) {
        mockRules.add(rule);
    }

    @Override
    public HttpResponse sendRequest(HttpRequest request) throws IOException {
        ObjectUtils.requireNonNull(request, "请求不能为 null");
        ObjectUtils.requireNonNull(request.getUrl(), "请求 url 不能为 null");
        if (!ignoreWarning) {
            log.warn("当前使用 MOCK 请求器，注意：一般只在开发环境使用，生产环境千万不要使用此请求器！！");
        }
        List<MockRule> matchedRule = new LinkedList<MockRule>();
        for (MockRule rule : mockRules) {
            if (isMatch(request, rule)) {
                matchedRule.add(rule);
            }
        }
        // 没有匹配则发起真实请求
        if (matchedRule.isEmpty()) {
            log.info("请求没有找到对应的 mock，所以发起真实请求: " + request.getUrl());
            return realRequestor.sendRequest(request);
        }
        if (matchedRule.size() > 1) {
            List<MockRule> exactlyMatches = new LinkedList<MockRule>();
            // 在匹配到的规则器里找url或uri完全匹配的
            for (MockRule rule : matchedRule) {
                if (ObjectUtils.equals(rule.getUrlReg(), request.getUrl())
                        || ObjectUtils.equals(rule.getUriReg(), getUri(request.getUrl()))) {
                    exactlyMatches.add(rule);
                }
            }
            matchedRule = exactlyMatches;
            // 如果还是有多个，则抛出异常
            if (matchedRule.size() > 1) {
                throw new IllegalStateException("一个请求匹配到 " + matchedRule.size() + " 个 mock 规则，请确认是否重复添加: " + request.getUrl());
            }
        }
        MockRule rule = matchedRule.get(0);
        log.info("mock匹配成功，使用匹配到的规则，请求url: " + request.getUrl() + ", 规则: " + rule);
        return rule.getResponse();
    }

    private String getUri(String url) {
        try {
            return new URL(url).getPath();
        } catch (MalformedURLException e) {
            return url;
        }
    }

    private boolean isMatch(HttpRequest request, MockRule rule) {
        if (rule == null) {
            return false;
        }
        if (rule.getMethod() != null && !rule.getMethod().isEmpty()
                && !ObjectUtils.equals(request.getMethod().toUpperCase(), rule.getMethod().toUpperCase())) {
            log.info("请求方法规则不匹配: requestMethod: " + request.getMethod() + ", ruleMethod: " + rule.getMethod());
            return false;
        }
        if (!isUrlOrUriMatch(request, rule)) {
            return false;
        }
        if (!isMapMatch(rule.getData(), request.getData())) {
            log.info("参数规则不匹配: requestData: " + request.getData() + ", ruleData: " + rule.getData());
            return false;
        }
        if (!ObjectUtils.equals(rule.getBody(), request.getBody())) {
            log.info("请求体规则不匹配: requestBody: " + request.getBody() + ", ruleBody: " + rule.getBody());
            return false;
        }
        // 校验 cookie
        if (!isMapMatch(rule.getCookies(), request.getCookies())) {
            log.info("Cookie规则不匹配: requestCookies: " + request.getCookies() + ", ruleCookies: " + rule.getCookies());
            return false;
        }
        // 校验 header
        if (!isMapMatch(rule.getHeaders(), request.getHeaders())) {
            log.info("Header规则不匹配: requestHeaders: " + request.getHeaders() + ", ruleHeaders: " + rule.getHeaders());
            return false;
        }
        // 全部校验通过，则匹配
        return true;
    }

    private boolean isUrlOrUriMatch(HttpRequest request, MockRule m) {
        // url 规则
        if (m.getUrlReg() != null && !m.getUrlReg().isEmpty()) {
            boolean urlMatch = isStringMatch(request.getUrl(), m.getUrlReg());
            if (!urlMatch) {
                log.info("url规则不匹配: requestUrl: " + request.getUrl() + ", ruleUrl: " + m.getUrlReg());
                return false;
            }
        } else if (m.getUriReg() != null && !m.getUriReg().isEmpty()) {
            // uri 规则
            String uri = getUri(request.getUrl());
            boolean uriMatch = isStringMatch(uri, m.getUriReg());
            if (!uriMatch) {
                log.info("uri 规则不匹配: requestUri: " + uri + ", ruleUri: " + m.getUriReg());
                return false;
            }
        } else {
            log.info("url 和 uri 规则不能同时为空不匹配: requestUrl: " + request.getUrl() + ", ruleUrl: " + m.getUrlReg());
            return false;
        }
        return true;
    }

    private boolean isStringMatch(String uri, String urlReg) {
        return ObjectUtils.equals(uri, urlReg)
                || uri.matches(urlReg);
    }

    private boolean isMapMatch(Map<String, ?> mapFromMockRule, Map<String, ?> mapFromRequest) {
        // 无需匹配
        if (mapFromMockRule == null || mapFromMockRule.isEmpty()) {
            return true;
        }
        // 请求中没有 cookies
        if (mapFromRequest == null || mapFromRequest.isEmpty()) {
            return false;
        }
        for (Map.Entry<String, ?> entry : mapFromMockRule.entrySet()) {
            Object value = mapFromRequest.get(entry.getKey());
            // 只要有一个 cookie 与请求不符，则不匹配
            if (!ObjectUtils.equals(entry.getValue(), value)) {
                return false;
            }
        }
        return true;
    }

    public void setIgnoreWarning(boolean ignoreWarning) {
        this.ignoreWarning = ignoreWarning;
    }
}
