package org.example.taskservice.service;

import lombok.RequiredArgsConstructor;
import org.example.taskservice.entity.Category;
import org.example.taskservice.repository.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public Iterable<Category> findAll() {
        return categoryRepository.findAll();
    }
}
