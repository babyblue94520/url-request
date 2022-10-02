package pers.clare.server.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("redirect")
public class RedirectController {

    @RequestMapping
    public void request(
            HttpServletResponse response
            , String path
    ) throws IOException {
        response.sendRedirect(path);
    }
}
