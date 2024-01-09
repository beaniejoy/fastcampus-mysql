package com.example.fastcampusmysql.domain.post.repository;

import com.example.fastcampusmysql.domain.post.dto.DailyPostCount;
import com.example.fastcampusmysql.domain.post.dto.DailyPostCountRequest;
import com.example.fastcampusmysql.domain.post.entity.Post;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class PostRepository {

    private static final String TABLE = "Post";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final RowMapper<DailyPostCount> ROW_MAPPER = (rs, rowNum) -> new DailyPostCount(
        rs.getLong("memberId"),
        rs.getObject("createdDate", LocalDate.class),
        rs.getLong("count")
    );

    public List<DailyPostCount> groupByCreatedDate(DailyPostCountRequest request) {
        var sql = String.format("""
            SELECT createdDate, memberId, COUNT(id) AS count
            FROM %s
            WHERE memberId = :memberId AND createdDate between :firstDate and :lastDate
            GROUP BY memberId, createdDate
            """, TABLE);

        var params = new BeanPropertySqlParameterSource(request);

        return namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public Post save(Post post) {
        if (post.getId() == null) {
            return insert(post);
        }

        throw new UnsupportedOperationException("Post는 갱신을 지원하지 않습니다.");
    }

    private Post insert(Post post) {
        SimpleJdbcInsert simpleJdbcInsert =
            new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate())
                .withTableName(TABLE)
                .usingGeneratedKeyColumns("id");

        SqlParameterSource params = new BeanPropertySqlParameterSource(post);

        var id = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return Post.builder()
            .id(id)
            .memberId(post.getMemberId())
            .contents(post.getContents())
            .createdDate(post.getCreatedDate())
            .createdAt(post.getCreatedAt())
            .build();
    }

    public void bulkInsert(List<Post> posts) {
        var sql = String.format("""
            INSERT INTO `%s` (memberId, contents, createdDate, createdAt)
            VALUES (:memberId, :contents, :createdDate, :createdAt)
            """, TABLE);

        SqlParameterSource[] params = posts.stream()
            .map(BeanPropertySqlParameterSource::new)
            .toArray(SqlParameterSource[]::new);

        namedParameterJdbcTemplate.batchUpdate(sql, params);
    }
}
