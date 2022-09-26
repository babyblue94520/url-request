package pers.clare.urlrequest.handler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@FunctionalInterface
public interface ResponseHandler<T> {
    T apply(InputStream in, Charset charset) throws IOException;
}
