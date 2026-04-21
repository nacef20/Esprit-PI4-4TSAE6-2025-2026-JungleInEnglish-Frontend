package tn.esprit.jungleinenglishbackend.Services.impl;

import tn.esprit.jungleinenglishbackend.Models.Category;
import tn.esprit.jungleinenglishbackend.Reposotories.CategoryRepo;
import tn.esprit.jungleinenglishbackend.Services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service

public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepo categoryRepo;

    @Override
    public Category saveCategory(Category category) {
        return categoryRepo.save(category);
    }

    @Override
    public List<Category> getAllCategory() {
        return categoryRepo.findAll();
    }

    @Override
    public Boolean existCategory(String name) {
        return categoryRepo.existsByNameIgnoreCase(name);
    }

    @Override
    public Boolean deleteCategory(int id) {
        Category category = categoryRepo.findById(id).orElse(null);

        if (!ObjectUtils.isEmpty(category)) {
            categoryRepo.delete(category);
            return true;
        }
        return false;
    }

    @Override
    public Category getCategoryById(int id) {
        Category category = categoryRepo.findById(id).orElse(null);
        return category;
    }

    @Override
    public List<Category> getAllActiveCategory() {
        return categoryRepo.findByIsActiveTrueOrIsNull();
    }

    @Override
    public Page<Category> getAllCategorPagination(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return categoryRepo.findAll(pageable);
    }
}
