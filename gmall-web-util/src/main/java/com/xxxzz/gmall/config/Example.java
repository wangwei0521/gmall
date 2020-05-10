package com.xxxzz.gmall.config;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

 class Example {
    private String baseName = "base";
    public Example(){
        callName();
    }
    public void callName(){
        System.out.println(baseName);
    }
}

 class Sub extends Example {
    private static String baseName = "sub";

    public void callName() {
        System.out.println(baseName);
    }

    public static void main(String[] args) {
        Example b = new Sub();
    }
}

