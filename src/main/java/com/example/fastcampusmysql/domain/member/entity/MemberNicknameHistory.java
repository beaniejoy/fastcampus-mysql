package com.example.fastcampusmysql.domain.member.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberNicknameHistory {

    private final Long id;

    private final Long memberId;

    private final String name;

    private final LocalDateTime createdAt;

    @Builder
    public MemberNicknameHistory(Long id, Long memberId, String name, LocalDateTime createdAt) {
        this.id = id;
        this.memberId = Objects.requireNonNull(memberId);
        this.name = Objects.requireNonNull(name);
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }
}
