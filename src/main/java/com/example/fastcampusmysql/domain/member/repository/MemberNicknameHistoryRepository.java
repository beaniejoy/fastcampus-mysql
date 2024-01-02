package com.example.fastcampusmysql.domain.member.repository;

import com.example.fastcampusmysql.domain.member.entity.Member;
import com.example.fastcampusmysql.domain.member.entity.MemberNicknameHistory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberNicknameHistoryRepository {
    private static final String TABLE = "MemberNicknameHistory";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final RowMapper<MemberNicknameHistory> rowMapper = (rs, rowNum) -> MemberNicknameHistory.builder()
        .id(rs.getLong("id"))
        .memberId(rs.getLong("memberId"))
        .nickname(rs.getString("nickname"))
        .createdAt(rs.getObject("createdAt", LocalDateTime.class))
        .build();

    public List<MemberNicknameHistory> findAllByMemberId(Long memberId) {
        var sql = String.format("SELECT * FROM %s WHERE memberId = :memberId", TABLE);
        var params = new MapSqlParameterSource()
            .addValue("memberId", memberId);

        return namedParameterJdbcTemplate.query(sql, params, rowMapper);
    }

    public MemberNicknameHistory save(MemberNicknameHistory history) {
        if (history.getId() == null) {
            return insert(history);
        }

        // history update 불가
        throw new UnsupportedOperationException("MemberNicknameHistory는 갱신을 지원하지 않습니다.");
    }

    private MemberNicknameHistory insert(MemberNicknameHistory history) {
        SimpleJdbcInsert simpleJdbcInsert =
            new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate())
                .withTableName(TABLE)
                .usingGeneratedKeyColumns("id");

        SqlParameterSource params = new BeanPropertySqlParameterSource(history);

        var id = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return MemberNicknameHistory.builder()
            .id(id)
            .memberId(history.getMemberId())
            .nickname(history.getNickname())
            .createdAt(history.getCreatedAt())
            .build();
    }
}
