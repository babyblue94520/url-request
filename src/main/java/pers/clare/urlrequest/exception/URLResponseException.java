package pers.clare.urlrequest.exception;


import pers.clare.urlrequest.URLResponse;

public class URLResponseException extends RuntimeException {
    private final URLResponse<String> response;

    public URLResponseException(String url, URLResponse<String> response) {
        super(String.format("Request %s %d %s", url, response.getStatus(), response.getMessage()));
        this.response = response;
    }

    public URLResponse<String> getResponse() {
        return response;
    }
}
