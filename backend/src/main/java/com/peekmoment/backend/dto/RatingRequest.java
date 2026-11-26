package com.peekmoment.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 评分请求。
 */
public record RatingRequest(
        @NotNull(message = "评分不能为空")
        @Min(value = 1, message = "评分范围 1-5")
        @Max(value = 5, message = "评分范围 1-5") Integer score) {
}
