package pers.clare.urlrequest;

import pers.clare.urlrequest.exception.URLRequestException;
import pers.clare.urlrequest.exception.URLResponseException;
import pers.clare.urlrequest.handler.ResponseHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLRequestUtil {
    public static final int CONNECTION_TIMEOUT = 60000;

    public static final int GET_READ_TIMEOUT = 360000;

    public static final int OTHER_READ_TIMEOUT = 3600000;

    public static final byte[] NULL = "null".getBytes();

    public static final byte[] EQUAL = "=".getBytes();

    public static final byte[] AND = "&".getBytes();

    public static final ResponseHandler<String> toStringHandler = URLRequestUtil::streamToString;

    private static final Set<String> singleHeaders = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    private static final Pattern contentTypePattern = Pattern.compile("^content-type$", Pattern.CASE_INSENSITIVE);

    private static final Pattern charsetPattern = Pattern.compile("charset=(\\S+)", Pattern.CASE_INSENSITIVE);

    static {
        singleHeaders.add(HeaderNames.CONTENT_TYPE);
        singleHeaders.add(HeaderNames.HOST);
        singleHeaders.add(HeaderNames.USER_AGENT);
        singleHeaders.add(HeaderNames.CONNECTION);
    }

    public static boolean isSingleHeader(String name) {
        return singleHeaders.contains(name);
    }

    /**
     * 執行連線產生資料.
     *
     * @param method  the method
     * @param request the request
     * @return the URL response
     * @throws URLResponseException the URL request exception
     */
    static <T> URLResponse<T> execute(
            String method
            , URLRequest<T> request
    ) throws URLResponseException {
        return doExecute(method, null, request);
    }

    /**
     * Do execute.
     *
     * @param method            the method
     * @param request           the request
     * @param redirectLocations the redirect locations
     * @return the URL response
     * @throws URLResponseException the URL request exception
     */
    private static <T> URLResponse<T> doExecute(
            String method
            , Set<String> redirectLocations
            , URLRequest<T> request
    ) throws URLResponseException {
        HttpURLConnection connection;
        int status;
        String message;
        Map<String, List<String>> headers;
        try {
            connection = setConnection(method, request);
            status = connection.getResponseCode();
            message = connection.getResponseMessage();
            headers = connection.getHeaderFields();

            Charset charset = findCharset(headers);
            if (charset == null) {
                charset = request.getCharset();
            }

            if (request.getCookieManager() != null) {
                request.getCookieManager().put(request.getUrl().toURI(), headers);
            }
            if (status > 300) {
                // 重新定向
                if (request.isRedirectAny()
                        && (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM)) {
                    if (redirectLocations == null) {
                        redirectLocations = new HashSet<>();
                    }
                    return redirectCrossProtocol(connection, request, redirectLocations);
                } else {
                    String body;
                    if (connection.getErrorStream() == null) {
                        body = streamToString(connection.getInputStream(), charset);
                    } else {
                        body = streamToString(connection.getErrorStream(), charset);
                    }
                    throw new URLResponseException(connection.getURL().toString()
                            , URLResponse.build(toStringHandler)
                            .url(connection.getURL())
                            .status(status)
                            .headers(headers)
                            .message(message)
                            .body(body)
                    );
                }
            }
            return URLResponse.build(request.getHandler())
                    .url(connection.getURL())
                    .status(status)
                    .headers(headers)
                    .message(message)
                    .body(request.getHandler().apply(connection.getInputStream(), charset))
                    ;
        } catch (URLResponseException e) {
            throw e;
        } catch (UnknownHostException e) {
            throw new URLRequestException(request.getUrl().toString(), "unknown host " + e.getMessage(), request, e.getCause());
        } catch (Exception e) {
            e.printStackTrace();
            throw new URLRequestException(request.getUrl().toString(), e.getMessage(), request, e.getCause());
        }
    }

    /**
     * 重新定向.
     *
     * @param connection        the connection
     * @param request           the request
     * @param redirectLocations the redirect locations
     * @return the URL response
     * @throws Exception the exception
     */
    private static <T> URLResponse<T> redirectCrossProtocol(HttpURLConnection connection, URLRequest<T> request, Set<String> redirectLocations) throws Exception {
        String location = getLocation(connection);
        int status = connection.getResponseCode();
        Map<String, List<String>> headers = connection.getHeaderFields();
        if (request.getCookieManager() != null) {
            request.getCookieManager().put(request.getUrl().toURI(), headers);
        }
        if (location == null) {
            throw new URLResponseException(connection.getURL().toString()
                    , URLResponse.build(toStringHandler)
                    .url(connection.getURL())
                    .status(status)
                    .headers(headers)
                    .message("Has Redirect, but header not location")
                    .body("")
            );
        } else if (redirectLocations.contains(location)) {
            throw new URLResponseException(connection.getURL().toString()
                    , URLResponse.build(toStringHandler)
                    .url(connection.getURL())
                    .status(status)
                    .headers(headers)
                    .message("Infinity redirects")
                    .body("")
            );
        }
        redirectLocations.add(location);
        return doExecute(request.getMethod(), redirectLocations, URLRequest.build(location, request));
    }

    /**
     * Gets the location.
     *
     * @param connection the connection
     * @return the location
     */
    private static String getLocation(HttpURLConnection connection) {
        for (Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
            if (!HeaderNames.LOCATION.equalsIgnoreCase(entry.getKey())) continue;
            return String.join(",", entry.getValue());
        }
        return null;
    }

    /**
     * Sets the connection.
     */
    private static <T> HttpURLConnection setConnection(String method, URLRequest<T> request) throws IOException, URISyntaxException {
        HttpURLConnection connection;
        Charset charset = findCharset(request.getHeaders());
        if (charset == null) {
            charset = request.getCharset();
        }
        boolean get = URLRequestMethod.GET.equalsIgnoreCase(method);
        if (get) {
            connection = (HttpURLConnection) getEncodeURL(request.getUrl(), request.getParams(), request.getUriCharset()).openConnection();
        } else {
            connection = (HttpURLConnection) getEncodeURL(request.getUrl(), null, charset).openConnection();
        }
        connection.setDoOutput(true);
        connection.setRequestMethod(method.toUpperCase());
        if (request.isRedirectAny()) {
            connection.setInstanceFollowRedirects(false);
        }
        if (request.getTimeout() == null) {
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
        } else {
            connection.setConnectTimeout(request.getTimeout());
        }
        if (request.getReadTimeout() == null) {
            if (get) {
                connection.setReadTimeout(GET_READ_TIMEOUT);
            } else {
                connection.setReadTimeout(OTHER_READ_TIMEOUT);
            }
        } else {
            connection.setReadTimeout(request.getReadTimeout());
        }

        mergeCookie(request);
        writeHeaders(connection, request.getHeaders());
        if (!get) {
            if (request.getBodyString() == null) {
                write(connection.getOutputStream(), request.getParams(), charset);
            } else {
                write(connection.getOutputStream(), request.getBodyString(), charset);
            }
        }
        return connection;
    }

    /**
     * 根據參數產生GET URI.
     */
    public static URL getEncodeURL(URL url, Map<String, List<Object>> params, Charset charset) throws MalformedURLException {
        if (url.getQuery() == null && params == null) return url;
        return new URL(url, encodeUrlFile(url, params, charset));
    }

    private static <T> void mergeCookie(URLRequest<T> request) throws URISyntaxException {
        if (request.getCookieManager() == null) return;
        List<HttpCookie> cookies = request.getCookieManager().getCookieStore().get(request.getUrl().toURI());
        if (cookies.size() == 0) return;
        Map<String, String> cookieMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (HttpCookie cookie : cookies) {
            cookieMap.put(cookie.getName(), cookie.getValue());
        }
        List<String> values = request.getHeaders().get("cookie");
        if (values != null && values.size() > 0) {
            for (String value : values) {
                String[] split = value.split("=");
                if (split.length > 1) cookieMap.put(split[0], split[1]);
            }
        }
        List<String> newCookies = new ArrayList<>();
        for (Entry<String, String> entry : cookieMap.entrySet()) {
            newCookies.add(entry.getKey() + "=" + entry.getValue());
        }
        request.getHeaders().put("cookie", newCookies);
    }

    /**
     * 初始化表頭.
     */
    private static void writeHeaders(URLConnection connection, Map<String, List<String>> headers) {
        if (headers == null || headers.size() == 0) return;
        for (Entry<String, List<String>> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), String.join("; ", entry.getValue()));
        }
    }

    private static Charset findCharset(Map<String, List<String>> headers) {
        try {
            for (Entry<String, List<String>> entry : headers.entrySet()) {
                if (!hasLength(entry.getKey())) continue;
                if (contentTypePattern.matcher(entry.getKey()).find()) {
                    for (String value : entry.getValue()) {
                        Matcher matcher = charsetPattern.matcher(value);
                        if (matcher.find()) {
                            return Charset.forName(matcher.group(1));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 取得URL encode後的URL.
     */
    private static String encodeUrlFile(URL url, Map<String, List<Object>> params, Charset charset) {
        StringBuilder sb = new StringBuilder();
        sb.append(url.getPath());
        StringBuilder query = mergeQueryParam(url.getQuery(), params, charset);
        if (query.length() > 0) {
            sb.append('?');
            sb.append(query);
        }
        if (url.getRef() != null) {
            sb.append('#');
            sb.append(url.getRef());
        }
        return sb.toString();
    }

    /**
     * Merge query param.
     */
    private static StringBuilder mergeQueryParam(
            String query
            , Map<String, List<Object>> params
            , Charset charset
    ) {
        StringBuilder sb = new StringBuilder();
        if (hasLength(query)) {
            String[] queryParams = query.split("&");
            int index;
            for (String param : queryParams) {
                index = param.indexOf('=');
                if (index > -1) {
                    encode(sb, param.substring(0, index), param.substring(index + 1), charset);
                } else {
                    encode(sb, "", param, charset);
                }
            }
        }
        if (params != null && params.size() > 0) {
            for (Entry<String, List<Object>> entry : params.entrySet()) {
                for (Object value : entry.getValue())
                    encode(sb, entry.getKey(), value, charset);
            }
        }
        if (sb.length() > 0) {
            int index = sb.length() - 1;
            if (sb.lastIndexOf("&") == index) {
                sb.deleteCharAt(index);
            }
        }
        return sb;
    }

    /**
     * Encode.
     */
    private static void encode(StringBuilder sb, String name, Object value, Charset charset) {
        if (value == null) return;
        sb.append(URLEncoder.encode(name, charset))
                .append('=')
                .append(URLEncoder.encode(value.toString(), charset))
                .append('&');
    }

    /**
     * Stream to string.
     */
    private static String streamToString(InputStream in, Charset charset) throws IOException {
        if (in == null) return "";
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(charset);
    }

    private static void write(
            OutputStream os
            , Map<String, List<Object>> params
            , Charset charset
    ) throws IOException {
        if (params == null || params.size() == 0) return;
        boolean first = true;
        for (Entry<String, List<Object>> entry : params.entrySet()) {
            for (Object value : entry.getValue()) {
                if (value == null) continue;
                if (first) {
                    first = false;
                } else {
                    os.write(AND);
                }
                writeParameter(os, entry.getKey(), value, charset);
            }
        }
        os.flush();
    }

    private static void writeParameter(
            OutputStream os
            , String name
            , Object value
            , Charset charset
    ) throws IOException {
        if (value == null) return;
        if (name == null) {
            os.write(NULL);
        } else {
            os.write(URLEncoder.encode(name, charset).getBytes(charset));
        }
        os.write(EQUAL);
        os.write(URLEncoder.encode(value.toString(), charset).getBytes(charset));
    }

    /**
     * Write.
     */
    private static void write(
            OutputStream os
            , String str
            , Charset charset
    ) throws IOException {
        if (hasLength(str)) {
            os.write(str.getBytes(charset));
            os.flush();
        }
    }

    /**
     * Creates the request.
     */
    @SuppressWarnings("unused")
    public static <T> URLRequest<T> createRequest(
            HttpRequest bean
            , ResponseHandler<T> handler
    ) {
        URLRequest<T> request = URLRequest.build(bean.getUrl(), handler)
                .method(bean.getMethod())
                .timeout(bean.getConnectionTimeout())
                .readTimeout(bean.getReadTimeout())
                .redirectAny(true);

        int index;
        if (hasLength(bean.getBody()) && hasLength(bean.getParams())) {
            String[] params = bean.getParams().split("\n");
            for (String param : params) {
                if (!hasLength(param)) continue;
                index = param.indexOf(':');
                if (index > -1) {
                    request.param(param.substring(0, index).trim(), param.substring(index + 1).trim());
                } else {
                    request.param("", param);
                }
            }
        } else {
            request.body(bean.getBody());
        }
        if (hasLength(bean.getHeaders())) {
            String[] headers = bean.getHeaders().split("\n");
            for (String header : headers) {
                if (!hasLength(header)) continue;
                index = header.indexOf(':');
                if (index > -1) {
                    request.header(header.substring(0, index).trim(), header.substring(index + 1).trim());
                } else {
                    request.header("", header);
                }
            }
        }
        return request;
    }

    private static boolean hasLength(String str){
        return str != null && str.length() > 0;
    }
}
