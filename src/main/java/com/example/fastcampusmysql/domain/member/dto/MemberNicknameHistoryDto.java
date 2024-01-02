package com.example.fastcampusmysql.domain.member.dto;

import com.example.fastcampusmysql.domain.member.entity.MemberNicknameHistory;
import java.time.LocalDateTime;

public record MemberNicknameHistoryDto(
    Long id,
    Long memberId,
    String nickname,
    LocalDateTime createdAt
) {

    public static MemberNicknameHistoryDto of(MemberNicknameHistory history) {
        return new MemberNicknameHistoryDto(
            history.getId(),
            history.getMemberId(),
            history.getNickname(),
            history.getCreatedAt()
        );
    }
}
