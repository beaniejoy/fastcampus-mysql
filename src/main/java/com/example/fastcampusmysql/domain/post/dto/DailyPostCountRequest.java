package com.example.fastcampusmysql.domain.post.dto;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

public record DailyPostCountRequest(
    Long memberId,
    @DateTimeFormat(iso = ISO.DATE)
    LocalDate firstDate,
    @DateTimeFormat(iso = ISO.DATE)
    LocalDate lastDate
) {

}
