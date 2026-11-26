package cn.peekmoment.content;

import cn.peekmoment.common.ApiResponse;
import cn.peekmoment.common.Paging;
import cn.peekmoment.platform.DevPublisher;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/moments")
public class MomentController {
    private final ContentService content;
    private final DevPublisher publisher;
    public MomentController(ContentService content, DevPublisher publisher) {
        this.content = content;
        this.publisher = publisher;
    }

    @GetMapping
    public ApiResponse<ContentService.Page> list(@RequestParam MultiValueMap<String, String> params) {
        Paging paging = Paging.from(params);
        return ApiResponse.ok(content.page(paging.page(), paging.pageSize(), false, null));
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<ApiResponse<ContentService.View>> publish(
            @RequestHeader(value = "Authorization", required = false) String authorization, @RequestBody JsonNode body) {
        String userId = publisher.require(authorization);
        return ResponseEntity.status(201).body(ApiResponse.ok(content.publish(userId, MomentDraft.from(body))));
    }
}
