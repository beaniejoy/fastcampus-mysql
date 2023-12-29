package com.example.fastcampusmysql.util;

import com.example.fastcampusmysql.domain.member.entity.Member;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

public class MemberFixtureFactory {
    public static Member create() {
        EasyRandomParameters params = new EasyRandomParameters();
        return new EasyRandom(params).nextObject(Member.class);
    }

    public static Member create(Long seed) {
        EasyRandomParameters params = new EasyRandomParameters().seed(seed);
        return new EasyRandom(params).nextObject(Member.class);
    }
}
