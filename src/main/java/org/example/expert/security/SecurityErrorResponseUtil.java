package org.example.expert.security;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class SecurityErrorResponseUtil {

    //Security 401, 403 응답용 메서드
    public static Map<String, Object> getErrorMap(HttpStatus status, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", status.name());
        errorResponse.put("code", status.value());
        errorResponse.put("message", message);
        return errorResponse;
    }

}
