package pers.clare.urlrequest.exception;


import pers.clare.urlrequest.URLRequest;

@SuppressWarnings("unused")
public class URLRequestException extends RuntimeException {
    private final URLRequest<?> request;

    public URLRequestException(String url, String message, URLRequest<?> request, Throwable cause) {
        super(url + " " + message, cause);
        this.request = request;
    }

    public URLRequest<?> getRequest() {
        return request;
    }
}
