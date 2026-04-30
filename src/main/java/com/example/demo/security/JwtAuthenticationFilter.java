package com.example.demo.security;

import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 读取请求头中的 Authorization
        String authHeader = request.getHeader("Authorization");

        // 2. 如果没有 Authorization，或者不是 Bearer 开头，直接放行给后续过滤器
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 截取真正的 JWT 字符串
        String jwt = authHeader.substring(7);
        String username;

        try {
            // 4. 从 JWT 中解析用户名
            username = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            // token 解析失败，直接继续后续过滤器
            filterChain.doFilter(request, response);
            return;
        }

        // 5. 如果 SecurityContext 中没有认证信息，且 token 未过期
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = userMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                            .eq(User::getUsername, username)
            );

            if (user != null && !jwtUtil.isTokenExpired(jwt)) {
                // 6. 构建认证对象并放入 SecurityContext
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        // 7. 放行
        filterChain.doFilter(request, response);
    }
}
