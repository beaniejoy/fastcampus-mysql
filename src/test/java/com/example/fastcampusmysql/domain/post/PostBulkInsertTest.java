package com.example.fastcampusmysql.domain.post;

import com.example.fastcampusmysql.domain.post.entity.Post;
import com.example.fastcampusmysql.domain.post.repository.PostRepository;
import com.example.fastcampusmysql.util.PostFixtureFactory;
import java.time.LocalDate;
import java.util.stream.IntStream;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

@SpringBootTest
public class PostBulkInsertTest {

    @Autowired
    private PostRepository postRepository;

    @RepeatedTest(4)
    public void bulkInsert() {
        var easyRandom = PostFixtureFactory.get(
            0L,
            LocalDate.of(1970, 1, 1),
            LocalDate.of(2024, 1, 1)
        );

        var stopWatch = new StopWatch();
        stopWatch.start();

        var posts = IntStream.range(0, 10_000 * 100)
            .parallel()
            .mapToObj(i -> easyRandom.nextObject(Post.class))
            .toList();
        stopWatch.stop();
        System.out.println("객체 생성 시간 : " + stopWatch.getTotalTimeSeconds());

        postRepository.bulkInsert(posts);
    }
}
