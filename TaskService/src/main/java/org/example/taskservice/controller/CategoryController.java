package org.example.taskservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.taskservice.entity.Category;
import org.example.taskservice.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("task-manager-api/tasks/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public Iterable<Category> getAllCategories() {
        return categoryService.findAll();
    }
}
