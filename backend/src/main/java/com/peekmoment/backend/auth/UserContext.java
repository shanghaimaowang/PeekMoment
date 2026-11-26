package com.peekmoment.backend.auth;

/**
 * 当前登录身份的线程上下文：拦截器写入，业务代码读取，请求结束由拦截器清理。
 */
public class UserContext {

    /**
     * @param id   用户或管理员 id
     * @param role user / admin
     */
    public record AuthUser(Long id, String role) {
    }

    private static final ThreadLocal<AuthUser> HOLDER = new ThreadLocal<>();

    public static void set(AuthUser authUser) {
        HOLDER.set(authUser);
    }

    public static AuthUser get() {
        return HOLDER.get();
    }

    /** 当前登录用户 id（小程序端），未登录返回 null */
    public static Long currentUserId() {
        AuthUser authUser = HOLDER.get();
        return authUser == null ? null : authUser.id();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
