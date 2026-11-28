package cn.peekmoment.admin;

import cn.peekmoment.common.ApiException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AdminSessions {
    private static final long LIFETIME_MS = Duration.ofHours(2).toMillis();
    private static final Pattern BEARER = Pattern.compile("Bearer ([A-Za-z0-9_-]{43})", Pattern.CASE_INSENSITIVE);
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Long> expirations = new ConcurrentHashMap<>();
    private final String username;
    private final byte[] salt = new byte[16];
    private final byte[] passwordHash;

    public AdminSessions(String username, String password) {
        this.username = username;
        random.nextBytes(salt);
        passwordHash = username.isBlank() || password.isBlank() ? null : hashPassword(password);
    }

    public record LoginResult(String token, long expiresAt, String username) {}

    public LoginResult login(String account, String password) {
        if (passwordHash == null) {
            throw new ApiException(503, "管理员账号尚未配置");
        }
        if (account == null || account.isBlank() || password == null || password.isEmpty()) {
            throw new ApiException(400, "请输入管理员账号和密码");
        }
        byte[] candidate = hashPassword(password);
        boolean matches = MessageDigest.isEqual(candidate, passwordHash);
        Arrays.fill(candidate, (byte) 0);
        if (!matches || !username.equals(account)) {
            throw new ApiException(401, "账号或密码错误");
        }
        long now = System.currentTimeMillis();
        expirations.values().removeIf(expiry -> expiry <= now);
        byte[] secret = new byte[32];
        random.nextBytes(secret);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
        long expiry = now + LIFETIME_MS;
        expirations.put(digest(token), expiry);
        return new LoginResult(token, expiry, username);
    }

    public void requireBearer(String header) {
        Matcher matcher = BEARER.matcher(header == null ? "" : header);
        if (matcher.matches()) {
            String key = digest(matcher.group(1));
            Long expiry = expirations.get(key);
            if (expiry != null && expiry > System.currentTimeMillis()) {
                return;
            }
            if (expiry != null) {
                expirations.remove(key, expiry);
            }
        }
        throw new ApiException(401, "管理员会话无效或已过期");
    }

    private byte[] hashPassword(String password) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 210_000, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        } finally {
            spec.clearPassword();
        }
    }

    private String digest(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.US_ASCII));
            return Base64.getEncoder().encodeToString(hash);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
