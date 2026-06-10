package com.classmarket.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
protected void doFilterInternal(HttpServletRequest req,
                                HttpServletResponse res,
                                FilterChain chain) throws ServletException, IOException {

    String header = req.getHeader("Authorization");

    try {
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.validar(token)) {
                String email = jwtUtil.getEmail(token);
                boolean adm  = jwtUtil.isAdm(token);

                var authorities = adm
                        ? List.of(new SimpleGrantedAuthority("ROLE_ADM"),
                                  new SimpleGrantedAuthority("ROLE_USER"))
                        : List.of(new SimpleGrantedAuthority("ROLE_USER"));

                var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
    } catch (Exception e) {
        // Token inválido ou malformado — apenas ignora e continua sem autenticar
        SecurityContextHolder.clearContext();
    }
        chain.doFilter(req, res);
    }
}
