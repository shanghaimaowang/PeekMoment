package com.peekmoment.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 状态更新请求：user 0 禁用/1 正常；content 1 发布/3 下架。
 */
public record StatusRequest(
        @NotNull(message = "状态不能为空")
        @Min(value = 0, message = "状态值错误")
        @Max(value = 3, message = "状态值错误") Integer status) {
}
