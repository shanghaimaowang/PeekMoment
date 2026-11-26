package cn.peekmoment.platform;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class WebConfig implements WebMvcConfigurer {
    private final String origin;

    WebConfig(@Value("${peek.cors-origin:}") String origin) {
        this.origin = origin.trim();
        if (!this.origin.isEmpty()) {
            URI uri = URI.create(this.origin);
            boolean http = "http".equals(uri.getScheme()) || "https".equals(uri.getScheme());
            if (!http || uri.getHost() == null || uri.getUserInfo() != null ||
                    !uri.getRawPath().isEmpty() || uri.getRawQuery() != null || uri.getFragment() != null) {
                throw new IllegalArgumentException("PEEK_CORS_ORIGIN requires a single HTTP(S) origin");
            }
        }
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (!origin.isEmpty()) {
            registry.addMapping("/api/**").allowedOrigins(origin).allowedMethods("GET", "POST")
                    .allowedHeaders("Authorization", "Content-Type");
        }
    }
}
