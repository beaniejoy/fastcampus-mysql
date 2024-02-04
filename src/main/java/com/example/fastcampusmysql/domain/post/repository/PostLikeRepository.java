package com.example.fastcampusmysql.domain.post.repository;

import com.example.fastcampusmysql.domain.post.entity.PostLike;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class PostLikeRepository {

    private static final String TABLE = "PostLike";
    private static final RowMapper<PostLike> ROW_MAPPER = (rs, rowNum) -> PostLike.builder()
        .id(rs.getLong("id"))
        .memberId(rs.getLong("memberId"))
        .postId(rs.getLong("postId"))
        .createdAt(rs.getObject("createdAt", LocalDateTime.class))
        .build();

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public Long count(Long postId) {
        var params = new MapSqlParameterSource()
            .addValue("postId", postId);

        var sql = String.format("""
            SELECT COUNT(*)
            FROM %s
            WHERE postId = :postId
            """, TABLE);

        return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
    }

    public PostLike save(PostLike postLike) {
        if (postLike.getId() == null) {
            return insert(postLike);
        }

        throw new UnsupportedOperationException("Timeline은 갱신을 지원하지 않습니다.");
    }

    private PostLike insert(PostLike timeline) {
        SimpleJdbcInsert simpleJdbcInsert =
            new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate())
                .withTableName(TABLE)
                .usingGeneratedKeyColumns("id");

        SqlParameterSource params = new BeanPropertySqlParameterSource(timeline);

        var id = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return PostLike.builder()
            .id(id)
            .memberId(timeline.getMemberId())
            .postId(timeline.getPostId())
            .createdAt(timeline.getCreatedAt())
            .build();
    }
}
