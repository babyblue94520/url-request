package pers.clare.server.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pers.clare.urlrequest.vo.Data;

@RestController
@RequestMapping("json")
public class JsonController {

    @RequestMapping
    public Data request(@RequestBody Data data) {
        return data;
    }
}
