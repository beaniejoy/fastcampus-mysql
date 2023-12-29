package com.example.fastcampusmysql.domain.member.repository;

import com.example.fastcampusmysql.domain.member.entity.Member;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    final private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public Optional<Member> findById(Long id) {

        var sql = String.format("SELECT * FROM %s WHERE id = :id", TABLE);
        var params = new MapSqlParameterSource()
            .addValue("id", id);

        RowMapper<Member> rowMapper = (rs, rowNum) -> Member.builder()
            .id(rs.getLong("id"))
            .email(rs.getString("email"))
            .nickname(rs.getString("nickname"))
            .birthday(rs.getObject("birthday", LocalDate.class))
            .createdAt(rs.getObject("createdAt", LocalDateTime.class))
            .build();

        var member = namedParameterJdbcTemplate.queryForObject(sql, params, rowMapper);

        return Optional.ofNullable(member);
    }

    public Member save(Member member) {
        if (member.getId() == null) {
            return insert(member);
        }

        return update(member);
    }

    private Member insert(Member member) {
        SimpleJdbcInsert simpleJdbcInsert =
            new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate())
                .withTableName("Member")
                .usingGeneratedKeyColumns("id");

        SqlParameterSource params = new BeanPropertySqlParameterSource(member);

        var id = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return Member.builder()
            .id(id)
            .email(member.getEmail())
            .nickname(member.getNickname())
            .birthday(member.getBirthday())
            .createdAt(member.getCreatedAt())
            .build();
    }

    private Member update(Member member) {
        var sql = String.format(
            "UPDATE %s SET email = :email, nickname = :nickname, birthday = :birthday WHERE id = :id",
            TABLE
        );

        SqlParameterSource params = new BeanPropertySqlParameterSource(member);
        namedParameterJdbcTemplate.update(sql, params);

        return member;
    }
}
