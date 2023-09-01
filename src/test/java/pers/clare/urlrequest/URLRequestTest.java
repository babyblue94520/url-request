package pers.clare.urlrequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import pers.clare.server.Application;
import pers.clare.urlrequest.exception.URLRequestException;
import pers.clare.urlrequest.exception.URLResponseException;
import pers.clare.urlrequest.handler.ResponseHandler;
import pers.clare.urlrequest.util.PerformanceUtil;
import pers.clare.urlrequest.vo.Data;

import java.io.InputStreamReader;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@DisplayName("URLRequestTest")
@TestInstance(PER_CLASS)
class URLRequestTest {
    public static final String port = "8080";

    static {
        System.setProperty("http.maxConnections", "2000");
    }

    @BeforeAll
    void before() {
        Application.main(new String[]{"--server.port=" + URLRequestTest.port});
    }

    @Nested
    @TestInstance(PER_CLASS)
    class string_response {
        private final String name = "body";
        private final String value = "身體";
        private final String answer = "param" + value;
        private final String url = toUrl("/string");
        private final String url_404 = toUrl("/string" + UUID.randomUUID());


        private String toUrl(String path) {
            String queryString = "?param=param";
            return "http://127.0.0.1:" + port + path + queryString;
        }

        private URLRequest<String> build(String method) {
            return URLRequest
                    .build(url)
                    .method(method)
                    .header(HeaderNames.CONTENT_TYPE, HeaderValues.X_WWW_FORM_URLENCODED + "; charset=ms950")
                    .param(name, value);
        }

        @Test
        void error_url() {
            assertThrows(URLRequestException.class, () -> URLRequest.build(UUID.randomUUID().toString()).param(name, value));
        }

        @Test
        void get_404() {
            assertThrows(URLResponseException.class, URLRequest.build(url_404).param(name, value)::get);
        }

        @Test
        void get() {
            assertEquals(answer, build(URLRequestMethod.GET).go().getBody());
        }

        @Test
        void post() {
            assertEquals(answer, build(URLRequestMethod.POST).go().getBody());
        }

        @Test
        void put() {
            assertEquals(answer, build(URLRequestMethod.PUT).go().getBody());
        }

        @Test
        void delete() {
            assertEquals(answer, build(URLRequestMethod.DELETE).go().getBody());
        }

        @Test
        void performance() throws Exception {
            PerformanceUtil.byCount(100, () -> {
                get();
                get_404();
            });
            PerformanceUtil.byTime(100, 10000, () -> {
                get();
            });
        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    class json_response {
        private final Data data = new Data(System.currentTimeMillis(), new String[]{"1", "2"});
        private final String url = toUrl();

        private String toUrl() {
            String queryString = "?param=param";
            return "http://127.0.0.1:" + port + "/json" + queryString;
        }

        private final ObjectMapper om = new ObjectMapper();

        private final ResponseHandler<Data> handler = (in, charset) -> om.readValue(new InputStreamReader(in, charset), Data.class);

        private URLRequest<Data> build(String method) {
            try {
                return URLRequest
                        .build(url, handler)
                        .method(method)
                        .header(HeaderNames.CONTENT_TYPE, HeaderValues.JSON)
                        .body(om.writeValueAsString(data))
                        ;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        @Test
        void get() {
            URLResponseException exception = assertThrows(URLResponseException.class, build(URLRequestMethod.GET)::go);
            assertEquals(400, exception.getResponse().getStatus());
        }

        @Test
        void post() {
            assertEquals(data, build(URLRequestMethod.POST).go().getBody());
        }

        @Test
        void put() {
            assertEquals(data, build(URLRequestMethod.PUT).go().getBody());
        }

        @Test
        void delete() {
            assertEquals(data, build(URLRequestMethod.DELETE).go().getBody());
        }
        @Test
        void performance() throws Exception {
            PerformanceUtil.byTime(100, 10000, this::post);
        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    class bytes_json_response {
        private final Data data = new Data(System.currentTimeMillis(), new String[]{"1", "2"});
        private final String url = toUrl();

        private String toUrl() {
            String queryString = "?param=param";
            return "http://127.0.0.1:" + port + "/json" + queryString;
        }

        private final ObjectMapper om = new ObjectMapper();

        private final ResponseHandler<Data> handler = (in, charset) -> om.readValue(new InputStreamReader(in, charset), Data.class);

        private URLRequest<Data> build(String method) {
            try {
                return URLRequest
                        .build(url, handler)
                        .method(method)
                        .header(HeaderNames.CONTENT_TYPE, HeaderValues.JSON)
                        .body(om.writeValueAsBytes(data))
                        ;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        @Test
        void get() {
            URLResponseException exception = assertThrows(URLResponseException.class, build(URLRequestMethod.GET)::go);
            assertEquals(400, exception.getResponse().getStatus());
        }

        @Test
        void post() {
            assertEquals(data, build(URLRequestMethod.POST).go().getBody());
        }

        @Test
        void put() {
            assertEquals(data, build(URLRequestMethod.PUT).go().getBody());
        }

        @Test
        void delete() {
            assertEquals(data, build(URLRequestMethod.DELETE).go().getBody());
        }
        @Test
        void performance() throws Exception {
            PerformanceUtil.byTime(100, 10000, this::post);
        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    class redirect_response {
        private final Data data = new Data(System.currentTimeMillis(), new String[]{"1", "2"});
        private final String url = toUrl();

        private String toUrl() {
            String queryString = "?path=json";
            return "http://127.0.0.1:" + port + "/redirect" + queryString;
        }

        private final ObjectMapper om = new ObjectMapper();

        private final ResponseHandler<Data> handler = (in, charset) -> om.readValue(new InputStreamReader(in, charset), Data.class);

        private URLRequest<Data> build(String method) {
            try {
                return URLRequest
                        .build(url, handler)
                        .redirectAny(true)
                        .method(method)
                        .header(HeaderNames.CONTENT_TYPE, HeaderValues.JSON)
                        .body(om.writeValueAsString(data))
                        ;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        @Test
        void get() {
            URLResponseException exception = assertThrows(URLResponseException.class, build(URLRequestMethod.GET)::go);
            assertEquals(400, exception.getResponse().getStatus());
        }

        @Test
        void post() {
            assertEquals(data, build(URLRequestMethod.POST).go().getBody());
        }

        @Test
        void put() {
            assertEquals(data, build(URLRequestMethod.PUT).go().getBody());
        }

        @Test
        void delete() {
            assertEquals(data, build(URLRequestMethod.DELETE).go().getBody());
        }
    }

}
