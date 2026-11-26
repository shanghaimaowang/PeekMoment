package com.peekmoment.backend.common;

import lombok.Getter;

/**
 * 业务状态码，数字沿用 HTTP 语义，便于前端理解。
 */
@Getter
public enum ResultCode {

    SUCCESS(0, "ok"),

    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已失效"),
    FORBIDDEN(403, "没有权限"),
    NOT_FOUND(404, "资源不存在"),
    SERVER_ERROR(500, "服务器内部错误");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
