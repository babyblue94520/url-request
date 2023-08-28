package pers.clare.urlrequest;

import pers.clare.urlrequest.exception.URLRequestException;
import pers.clare.urlrequest.handler.ResponseHandler;

import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

@SuppressWarnings("unused")
public class URLRequest<T> {
    private final URL url;
    private final Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, List<Object>> params = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final ResponseHandler<T> handler;
    private CookieManager cookieManager;
    /**
     * The value is true, use original method to redirect. Default use GET method to redirect.
     */
    private boolean redirectAny = false;
    private Integer timeout;
    private Integer readTimeout;
    private String method;
    private String bodyString;

    private Charset uriCharset = StandardCharsets.UTF_8;
    private Charset charset = StandardCharsets.UTF_8;

    private Proxy proxy;

    public URLRequest(String urlString, ResponseHandler<T> handler) {
        try {
            this.url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new URLRequestException(urlString, e.getMessage(), this, e);
        }
        this.handler = handler;
    }

    public URLRequest(String urlString, URLRequest<T> request) {
        try {
            this.url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new URLRequestException(urlString, e.getMessage(), this, e);
        }
        this.headers.putAll(request.headers);
        this.params.putAll(request.params);
        this.handler = request.handler;
        this.cookieManager = request.cookieManager;
        this.redirectAny = request.redirectAny;
        this.timeout = request.timeout;
        this.readTimeout = request.readTimeout;
        this.method = request.method;
        this.bodyString = request.bodyString;
        this.uriCharset = request.uriCharset;
        this.charset = request.charset;
    }

    public static URLRequest<String> build(String url) {
        return build(url, URLRequestUtil.toStringHandler);
    }

    public static <T> URLRequest<T> build(String url, ResponseHandler<T> handler) {
        return new URLRequest<>(url, handler);
    }

    public static <T> URLRequest<T> build(String url, URLRequest<T> request) {
        return new URLRequest<>(url, request);
    }

    public URLResponse<T> go() throws URLRequestException {
        return URLRequestUtil.execute(method, this);
    }

    public URLResponse<T> go(String method) throws URLRequestException {
        this.method = method;
        return URLRequestUtil.execute(method, this);
    }

    public URLResponse<T> get() throws URLRequestException {
        method = URLRequestMethod.GET;
        return URLRequestUtil.execute(method, this);
    }

    public URLResponse<T> post() throws URLRequestException {
        method = URLRequestMethod.POST;
        return URLRequestUtil.execute(method, this);
    }

    public URLResponse<T> put() throws URLRequestException {
        method = URLRequestMethod.PUT;
        return URLRequestUtil.execute(method, this);
    }

    public URLResponse<T> delete() throws URLRequestException {
        method = URLRequestMethod.DELETE;
        return URLRequestUtil.execute(method, this);
    }

    ResponseHandler<T> getHandler() {
        return handler;
    }

    public URLRequest<T> redirectAny(boolean redirectAny) {
        this.redirectAny = redirectAny;
        return this;
    }

    public URLRequest<T> uriCharset(Charset charset) {
        this.uriCharset = charset;
        return this;
    }

    public URLRequest<T> charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public URLRequest<T> method(String method) {
        this.method = method;
        return this;
    }

    public URLRequest<T> timeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    public URLRequest<T> readTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public URLRequest<T> header(String name, String value) {
        List<String> values = this.headers.computeIfAbsent(name, (key) -> new ArrayList<>());
        if (URLRequestUtil.isSingleHeader(name) && values.size() > 0) {
            values.set(0, value);
        } else {
            values.add(value);
        }
        return this;
    }

    public URLRequest<T> headers(Map<String, String> map) {
        if (map != null && map.size() > 0) {
            for (Entry<String, String> entry : map.entrySet()) {
                header(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    public URLRequest<T> body(String body) {
        bodyString = body;
        return this;
    }

    public URLRequest<T> param(String name, Object value) {
        if (value == null) return this;
        params.computeIfAbsent(name, (key) -> new ArrayList<>())
                .add(value);
        return this;
    }

    public URLRequest<T> params(Map<String, Object> map) {
        if (map != null && map.size() > 0) {
            for (Entry<String, Object> entry : map.entrySet()) {
                param(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    public URLRequest<T> cookieManager(CookieManager cookieManager) {
        this.cookieManager = cookieManager;
        return this;
    }


    public URLRequest<T> proxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }


    public URL getUrl() {
        return url;
    }

    public Map<String, List<Object>> getParams() {
        return params;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public CookieManager getCookieManager() {
        return cookieManager;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public boolean isRedirectAny() {
        return redirectAny;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public String getMethod() {
        return method;
    }

    public String getBodyString() {
        return bodyString;
    }

    public Charset getUriCharset() {
        return uriCharset;
    }

    public Charset getCharset() {
        return charset;
    }

    @Override
    public String toString() {
        return "URLRequest{" +
               "url=" + url +
               ", params=" + params +
               ", headers=" + headers +
               ", cookieManager=" + cookieManager +
               ", redirectCrossProtocol=" + redirectAny +
               ", timeout=" + timeout +
               ", readTimeout=" + readTimeout +
               ", method='" + method + '\'' +
               ", bodyString='" + bodyString + '\'' +
               '}';
    }
}
