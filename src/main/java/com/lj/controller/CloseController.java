package com.lj.controller;


import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/*
 * close request:
 * http://localhost:8082/close
 * class for closing server processes
 */
@RestController
public class CloseController {

    public static final Logger log = LoggerFactory.getLogger(CloseController.class);

    @Autowired
    private ApplicationContext context;

    @RequestMapping("/close")
    public void close() {

        log.info("close operation...");
        int exitCode = SpringApplication.exit(context, () -> 0);
        System.exit(exitCode);
    }

    @PreDestroy
    public void destroy() {

        log.info("CloseController::preDestroy");
    }
}