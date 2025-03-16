package com.example.tasksmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PagesController {

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("edit")
    public String editTaskPage(@RequestParam("id") int id) {
        return "edit-task";
    }
    @RequestMapping("create-task")
    public String createTaskPage() {
        return "create-task";
    }
}
