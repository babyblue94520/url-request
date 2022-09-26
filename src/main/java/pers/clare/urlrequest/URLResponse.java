package pers.clare.urlrequest;

import pers.clare.urlrequest.handler.ResponseHandler;

import java.net.URL;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class URLResponse<T> {
    private URL url;
    private int status;
    private Map<String, List<String>> headers;
    private String message;
    private T body;

    URLResponse(){}

    static <T> URLResponse<T> build(ResponseHandler<T> handler){
        return new URLResponse<>();
    }

    URLResponse<T> url(URL url) {
        this.url = url;
        return this;
    }

    URLResponse<T> status(int status) {
        this.status = status;
        return this;
    }

    URLResponse<T> headers(Map<String, List<String>> headers) {
        this.headers = headers;
        return this;
    }

    URLResponse<T> message(String message) {
        this.message = message;
        return this;
    }

    URLResponse<T> body(T body) {
        this.body = body;
        return this;
    }

    public URL getUrl() {
        return url;
    }

    public int getStatus() {
        return status;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public String getMessage() {
        return message;
    }

    public T getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "URLResponse{" +
                "url=" + url +
                ", status=" + status +
                ", headers=" + headers +
                ", message='" + message + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
