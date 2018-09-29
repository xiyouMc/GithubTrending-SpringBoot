package com.xiyoumc.controller;

import com.xiyoumc.model.Greeting;
import com.xiyoumc.service.HelloWorldService;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

  private final AtomicInteger counter = new AtomicInteger();
  private static final String TEMPLATE = "Hello, %s!";
  @Autowired
  private HelloWorldService helloWorldService;

  @RequestMapping("/greeting")
  public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
    return new Greeting(counter.incrementAndGet(), String.format(TEMPLATE, name));
  }

  @RequestMapping("/")
  public String helloWorld() {
    return this.helloWorldService.getHelloMessage();
  }

}
