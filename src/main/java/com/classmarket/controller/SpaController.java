package com.classmarket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Serve o index.html para todas as rotas que não sejam /api/** ou assets estáticos.
 * Necessário para o SPA com hash routing funcionar após refresh.
 */
@Controller
public class SpaController {

    @RequestMapping(value = {"/", "/index.html"})
    public String index() {
        return "forward:/index.html";
    }
}
