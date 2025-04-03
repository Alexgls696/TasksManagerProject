package org.example.securityservice.controller;

import org.example.securityservice.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegisterController {

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/registration")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String password) {
        userService.registerUser(username, password);
        return "redirect:/profile";
    }
}