package com.example.fastcampusmysql.application.usecase;

import com.example.fastcampusmysql.domain.follow.entity.Follow;
import com.example.fastcampusmysql.domain.follow.service.FollowReadService;
import com.example.fastcampusmysql.domain.post.dto.PostCommand;
import com.example.fastcampusmysql.domain.post.service.PostWriteService;
import com.example.fastcampusmysql.domain.post.service.TimelineWriteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CreatePostUseCase {

    private final PostWriteService postWriteService;

    private final FollowReadService followReadService;

    private final TimelineWriteService timelineWriteService;

    public Long execute(PostCommand postCommand) {
        Long postId = postWriteService.create(postCommand);

        List<Long> followerMemberIds = followReadService.getFollowers(postCommand.memberId())
            .stream()
            .map(Follow::getFromMemberId)
            .toList();

        timelineWriteService.deliveryToTimeline(postId, followerMemberIds);

        return postId;
    }
}
