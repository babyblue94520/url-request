package pers.clare.urlrequest.controller;

import pers.clare.urlrequest.vo.Data;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("json")
public class JsonController {

    @RequestMapping
    public Data request(@RequestBody Data data) {
        return data;
    }
}
