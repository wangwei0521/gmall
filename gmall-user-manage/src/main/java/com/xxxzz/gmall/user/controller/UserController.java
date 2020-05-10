package com.xxxzz.gmall.user.controller;

import com.xxxzz.gmall.UserInfo;
import com.xxxzz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/findAll")
    public List<UserInfo> findAll(){
        return userService.findAll();
    }
}
