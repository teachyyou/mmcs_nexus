package ru.sfedu.mmcs_nexus.util;

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
}
