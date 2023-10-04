# Url Request

## Overview

單純覺得市面上的 Http Client 很難用，只好自己開發一個基於`java.net.HttpUrlConnection`，實現一個更容易使用的 `URLRequest`。

## QuickStart

### Usage

* Default return `string` body.

```java

import pers.clare.urlrequest.URLRequest;
import pers.clare.urlrequest.URLResponse;

import java.util.concurrent.Executors;

class Example {

    public static void main(String[] args) {
        URLResponse<String> response = URLRequest.build(url)
                .header(HeaderNames.CONTENT_TYPE, HeaderValues.JSON)
                .body(om.writeValueAsString(data))
                .get(); // or post(), put() and delete()
        String body = response.getBody();
    }
}

```

* Custom response handler.

```java

import pers.clare.urlrequest.URLRequest;
import pers.clare.urlrequest.URLResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import pers.clare.urlrequest.handler.ResponseHandler;

import java.io.InputStreamReader;
import java.util.concurrent.Executors;

class Example {

    public static void main(String[] args) {
        ObjectMapper om = new ObjectMapper();
        ResponseHandler<Data> handler = (in, charset) -> om.readValue(new InputStreamReader(in, charset), Data.class);
        URLResponse<Data> response = URLRequest.build(url, handler)
                .header(HeaderNames.CONTENT_TYPE, HeaderValues.JSON)
                .body(om.writeValueAsString(data))
                .get();
        Data body = response.getBody();
    }
}

```

* Set CookieManager.

```java

import pers.clare.urlrequest.URLRequest;
import pers.clare.urlrequest.URLResponse;

import java.net.CookieManager;
import java.util.concurrent.Executors;

class Example {
    public static CookieManager cookieManager = new CookieManager();

    public static void main(String[] args) {
        URLResponse<String> response = URLRequest.build(url)
                .header(HeaderNames.CONTENT_TYPE, HeaderValues.JSON)
                .cookieManager(cookieManager)
                .body(om.writeValueAsString(data))
                .get();
        String body = response.getBody();
    }
}

```


* Reusable

```java

import pers.clare.urlrequest.URLRequest;
import pers.clare.urlrequest.URLResponse;

import java.util.concurrent.Executors;

class Example {

    public static void main(String[] args) {
        URLRequest<String> request = URLRequest.build(url)
                .header(HeaderNames.CONTENT_TYPE, HeaderValues.JSON)
                .body(om.writeValueAsString(data));

        URLResponse<String> response = request.get();
        // can reuse
        response = request.get();
    }
}

```

