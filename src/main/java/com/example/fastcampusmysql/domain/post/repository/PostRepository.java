package com.example.fastcampusmysql.domain.post.repository;

import com.example.fastcampusmysql.domain.post.dto.DailyPostCount;
import com.example.fastcampusmysql.domain.post.dto.DailyPostCountRequest;
import com.example.fastcampusmysql.domain.post.entity.Post;
import com.example.fastcampusmysql.utils.PageHelper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class PostRepository {

    private static final String TABLE = "Post";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final RowMapper<Post> POST_ROW_MAPPER = (rs, rowNum) -> Post.builder()
        .id(rs.getLong("id"))
        .memberId(rs.getLong("memberId"))
        .contents(rs.getString("contents"))
        .createdDate(rs.getObject("createdDate", LocalDate.class))
        .likeCount(rs.getLong("likeCount"))
        .createdAt(rs.getObject("createdAt", LocalDateTime.class))
        .version(rs.getLong("version"))
        .build();

    private static final RowMapper<DailyPostCount> DAILY_POST_ROW_MAPPER = (rs, rowNum) -> new DailyPostCount(
        rs.getLong("memberId"),
        rs.getObject("createdDate", LocalDate.class),
        rs.getLong("count")
    );

    public Optional<Post> findById(Long postId, Boolean requiredLock) {
        var sql = String.format("SELECT * FROM %s WHERE id = :postId ", TABLE);
        if (requiredLock) {
            sql += "FOR UPDATE";
        }
        var params = new MapSqlParameterSource().addValue("postId", postId);
        Post post = namedParameterJdbcTemplate.queryForObject(sql, params, POST_ROW_MAPPER);

        return Optional.ofNullable(post);
    }

    public List<DailyPostCount> groupByCreatedDate(DailyPostCountRequest request) {
        var sql = String.format("""
            SELECT createdDate, memberId, COUNT(id) AS count
            FROM %s
            WHERE memberId = :memberId AND createdDate between :firstDate and :lastDate
            GROUP BY memberId, createdDate
            """, TABLE);

        var params = new BeanPropertySqlParameterSource(request);

        return namedParameterJdbcTemplate.query(sql, params, DAILY_POST_ROW_MAPPER);
    }

    public Page<Post> findAllByMemberId(Long memberId, Pageable pageable) {
        var params = new MapSqlParameterSource()
            .addValue("memberId", memberId)
            .addValue("size", pageable.getPageSize())
            .addValue("offset", pageable.getOffset());

        var sql = String.format("""
            SELECT *
            FROM %s
            WHERE memberId = :memberId
            ORDER BY %s
            LIMIT :size
            OFFSET :offset
            """, TABLE, PageHelper.orderBy(pageable.getSort()));

        List<Post> results = namedParameterJdbcTemplate.query(sql, params, POST_ROW_MAPPER);

        return new PageImpl<>(results, pageable, getCount(memberId));
    }

    // cursor가 없는 최초 조회
    public List<Post> findAllByMemberIdAndOrderByIdDesc(Long memberId, Long size) {
        var params = new MapSqlParameterSource()
            .addValue("memberId", memberId)
            .addValue("size", size);

        var sql = String.format("""
            SELECT *
            FROM %s
            WHERE memberId = :memberId
            ORDER BY id DESC
            LIMIT :size
            """, TABLE);

        return namedParameterJdbcTemplate.query(sql, params, POST_ROW_MAPPER);
    }

    // cursor 방식
    public List<Post> findAllByLessThanIdAndMemberIdAndOrderByIdDesc(Long id, Long memberId,
        Long size) {
        var params = new MapSqlParameterSource()
            .addValue("memberId", memberId)
            .addValue("id", id)
            .addValue("size", size);

        var sql = String.format("""
            SELECT *
            FROM %s
            WHERE memberId = :memberId
            and id < :id
            ORDER BY id DESC
            LIMIT :size
            """, TABLE);

        return namedParameterJdbcTemplate.query(sql, params, POST_ROW_MAPPER);
    }

    public List<Post> findAllByInMemberIdsAndOrderByIdDesc(List<Long> memberIds, Long size) {
        if (memberIds.isEmpty()) {
            return Collections.emptyList();
        }

        var params = new MapSqlParameterSource()
            .addValue("memberIds", memberIds)
            .addValue("size", size);

        var sql = String.format("""
            SELECT *
            FROM %s
            WHERE memberId IN (:memberIds)
            ORDER BY id DESC
            LIMIT :size
            """, TABLE);

        return namedParameterJdbcTemplate.query(sql, params, POST_ROW_MAPPER);
    }

    public List<Post> findAllByLessThanIdAndInMemberIdsAndOrderByIdDesc(Long id,
        List<Long> memberIds, Long size) {
        if (memberIds.isEmpty()) {
            return Collections.emptyList();
        }

        var params = new MapSqlParameterSource()
            .addValue("memberIds", memberIds)
            .addValue("id", id)
            .addValue("size", size);

        var sql = String.format("""
            SELECT *
            FROM %s
            WHERE memberId IN (:memberIds)
            and id < :id
            ORDER BY id DESC
            LIMIT :size
            """, TABLE);

        return namedParameterJdbcTemplate.query(sql, params, POST_ROW_MAPPER);
    }

    public List<Post> findAllByInIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        var params = new MapSqlParameterSource()
            .addValue("ids", ids);

        var sql = String.format("""
            SELECT *
            FROM %s
            WHERE id IN (:ids)
            """, TABLE);

        return namedParameterJdbcTemplate.query(sql, params, POST_ROW_MAPPER);
    }

    private Long getCount(Long memberId) {
        var params = new MapSqlParameterSource()
            .addValue("memberId", memberId);

        var sql = String.format("""
            SELECT COUNT(*)
            FROM %s
            WHERE memberId = :memberId
            """, TABLE);

        return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
    }

    public Post save(Post post) {
        if (post.getId() == null) {
            return insert(post);
        }

        return update(post);
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

    private Post update(Post post) {
        var sql = String.format("""
            UPDATE %s SET
            memberId = :memberId,
            contents = :contents,
            createdDate = :createdDate,
            likeCount = :likeCount
            version = :version + 1
            WHERE id = :id
            AND version = :version
        """, TABLE);

        SqlParameterSource params = new BeanPropertySqlParameterSource(post);
        int updatedCount = namedParameterJdbcTemplate.update(sql, params);

        if (updatedCount == 0) {
            throw new RuntimeException("갱신 실패");
        }

        return post;
    }
}
