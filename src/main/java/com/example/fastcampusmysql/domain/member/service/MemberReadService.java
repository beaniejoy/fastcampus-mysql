package com.example.fastcampusmysql.domain.member.service;

import com.example.fastcampusmysql.domain.member.dto.MemberDto;
import com.example.fastcampusmysql.domain.member.dto.MemberNicknameHistoryDto;
import com.example.fastcampusmysql.domain.member.entity.MemberNicknameHistory;
import com.example.fastcampusmysql.domain.member.repository.MemberNicknameHistoryRepository;
import com.example.fastcampusmysql.domain.member.repository.MemberRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberReadService {

    private final MemberRepository memberRepository;

    private final MemberNicknameHistoryRepository memberNicknameHistoryRepository;

    public MemberDto getMember(Long id) {
        return MemberDto.of(memberRepository.findById(id).orElseThrow());
    }

    public List<MemberDto> getMembers(List<Long> ids) {
        var members = memberRepository.findAllByIdIn(ids);
        return members
            .stream()
            .map(MemberDto::of)
            .toList();
    }

    public List<MemberNicknameHistoryDto> getNicknameHistories(Long memberId) {
        return memberNicknameHistoryRepository.findAllByMemberId(memberId)
            .stream()
            .map(MemberNicknameHistoryDto::of)
            .toList();
    }
}
