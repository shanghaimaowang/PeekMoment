package com.peekmoment.backend.auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peekmoment.backend.common.Result;
import com.peekmoment.backend.common.ResultCode;

/**
 * 鉴权拦截器：
 * - /api/admin/** 要求 admin 角色
 * - /api/mp/** 要求 user 角色，其中 GET 浏览类接口（游客可看）在 WHITE_LIST 中放行
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    /** 游客可访问的接口（method + path 前缀） */
    private static final List<String> PUBLIC_GET_PREFIXES = List.of(
            "/api/mp/contents",
            "/api/mp/search",
            "/api/health");

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (HttpMethod.GET.matches(method) && isPublic(path)) {
            return true;
        }

        Claims claims = parseBearerToken(request);
        if (claims == null) {
            return reject(response, ResultCode.UNAUTHORIZED);
        }

        String role = claims.get("role", String.class);
        boolean needAdmin = path.startsWith("/api/admin/");
        boolean roleMatched = needAdmin
                ? JwtUtil.ROLE_ADMIN.equals(role)
                : JwtUtil.ROLE_USER.equals(role);
        if (!roleMatched) {
            return reject(response, ResultCode.FORBIDDEN);
        }

        UserContext.set(new UserContext.AuthUser(Long.valueOf(claims.getSubject()), role));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) {
        UserContext.clear();
    }

    private boolean isPublic(String path) {
        return PUBLIC_GET_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private Claims parseBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return jwtUtil.parse(header.substring(7));
    }

    private boolean reject(HttpServletResponse response, ResultCode resultCode)
            throws IOException {
        response.setStatus(resultCode.getCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(resultCode)));
        return false;
    }
}
