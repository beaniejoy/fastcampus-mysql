package com.example.fastcampusmysql.domain.member.service;

import com.example.fastcampusmysql.domain.member.dto.RegisterMemberCommand;
import com.example.fastcampusmysql.domain.member.entity.Member;
import com.example.fastcampusmysql.domain.member.entity.MemberNicknameHistory;
import com.example.fastcampusmysql.domain.member.repository.MemberNicknameHistoryRepository;
import com.example.fastcampusmysql.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberWriteService {

    private final MemberRepository memberRepository;

    private final MemberNicknameHistoryRepository memberNicknameHistoryRepository;

    @Transactional
    public Member create(RegisterMemberCommand command) {
        var member = Member.builder()
            .nickname(command.nickname())
            .email(command.email())
            .birthday(command.birthDay())
            .build();

        Member savedMember = memberRepository.save(member);
        saveMemberNicknameHistory(savedMember);
        return savedMember;
    }

    @Transactional
    public void changeNickname(Long memberId, String nickname) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        member.changeNickname(nickname);
        memberRepository.save(member);

        saveMemberNicknameHistory(member);
    }

    // 바뀐 이름으로 이력 데이터 생성
    private void saveMemberNicknameHistory(Member member) {
        var history = MemberNicknameHistory.builder()
            .memberId(member.getId())
            .nickname(member.getNickname())
            .build();

        memberNicknameHistoryRepository.save(history);
    }
}
