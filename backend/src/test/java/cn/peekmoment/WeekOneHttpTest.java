package cn.peekmoment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "peek.admin-username=p0-runner",
    "peek.admin-password=OnlyForWeek1Test!",
    "peek.demo-publish-token=p0-local-demo-credential"
})
@ActiveProfiles("test")
class WeekOneHttpTest {
    private static final String DEMO = "p0-local-demo-credential";
    private static final String PASSWORD = "OnlyForWeek1Test!";

    @LocalServerPort int port;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate fixtures;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    private record Reply(int status, JsonNode envelope) {
        JsonNode expect(int expected) {
            assertEquals(expected, status, envelope::toString);
            assertTrue(envelope.path("code").isIntegralNumber(), envelope::toString);
            assertEquals(expected < 300 ? 0 : expected, envelope.get("code").intValue());
            assertTrue(envelope.path("message").isTextual());
            assertTrue(envelope.has("data"));
            if (expected >= 400) assertTrue(envelope.get("data").isNull());
            return envelope.get("data");
        }
    }

    private Reply call(String method, String path, String body, String token)
            throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api" + path))
                .timeout(Duration.ofSeconds(10)).header("Accept", "application/json");
        if (token != null) request.header("Authorization", "Bearer " + token);
        if (body != null) request.header("Content-Type", "application/json");
        request.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body));
        var response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"),
                () -> method + " " + path + " returned " + response.statusCode());
        return new Reply(response.statusCode(), json.readTree(response.body()));
    }

    private Reply get(String path, String token) throws Exception {
        return call("GET", path, null, token);
    }

    private Reply post(String path, Object obj, String token) throws Exception {
        return call("POST", path, json.writeValueAsString(obj), token);
    }

    private String adminToken() throws Exception {
        JsonNode login = post("/admin/login", Map.of("username", "p0-runner", "password", PASSWORD), null).expect(200);
        assertTrue(login.path("token").isTextual());
        assertFalse(login.get("token").textValue().isBlank());
        assertTrue(login.path("expiresAt").asLong() > System.currentTimeMillis());
        return login.get("token").textValue();
    }

    private static String title() {
        return "qa-" + UUID.randomUUID().toString().substring(0, 16);
    }

    @Test void readyAndGuestCanReadFeed() throws Exception {
        assertEquals("ok", get("/health", null).expect(200).path("status").textValue());
        JsonNode feed = get("/moments", null).expect(200);
        assertEquals(1, feed.path("page").asInt());
        assertEquals(10, feed.path("pageSize").asInt());
        assertTrue(feed.path("total").isIntegralNumber());
        assertTrue(feed.path("items").isArray());
    }

    @Test void loginAndRolesAreEnforcedOnTheServer() throws Exception {
        post("/admin/login", Map.of("username", "p0-runner", "password", PASSWORD + "-wrong"), null).expect(401);
        get("/admin/users", null).expect(401);
        get("/admin/moments", DEMO).expect(401);
        Map<String, String> draft = Map.of("body", "测试动态");
        post("/moments", draft, null).expect(401);
        post("/moments", draft, adminToken()).expect(401);
    }

    @Test void publishedMomentIsTheSameRecordInFeedAndAdminSearch() throws Exception {
        String title = title();
        var media = List.of(Map.of("kind", "image", "src", "https://example.org/sample.jpg"));
        var draft = Map.of("title", title, "body", "  运动会  ", "tags", List.of(" 校园 ", "运动", "校园"), "media", media);
        JsonNode saved = post("/moments", draft, DEMO).expect(201);
        assertTrue(saved.path("id").isTextual());
        String id = saved.get("id").textValue();
        assertFalse(id.isBlank());
        assertEquals("demo-xiaoman", saved.path("userId").textValue());
        assertEquals("published", saved.path("status").textValue());
        assertEquals("运动会", saved.path("body").textValue());
        assertEquals(json.valueToTree(List.of("校园", "运动")), saved.path("tags"));
        assertEquals(json.valueToTree(media), saved.path("media"));
        JsonNode feed = get("/moments?page=1&pageSize=100", null).expect(200).get("items");
        assertTrue(feed.findValuesAsText("id").contains(id));
        JsonNode found = get("/admin/moments?page=1&pageSize=1&keyword=" + title, adminToken()).expect(200);
        assertEquals(1, found.path("total").asInt());
        assertEquals(id, found.path("items").path(0).path("id").textValue());
    }

    @Test void invalidDraftsLeaveNoPartialSearchResults() throws Exception {
        String prefix = title();
        for (Map<String, ?> draft : List.of(
                Map.of("title", prefix, "body", "x".repeat(1001)),
                Map.of("title", prefix, "userId", "other-user"),
                Map.of("title", prefix, "media", List.of(Map.of("kind", "image", "src", "blob:temporary"))),
                Map.of("title", prefix, "tags", List.of("a", "b", "c", "d", "e", "f")))) {
            post("/moments", draft, DEMO).expect(400);
        }
        post("/moments", Map.of("body", "   "), DEMO).expect(400);
        call("POST", "/moments", "{", DEMO).expect(400);
        assertEquals(0, get("/admin/moments?keyword=" + prefix, adminToken()).expect(200).path("total").asInt());
    }

    @Test void authorizedUserQueryAndPaginationBounds() throws Exception {
        String token = adminToken();
        JsonNode users = get("/admin/users?page=1&pageSize=1&keyword=demo-xiaoman", token).expect(200);
        assertEquals(1, users.path("page").asInt());
        assertEquals(1, users.path("pageSize").asInt());
        assertEquals(1, users.path("total").asInt());
        JsonNode user = users.path("items").path(0);
        assertEquals("demo-xiaoman", user.path("id").textValue());
        assertTrue(user.path("nickname").isTextual());
        assertFalse(user.has("password"));
        assertFalse(user.has("passwordHash"));
        assertTrue(get("/admin/users?page=2&pageSize=1&keyword=demo-xiaoman", token)
                .expect(200).path("items").isEmpty());
        get("/admin/users?page=0", token).expect(400);
        get("/moments?pageSize=101", null).expect(400);
    }

    @Test void adminPagesAreStableAndTreatKeywordLiterally() throws Exception {
        String prefix = title();
        Set<String> ids = new HashSet<>();
        for (String tail : List.of("_one", "Xtwo")) {
            ids.add(post("/moments", Map.of("title", prefix + tail), DEMO).expect(201).get("id").textValue());
        }
        String token = adminToken();
        for (int page : List.of(1, 2)) {
            JsonNode result = get("/admin/moments?keyword=" + prefix + "&pageSize=1&page=" + page, token).expect(200);
            assertEquals(2, result.path("total").asInt());
            assertEquals(1, result.path("items").size());
            assertTrue(ids.remove(result.path("items").get(0).path("id").textValue()));
        }
        assertTrue(ids.isEmpty());
        JsonNode literal = get("/admin/moments?keyword=" + prefix + "_", token).expect(200);
        assertEquals(1, literal.path("total").asInt());
        assertEquals(prefix + "_one", literal.path("items").path(0).path("title").textValue());
    }

    @Test void onlyAdminsCanSeeUnpublishedFixtures() throws Exception {
        String prefix = title();
        Set<String> ids = new HashSet<>();
        for (String status : List.of("pending", "hidden")) {
            String id = post("/moments", Map.of("title", prefix + status), DEMO).expect(201).get("id").textValue();
            // Moderation writes are scheduled for a later iteration. Only fixture setup goes through SQL.
            assertEquals(1, fixtures.update("UPDATE moments SET status = ? WHERE id = ?", status, Long.valueOf(id)));
            ids.add(id);
        }
        JsonNode feed = get("/moments?pageSize=100", null).expect(200);
        assertTrue(feed.path("items").findValuesAsText("id").stream().noneMatch(ids::contains));
        JsonNode admin = get("/admin/moments?keyword=" + prefix, adminToken()).expect(200);
        assertEquals(2, admin.path("total").asInt());
        assertEquals(Set.of("pending", "hidden"), new HashSet<>(admin.path("items").findValuesAsText("status")));
    }
}
