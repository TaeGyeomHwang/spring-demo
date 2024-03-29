package com.example.springdemo.controller;

import com.example.springdemo.UserDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping(value = "/test")
    public UserDTO test(){
        UserDTO userDTO = new UserDTO();
        userDTO.setAge(20);
        userDTO.setName("hoon");

        return userDTO;
    }
}