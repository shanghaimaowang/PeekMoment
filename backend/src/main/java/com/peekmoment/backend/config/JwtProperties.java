package com.peekmoment.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置项，对应 application.yml 的 peekmoment.jwt。
 */
@Data
@ConfigurationProperties(prefix = "peekmoment.jwt")
public class JwtProperties {

    /** HS256 签名密钥，长度需 >= 32 字节 */
    private String secret;

    /** token 有效期（分钟） */
    private long expireMinutes = 720;
}
