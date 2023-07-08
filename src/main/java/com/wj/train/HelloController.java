package com.wj.train;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wj
 * @create_time 2023/7/8
 * @description
 */
@RestController
public class HelloController {

    @GetMapping("/hello")
    public void hello() {
        System.out.println("hell11");
    }

}
