package pers.clare.urlrequest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("string")
public class StringController {

    @RequestMapping
    public String request(
            String param
            , String body
    ) {
        return param + body;
    }
}
