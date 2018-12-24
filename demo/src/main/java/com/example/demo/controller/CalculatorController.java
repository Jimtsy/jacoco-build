package com.example.demo.controller;

import com.example.demo.service.Calculator;
import jdk.nashorn.internal.objects.annotations.Constructor;
import org.springframework.web.bind.annotation.*;

import javax.jws.WebResult;

@RestController
@RequestMapping("/")
public class CalculatorController {

    @RequestMapping(value = "/add/{v1}/{v2}", method = RequestMethod.GET)
    public int add(@PathVariable("v1") Integer v1, @PathVariable("v2") Integer v2){
        Calculator calculator = new Calculator();
        return calculator.add(v1, v2);
    }

    @RequestMapping(value = "/divide/{v1}/{v2}", method = RequestMethod.GET)
    public int devise(@PathVariable("v1") Integer v1, @PathVariable("v2") Integer v2){
        Calculator calculator = new Calculator();
        return calculator.divide(v1, v2);
    }
}
