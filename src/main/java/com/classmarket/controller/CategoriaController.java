package com.classmarket.controller;

import com.classmarket.model.Categoria;
import com.classmarket.repository.CategoriaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaRepository categoriaRepo;

    public CategoriaController(CategoriaRepository categoriaRepo) {
        this.categoriaRepo = categoriaRepo;
    }

    @GetMapping
    public List<Categoria> listar() {
        return categoriaRepo.findAll();
    }
}
