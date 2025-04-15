package com.example.tasksmanager.controller;

import com.example.tasksmanager.client.SecurityRestClient;
import com.example.tasksmanager.controller.payload.NewUserPayload;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class PagesController {

    private final SecurityRestClient securityRestClient;

    @RequestMapping(value = {"/","index"})
    public String index() {
        return "index";
    }

    @RequestMapping("/profile")
    public String profile() {
        return "profile";
    }

    @RequestMapping("/registration")
    public String registration() {
        return "registration";
    }

    @ResponseBody
    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String registrationPostMethod(@Valid @RequestBody NewUserPayload payload, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "registration";
        }
        return securityRestClient.registerUser(payload);
    }

    @RequestMapping("/edit-task")
    public String editTaskPage(@RequestParam("id") int id) {
        return "edit-task";
    }

    @RequestMapping("/create-task")
    public String createTaskPage(@RequestParam("projectId") Integer projectId, @RequestParam("projectName") String projectName, Model model) {
        model.addAttribute("projectId", projectId);
        model.addAttribute("projectName", projectName);
        return "create-task";
    }

    @RequestMapping("/tasks")
    public String tasksPage(@RequestParam("projectId") int id) {
        return "tasks";
    }

    @RequestMapping("create-project")
    public String createProjectPage(){
        return "create-project";
    }

    @RequestMapping("test")
    public String testPage(){
        return "test";
    }

    @RequestMapping("login-callback")
    public String loginCallback(){
        return "login-callback";
    }

    @RequestMapping("home")
    public String homePage(){
        return "home";
    }

    @RequestMapping("edit-project")
    public String editProjectPage(@RequestParam("id") int id) {
        return "edit-project";
    }

    @RequestMapping("task-page/{id}")
    public String taskPage(@PathVariable("id") int id) {
        return "task-page";
    }
}
