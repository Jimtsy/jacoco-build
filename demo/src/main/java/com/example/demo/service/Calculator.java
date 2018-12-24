package com.example.demo.service;

public class Calculator {

    public int add(int a, int b) {
        return a + b;
    }

    public int divide(int a, int b) {
        if (b == 0) {
            // 很明显这是一个bug
            return 0;
        }
        return a - b;
    }
}