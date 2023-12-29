package com.example.fastcampusmysql.domain.member.entity;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.fastcampusmysql.util.MemberFixtureFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// object mother pattern (test 때)
class MemberTest {

    @DisplayName("회원은 닉네임을 변경할 수 있다.")
    @Test
    void testChangeName() {
        var member = MemberFixtureFactory.create();
        var expected = "beanie";

        member.changeNickname(expected);

        assertEquals(expected, member.getNickname());
    }

    @DisplayName("회원은 닉네임은 10자를 초과할 수 없다.")
    @Test
    void testNicknameMaxLength() {
        var member = MemberFixtureFactory.create();
        var overMaxLengthNickname = "beaniebeanie";

        assertThatIllegalArgumentException()
            .isThrownBy(() -> member.changeNickname(overMaxLengthNickname))
            .withMessage("최대 길이를 초과하였습니다.");
    }
}