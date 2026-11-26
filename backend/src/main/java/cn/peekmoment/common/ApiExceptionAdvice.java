package cn.peekmoment.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionAdvice {
    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionAdvice.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiResponse<Void>> applicationError(ApiException error) {
        return failure(error.status(), error.getMessage());
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    ResponseEntity<ApiResponse<Void>> invalidJson() {
        return failure(400, "请求体必须是有效的 JSON 对象");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> otherError(Exception error) {
        if (error instanceof ErrorResponse webError) {
            int status = webError.getStatusCode().value();
            HttpStatus httpStatus = HttpStatus.resolve(status);
            String message = httpStatus == null ? "HTTP error" : httpStatus.getReasonPhrase();
            return ResponseEntity.status(status).headers(webError.getHeaders())
                    .body(new ApiResponse<>(status, message, null));
        }
        LOG.error("Unexpected API error", error);
        return failure(500, "服务器暂时无法完成请求");
    }

    private ResponseEntity<ApiResponse<Void>> failure(int status, String message) {
        return ResponseEntity.status(status).body(new ApiResponse<>(status, message, null));
    }
}
