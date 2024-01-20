package com.example.fastcampusmysql.utils;

import io.swagger.v3.oas.annotations.Parameter;

public record CursorRequest(
    @Parameter(name = "key", description = "커서 id") Long key,
    @Parameter(name = "size", description = "사이즈") Long size
) {

    public static final Long NONE_KEY = -1L;

    public Boolean hasKey() {
        return key != null;
    }

    public CursorRequest next(Long key) {
        return new CursorRequest(key, size);
    }
}
