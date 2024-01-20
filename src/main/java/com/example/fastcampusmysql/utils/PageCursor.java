package com.example.fastcampusmysql.utils;

import java.util.List;

public record PageCursor<T>(
    CursorRequest nextCursorRequest,
    List<T> contents
) {

}
