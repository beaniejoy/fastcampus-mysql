package com.example.fastcampusmysql.application.usecase;

import com.example.fastcampusmysql.domain.follow.entity.Follow;
import com.example.fastcampusmysql.domain.follow.service.FollowReadService;
import com.example.fastcampusmysql.domain.post.entity.Post;
import com.example.fastcampusmysql.domain.post.entity.Timeline;
import com.example.fastcampusmysql.domain.post.service.PostReadService;
import com.example.fastcampusmysql.domain.post.service.TimelineReadService;
import com.example.fastcampusmysql.utils.CursorRequest;
import com.example.fastcampusmysql.utils.PageCursor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetTimelinePostsUseCase {

    private final FollowReadService followReadService;
    private final PostReadService postReadService;
    private final TimelineReadService timelineReadService;

    // fan out on read (읽기를 희생)
    public PageCursor<Post> execute(Long memberId, CursorRequest cursorRequest) {
        // 모든 팔로잉하는 대상 ids 조회
        var followings = followReadService.getFollowings(memberId);

        var followingMemberIds = followings
            .stream()
            .map(Follow::getToMemberId)
            .toList();

        // 대상 ids 기준으로 모든 Post 조회
        // 시간복잡도: log(num(Follow)) + num(followings) * log(num(Post))
        return postReadService.getPostsByCursor(followingMemberIds, cursorRequest);
    }

    // fan out on write (쓰기를 희생)
    public PageCursor<Post> executeByTimeline(Long memberId, CursorRequest cursorRequest) {
        /*
            1. Timeline 조회
            2. 1번에 해당하는 게시물을 조회한다.
         */

        var pagedTimelines = timelineReadService.getTimelines(memberId, cursorRequest);
        var postIds = pagedTimelines.contents()
            .stream()
            .map(Timeline::getPostId)
            .toList();

        // 대상 ids 기준으로 모든 Post 조회
        List<Post> posts = postReadService.getPosts(postIds);

        return new PageCursor<>(pagedTimelines.nextCursorRequest(), posts);
    }
}
