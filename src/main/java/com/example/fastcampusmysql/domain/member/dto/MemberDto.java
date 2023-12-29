package com.example.fastcampusmysql.domain.member.dto;

import com.example.fastcampusmysql.domain.member.entity.Member;
import java.time.LocalDate;

public record MemberDto(
    Long id,
    String nickname,
    String email,
    LocalDate birthday
) {
    public static MemberDto of(Member member) {
        return new MemberDto(member.getId(), member.getNickname(), member.getEmail(), member.getBirthday());
    }
}
