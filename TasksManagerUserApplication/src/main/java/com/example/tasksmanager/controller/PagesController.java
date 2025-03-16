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

    @RequestMapping("/edit")
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
}
