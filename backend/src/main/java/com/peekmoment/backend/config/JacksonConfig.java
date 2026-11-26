package com.peekmoment.backend.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * JSON 序列化约定：包装类 Long 统一输出为字符串（用于各类 id）。
 * primitive long 不受影响，时间戳等数值字段仍输出数字。
 * JS 的 Number 只能安全表示 2^53 以内的整数，前端按字符串处理 id。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> builder.serializerByType(Long.class, ToStringSerializer.instance);
    }
}
