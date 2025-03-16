package org.example.taskservice.service;

import org.example.taskservice.entity.Category;

public interface CategoryService {
    Iterable<Category> findAll();
}
