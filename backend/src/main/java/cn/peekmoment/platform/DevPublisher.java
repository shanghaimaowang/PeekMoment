package cn.peekmoment.platform;

import cn.peekmoment.common.ApiException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

@Component
public class DevPublisher {
    static final String USER_ID = "demo-xiaoman";
    private final byte[] expectedToken;

    public DevPublisher(Environment environment, @Value("${peek.demo-publish-token:}") String token) {
        boolean demoProfile = environment.acceptsProfiles(Profiles.of("local", "test", "demo"));
        expectedToken = demoProfile && !token.isBlank() ? token.getBytes(StandardCharsets.UTF_8) : null;
    }

    public String require(String authorization) {
        if (expectedToken == null || authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7) ||
                !MessageDigest.isEqual(expectedToken, authorization.substring(7).getBytes(StandardCharsets.UTF_8))) {
            throw new ApiException(401, "请提供有效的发布凭证");
        }
        return USER_ID;
    }
}
