package ru.sfedu.mmcs_nexus.util;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ResponseUtils {
    private ResponseUtils() { }

    public static ResponseEntity<Map<String, Object>> error(HttpStatus status, String error, Object... extraPairs) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", error);
        for (int i = 0; i < extraPairs.length - 1; i += 2) {
            body.put(String.valueOf(extraPairs[i]), extraPairs[i+1]);
        }
        return ResponseEntity.status(status).body(body);
    }

    public static ResponseEntity<Map<String, Object>> success(HttpStatus status, String message, Object... extraPairs) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        for (int i = 0; i < extraPairs.length - 1; i += 2) {
            body.put(String.valueOf(extraPairs[i]), extraPairs[i+1]);
        }
        return ResponseEntity.status(status).body(body);
    }

    public static Map<String, Object> buildResponse(Object content, long totalElements) {
        return Map.of(
                "content", content,
                "totalElements", totalElements
        );
    }

    public static ResponseEntity<Map<String, Object>> buildPageResponse(Page<?> page) {
        Map<String, Object> body = Map.of(
                "content", page.getContent(),
                "totalElements", page.getTotalElements()
        );

        return ResponseEntity.ok().body(body);
    }

}
