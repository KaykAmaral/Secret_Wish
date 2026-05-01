package com.example.springtest;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TestController {
    @GetMapping("/test")
    public String test() {
        return "Conexão com o Backend estabelecida com sucesso!";
    }
}
