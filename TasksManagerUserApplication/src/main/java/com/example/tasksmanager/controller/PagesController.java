package com.example.tasksmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PagesController {

    @RequestMapping(value = {"/","index"})
    public String index() {
        return "index";
    }

    @RequestMapping("/profile")
    public String profile() {
        return "profile";
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
