package com.example.demo.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String method = request.getMethod();
        String uri = request.getRequestURI();

        // 放行规则：注册、登录
        boolean isRegister = "POST".equalsIgnoreCase(method) && "/api/users".equals(uri);
        boolean isLogin = "POST".equalsIgnoreCase(method) && "/api/users/login".equals(uri);

        if (isRegister || isLogin) {
            return true;
        }

        // 其它接口需要 Token（包括 GET /api/users/{id}）
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            response.setContentType("application/json;charset=UTF-8");
            String errorJson = "{\"code\": 401, \"msg\": \"非法操作：敏感动作 " + method + " 需要 Token\"}";
            response.getWriter().write(errorJson);
            return false;
        }

        return true;
    }
}
