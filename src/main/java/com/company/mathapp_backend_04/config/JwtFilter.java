package com.company.mathapp_backend_04.config;

import com.company.mathapp_backend_04.service.JwtService;
import com.company.mathapp_backend_04.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String header = request.getHeader("Authorization");

        // ❌ Không có token → bỏ qua
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = header.substring(7);

        try {
            String email = jwtService.extractEmail(token);

            // ❗ chỉ set nếu chưa có authentication
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 👉 nếu bạn có hàm validate thì dùng
                //if (jwtService.isValid(token, userDetails)) {

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities() // 🔥 QUAN TRỌNG
                            );

                    SecurityContextHolder.getContext().setAuthentication(auth);

            }

        } catch (Exception e) {
            // token lỗi → bỏ qua
        }

        filterChain.doFilter(request, response);
    }
}