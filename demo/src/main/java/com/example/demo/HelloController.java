package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // ① このクラスがWeb APIのコントローラーであることを示す
public class HelloController {
  
  @GetMapping("/hello") // ② /hello というURLへのGETリクエストを処理する
  public String hello() {
    return "Hello World!";
  }
}
